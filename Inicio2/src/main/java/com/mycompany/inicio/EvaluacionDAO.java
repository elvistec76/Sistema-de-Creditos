package com.mycompany.inicio;

import java.math.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** CU04/CU05. Historial externo registrado manualmente, sin simular una consulta en linea. */
public class EvaluacionDAO {
    public static final String SQL_HISTORIAL="SELECT e.numero_version AS Version,e.fecha_evaluacion AS Fecha,e.vigente AS Vigente,u.nombre_completo AS Analista,"
            +"e.ingresos AS Ingresos,e.egresos AS Gastos,e.obligaciones_financieras AS `Cuotas externas`,e.cuota_proyectada AS `Cuota nueva`,e.capacidad_pago AS `Cuota maxima`,"
            +"e.resultado_capacidad AS Capacidad,e.nivel_riesgo AS Riesgo,e.razones_riesgo AS Razones,e.resultado AS Resultado,e.recomendacion AS Recomendacion,e.observaciones AS Evidencia,"
            +"h.referencia_consulta AS `Referencia historial`,h.fecha_vigencia AS `Valido hasta`,h.datos_respuesta AS `Historial manual` "
            +"FROM evaluacion_credito e JOIN usuario u ON u.id_usuario=e.id_usuario LEFT JOIN consulta_riesgo h ON h.id_evaluacion=e.id_evaluacion WHERE e.id_solicitud=? ORDER BY e.numero_version DESC";
    public static final String SQL_SOLICITUDES="SELECT s.id_solicitud AS Solicitud,c.ci AS CI,CONCAT(c.nombres,' ',c.apellidos) AS Cliente,"
            +"p.nombre AS Producto,s.monto_solicitado AS Monto,s.plazo_meses AS Meses,"
            +"COALESCE(e.resultado,'SIN EVALUAR') AS Evaluacion,COALESCE(e.nivel_riesgo,'PENDIENTE') AS Riesgo "
            +"FROM solicitud_credito s JOIN cliente c ON c.id_cliente=s.id_cliente "
            +"LEFT JOIN producto_credito p ON p.id_producto=s.id_producto "
            +"LEFT JOIN evaluacion_credito e ON e.id_solicitud=s.id_solicitud AND e.vigente=1 "
            +"WHERE s.estado='EN_EVALUACION' OR (s.estado='APROBADA' AND EXISTS(SELECT 1 FROM credito cr WHERE cr.id_solicitud=s.id_solicitud AND cr.estado='APROBADO')) ORDER BY s.id_solicitud DESC";
    public record Calculo(int politica,BigDecimal ingresos,BigDecimal egresos,BigDecimal externas,
            BigDecimal internas,BigDecimal disponible,BigDecimal porcentaje,BigDecimal maxima,
            BigDecimal proyectada,int vencidas,String faltantes) {
        public boolean suficiente(){return vencidas==0&&proyectada.compareTo(maxima)<=0;}
        public String resumen(){return "Ingresos Bs "+ingresos+" | Gastos Bs "+egresos+" | Cuotas internas Bs "+internas
                +" | Externas Bs "+externas+"\nDisponible Bs "+disponible+" x "+porcentaje+"% = cuota maxima Bs "+maxima
                +"\nCuota nueva maxima (incluye interes): Bs "+proyectada+" | Cuotas vencidas: "+vencidas
                +"\nCapacidad: "+(suficiente()?"SUFICIENTE":"INSUFICIENTE")+"\nExpediente: "+(faltantes.isBlank()?"Completo":faltantes);}
    }
    public record Datos(BigDecimal egresos,BigDecimal externas,String riesgo,String razones,
            String recomendacion,String observaciones,String referencia,LocalDate vigencia,String historial) {}

