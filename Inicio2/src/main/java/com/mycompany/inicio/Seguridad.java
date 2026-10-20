package com.mycompany.inicio;

import java.sql.*;

/** Autorizacion contra los permisos vigentes en MariaDB, dentro de la transaccion. */
public final class Seguridad {
    private Seguridad() {}

    public static int exigir(Connection con, String login, String permiso) throws SQLException {
        Sesion.exigirIdentidad(login);
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT u.id_usuario FROM usuario u "
                + "JOIN rol_permiso rp ON rp.id_rol=u.id_rol JOIN permiso p ON p.id_permiso=rp.id_permiso "
                + "WHERE u.login=? AND u.activo=1 AND p.codigo=? LOCK IN SHARE MODE")) {
            ps.setString(1, login); ps.setString(2, permiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Tu cuenta no esta activa o no tiene el permiso " + permiso + ".");
                return rs.getInt(1);
            }
        }
    }

    public static void comprobar(String permiso) throws SQLException {
        try (Connection con = Conexion.getConexion()) { exigir(con, Sesion.login(), permiso); }
    }

    public static void auditar(Connection con, int usuario, String operacion, String entidad,
                               long id, String detalle) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO auditoria(id_usuario,operacion,entidad,id_entidad,datos_nuevos,resultado) VALUES(?,?,?,?,?,'OK')")) {
            ps.setInt(1, usuario); ps.setString(2, operacion); ps.setString(3, entidad);
            ps.setLong(4, id); ps.setString(5, detalle); ps.executeUpdate();
        }
    }
}
