package com.mycompany.inicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

/** Registra el seguimiento realizado por el gestor; no envia mensajes. */
public class GestionCobranzaDAO {
    public static final String SQL_CUOTAS =
            "SELECT cr.id_credito AS ID,cr.numero_credito AS Credito,c.ci AS CI,"
          + "CONCAT(c.nombres,' ',c.apellidos) AS Cliente,c.telefono AS Telefono,"
          + "q.numero AS Cuota,q.fecha_vencimiento AS Vence,q.saldo AS Saldo,"
          + "GREATEST(DATEDIFF(CURDATE(),q.fecha_vencimiento),0) AS `Dias atraso` "
          + "FROM cuota q JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma "
          + "JOIN credito cr ON cr.id_credito=cp.id_credito "
          + "JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud "
          + "JOIN cliente c ON c.id_cliente=s.id_cliente "
          + "WHERE cp.vigente=1 AND q.saldo>0 AND cr.estado IN ('VIGENTE','EN_MORA') "
          + "AND ((?='COBRANZA' AND q.fecha_vencimiento<CURDATE()) OR "
          + "(?='PREVENTIVA' AND q.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL (SELECT CAST(valor AS UNSIGNED) FROM parametro WHERE clave='DIAS_RECORDATORIO') DAY))) "
          + "ORDER BY q.fecha_vencimiento,cr.id_credito,q.numero";

    public static final String SQL_HISTORIAL =
            "SELECT g.id_gestion AS Gestion,cr.numero_credito AS Credito,g.fecha_gestion AS Fecha,"
          + "g.tipo AS Tipo,g.medio_contacto AS Medio,g.resultado AS Resultado,g.observaciones AS Observaciones "
          + "FROM gestion_cobranza g JOIN credito cr ON cr.id_credito=g.id_credito "
          + "JOIN usuario u ON u.id_usuario=g.id_usuario WHERE u.login=? ORDER BY g.id_gestion DESC";

