package com.mycompany.inicio;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Avisos internos: no utiliza correo, SMS ni servicios de mensajeria. */
public class AvisosClienteDAO {
    public record Aviso(int id,String fecha,String tipo,String mensaje,String estado) {
        public boolean nuevo(){return "PENDIENTE".equals(estado);}
    }
    public record Recordatorio(int id,boolean creado) {}
    public static final String SQL_AVISOS =
            "SELECT n.id_notificacion,n.fecha_hora,n.tipo,n.mensaje,n.estado_envio "
          + "FROM notificacion n JOIN usuario u ON u.id_cliente=n.id_cliente "
          + "WHERE u.login=? AND u.activo=1 ORDER BY n.id_notificacion DESC";

    public Recordatorio recordarCuota(int cronograma,int numero,String cajero) throws SQLException {
        try(Connection con=Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            con.setAutoCommit(false);
            try {
                int emisor=UsuarioDAO.exigirRol(con,cajero,"CAJERO");
            Seguridad.exigir(con,cajero,"CRONOGRAMA_CONSULTAR");
                int credito;
                try(PreparedStatement ps=con.prepareStatement("SELECT id_credito FROM cronograma_pago WHERE id_cronograma=?")) {
                    ps.setInt(1,cronograma);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El cronograma no existe.");credito=rs.getInt(1);}
                }
                bloquearCredito(con,credito);
                String mensaje;
                try(PreparedStatement ps=con.prepareStatement("SELECT cr.numero_credito,q.fecha_vencimiento,q.saldo,"
                        + "DATEDIFF(CURDATE(),q.fecha_vencimiento),cp.vigente FROM cuota q "
                        + "JOIN cronograma_pago cp ON cp.id_cronograma=q.id_cronograma "
                        + "JOIN credito cr ON cr.id_credito=cp.id_credito WHERE q.id_cronograma=? AND q.numero=? FOR UPDATE")) {
                    ps.setInt(1,cronograma);ps.setInt(2,numero);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()||!rs.getBoolean(5)||rs.getBigDecimal(3).signum()<=0)throw new IllegalArgumentException("La cuota ya no tiene saldo pendiente o su cronograma no esta vigente.");
                        int atraso=rs.getInt(4);
                        mensaje="Credito "+rs.getString(1)+", cuota "+numero+": saldo pendiente Bs "+rs.getBigDecimal(3)+". "
                                +(atraso>0?"Vencio el "+rs.getDate(2)+"; atraso de "+atraso+" dias.":"Vencimiento: "+rs.getDate(2)+".")
                                +" Revise sus cuotas y consulte en caja para realizar el pago.";
                    }
                }
                int cliente=clienteCredito(con,credito);
                try(PreparedStatement ps=con.prepareStatement("SELECT id_notificacion FROM notificacion WHERE id_cliente=? "
                        + "AND tipo='RECORDATORIO_CUOTA' AND mensaje=? AND DATE(fecha_hora)=CURDATE() LIMIT 1")) {
                    ps.setInt(1,cliente);ps.setString(2,mensaje);
                    try(ResultSet rs=ps.executeQuery()) {if(rs.next()){int id=rs.getInt(1);con.commit();return new Recordatorio(id,false);}}
                }
                int id=insertar(con,cliente,emisor,"RECORDATORIO_CUOTA",mensaje);
                con.commit();return new Recordatorio(id,true);
            }catch(SQLException | RuntimeException e){con.rollback();throw e;}
        }
    }

    /** Se llama desde la misma transaccion que registra el pago. */
    static void confirmarPago(Connection con,int credito,int numero,BigDecimal monto,
            BigDecimal restante,String comprobante,int cajero) throws SQLException {
        insertar(con,clienteCredito(con,credito),cajero,"CONFIRMACION_PAGO",
                "Pago recibido: Bs "+monto+" para la cuota "+numero+". Comprobante: "+comprobante
                +". Saldo restante de esa cuota: Bs "+restante+".");
    }

    public List<Aviso> listar(String clienteLogin) throws SQLException {
        List<Aviso> avisos=new ArrayList<>();
        try(Connection con=Conexion.getConexion()) {
            UsuarioDAO.exigirRol(con,clienteLogin,"CLIENTE");
            Seguridad.exigir(con,clienteLogin,"CRONOGRAMA_CONSULTAR");
            try(PreparedStatement ps=con.prepareStatement(SQL_AVISOS)) {
                ps.setString(1,clienteLogin);try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next())avisos.add(new Aviso(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)));
                }
            }
        }
        return avisos;
    }

    public void marcarMostrados(String clienteLogin,List<Integer> ids) throws SQLException {
        try(Connection con=Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                UsuarioDAO.exigirRol(con,clienteLogin,"CLIENTE");
            Seguridad.exigir(con,clienteLogin,"CRONOGRAMA_CONSULTAR");
                try(PreparedStatement ps=con.prepareStatement("UPDATE notificacion n JOIN usuario u ON u.id_cliente=n.id_cliente "
                        + "SET n.estado_envio='ENVIADA' WHERE n.id_notificacion=? AND u.login=? AND u.activo=1 AND n.estado_envio='PENDIENTE'")) {
                    for(int id:ids){ps.setInt(1,id);ps.setString(2,clienteLogin);ps.addBatch();}ps.executeBatch();
                }
                con.commit();
            }catch(SQLException | RuntimeException e){con.rollback();throw e;}
        }
    }

    private static void bloquearCredito(Connection con,int id) throws SQLException {
        BloqueoCliente.credito(con,id);
        try(PreparedStatement ps=con.prepareStatement("SELECT estado FROM credito WHERE id_credito=? FOR UPDATE")) {
            ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next()||!("VIGENTE".equals(rs.getString(1))||"EN_MORA".equals(rs.getString(1))))throw new IllegalArgumentException("Solo se avisa sobre cuotas pendientes de creditos vigentes o en mora.");
            }
        }
    }
    private static int clienteCredito(Connection con,int credito) throws SQLException {
        try(PreparedStatement ps=con.prepareStatement("SELECT s.id_cliente FROM credito cr JOIN solicitud_credito s ON s.id_solicitud=cr.id_solicitud WHERE cr.id_credito=?")) {
            ps.setInt(1,credito);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("El credito no existe.");return rs.getInt(1);}
        }
    }
    static int insertar(Connection con,int cliente,int emisor,String tipo,String mensaje) throws SQLException {
        int id;
        try(PreparedStatement ps=con.prepareStatement("INSERT INTO notificacion(id_cliente,id_usuario,tipo,mensaje) "
                + "VALUES(?,(SELECT id_usuario FROM usuario WHERE id_cliente=? AND activo=1 LIMIT 1),?,?)",Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,cliente);ps.setInt(2,cliente);ps.setString(3,tipo);ps.setString(4,mensaje);ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(!rs.next())throw new SQLException("No se obtuvo el aviso generado.");id=rs.getInt(1);}
        }
        try(PreparedStatement ps=con.prepareStatement("INSERT INTO auditoria(id_usuario,operacion,entidad,id_entidad,datos_nuevos,resultado) "
                + "VALUES(?,'AVISO_CLIENTE','notificacion',?,?,'CORRECTO')")) {
            ps.setInt(1,emisor);ps.setInt(2,id);ps.setString(3,tipo+": "+mensaje);ps.executeUpdate();
        }
        return id;
    }
}
