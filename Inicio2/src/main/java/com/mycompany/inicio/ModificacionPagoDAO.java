package com.mycompany.inicio;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** CU23-CU28. Cambios autorizados de fechas, sin cargos adicionales ni cambio de capital. */
public class ModificacionPagoDAO {
    public static final String SQL_JEFE="SELECT m.id_modificacion AS ID,cr.numero_credito AS Credito,c.ci AS CI,CONCAT(c.nombres,' ',c.apellidos) AS Cliente,"
            +"m.estado AS Estado,m.tipo AS Tipo,q.numero AS Cuota,q.fecha_vencimiento AS Anterior,m.fecha_propuesta AS Propuesta,q.saldo AS Saldo,"
            +"m.motivo AS Motivo,m.justificacion_decision AS Justificacion,m.tratamiento_importe AS Tratamiento,m.id_cronograma_nuevo AS `Nuevo plan` "
            +"FROM solicitud_modificacion m JOIN credito cr ON cr.id_credito=m.id_credito "
            +"JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud JOIN cliente c ON c.id_cliente=s.id_cliente "
            +"JOIN cuota q ON q.id_cuota=m.id_cuota ORDER BY (m.estado='PENDIENTE') DESC,m.id_modificacion DESC";
    public static final String SQL_CUOTAS_CLIENTE="SELECT q.id_cuota AS ID,cr.numero_credito AS Credito,q.numero AS Cuota,q.fecha_vencimiento AS Vence,q.saldo AS Saldo,"
            +"CASE WHEN q.capital_pagado+q.interes_pagado>0 THEN 'Tiene pagos: no modificable' WHEN q.fecha_vencimiento<CURDATE() THEN 'Vencida: consultar al gestor' ELSE 'Consultar elegibilidad' END AS Situacion "
            +"FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma AND cp.vigente=1 "
            +"JOIN credito cr ON cr.id_credito=cp.id_credito JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
            +"JOIN usuario u ON u.id_cliente=s.id_cliente WHERE u.login=? AND q.saldo>0 AND cr.estado IN ('VIGENTE','EN_MORA') ORDER BY q.fecha_vencimiento";
    public static final String SQL_MIS_SOLICITUDES="SELECT m.id_modificacion AS ID,cr.numero_credito AS Credito,m.tipo AS Tipo,m.fecha_solicitud AS Fecha,m.fecha_propuesta AS Propuesta,"
            +"m.estado AS Estado,m.justificacion_decision AS Respuesta,m.tratamiento_importe AS Tratamiento "
            +"FROM solicitud_modificacion m JOIN credito cr ON cr.id_credito=m.id_credito JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
            +"JOIN usuario u ON u.id_cliente=s.id_cliente WHERE u.login=? ORDER BY m.id_modificacion DESC";
    private record Cuota(int id,int numero,LocalDate fecha,BigDecimal capital,BigDecimal interes,BigDecimal monto,
                         BigDecimal capitalPagado,BigDecimal interesPagado,BigDecimal saldo){}
    private record Contexto(int credito,int cliente,int cronograma,int version,LocalDate desembolso,List<Cuota> cuotas){}