    public Calculo calcular(int solicitud,BigDecimal egresos,BigDecimal externas)throws SQLException{
        try(Connection c=Conexion.getConexion()){
            Seguridad.exigir(c,Sesion.login(),"EVALUACION_REALIZAR");return calcular(c,solicitud,egresos,externas);
        }
    }
    static void dinero(BigDecimal v,String nombre){
        if(v==null||v.signum()<0||v.compareTo(new BigDecimal("999999999999.99"))>0||v.stripTrailingZeros().scale()>2)
            throw new IllegalArgumentException(nombre+": valor positivo o cero, con hasta dos decimales.");
    }
    static Calculo calcular(Connection c,int solicitud,BigDecimal egresos,BigDecimal externas)throws SQLException{
        dinero(egresos,"Gastos");dinero(externas,"Obligaciones externas");
        int cliente,politica,meses;BigDecimal ingreso,monto,tasa,porcentaje;String tipo;boolean garantia;
        LocalDate hoy=CicloCreditoDAO.hoy(c);
        try(PreparedStatement p=c.prepareStatement("SELECT s.id_cliente,c.ingresos_mensuales,s.monto_solicitado,s.plazo_meses,"
                +"p.tasa_interes,p.tipo_cuota,p.exige_garantia,p.id_politica,pc.porcentaje_max_cuota,"
                +"pc.activa,pc.vigente_desde,pc.vigente_hasta,p.activo FROM solicitud_credito s "
                +"JOIN cliente c ON c.id_cliente=s.id_cliente JOIN producto_credito p ON p.id_producto=s.id_producto "
                +"JOIN politica_crediticia pc ON pc.id_politica=p.id_politica WHERE s.id_solicitud=?")){
            p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){
                if(!r.next()||r.getBigDecimal(3)==null||r.getInt(4)<1)throw new IllegalArgumentException("Primero define producto, monto y plazo en Solicitudes pendientes.");
                cliente=r.getInt(1);ingreso=r.getBigDecimal(2);monto=r.getBigDecimal(3);meses=r.getInt(4);tasa=r.getBigDecimal(5);
                tipo=r.getString(6);garantia=r.getBoolean(7);politica=r.getInt(8);porcentaje=r.getBigDecimal(9);
                if(!r.getBoolean(10)||!r.getBoolean(13)||r.getDate(11).toLocalDate().isAfter(hoy)
                        ||(r.getDate(12)!=null&&r.getDate(12).toLocalDate().isBefore(hoy))||porcentaje.signum()<=0||porcentaje.compareTo(new BigDecimal("100"))>0)
                    throw new IllegalArgumentException("Producto o politica sin vigencia o porcentaje valido.");
            }
        }
        if(ingreso==null||ingreso.signum()<=0)throw new IllegalArgumentException("El oficial debe completar los ingresos del cliente.");
        Map<String,BigDecimal> mesesInternos=new HashMap<>();int vencidas=0;
        try(PreparedStatement p=c.prepareStatement("SELECT DATE_FORMAT(q.fecha_vencimiento,'%Y-%m'),q.saldo,q.fecha_vencimiento "
                +"FROM credito cr JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
                +"JOIN cronograma_pago cp ON cp.id_credito=cr.id_credito AND cp.vigente=1 "
                +"JOIN cuota q ON q.id_cronograma=cp.id_cronograma WHERE s.id_cliente=? AND cr.estado IN ('VIGENTE','EN_MORA') AND q.saldo>0")){
            p.setInt(1,cliente);try(ResultSet r=p.executeQuery()){while(r.next()){
                mesesInternos.merge(r.getString(1),r.getBigDecimal(2),BigDecimal::add);
                if(r.getDate(3).toLocalDate().isBefore(hoy))vencidas++;
            }}
        }
        try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM credito cr JOIN solicitud_credito s USING(id_solicitud) WHERE s.id_cliente=? AND cr.estado IN ('VIGENTE','EN_MORA') AND (SELECT COUNT(*) FROM cronograma_pago cp WHERE cp.id_credito=cr.id_credito AND cp.vigente=1)<>1")){
            p.setInt(1,cliente);try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new IllegalArgumentException("Un credito activo no tiene un unico cronograma vigente. Requiere revision antes de calcular capacidad.");}
        }
        BigDecimal internas=mesesInternos.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO).setScale(2);
        BigDecimal disponible=ingreso.subtract(egresos).subtract(externas).subtract(internas);
        BigDecimal maxima=disponible.max(BigDecimal.ZERO).multiply(porcentaje).divide(new BigDecimal("100"),2,RoundingMode.DOWN);
        BigDecimal proyectada=Amortizacion.generar(monto,tasa,meses,tipo,hoy).stream().map(Amortizacion.CuotaPlan::monto).max(BigDecimal::compareTo).orElseThrow();
        List<String> faltantes=new ArrayList<>();int documentos=0;
        try(PreparedStatement p=c.prepareStatement("SELECT estado_verificacion,nombre_archivo FROM documento_solicitud WHERE id_solicitud=?")){
            p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){while(r.next()){
                documentos++;if(!"VERIFICADO".equals(r.getString(1))||!archivo(r.getString(2)))faltantes.add("Documento pendiente u observado / archivo ausente");
            }}
        }
        if(documentos==0)faltantes.add("Faltan documentos verificados por el oficial");
        if(garantia){
            int validas=0;
            try(PreparedStatement p=c.prepareStatement("SELECT g.id_garantia,g.valor,t.nombre,g.titular,g.evidencia_verificacion FROM garantia g "
                    +"JOIN tipo_garantia t ON t.id_tipo_garantia=g.id_tipo_garantia WHERE g.id_solicitud=? AND g.estado_verificacion='VALIDADA'")){
                p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){while(r.next()){
                    if(r.getString(4)==null||r.getString(4).isBlank()||r.getString(5)==null||r.getString(5).isBlank())continue;
                    if(!"PERSONAL".equals(r.getString(3))&&r.getBigDecimal(2).signum()<=0)continue;
                    try(PreparedStatement b=c.prepareStatement("SELECT nombre_archivo FROM documento_garantia WHERE id_garantia=?")){
                        b.setInt(1,r.getInt(1));try(ResultSet br=b.executeQuery()){while(br.next())if(archivo(br.getString(1))){validas++;break;}}
                    }
                }}
            }
            if(validas==0)faltantes.add("Falta garantia validada con respaldo disponible");
        }
        return new Calculo(politica,ingreso,egresos,externas,internas,disponible,porcentaje,maxima,proyectada,vencidas,
                String.join("; ",new LinkedHashSet<>(faltantes)));
    }
    private static boolean archivo(String s){try{return java.nio.file.Files.isRegularFile(ArchivoDocumental.resolver(s));}catch(Exception e){return false;}}

    public int guardar(int solicitud,Datos d)throws SQLException{
        if(d==null||!Set.of("BAJO","MEDIO","ALTO").contains(d.riesgo()==null?"":d.riesgo()))throw new IllegalArgumentException("Selecciona el nivel de riesgo.");
        ReglasCredito.texto(d.razones(),500,"Razones de riesgo");ReglasCredito.texto(d.recomendacion(),500,"Recomendacion");ReglasCredito.texto(d.observaciones(),500,"Observaciones y evidencias");
        boolean historia=d.referencia()!=null&&!d.referencia().isBlank();
        if(historia){ReglasCredito.texto(d.referencia(),80,"Referencia del historial");ReglasCredito.texto(d.historial(),2000,"Resultado del historial");}
        try(Connection c=Conexion.getConexion()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try{
                int actor=UsuarioDAO.exigirRol(c,Sesion.login(),"ANALISTA");Seguridad.exigir(c,Sesion.login(),"EVALUACION_REALIZAR");
                BloqueoCliente.solicitud(c,solicitud);
                try(PreparedStatement p=c.prepareStatement("SELECT estado FROM solicitud_credito WHERE id_solicitud=? FOR UPDATE")){
                    p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){if(!r.next()||!Set.of("EN_EVALUACION","APROBADA").contains(r.getString(1)))throw new IllegalArgumentException("Solo se evaluan solicitudes EN_EVALUACION o aprobadas pendientes de desembolso.");}
                }
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM credito WHERE id_solicitud=? AND estado<>'APROBADO'")){
                    p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new IllegalArgumentException("El credito ya fue desembolsado; esta evaluacion no se puede reemplazar.");}
                }
                Calculo x=calcular(c,solicitud,d.egresos(),d.externas());
                LocalDate hoy=CicloCreditoDAO.hoy(c);
                int mesesHistoria=ReglasCredito.parametro(c,"MESES_VIGENCIA_HISTORIAL",120);
                if(historia&&d.vigencia()!=null&&d.vigencia().isAfter(hoy.plusMonths(mesesHistoria)))throw new IllegalArgumentException("La vigencia del historial no puede superar "+mesesHistoria+" meses desde hoy.");
                boolean vigente=historia&&d.vigencia()!=null&&!d.vigencia().isBefore(hoy);
                String resultado=!x.suficiente()||"ALTO".equals(d.riesgo())?"DESFAVORABLE":x.faltantes().isBlank()&&vigente?"FAVORABLE":"PENDIENTE";
                int version;
                try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(MAX(numero_version),0)+1 FROM evaluacion_credito WHERE id_solicitud=?")){
                    p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){r.next();version=r.getInt(1);}
                }
                ReglasCredito.actualizar(c,"UPDATE evaluacion_credito SET vigente=0 WHERE id_solicitud=?",solicitud);
                int id=ReglasCredito.insertar(c,"INSERT INTO evaluacion_credito(id_solicitud,id_politica,id_usuario,numero_version,ingresos,egresos,obligaciones_financieras,cuota_proyectada,capacidad_pago,resultado_capacidad,nivel_riesgo,razones_riesgo,resultado,recomendacion,observaciones) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        solicitud,x.politica(),actor,version,x.ingresos(),d.egresos(),d.externas(),x.proyectada(),x.maxima(),x.suficiente()?"SUFICIENTE":"INSUFICIENTE",d.riesgo(),d.razones().trim(),resultado,d.recomendacion().trim(),d.observaciones().trim());
                if(historia)ReglasCredito.insertar(c,"INSERT INTO consulta_riesgo(id_evaluacion,id_usuario,referencia_consulta,fecha_vigencia,datos_respuesta,estado) VALUES(?,?,?,?,?,?)",
                        id,actor,d.referencia().trim(),d.vigencia()==null?null:java.sql.Date.valueOf(d.vigencia()),"REGISTRO MANUAL: "+d.historial().trim(),vigente?"EXITOSA":"INVALIDA");
                Seguridad.auditar(c,actor,"GUARDAR_EVALUACION","evaluacion_credito",id,x.resumen()+"; resultado "+resultado+"; version "+version);
                c.commit();return id;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }

    /** Revalida la capacidad y el expediente, sin considerar una deuda al dia como mora. */
    static int exigirFavorable(Connection c,int solicitud)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT e.id_evaluacion,e.egresos,e.obligaciones_financieras,e.ingresos,e.id_politica,e.cuota_proyectada,e.resultado "
                +"FROM evaluacion_credito e WHERE e.id_solicitud=? AND e.vigente=1 ORDER BY e.numero_version DESC")){
            p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){
                if(!r.next()||!"FAVORABLE".equals(r.getString(7)))throw new IllegalArgumentException("El analista debe guardar una evaluacion FAVORABLE vigente en Evaluacion de pagos y riesgo.");
                int id=r.getInt(1);Calculo x=calcular(c,solicitud,r.getBigDecimal(2),r.getBigDecimal(3));
                if(!x.suficiente())throw new IllegalArgumentException("No se puede aprobar/desembolsar: "+x.resumen());
                if(!x.faltantes().isBlank()||x.ingresos().compareTo(r.getBigDecimal(4))!=0||x.politica()!=r.getInt(5)||x.proyectada().compareTo(r.getBigDecimal(6))!=0)
                    throw new IllegalArgumentException("Cambios o faltantes en el expediente. El analista debe actualizar la evaluacion: "+x.faltantes());
                try(PreparedStatement h=c.prepareStatement("SELECT COUNT(*) FROM consulta_riesgo WHERE id_evaluacion=? AND estado='EXITOSA' AND fecha_vigencia>=CURDATE() AND fecha_consulta>=DATE_SUB(NOW(),INTERVAL (SELECT CAST(valor AS UNSIGNED) FROM parametro WHERE clave='MESES_VIGENCIA_HISTORIAL') MONTH)")){
                    h.setInt(1,id);try(ResultSet hr=h.executeQuery()){hr.next();if(hr.getInt(1)==0)throw new IllegalArgumentException("El historial de la evaluacion falta o esta vencido; actualizar con el analista.");}
                }
                return id;
            }
        }
    }
}
