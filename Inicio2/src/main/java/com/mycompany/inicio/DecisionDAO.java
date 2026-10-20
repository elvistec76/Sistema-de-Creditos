package com.mycompany.inicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DecisionDAO {

    /** Ratifica una evaluacion actualizada sin sobrescribir la decision original ni el contrato. */
    public void ratificarEvaluacion(int solicitud,String jefe,String motivo)throws SQLException{
        ReglasCredito.texto(motivo,300,"Justificacion de la ratificacion");
        try(Connection c=Conexion.getConexion()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);c.setAutoCommit(false);
            try{
                int actor=UsuarioDAO.exigirRol(c,jefe,"JEFE");Seguridad.exigir(c,jefe,"CREDITO_DECIDIR");BloqueoCliente.solicitud(c,solicitud);
                try(PreparedStatement p=c.prepareStatement("SELECT s.estado,s.monto_solicitado,u.limite_aprobacion FROM solicitud_credito s JOIN usuario u ON u.id_usuario=? WHERE s.id_solicitud=? FOR UPDATE")){
                    p.setInt(1,actor);p.setInt(2,solicitud);try(ResultSet r=p.executeQuery()){
                        if(!r.next()||!"APROBADA".equals(r.getString(1)))throw new IllegalArgumentException("Selecciona una solicitud APROBADA sin desembolso.");
                        if(r.getBigDecimal(2)==null||r.getBigDecimal(3)==null||r.getBigDecimal(2).compareTo(r.getBigDecimal(3))>0)throw new IllegalArgumentException("El monto supera tu atribucion de aprobacion.");
                    }
                }
                try(PreparedStatement p=c.prepareStatement("SELECT estado FROM credito WHERE id_solicitud=? FOR UPDATE")){
                    p.setInt(1,solicitud);try(ResultSet r=p.executeQuery()){if(r.next()&&!"APROBADO".equals(r.getString(1)))throw new IllegalArgumentException("El credito ya fue desembolsado; no se ratifica por este procedimiento.");}
                }
                int evaluacion=EvaluacionDAO.exigirFavorable(c,solicitud);
                ReglasCredito.actualizar(c,"UPDATE solicitud_credito SET id_evaluacion_decision=? WHERE id_solicitud=?",evaluacion,solicitud);
                Seguridad.auditar(c,actor,"RATIFICAR_EVALUACION","solicitud_credito",solicitud,"Evaluacion "+evaluacion+"; "+motivo.trim());c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }

    /** Corrige aprobaciones antiguas sin credito preparado, conservando el motivo en auditoria. */
    public void devolverAAnalisis(int solicitud, String jefe, String motivo) throws SQLException {
        if(motivo==null || motivo.isBlank() || motivo.length()>300)
            throw new IllegalArgumentException("Indica el motivo de devolucion, hasta 300 caracteres.");
        try(Connection con=Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);con.setAutoCommit(false);
            try {
                UsuarioDAO.exigirRol(con,jefe,"JEFE");
                int actor=Seguridad.exigir(con,jefe,"CREDITO_DECIDIR");
                BloqueoCliente.solicitud(con,solicitud);
                try(PreparedStatement ps=con.prepareStatement("SELECT estado FROM solicitud_credito WHERE id_solicitud=? FOR UPDATE")){
                    ps.setInt(1,solicitud);try(ResultSet rs=ps.executeQuery()){if(!rs.next()||!"APROBADA".equals(rs.getString(1)))throw new IllegalArgumentException("Selecciona una solicitud aprobada.");}
                }
                try(PreparedStatement ps=con.prepareStatement("SELECT id_credito FROM credito WHERE id_solicitud=? FOR UPDATE")){
                    ps.setInt(1,solicitud);try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new IllegalArgumentException("Ya existe un credito preparado. La devolucion requiere una revision del contrato; no se puede cambiar esta solicitud.");}
                }
                try(PreparedStatement ps=con.prepareStatement("UPDATE solicitud_credito SET estado='OBSERVADA',motivo_decision=?,id_usuario_decision=?,fecha_decision=NOW() WHERE id_solicitud=?")){
                    ps.setString(1,motivo.trim());ps.setInt(2,actor);ps.setInt(3,solicitud);ps.executeUpdate();
                }
                Seguridad.auditar(con,actor,"DEVOLVER_A_ANALISIS","solicitud_credito",solicitud,motivo.trim());con.commit();
            }catch(SQLException | RuntimeException e){con.rollback();throw e;}
        }
    }

    /**
     * Registra la decision del jefe sobre una solicitud.
     * nuevoEstado: APROBADA, RECHAZADA u OBSERVADA.
     * Lanza IllegalArgumentException (con mensaje para mostrar) si no se puede decidir.
     */
    public void decidir(int idSolicitud, String jefeLogin, String nuevoEstado, String motivo)
            throws SQLException {

        if (!java.util.Set.of("APROBADA", "RECHAZADA", "OBSERVADA").contains(
                nuevoEstado == null ? "" : nuevoEstado)) {
            throw new IllegalArgumentException("Decision no valida.");
        }

        try (Connection con = Conexion.getConexion()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);con.setAutoCommit(false);
            try {
                UsuarioDAO.exigirRol(con, jefeLogin, "JEFE");
                Seguridad.exigir(con, jefeLogin, "CREDITO_DECIDIR");
                BloqueoCliente.solicitud(con,idSolicitud);
                BigDecimal monto;
                String estadoActual;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT monto_solicitado, estado FROM solicitud_credito "
                      + "WHERE id_solicitud = ? FOR UPDATE")) {
                    ps.setInt(1, idSolicitud);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("La solicitud no existe.");
                        }
                        monto = rs.getBigDecimal(1);
                        estadoActual = rs.getString(2);
                    }
                }

                if (!estadoActual.equals("PENDIENTE") && !estadoActual.equals("EN_EVALUACION")) {
                    throw new IllegalArgumentException(
                            "Esta solicitud ya no esta por decidir (estado: " + estadoActual + ").");
                }

                int idJefe;
                BigDecimal limite;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_usuario, limite_aprobacion FROM usuario WHERE login = ?")) {
                    ps.setString(1, jefeLogin);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Usuario no encontrado.");
                        }
                        idJefe = rs.getInt(1);
                        limite = rs.getBigDecimal(2);
                    }
                }

                if (nuevoEstado.equals("APROBADA")) {
                    if (monto == null) {
                        throw new IllegalArgumentException(
                                "La solicitud no tiene monto definido. El analista debe completarlo.");
                    }
                    if (monto.compareTo(limite) > 0) {
                        throw new IllegalArgumentException(
                                "El monto (" + monto + ") supera tu limite de aprobacion (" + limite + ").");
                    }
                }

                Integer evaluacion=null;
                if("APROBADA".equals(nuevoEstado))evaluacion=EvaluacionDAO.exigirFavorable(con,idSolicitud);
                if(!"APROBADA".equals(nuevoEstado))ReglasCredito.texto(motivo,300,"Motivo de la decision");
                if(motivo!=null&&motivo.length()>300)throw new IllegalArgumentException("Motivo: hasta 300 caracteres.");
                ReglasCredito.actualizar(con,"UPDATE solicitud_credito SET id_evaluacion_decision=? WHERE id_solicitud=?",evaluacion,idSolicitud);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitud_credito SET estado = ?, id_usuario_decision = ?, "
                      + "fecha_decision = NOW(), motivo_decision = ? WHERE id_solicitud = ?")) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, idJefe);
                    ps.setString(3, motivo);
                    ps.setInt(4, idSolicitud);
                    ps.executeUpdate();
                }

                Seguridad.auditar(con,idJefe,"DECIDIR_SOLICITUD","solicitud_credito",idSolicitud,
                        estadoActual+" -> "+nuevoEstado+"; motivo "+motivo);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