    public int solicitar(int cuota,String tipo,LocalDate propuesta,String motivo)throws SQLException{
        ReglasCredito.texto(motivo,300,"Motivo");
        try(Connection c=Conexion.getConexion()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try{
                int actor=UsuarioDAO.exigirRol(c,Sesion.login(),"CLIENTE");Seguridad.exigir(c,Sesion.login(),"MODIFICACION_SOLICITAR");
                Contexto x=bloquear(c,cuota);
                try(PreparedStatement p=c.prepareStatement("SELECT id_cliente FROM usuario WHERE id_usuario=?")){
                    p.setInt(1,actor);try(ResultSet r=p.executeQuery()){if(!r.next()||r.getInt(1)!=x.cliente())throw new IllegalArgumentException("Solo puedes solicitar cambios de tus propias cuotas.");}
                }
                elegible(c,x,cuota,tipo,propuesta);
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM solicitud_modificacion WHERE id_credito=? AND estado='PENDIENTE'")){
                    p.setInt(1,x.credito());try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new IllegalArgumentException("Este credito ya tiene una modificacion pendiente. Espera la decision del jefe.");}
                }
                int id=ReglasCredito.insertar(c,"INSERT INTO solicitud_modificacion(id_credito,id_cuota,id_usuario_registro,tipo,motivo,fecha_propuesta) VALUES(?,?,?,?,?,?)",x.credito(),cuota,actor,tipo,motivo.trim(),java.sql.Date.valueOf(propuesta));
                Seguridad.auditar(c,actor,"SOLICITAR_MODIFICACION","solicitud_modificacion",id,"Cuota "+cuota+"; "+tipo+"; fecha "+propuesta+"; "+motivo.trim());
                c.commit();return id;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public String revisar(int modificacion)throws SQLException{
        try(Connection c=Conexion.getConexion()){
            Seguridad.exigir(c,Sesion.login(),"MODIFICACION_DECIDIR");
            try(PreparedStatement p=c.prepareStatement("SELECT id_cuota,tipo,fecha_propuesta,estado FROM solicitud_modificacion WHERE id_modificacion=?")){
                p.setInt(1,modificacion);try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new IllegalArgumentException("Solicitud inexistente.");
                    if(!"PENDIENTE".equals(r.getString(4)))return "Solicitud ya decidida: "+r.getString(4)+". Consulta la justificacion en la tabla.";
                    c.setAutoCommit(false);try{Contexto x=bloquear(c,r.getInt(1));elegible(c,x,r.getInt(1),r.getString(2),r.getDate(3).toLocalDate());
                        return "Elegible con los datos actuales. Nueva fecha: "+r.getDate(3)+".\nSe conserva el importe de capital e interes; sin nuevos cargos.\nRequiere autorizacion expresa del jefe. El plan anterior se conserva.";
                    }finally{c.rollback();}
                }
            }
        }
    }
    public void decidir(int modificacion,boolean aprobar,String justificacion,boolean conservarImportes)throws SQLException{
        ReglasCredito.texto(justificacion,300,"Justificacion del jefe");
        if(aprobar&&!conservarImportes)throw new IllegalArgumentException("Debes autorizar expresamente conservar los importes, sin intereses ni cargos nuevos.");
        try(Connection c=Conexion.getConexion()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try{
                int actor=UsuarioDAO.exigirRol(c,Sesion.login(),"JEFE");Seguridad.exigir(c,Sesion.login(),"MODIFICACION_DECIDIR");
                int cuota;
                try(PreparedStatement p=c.prepareStatement("SELECT id_cuota FROM solicitud_modificacion WHERE id_modificacion=?")){
                    p.setInt(1,modificacion);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Solicitud inexistente.");cuota=r.getInt(1);}
                }
                Contexto x=bloquear(c,cuota);String tipo;LocalDate propuesta;
                try(PreparedStatement p=c.prepareStatement("SELECT estado,tipo,fecha_propuesta FROM solicitud_modificacion WHERE id_modificacion=? FOR UPDATE")){
                    p.setInt(1,modificacion);try(ResultSet r=p.executeQuery()){r.next();if(!"PENDIENTE".equals(r.getString(1)))throw new IllegalArgumentException("Esta solicitud ya tiene una decision.");tipo=r.getString(2);propuesta=r.getDate(3).toLocalDate();}
                }
                Integer nuevo=null;String tratamiento="Sin cambio del cronograma";
                StringBuilder cambios=new StringBuilder();
                if(aprobar){
                    elegible(c,x,cuota,tipo,propuesta);
                    tratamiento="Conservar capital e interes pactados; sin cargos nuevos. Trasladar cuota a "+propuesta;
                    nuevo=ReglasCredito.insertar(c,"INSERT INTO cronograma_pago(id_credito,id_usuario,version,motivo,fecha_desembolso_prevista,vigente) VALUES(?,?,?,?,?,0)",
                            x.credito(),actor,x.version()+1,"Modificacion "+modificacion+": "+tipo+"; conserva importes",java.sql.Date.valueOf(x.desembolso()));
                    List<Cuota> orden=new ArrayList<>(x.cuotas());
                    orden.sort(Comparator.comparing(q->q.id()==cuota?propuesta:q.fecha()));
                    int numero=0;LocalDate hoy=CicloCreditoDAO.hoy(c);
                    for(Cuota q:orden){
                        LocalDate fecha=q.id()==cuota?propuesta:q.fecha();
                        String estado=q.saldo().signum()==0?"PAGADA":fecha.isBefore(hoy)?"VENCIDA":"PENDIENTE";
                        ReglasCredito.insertar(c,"INSERT INTO cuota(id_cronograma,numero,fecha_vencimiento,capital,interes,monto,capital_pagado,interes_pagado,saldo,dias_atraso,estado) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                                nuevo,++numero,java.sql.Date.valueOf(fecha),q.capital(),q.interes(),q.monto(),q.capitalPagado(),q.interesPagado(),q.saldo(),q.saldo().signum()>0?Math.max(0,ChronoUnit.DAYS.between(fecha,hoy)):0,estado);
                        cambios.append("cuotaID ").append(q.id()).append(" nro ").append(q.numero()).append(" -> ").append(numero).append(" fecha ").append(q.fecha()).append(" -> ").append(fecha)
                                .append(" capital ").append(q.capital()).append(" interes ").append(q.interes()).append(" pagado ").append(q.capitalPagado().add(q.interesPagado())).append(" saldo ").append(q.saldo()).append("; ");
                    }
                    ReglasCredito.actualizar(c,"UPDATE cronograma_pago SET vigente=0 WHERE id_cronograma=?",x.cronograma());
                    ReglasCredito.actualizar(c,"UPDATE cronograma_pago SET vigente=1 WHERE id_cronograma=?",nuevo);
                    ReglasCredito.actualizar(c,"UPDATE alerta a JOIN cuota q ON q.id_cuota=a.id_cuota SET a.estado='ATENDIDA' WHERE q.id_cronograma=? AND a.estado='GENERADA'",x.cronograma());
                }
                String estado=aprobar?"APROBADA":"RECHAZADA";
                ReglasCredito.actualizar(c,"UPDATE solicitud_modificacion SET estado=?,id_usuario_decision=?,fecha_decision=NOW(),justificacion_decision=?,tratamiento_importe=?,id_cronograma_nuevo=? WHERE id_modificacion=?",
                        estado,actor,justificacion.trim(),tratamiento,nuevo,modificacion);
                AvisosClienteDAO.insertar(c,x.cliente(),actor,"RESULTADO_MODIFICACION","Solicitud de modificacion "+modificacion+": "+estado+". "+justificacion.trim()+". "+tratamiento+". "+(aprobar?"Consulta tus cuotas actualizadas.":"Conserva las fechas y obligaciones originales."));
                Seguridad.auditar(c,actor,"DECIDIR_MODIFICACION","solicitud_modificacion",modificacion,estado+"; "+justificacion.trim()+"; plan anterior "+x.cronograma()+"; nuevo "+nuevo+"; "+tratamiento+"; "+cambios);
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    private static Contexto bloquear(Connection c,int cuota)throws SQLException{
        int credito;
        try(PreparedStatement p=c.prepareStatement("SELECT cp.id_credito FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma WHERE q.id_cuota=?")){
            p.setInt(1,cuota);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Cuota inexistente.");credito=r.getInt(1);}
        }
        BloqueoCliente.credito(c,credito);
        int cliente;
        try(PreparedStatement p=c.prepareStatement("SELECT cr.estado,s.id_cliente FROM credito cr JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud WHERE cr.id_credito=? FOR UPDATE")){
            p.setInt(1,credito);try(ResultSet r=p.executeQuery()){r.next();if(!Set.of("VIGENTE","EN_MORA").contains(r.getString(1)))throw new IllegalArgumentException("El credito debe estar desembolsado y con deuda pendiente.");cliente=r.getInt(2);}
        }
        int cp,version;LocalDate desembolso;
        try(PreparedStatement p=c.prepareStatement("SELECT id_cronograma,version,fecha_desembolso_prevista FROM cronograma_pago WHERE id_credito=? AND vigente=1 FOR UPDATE")){
            p.setInt(1,credito);try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new IllegalArgumentException("El credito no tiene un plan vigente.");cp=r.getInt(1);version=r.getInt(2);desembolso=r.getDate(3).toLocalDate();
                if(r.next())throw new IllegalArgumentException("Hay mas de un cronograma vigente. Requiere revision.");
            }
        }
        List<Cuota> lista=new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement("SELECT id_cuota,numero,fecha_vencimiento,capital,interes,monto,capital_pagado,interes_pagado,saldo FROM cuota WHERE id_cronograma=? ORDER BY numero FOR UPDATE")){
            p.setInt(1,cp);try(ResultSet r=p.executeQuery()){while(r.next())lista.add(new Cuota(r.getInt(1),r.getInt(2),r.getDate(3).toLocalDate(),r.getBigDecimal(4),r.getBigDecimal(5),r.getBigDecimal(6),r.getBigDecimal(7),r.getBigDecimal(8),r.getBigDecimal(9)));}
        }
        return new Contexto(credito,cliente,cp,version,desembolso,lista);
    }
    private static void elegible(Connection c,Contexto x,int cuota,String tipo,LocalDate propuesta)throws SQLException{
        if(!Set.of("CAMBIO_FECHA","APLAZAMIENTO").contains(tipo==null?"":tipo)||propuesta==null)throw new IllegalArgumentException("Indica tipo y fecha propuesta.");
        Cuota q=x.cuotas().stream().filter(a->a.id()==cuota).findFirst().orElseThrow(()->new IllegalArgumentException("La cuota pertenece a un plan anterior. Actualiza la consulta."));
        LocalDate hoy=CicloCreditoDAO.hoy(c);int dias=ReglasCredito.parametro(c,"DIAS_MIN_ANTICIPACION_MOD",366);
        if(q.saldo().signum()<=0||q.capitalPagado().add(q.interesPagado()).signum()>0)throw new IllegalArgumentException("Solo se modifican cuotas pendientes sin pagos aplicados.");
        if(ChronoUnit.DAYS.between(hoy,q.fecha())<dias)throw new IllegalArgumentException("Se requieren al menos "+dias+" dias de anticipacion antes del vencimiento.");
        if(!propuesta.isAfter(hoy)||propuesta.equals(q.fecha()))throw new IllegalArgumentException("La nueva fecha debe ser futura y distinta de la actual.");
        if(x.cuotas().stream().anyMatch(a->a.saldo().signum()>0&&a.fecha().isBefore(hoy)))throw new IllegalArgumentException("El credito tiene cuotas vencidas. Regulariza los pagos antes de solicitar flexibilidad.");
        for(Cuota otra:x.cuotas())if(otra.id()!=cuota&&otra.fecha().equals(propuesta))throw new IllegalArgumentException("La fecha coincide con otra cuota.");
        if("CAMBIO_FECHA".equals(tipo)){
            LocalDate anterior=x.cuotas().stream().filter(a->a.numero()<q.numero()).map(Cuota::fecha).max(LocalDate::compareTo).orElse(x.desembolso());
            LocalDate siguiente=x.cuotas().stream().filter(a->a.numero()>q.numero()).map(Cuota::fecha).min(LocalDate::compareTo).orElse(q.fecha().plusMonths(1));
            if(!propuesta.isAfter(anterior)||!propuesta.isBefore(siguiente))throw new IllegalArgumentException("El cambio de fecha debe mantener el orden: despues de "+anterior+" y antes de "+siguiente+".");
        }else{
            int max=ReglasCredito.parametro(c,"MAX_APLAZAMIENTOS_ANIO",100);
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM solicitud_modificacion WHERE id_credito=? AND tipo='APLAZAMIENTO' AND estado='APROBADA' AND fecha_decision>=DATE_SUB(NOW(),INTERVAL 12 MONTH)")){
                p.setInt(1,x.credito());try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>=max)throw new IllegalArgumentException("Alcanzo el limite de "+max+" aplazamientos aprobados en 12 meses.");}
            }
            LocalDate ultima=x.cuotas().stream().map(Cuota::fecha).max(LocalDate::compareTo).orElseThrow();
            if(!propuesta.isAfter(ultima)||propuesta.isAfter(ultima.plusMonths(1)))throw new IllegalArgumentException("El aplazamiento traslada esta cuota al final, entre "+ultima.plusDays(1)+" y "+ultima.plusMonths(1)+". Requiere autorizacion del jefe sin nuevos cargos.");
        }
    }
}
