package com.mycompany.inicio;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Formalizacion y registro manual de desembolso; no ejecuta transferencias bancarias. */
public class CicloCreditoDAO {
    public static final String SQL_APROBADAS =
            "SELECT s.id_solicitud AS Solicitud,c.ci AS CI,CONCAT(c.nombres,' ',c.apellidos) AS Cliente,"
            + "p.nombre AS Producto,s.monto_solicitado AS Monto,s.plazo_meses AS Meses,"
            + "cr.numero_credito AS Credito,ct.numero_contrato AS Contrato,"
            + "COALESCE(ct.estado,'SIN PREPARAR') AS Formalizacion "
            + "FROM solicitud_credito s JOIN cliente c ON c.id_cliente=s.id_cliente "
            + "LEFT JOIN producto_credito p ON p.id_producto=s.id_producto "
            + "LEFT JOIN credito cr ON cr.id_solicitud=s.id_solicitud "
            + "LEFT JOIN contrato ct ON ct.id_credito=cr.id_credito AND ct.version="
            + "(SELECT MAX(c2.version) FROM contrato c2 WHERE c2.id_credito=cr.id_credito) "
            + "WHERE s.estado='APROBADA' AND (cr.id_credito IS NULL OR cr.estado='APROBADO') ORDER BY s.id_solicitud DESC";
    public static final String SQL_DESEMBOLSOS =
            "SELECT cr.id_credito AS ID,cr.numero_credito AS Credito,c.ci AS CI,"
            + "CONCAT(c.nombres,' ',c.apellidos) AS Cliente,cr.monto_aprobado AS Monto,"
            + "COALESCE(ct.estado,'SIN CONTRATO') AS Contrato,d.id_desembolso AS Operacion,"
            + "COALESCE(d.estado,'SIN SOLICITAR') AS Desembolso,d.fecha_prevista AS Prevista,"
            + "d.fecha_efectiva AS Entrega,d.cuenta_destino AS Destino,d.referencia_externa AS Referencia "
            + "FROM credito cr JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
            + "JOIN cliente c ON c.id_cliente=s.id_cliente "
            + "LEFT JOIN contrato ct ON ct.id_credito=cr.id_credito AND ct.version="
            + "(SELECT MAX(c2.version) FROM contrato c2 WHERE c2.id_credito=cr.id_credito) "
            + "LEFT JOIN desembolso d ON d.id_credito=cr.id_credito AND d.id_desembolso="
            + "(SELECT MAX(d2.id_desembolso) FROM desembolso d2 WHERE d2.id_credito=cr.id_credito) "
            + "ORDER BY cr.id_credito DESC";
    public static final String SQL_PENDIENTES = SQL_DESEMBOLSOS.replace("ORDER BY cr.id_credito DESC",
            "WHERE cr.estado='APROBADO' AND (d.id_desembolso IS NULL OR d.estado IN ('SOLICITADO','PENDIENTE_VERIFICACION')) ORDER BY cr.id_credito DESC");
    public record Condiciones(int solicitud, int politica, String cliente, BigDecimal monto, BigDecimal tasa,
                              int meses, String tipo, boolean garantia) {}
    public record Preparacion(int credito, int contrato, String numero) {}

    public Condiciones condiciones(int solicitud) throws SQLException {
        Seguridad.comprobar("CONTRATO_FORMALIZAR");
        try (Connection con = Conexion.getConexion()) { return condiciones(con, solicitud); }
    }

