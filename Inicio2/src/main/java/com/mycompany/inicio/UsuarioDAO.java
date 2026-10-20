package com.mycompany.inicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDAO {

    /** Comprueba en la misma transaccion el trabajador que firma una operacion. */
    public static int exigirRol(Connection con, String login, String rol) throws SQLException {
        Sesion.exigirIdentidad(login);
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT u.id_usuario FROM usuario u JOIN rol r ON r.id_rol=u.id_rol "
              + "WHERE u.login=? AND u.activo=1 AND r.nombre=? LOCK IN SHARE MODE")) {
            ps.setString(1, login);
            ps.setString(2, rol);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Inicia sesion con un usuario activo del rol " + rol + ".");
                }
                return rs.getInt(1);
            }
        }
    }

    /** Lista los usuarios de un rol. Tiene un solo "?" (el nombre del rol) para usar con TablaUtil. */
    public static final String SQL_POR_ROL =
            "SELECT u.login AS Usuario, u.nombre_completo AS Nombre, "
          + "u.limite_aprobacion AS `Limite aprobacion`, "
          + "IF(u.activo = 1, 'ACTIVO', 'INACTIVO') AS Estado "
          + "FROM usuario u "
          + "JOIN rol r ON r.id_rol = u.id_rol "
          + "WHERE r.nombre = ? "
          + "ORDER BY u.nombre_completo";

    /**
     * Comprueba login y clave. Devuelve el nombre del rol (ej: "ADMIN"),
     * o null si no existe, esta desactivado o la clave no coincide.
     */
    public String autenticar(String login, String clave) throws SQLException {
        String sql = "SELECT u.clave_hash, r.nombre AS rol "
                   + "FROM usuario u "
                   + "JOIN rol r ON r.id_rol = u.id_rol "
                   + "WHERE u.login = ? AND u.activo = 1";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                try {
                    if (!BCrypt.checkpw(clave, rs.getString("clave_hash"))) {
                        return null;
                    }
                } catch (IllegalArgumentException e) {
                    return null;   // el hash guardado no es BCrypt valido
                }
                return rs.getString("rol");
            }
        }
    }

    /** Administracion transaccional: permiso real, sesion y auditoria sin guardar claves. */
    public void crearTrabajador(String nombreCompleto, String login, String clave,
                                String rol, BigDecimal limiteAprobacion) throws SQLException {
        if (nombreCompleto == null || nombreCompleto.isBlank() || nombreCompleto.length()>120
                || login==null || !login.matches("[a-z0-9._]{3,40}")
                || !java.util.Set.of("ADMIN","OFICIAL","ANALISTA","JEFE","CAJERO","GESTOR","GERENTE").contains(rol==null?"":rol)
                || limiteAprobacion==null || limiteAprobacion.signum()<0 || limiteAprobacion.stripTrailingZeros().scale()>2)
            throw new IllegalArgumentException("Datos del trabajador no validos.");
        validarClave(clave);
        String hash=BCrypt.hashpw(clave,BCrypt.gensalt());
        administrar("INSERT INTO usuario(id_rol,nombre_completo,login,clave_hash,limite_aprobacion) "
                + "VALUES((SELECT id_rol FROM rol WHERE nombre=?),?,?,?,?)",
                "CREAR_USUARIO",login,new Object[]{rol,nombreCompleto,login,hash,limiteAprobacion});
    }

    public void cambiarEstado(String login,boolean activo) throws SQLException {
        if(!activo && Sesion.login().equals(login))throw new IllegalArgumentException("No puedes desactivar tu propia cuenta.");
        administrar("UPDATE usuario SET activo=? WHERE login=?","CAMBIAR_ESTADO_USUARIO",login,new Object[]{activo,login});
    }

    public void cambiarClave(String login,String claveNueva) throws SQLException {
        validarClave(claveNueva);
        administrar("UPDATE usuario SET clave_hash=? WHERE login=?","RESTABLECER_CLAVE",login,
                new Object[]{BCrypt.hashpw(claveNueva,BCrypt.gensalt()),login});
    }

    private static void validarClave(String clave) {
        if(clave==null || clave.length()<6 || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("La clave debe tener al menos 6 caracteres y no superar 72 bytes UTF-8.");
    }

    private static void administrar(String sql,String operacion,String destino,Object[] parametros) throws SQLException {
        try(Connection con=Conexion.getConexion()){
            con.setAutoCommit(false);
            try{
                int actor=Seguridad.exigir(con,Sesion.login(),"USUARIOS_GESTIONAR");
                try(PreparedStatement ps=con.prepareStatement(sql)){
                    for(int i=0;i<parametros.length;i++)ps.setObject(i+1,parametros[i]);
                    if(ps.executeUpdate()!=1)throw new IllegalArgumentException("El usuario no existe o la operacion no produjo un cambio.");
                }
                Seguridad.auditar(con,actor,operacion,"usuario",0,"Usuario destino: "+destino);
                con.commit();
            }catch(SQLException | RuntimeException e){con.rollback();throw e;}
        }
    }
}