    public void registrar(int idCredito, String login, String tipo, String medio,
                          String resultado, String observaciones) throws SQLException {
        if (tipo == null || !Set.of("PREVENTIVA", "COBRANZA").contains(tipo)) {
            throw new IllegalArgumentException("Selecciona un tipo de gestion valido.");
        }
        if (medio == null || medio.isBlank() || medio.length() > 40
                || resultado == null || resultado.isBlank() || resultado.length() > 200
                || (observaciones != null && observaciones.length() > 500)) {
            throw new IllegalArgumentException("Indica medio y resultado (hasta 200 caracteres); observaciones hasta 500.");
        }
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idUsuario = UsuarioDAO.exigirRol(con, login, "GESTOR");
                Seguridad.exigir(con, login, "COBRANZA_GESTIONAR");
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO gestion_cobranza(id_credito,id_usuario,tipo,medio_contacto,resultado,observaciones) "
                      + "SELECT id_credito,?,?,?,?,? FROM credito WHERE id_credito=? AND estado IN ('VIGENTE','EN_MORA')")) {
                    ps.setInt(1, idUsuario); ps.setString(2, tipo); ps.setString(3, medio.trim());
                    ps.setString(4, resultado.trim()); ps.setString(5, observaciones);
                    ps.setInt(6, idCredito);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("El credito no existe o ya no esta vigente.");
                    }
                }
                Seguridad.auditar(con,idUsuario,"REGISTRAR_GESTION","credito",idCredito,tipo+"; "+medio+"; "+resultado+"; "+observaciones);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public static final String SQL_SEGUIMIENTO=SQL_CUOTAS.replace(" AS `Dias atraso` "," AS `Dias atraso`,q.id_cuota AS `ID cuota`,COALESCE((SELECT a.estado FROM alerta a WHERE a.id_cuota=q.id_cuota ORDER BY a.id_alerta DESC LIMIT 1),'SIN GENERAR') AS Alerta ");

    public int avisar(int cuota) throws SQLException {
        try(Connection c=Conexion.getConexion()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try{int actor=UsuarioDAO.exigirRol(c,Sesion.login(),"GESTOR");Seguridad.exigir(c,Sesion.login(),"COBRANZA_GESTIONAR");
                int id=alerta(c,cuota,actor);c.commit();return id;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public void registrarCuota(int cuota,String medio,String resultado,String alternativas)throws SQLException{
        ReglasCredito.texto(medio,40,"Medio de contacto");ReglasCredito.texto(resultado,200,"Resultado de la gestion");ReglasCredito.texto(alternativas,500,"Alternativa ofrecida y observaciones");
        try(Connection c=Conexion.getConexion()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try{
                int actor=UsuarioDAO.exigirRol(c,Sesion.login(),"GESTOR");Seguridad.exigir(c,Sesion.login(),"COBRANZA_GESTIONAR");
                int alerta=alerta(c,cuota,actor);
                int id;
                try(PreparedStatement p=c.prepareStatement("SELECT cp.id_credito,a.tipo FROM alerta a JOIN cuota q USING(id_cuota) JOIN cronograma_pago cp USING(id_cronograma) WHERE a.id_alerta=?")){
                    p.setInt(1,alerta);try(java.sql.ResultSet r=p.executeQuery()){r.next();
                        id=ReglasCredito.insertar(c,"INSERT INTO gestion_cobranza(id_credito,id_alerta,id_usuario,tipo,medio_contacto,resultado,observaciones) VALUES(?,?,?,?,?,?,?)",
                                r.getInt(1),alerta,actor,"MORA".equals(r.getString(2))?"COBRANZA":"PREVENTIVA",medio.trim(),resultado.trim(),alternativas.trim());
                    }
                }
                ReglasCredito.actualizar(c,"UPDATE alerta SET estado='ATENDIDA' WHERE id_alerta=?",alerta);
                Seguridad.auditar(c,actor,"ATENDER_ALERTA","gestion_cobranza",id,"Alerta "+alerta+"; cuota "+cuota+"; "+medio+"; "+resultado+"; alternativa "+alternativas);
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    private static int alerta(Connection c,int cuota,int actor)throws SQLException{
        int credito;
        try(PreparedStatement p=c.prepareStatement("SELECT cp.id_credito FROM cuota q JOIN cronograma_pago cp USING(id_cronograma) WHERE q.id_cuota=?")){
            p.setInt(1,cuota);try(java.sql.ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Cuota inexistente.");credito=r.getInt(1);}
        }
        BloqueoCliente.credito(c,credito);
        try(PreparedStatement p=c.prepareStatement("SELECT estado FROM credito WHERE id_credito=? FOR UPDATE")){
            p.setInt(1,credito);try(java.sql.ResultSet r=p.executeQuery()){r.next();if(!Set.of("VIGENTE","EN_MORA").contains(r.getString(1)))throw new IllegalArgumentException("Credito sin deuda activa.");}
        }
        int dias=ReglasCredito.parametro(c,"DIAS_RECORDATORIO",366),cliente,atraso;String tipo,mensaje;
        try(PreparedStatement p=c.prepareStatement("SELECT q.saldo,DATEDIFF(CURDATE(),q.fecha_vencimiento),q.fecha_vencimiento,q.numero,cp.vigente,s.id_cliente,cr.numero_credito FROM cuota q JOIN cronograma_pago cp USING(id_cronograma) JOIN credito cr USING(id_credito) JOIN solicitud_credito s USING(id_solicitud) WHERE q.id_cuota=? FOR UPDATE")){
            p.setInt(1,cuota);try(java.sql.ResultSet r=p.executeQuery()){
                if(!r.next()||!r.getBoolean(5)||r.getBigDecimal(1).signum()<=0)throw new IllegalArgumentException("Cuota pagada o cronograma anterior. Actualiza la tabla.");
                atraso=r.getInt(2);if(atraso< -dias)throw new IllegalArgumentException("La cuota todavia no esta dentro del periodo preventivo de "+dias+" dias.");
                tipo=atraso>0?"MORA":"PREVENTIVA";cliente=r.getInt(6);
                mensaje="Credito "+r.getString(7)+", cuota "+r.getInt(4)+": saldo Bs "+r.getBigDecimal(1)+". Vencimiento "+r.getDate(3)+". "
                        +(atraso>0?"Atraso: "+atraso+" dias. Acercate a caja para regularizar el pago.":"Recordatorio preventivo. Puedes consultar tu cuota y pagar en caja; este aviso no significa mora.");
            }
        }
        try(PreparedStatement p=c.prepareStatement("SELECT id_alerta FROM alerta WHERE id_cuota=? AND tipo=? AND fecha_generacion=CURDATE()")){
            p.setInt(1,cuota);p.setString(2,tipo);try(java.sql.ResultSet r=p.executeQuery()){if(r.next())return r.getInt(1);}
        }
        int id=ReglasCredito.insertar(c,"INSERT INTO alerta(id_cuota,tipo,fecha_generacion,dias_retraso) VALUES(?,?,CURDATE(),?)",cuota,tipo,Math.max(atraso,0));
        AvisosClienteDAO.insertar(c,cliente,actor,"ALERTA_"+tipo,mensaje);
        Seguridad.auditar(c,actor,"GENERAR_ALERTA","alerta",id,"Cuota "+cuota+"; "+tipo+"; aviso interno al cliente");return id;
    }
}