    public Preparacion preparacion(int solicitud) throws SQLException {
        Seguridad.comprobar("CONTRATO_FORMALIZAR");
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement("SELECT id_credito FROM credito WHERE id_solicitud=?")){
            ps.setInt(1,solicitud);try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())throw new IllegalArgumentException("Primero pulsa Preparar credito y contrato.");
                Preparacion ct=contrato(con,rs.getInt(1));
                if(ct==null)throw new IllegalArgumentException("Primero prepara el contrato.");return ct;
            }
        }
    }

    public static LocalDate fechaActual() throws SQLException {
        try(Connection con=Conexion.getConexion()){return hoy(con);}
    }

    private Condiciones condiciones(Connection con, int solicitud) throws SQLException {
        String sql = "SELECT s.estado,s.monto_solicitado,s.plazo_meses,p.id_politica,p.tasa_interes,p.tipo_cuota,"
                + "p.exige_garantia,p.activo,p.monto_minimo,p.monto_maximo,p.plazo_maximo_meses,"
                + "pc.activa,pc.vigente_desde,pc.vigente_hasta,CONCAT(c.nombres,' ',c.apellidos),"
                + "c.lugar_trabajo,c.ingresos_mensuales "
                + "FROM solicitud_credito s JOIN cliente c ON c.id_cliente=s.id_cliente "
                + "LEFT JOIN producto_credito p ON p.id_producto=s.id_producto "
                + "LEFT JOIN politica_crediticia pc ON pc.id_politica=p.id_politica WHERE s.id_solicitud=? LOCK IN SHARE MODE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, solicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"APROBADA".equals(rs.getString(1))) throw new IllegalArgumentException("Selecciona una solicitud APROBADA.");
                BigDecimal monto = rs.getBigDecimal(2); int meses = rs.getInt(3); LocalDate hoy = hoy(con);
                if (monto == null || rs.getObject(4) == null || !rs.getBoolean(8) || meses < 1
                        || monto.compareTo(rs.getBigDecimal(9)) < 0 || monto.compareTo(rs.getBigDecimal(10)) > 0
                        || meses > rs.getInt(11)) throw new IllegalArgumentException("Producto, monto o plazo aprobados no cumplen sus limites. El jefe debe devolver la solicitud a analisis.");
                if (!rs.getBoolean(12) || rs.getDate(13).toLocalDate().isAfter(hoy)
                        || (rs.getDate(14) != null && rs.getDate(14).toLocalDate().isBefore(hoy)))
                    throw new IllegalArgumentException("La politica del producto no esta vigente.");
                if (rs.getString(16) == null || rs.getString(16).isBlank() || rs.getBigDecimal(17) == null || rs.getBigDecimal(17).signum() <= 0)
                    throw new IllegalArgumentException("Completa trabajo e ingresos del cliente antes de preparar el credito.");
                return new Condiciones(solicitud, rs.getInt(4), rs.getString(15), monto, rs.getBigDecimal(5), meses, rs.getString(6), rs.getBoolean(7));
            }
        }
    }

    public Preparacion preparar(int solicitud, String login) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);con.setAutoCommit(false);
            try {
                int usuario = Seguridad.exigir(con, login, "CONTRATO_FORMALIZAR");
                bloquearSolicitud(con, solicitud);
                Condiciones c = condiciones(con, solicitud);
                revisarExpediente(con, c);
                int credito;
                try (PreparedStatement ps = con.prepareStatement("SELECT id_credito,estado FROM credito WHERE id_solicitud=? FOR UPDATE")) {
                    ps.setInt(1, solicitud);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (!"APROBADO".equals(rs.getString(2))) throw new IllegalArgumentException("El credito ya fue desembolsado.");
                            credito = rs.getInt(1);
                            comprobarCondiciones(con, credito, c);
                        } else credito = insertar(con, "INSERT INTO credito(id_solicitud,id_politica,numero_credito,monto_aprobado,tasa_interes,plazo_meses,tipo_cuota,fecha_aprobacion,saldo_capital,estado) "
                                + "SELECT id_solicitud,?,?,?,?,?,?,COALESCE(fecha_decision,NOW()),0,'APROBADO' FROM solicitud_credito WHERE id_solicitud=?",
                                c.politica(), numero("CR"), c.monto(), c.tasa(), c.meses(), c.tipo(), solicitud);
                    }
                }
                Preparacion existente = contrato(con, credito);
                if (existente != null) { con.commit(); return existente; }
                String numero = numero("CT");
                int contrato = insertar(con, "INSERT INTO contrato(id_credito,id_usuario,numero_contrato,version,version_plantilla) "
                        + "VALUES(?,?,?,1,'MENSUAL-NOM-V1')", credito, usuario, numero);
                Seguridad.auditar(con, usuario, "PREPARAR_CREDITO", "credito", credito,
                        "Solicitud " + solicitud + "; " + c.monto() + " Bs; tasa nominal anual " + c.tasa() + "%; " + c.meses() + " meses; " + c.tipo() + "; contrato " + numero);
                con.commit(); return new Preparacion(credito, contrato, numero);
            } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        }
    }

    public void formalizar(int credito, String login, LocalDate firma, String archivo) throws SQLException {
        archivo(archivo);
        try (Connection con = Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);con.setAutoCommit(false);
            try {
                int usuario = Seguridad.exigir(con, login, "CONTRATO_FORMALIZAR");
                int solicitud;
                try(PreparedStatement ps=con.prepareStatement("SELECT id_solicitud FROM credito WHERE id_credito=?")){ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Credito no encontrado.");solicitud=rs.getInt(1);}}
                bloquearSolicitud(con,solicitud); bloquearCredito(con, credito, "APROBADO");
                if (firma == null || firma.isAfter(hoy(con))) throw new IllegalArgumentException("Indica una fecha de firma que no sea futura.");
                Condiciones c = condiciones(con, solicitud); comprobarCondiciones(con, credito, c); revisarExpediente(con, c);
                Preparacion ct = contrato(con, credito);
                if (ct == null) throw new IllegalArgumentException("Primero prepara el credito y el contrato.");
                try (PreparedStatement ps = con.prepareStatement("SELECT estado,DATE(fecha_generacion) FROM contrato WHERE id_contrato=? FOR UPDATE")) {
                    ps.setInt(1, ct.contrato());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (!"GENERADO".equals(rs.getString(1))) throw new IllegalArgumentException("El contrato ya no esta pendiente de firma.");
                        if (firma.isBefore(rs.getDate(2).toLocalDate())) throw new IllegalArgumentException("La firma no puede ser anterior a la preparacion del contrato.");
                    }
                }
                actualizar(con, "UPDATE contrato SET referencia_documental=?,fecha_firma=?,estado='FORMALIZADO' WHERE id_contrato=?", archivo, java.sql.Date.valueOf(firma), ct.contrato());
                actualizar(con, "UPDATE garantia SET id_credito=?,estado='EN_CUSTODIA' WHERE id_solicitud=? AND estado='REGISTRADA' AND estado_verificacion='VALIDADA' AND id_credito IS NULL", credito, solicitud);
                Seguridad.auditar(con, usuario, "FORMALIZAR_CONTRATO", "contrato", ct.contrato(), "Firma " + firma + "; respaldo " + archivo);
                con.commit();
            } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        }
    }

    public int solicitarDesembolso(int credito, String login, LocalDate prevista, String destino) throws SQLException {
        texto(destino, 40, "Cuenta o destino de entrega");
        try (Connection con = Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);con.setAutoCommit(false);
            try {
                int usuario = Seguridad.exigir(con, login, "DESEMBOLSO_REGISTRAR");
                int solicitud=bloquearCredito(con, credito, "APROBADO");
                Condiciones condiciones=condiciones(con,solicitud);
                comprobarCondiciones(con,credito,condiciones); revisarExpediente(con,condiciones); exigirContrato(con, credito);
                if (prevista == null || prevista.isBefore(hoy(con))) throw new IllegalArgumentException("La fecha prevista debe ser hoy o una fecha futura.");
                try (PreparedStatement ps = con.prepareStatement("SELECT id_desembolso FROM desembolso WHERE id_credito=? AND estado<>'RECHAZADO' FOR UPDATE")) {
                    ps.setInt(1, credito); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) throw new IllegalArgumentException("El credito ya tiene una operacion de desembolso. Consulta Desembolsos pendientes."); }
                }
                int id = insertar(con, "INSERT INTO desembolso(id_credito,id_usuario,clave_operacion,fecha_prevista,monto,cuenta_destino) SELECT id_credito,?,?,?,monto_aprobado,? FROM credito WHERE id_credito=?",
                        usuario, UUID.randomUUID().toString(), java.sql.Date.valueOf(prevista), destino.trim(), credito);
                Seguridad.auditar(con, usuario, "SOLICITAR_DESEMBOLSO", "desembolso", id, "Credito " + credito + "; prevista " + prevista + "; destino " + destino);
                con.commit(); return id;
            } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        }
    }

    public void confirmarDesembolso(int operacion, String login, String referencia) throws SQLException {
        texto(referencia, 80, "Referencia de la entrega realizada");
        try (Connection con = Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); con.setAutoCommit(false);
            try {
                int usuario = Seguridad.exigir(con, login, "DESEMBOLSO_REGISTRAR");
                int credito = creditoOperacion(con, operacion);
                int solicitud=bloquearCredito(con, credito, "APROBADO");
                Condiciones condiciones=condiciones(con,solicitud);
                comprobarCondiciones(con,credito,condiciones); revisarExpediente(con,condiciones); exigirContrato(con, credito);
                int evaluacion=EvaluacionDAO.exigirFavorable(con,solicitud);
                try(PreparedStatement p=con.prepareStatement("SELECT id_evaluacion_decision FROM solicitud_credito WHERE id_solicitud=?")){
                    p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){
                        r.next();if(r.getObject(1)==null||r.getInt(1)!=evaluacion)throw new IllegalArgumentException("La evaluacion fue actualizada. El jefe debe revisarla y pulsar Ratificar evaluacion en Decisiones tomadas antes del desembolso.");
                    }
                }
                LocalDate hoy = hoy(con);
                try (PreparedStatement ps = con.prepareStatement("SELECT estado,fecha_prevista,monto FROM desembolso WHERE id_desembolso=? FOR UPDATE")) {
                    ps.setInt(1, operacion); try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (!Set.of("SOLICITADO", "PENDIENTE_VERIFICACION").contains(rs.getString(1))) throw new IllegalArgumentException("La operacion ya fue confirmada o rechazada.");
                        if (rs.getDate(2).toLocalDate().isAfter(hoy)) throw new IllegalArgumentException("Aun no llega la fecha prevista de entrega.");
                    }
                }
                if (contar(con, "SELECT COUNT(*) FROM desembolso WHERE id_credito=? AND estado='CONFIRMADO'", credito) > 0
                        || contar(con, "SELECT COUNT(*) FROM cronograma_pago WHERE id_credito=?", credito) > 0)
                    throw new IllegalArgumentException("El credito ya tiene desembolso o cronograma; requiere revision, no una segunda entrega.");
                BigDecimal principal; BigDecimal tasa; int meses; String tipo;
                try (PreparedStatement ps = con.prepareStatement("SELECT monto_aprobado,tasa_interes,plazo_meses,tipo_cuota FROM credito WHERE id_credito=?")) {
                    ps.setInt(1, credito); try (ResultSet rs = ps.executeQuery()) { rs.next(); principal=rs.getBigDecimal(1); tasa=rs.getBigDecimal(2); meses=rs.getInt(3); tipo=rs.getString(4); }
                }
                var plan = Amortizacion.generar(principal, tasa, meses, tipo, hoy);
                int cronograma = insertar(con, "INSERT INTO cronograma_pago(id_credito,id_usuario,version,motivo,fecha_desembolso_prevista,vigente) VALUES(?,?,1,'Plan mensual nominal: anual/12; sin cargos adicionales',?,1)", credito, usuario, java.sql.Date.valueOf(hoy));
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO cuota(id_cronograma,numero,fecha_vencimiento,capital,interes,monto,saldo,estado) VALUES(?,?,?,?,?,?,?,'PENDIENTE')")) {
                    for (var q : plan) {
                        ps.setInt(1,cronograma); ps.setInt(2,q.numero()); ps.setDate(3,java.sql.Date.valueOf(q.vence()));
                        ps.setBigDecimal(4,q.capital()); ps.setBigDecimal(5,q.interes()); ps.setBigDecimal(6,q.monto()); ps.setBigDecimal(7,q.monto()); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                actualizar(con, "UPDATE desembolso SET estado='CONFIRMADO',fecha_efectiva=NOW(),referencia_externa=? WHERE id_desembolso=?", referencia.trim(), operacion);
                actualizar(con, "UPDATE credito SET estado='VIGENTE',saldo_capital=monto_aprobado WHERE id_credito=?", credito);
                actualizar(con, "INSERT INTO movimiento_financiero(id_usuario,id_desembolso,tipo,concepto,monto) VALUES(?,?,'EGRESO','Desembolso confirmado manualmente',?)", usuario, operacion, principal);
                Seguridad.auditar(con, usuario, "CONFIRMAR_DESEMBOLSO", "desembolso", operacion, "Referencia " + referencia + "; cronograma " + cronograma + "; cuotas " + meses + "; fecha real " + hoy);
                con.commit();
            } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        }
    }

    public void rechazarDesembolso(int operacion, String login, String motivo) throws SQLException {
        texto(motivo,300,"Motivo del rechazo");
        try (Connection con = Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);con.setAutoCommit(false);
            try {
                int usuario = Seguridad.exigir(con,login,"DESEMBOLSO_REGISTRAR");
                bloquearCredito(con,creditoOperacion(con,operacion),"APROBADO");
                int n = actualizar(con,"UPDATE desembolso SET estado='RECHAZADO',motivo_rechazo=? WHERE id_desembolso=? AND estado IN ('SOLICITADO','PENDIENTE_VERIFICACION')",motivo.trim(),operacion);
                if(n!=1)throw new IllegalArgumentException("Solo se rechazan operaciones pendientes.");
                Seguridad.auditar(con,usuario,"RECHAZAR_DESEMBOLSO","desembolso",operacion,motivo); con.commit();
            } catch(SQLException | RuntimeException e){con.rollback();throw e;}
        }
    }

    public List<Amortizacion.CuotaPlan> planCredito(int credito, LocalDate fecha) throws SQLException {
        Seguridad.comprobar("DESEMBOLSO_REGISTRAR");
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement("SELECT monto_aprobado,tasa_interes,plazo_meses,tipo_cuota FROM credito WHERE id_credito=?")) {
            ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())throw new IllegalArgumentException("Credito no encontrado.");
                List<Amortizacion.CuotaPlan> guardado=new ArrayList<>();BigDecimal saldo=rs.getBigDecimal(1);
                try(PreparedStatement cuotas=con.prepareStatement("SELECT q.numero,q.fecha_vencimiento,q.capital,q.interes,q.monto FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma WHERE cp.id_credito=? AND cp.vigente=1 ORDER BY q.numero")){
                    cuotas.setInt(1,credito);try(ResultSet q=cuotas.executeQuery()){while(q.next()){
                        saldo=saldo.subtract(q.getBigDecimal(3));guardado.add(new Amortizacion.CuotaPlan(q.getInt(1),q.getDate(2).toLocalDate(),q.getBigDecimal(3),q.getBigDecimal(4),q.getBigDecimal(5),saldo));
                    }}
                }
                return guardado.isEmpty()?Amortizacion.generar(rs.getBigDecimal(1),rs.getBigDecimal(2),rs.getInt(3),rs.getString(4),fecha):List.copyOf(guardado);
            }
        }
    }

    public String resumenCredito(int credito) throws SQLException {
        Seguridad.comprobar("DESEMBOLSO_REGISTRAR");
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement("SELECT numero_credito,monto_aprobado,tasa_interes,plazo_meses,tipo_cuota FROM credito WHERE id_credito=?")){
            ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())throw new IllegalArgumentException("Credito no encontrado.");
                return "Credito "+rs.getString(1)+" | Monto Bs "+rs.getBigDecimal(2)+" | Tasa nominal anual "+rs.getBigDecimal(3)+"% | "+rs.getInt(4)+" meses | "+rs.getString(5);
            }
        }
    }

    private static void revisarExpediente(Connection con, Condiciones c) throws SQLException {
        int documentos=0;
        try(PreparedStatement ps=con.prepareStatement("SELECT nombre_archivo,estado_verificacion FROM documento_solicitud WHERE id_solicitud=? LOCK IN SHARE MODE")) {
            ps.setInt(1,c.solicitud());try(ResultSet rs=ps.executeQuery()){while(rs.next()){documentos++;if(!"VERIFICADO".equals(rs.getString(2)))throw new IllegalArgumentException("Verifica todos los documentos de la solicitud antes de formalizar.");archivo(rs.getString(1));}}
        }
        if(documentos==0)throw new IllegalArgumentException("Adjunta y verifica los documentos en Clientes > Validar datos y documentos.");
        if(c.garantia()) {
            int validas=0;
            try(PreparedStatement ps=con.prepareStatement("SELECT g.id_garantia,t.nombre,g.valor,g.titular,g.evidencia_verificacion FROM garantia g JOIN tipo_garantia t USING(id_tipo_garantia) WHERE g.id_solicitud=? AND g.estado_verificacion='VALIDADA' AND g.estado IN ('REGISTRADA','EN_CUSTODIA') LOCK IN SHARE MODE")) {
                ps.setInt(1,c.solicitud());try(ResultSet rs=ps.executeQuery()){while(rs.next()){
                    if(rs.getString(4)==null || rs.getString(4).isBlank() || rs.getString(5)==null || rs.getString(5).isBlank())continue;
                    if(!"PERSONAL".equals(rs.getString(2)) && rs.getBigDecimal(3).signum()<=0)continue;
                    try(PreparedStatement docs=con.prepareStatement("SELECT nombre_archivo FROM documento_garantia WHERE id_garantia=?")) {
                        docs.setInt(1,rs.getInt(1));try(ResultSet archivos=docs.executeQuery()){if(archivos.next()){do{archivo(archivos.getString(1));}while(archivos.next());validas++;}}
                    }
                }}
            }
            if(validas==0)throw new IllegalArgumentException("Este producto exige una garantia VALIDADA con evidencia y respaldo local.");
        }
    }
    private static void comprobarCondiciones(Connection con,int credito,Condiciones c) throws SQLException {
        try(PreparedStatement ps=con.prepareStatement("SELECT monto_aprobado,tasa_interes,plazo_meses,tipo_cuota,id_politica FROM credito WHERE id_credito=?")) {
            ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){rs.next();
                if(rs.getBigDecimal(1).compareTo(c.monto())!=0 || rs.getBigDecimal(2).compareTo(c.tasa())!=0 || rs.getInt(3)!=c.meses() || !rs.getString(4).equals(c.tipo()) || rs.getInt(5)!=c.politica())
                    throw new IllegalArgumentException("Las condiciones del producto o solicitud cambiaron despues de preparar el credito. Revisa el expediente antes de continuar.");
            }
        }
    }
    private static void exigirContrato(Connection con,int credito) throws SQLException {
        try(PreparedStatement ps=con.prepareStatement("SELECT referencia_documental FROM contrato WHERE id_credito=? AND estado='FORMALIZADO' ORDER BY version DESC LIMIT 1 LOCK IN SHARE MODE")) {
            ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Formaliza el contrato firmado antes de solicitar o confirmar el desembolso.");archivo(rs.getString(1));}
        }
    }
    private static Preparacion contrato(Connection con,int credito) throws SQLException {
        try(PreparedStatement ps=con.prepareStatement("SELECT id_contrato,numero_contrato FROM contrato WHERE id_credito=? ORDER BY version DESC LIMIT 1 FOR UPDATE")){ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){return rs.next()?new Preparacion(credito,rs.getInt(1),rs.getString(2)):null;}}
    }
    private static void bloquearSolicitud(Connection con,int solicitud) throws SQLException {
        BloqueoCliente.solicitud(con,solicitud);
        try(PreparedStatement ps=con.prepareStatement("SELECT id_solicitud FROM solicitud_credito WHERE id_solicitud=? FOR UPDATE")){ps.setInt(1,solicitud);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Solicitud inexistente.");}}
    }
    private static int bloquearCredito(Connection con,int credito,String estado) throws SQLException {
        // Mismo orden de bloqueos que preparar: solicitud antes que credito.
        try(PreparedStatement ps=con.prepareStatement("SELECT id_solicitud FROM credito WHERE id_credito=?")){
            ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Credito no encontrado.");bloquearSolicitud(con,rs.getInt(1));}
        }
        try(PreparedStatement ps=con.prepareStatement("SELECT id_solicitud,estado FROM credito WHERE id_credito=? FOR UPDATE")){ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){if(!rs.next()||!estado.equals(rs.getString(2)))throw new IllegalArgumentException("El credito no esta en estado "+estado+"; actualiza la pantalla.");return rs.getInt(1);}}
    }
    private static int creditoOperacion(Connection con,int operacion) throws SQLException {
        try(PreparedStatement ps=con.prepareStatement("SELECT id_credito FROM desembolso WHERE id_desembolso=?")){ps.setInt(1,operacion);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Selecciona una operacion de desembolso.");return rs.getInt(1);}}
    }
    public static LocalDate hoy(Connection con) throws SQLException {try(Statement st=con.createStatement();ResultSet rs=st.executeQuery("SELECT CURDATE()")){rs.next();return rs.getDate(1).toLocalDate();}}
    private static int contar(Connection con,String sql,int id) throws SQLException {try(PreparedStatement ps=con.prepareStatement(sql)){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}}
    private static int insertar(Connection con,String sql,Object... valores) throws SQLException {try(PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){parametros(ps,valores);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){if(!rs.next())throw new SQLException("No se obtuvo el identificador generado.");return rs.getInt(1);}}}
    private static int actualizar(Connection con,String sql,Object... valores) throws SQLException {try(PreparedStatement ps=con.prepareStatement(sql)){parametros(ps,valores);return ps.executeUpdate();}}
    private static void parametros(PreparedStatement ps,Object[] valores) throws SQLException {for(int i=0;i<valores.length;i++)ps.setObject(i+1,valores[i]);}
    private static String numero(String prefijo){return prefijo+"-"+UUID.randomUUID().toString().replace("-","").substring(0,26);}
    private static void texto(String s,int max,String campo){if(s==null||s.isBlank()||s.length()>max)throw new IllegalArgumentException(campo+": obligatorio, hasta "+max+" caracteres.");}
    private static void archivo(String referencia){try{ArchivoDocumental.resolver(referencia);}catch(java.io.IOException e){throw new IllegalArgumentException(e.getMessage(),e);}}
}
