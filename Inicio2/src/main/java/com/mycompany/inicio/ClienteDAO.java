package com.mycompany.inicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.mindrot.jbcrypt.BCrypt;

public class ClienteDAO {

    /** Inserta SOLO el cliente (sin cuenta de acceso). */
    public void registrar(Cliente c) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            Seguridad.exigir(con, Sesion.login(), "CLIENTE_REGISTRAR");
            insertarCliente(con, c);
        }
    }

    /**
     * Crea el cliente Y su cuenta para iniciar sesion, todo o nada.
     * El login del usuario es el CI.
     * Si el CI ya existe, la SQLException trae getErrorCode() == 1062.
     */
    public void registrarConCuenta(Cliente c, String clave) throws SQLException {
        if(clave==null || clave.length()<6 || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("La clave requiere al menos 6 caracteres y hasta 72 bytes UTF-8.");
        String hash = BCrypt.hashpw(clave, BCrypt.gensalt());

        String sqlUsuario = "INSERT INTO usuario (id_rol, id_cliente, nombre_completo, login, clave_hash) "
                + "VALUES ((SELECT id_rol FROM rol WHERE nombre = 'CLIENTE'), "
                + "(SELECT id_cliente FROM cliente WHERE ci = ?), ?, ?, ?)";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                insertarCliente(con, c);

                try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                    ps.setString(1, c.getCi());
                    ps.setString(2, c.getNombreCompleto());
                    ps.setString(3, c.getCi());
                    ps.setString(4, hash);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Busca un cliente por su CI. Devuelve null si no existe. */
    public Cliente buscarPorCi(String ci) throws SQLException {
        String sql = "SELECT ci, nombres, apellidos, telefono, lugar_trabajo, ingresos_mensuales "
                   + "FROM cliente WHERE ci = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if("CLIENTE".equals(Sesion.rol())) {
                Sesion.exigirIdentidad(ci); UsuarioDAO.exigirRol(con,Sesion.login(),"CLIENTE");
            } else Seguridad.exigir(con,Sesion.login(),"CLIENTE_CONSULTAR");
            ps.setString(1, ci);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Cliente c = new Cliente();
                c.setCi(rs.getString("ci"));
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setTelefono(rs.getString("telefono"));
                c.setLugarTrabajo(rs.getString("lugar_trabajo"));
                c.setIngresosMensuales(rs.getBigDecimal("ingresos_mensuales"));
                return c;
            }
        }
    }

    private void insertarCliente(Connection con, Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (ci, nombres, apellidos, telefono, lugar_trabajo, ingresos_mensuales, fecha_registro) "
                   + "VALUES (?, ?, ?, ?, ?, ?, CURDATE())";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCi());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidos());
            if (c.getTelefono() == null || c.getTelefono().isEmpty()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, c.getTelefono());
            }
            ps.setString(5, c.getLugarTrabajo());
            ps.setBigDecimal(6, c.getIngresosMensuales() == null
                    ? java.math.BigDecimal.ZERO : c.getIngresosMensuales());
            ps.executeUpdate();
        }
    }
        /** Actualiza los datos de un cliente existente (se identifica por su CI). */
    public int actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nombres = ?, apellidos = ?, telefono = ?, "
                   + "lugar_trabajo = ?, ingresos_mensuales = ? WHERE ci = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            Seguridad.exigir(con, Sesion.login(), "CLIENTE_REGISTRAR");
            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            if (c.getTelefono() == null || c.getTelefono().isEmpty()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, c.getTelefono());
            }
            ps.setString(4, c.getLugarTrabajo());
            ps.setBigDecimal(5, c.getIngresosMensuales() == null
                    ? java.math.BigDecimal.ZERO : c.getIngresosMensuales());
            ps.setString(6, c.getCi());
            return ps.executeUpdate();
        }
    }
}
