package com.mycompany.inicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SolicitudDAO {

    /** Alta presencial por un oficial, usando un cliente ya registrado. */
    public int registrarOficial(String ci, String destino, String login) throws SQLException {
        if(ci==null || ci.isBlank() || destino==null || destino.isBlank() || destino.length()>150)
            throw new IllegalArgumentException("Indica CI y uso del dinero (hasta 150 caracteres).");
        try(Connection con=Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int actor=Seguridad.exigir(con,login,"SOLICITUD_REGISTRAR");
                int cliente;
                try(PreparedStatement ps=con.prepareStatement("SELECT id_cliente,lugar_trabajo,ingresos_mensuales FROM cliente WHERE ci=? LOCK IN SHARE MODE")) {
                    ps.setString(1,ci.trim());try(ResultSet rs=ps.executeQuery()){
                        if(!rs.next())throw new IllegalArgumentException("Primero registra al cliente.");
                        if(rs.getString(2)==null||rs.getString(2).isBlank()||rs.getBigDecimal(3)==null||rs.getBigDecimal(3).signum()<=0)
                            throw new IllegalArgumentException("Completa trabajo e ingresos en Actualizar cliente.");
                        cliente=rs.getInt(1);
                    }
                }
                int id;
                try(PreparedStatement ps=con.prepareStatement("INSERT INTO solicitud_credito(id_cliente,id_usuario,fecha_solicitud,destino) VALUES(?,?,CURDATE(),?)",Statement.RETURN_GENERATED_KEYS)){
                    ps.setInt(1,cliente);ps.setInt(2,actor);ps.setString(3,destino.trim());ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();id=rs.getInt(1);}
                }
                Seguridad.auditar(con,actor,"REGISTRAR_SOLICITUD","solicitud_credito",id,"CI "+ci+"; destino "+destino);con.commit();return id;
            }catch(SQLException | RuntimeException e){con.rollback();throw e;}
        }
    }

    /**
     * Registra la solicitud de un cliente (identificado por su CI).
     * Hace 3 cosas, todo o nada:
     *   1) actualiza lugar de trabajo e ingresos del cliente
     *   2) inserta la solicitud (estado REGISTRADA)
     *   3) si eligio garantia, inserta un registro en la tabla garantia
     * Devuelve el numero de solicitud.
     */
    public int registrar(String ci, String destino, String trabajo,
                         BigDecimal ingresos, String garantia) throws SQLException {

        if(destino==null || destino.isBlank() || destino.length()>150 || trabajo==null || trabajo.isBlank() || trabajo.length()>100
                || ingresos==null || ingresos.signum()<=0 || ingresos.stripTrailingZeros().scale()>2
                || ingresos.compareTo(new BigDecimal("999999999999.99"))>0)
            throw new IllegalArgumentException("Completa uso del dinero, trabajo e ingresos validos.");
        String sqlCliente = "UPDATE cliente SET lugar_trabajo = ?, ingresos_mensuales = ? "
                          + "WHERE ci = ?";

        // El usuario que registra es la cuenta del propio cliente (login = CI)
        String sqlSolicitud = "INSERT INTO solicitud_credito "
                + "(id_cliente, id_usuario, fecha_solicitud, destino) "
                + "VALUES ((SELECT id_cliente FROM cliente WHERE ci = ?), "
                + "(SELECT id_usuario FROM usuario WHERE login = ?), "
                + "CURDATE(), ?)";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int actor=UsuarioDAO.exigirRol(con, ci, "CLIENTE");
                try (PreparedStatement propia = con.prepareStatement("SELECT 1 FROM usuario u JOIN cliente c ON c.id_cliente=u.id_cliente WHERE u.login=? AND c.ci=?")) {
                    propia.setString(1, ci); propia.setString(2, ci);
                    try (ResultSet rs = propia.executeQuery()) { if (!rs.next()) throw new IllegalArgumentException("La solicitud debe ser del cliente autenticado."); }
                }
                // 1. Trabajo e ingresos
                try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                    ps.setString(1, trabajo);
                    ps.setBigDecimal(2, ingresos);
                    ps.setString(3, ci);
                    ps.executeUpdate();
                }

                // 2. Solicitud
                int numero = -1;
                try (PreparedStatement ps = con.prepareStatement(sqlSolicitud,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, ci);
                    ps.setString(2, ci);
                    ps.setString(3, destino);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            numero = rs.getInt(1);
                        }
                    }
                }

                // 3. Garantia (opcional)
                if (garantia != null) {
                    registrarGarantia(con, numero, ci, garantia);
                }

                Seguridad.auditar(con,actor,"REGISTRAR_SOLICITUD","solicitud_credito",numero,"Solicitud del cliente "+ci);
                con.commit();
                return numero;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void registrarGarantia(Connection con, int numeroSolicitud,
                                   String ci, String tipo) throws SQLException {

        try(PreparedStatement ps=con.prepareStatement("SELECT id_tipo_garantia FROM tipo_garantia WHERE nombre=? AND activo=1")) {
            ps.setString(1,tipo);try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Selecciona un tipo de garantia activo del catalogo.");}
        }

        String sql = "INSERT INTO garantia "
                + "(id_solicitud, id_tipo_garantia, id_usuario, descripcion, titular, valor) "
                + "VALUES (?, "
                + "(SELECT id_tipo_garantia FROM tipo_garantia WHERE nombre = ?), "
                + "(SELECT id_usuario FROM usuario WHERE login = ?), "
                + "?, "
                + "(SELECT nombre_completo FROM usuario WHERE login = ?), "
                + "0)";   // valor: el oficial lo define al verificar la garantia

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroSolicitud);
            ps.setString(2, tipo);
            ps.setString(3, ci);
            ps.setString(4, "Garantia " + tipo + " declarada por el cliente");
            ps.setString(5, ci);
            ps.executeUpdate();
        }
    }
        /**
     * El analista define producto, monto y plazo y envia la solicitud al jefe
     * (estado EN_EVALUACION). Valida contra los limites del producto.
     * Lanza IllegalArgumentException con un mensaje para mostrar si algo no cumple.
     */
    public void enviarAEvaluacion(int idSolicitud, int idProducto,
                                  BigDecimal monto, int plazo) throws SQLException {
        if (monto == null || monto.signum() <= 0 || plazo < 1) {
            throw new IllegalArgumentException("Indica un monto positivo y un plazo mayor a cero.");
        }
        try {
            monto = monto.setScale(2, java.math.RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("El monto admite hasta dos decimales.");
        }

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int actor=Seguridad.exigir(con, Sesion.login(), "EVALUACION_REALIZAR");
                BloqueoCliente.solicitud(con,idSolicitud);
                String estado;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT estado FROM solicitud_credito WHERE id_solicitud = ? FOR UPDATE")) {
                    ps.setInt(1, idSolicitud);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("La solicitud no existe.");
                        }
                        estado = rs.getString(1);
                    }
                }
                if (!estado.equals("REGISTRADA") && !estado.equals("OBSERVADA")) {
                    throw new IllegalArgumentException(
                            "La solicitud ya no se puede modificar (estado: " + estado + ").");
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT nombre, monto_minimo, monto_maximo, plazo_maximo_meses "
                      + "FROM producto_credito WHERE id_producto = ? AND activo = 1")) {
                    ps.setInt(1, idProducto);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("El producto no existe o esta inactivo.");
                        }
                        String nombre = rs.getString(1);
                        BigDecimal min = rs.getBigDecimal(2);
                        BigDecimal max = rs.getBigDecimal(3);
                        int plazoMax = rs.getInt(4);

                        if (monto.compareTo(min) < 0 || monto.compareTo(max) > 0) {
                            throw new IllegalArgumentException("Para " + nombre
                                    + " el monto debe estar entre " + min + " y " + max + ".");
                        }
                        if (plazo < 1 || plazo > plazoMax) {
                            throw new IllegalArgumentException("Para " + nombre
                                    + " el plazo debe estar entre 1 y " + plazoMax + " meses.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitud_credito SET id_producto = ?, monto_solicitado = ?, "
                      + "plazo_meses = ?, estado = 'EN_EVALUACION' WHERE id_solicitud = ?")) {
                    ps.setInt(1, idProducto);
                    ps.setBigDecimal(2, monto);
                    ps.setInt(3, plazo);
                    ps.setInt(4, idSolicitud);
                    ps.executeUpdate();
                }

                ReglasCredito.actualizar(con,"UPDATE evaluacion_credito SET vigente=0 WHERE id_solicitud=?",idSolicitud);
                Seguridad.auditar(con,actor,"ENVIAR_A_EVALUACION","solicitud_credito",idSolicitud,
                        "Producto "+idProducto+"; monto "+monto+"; plazo "+plazo+" meses");
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
