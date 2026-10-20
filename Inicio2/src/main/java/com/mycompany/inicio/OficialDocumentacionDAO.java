package com.mycompany.inicio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Operaciones de CU30, CU31 y CU6 sobre el esquema existente. */
public class OficialDocumentacionDAO {
    public record Solicitud(int id, String ci, String cliente, String trabajo, BigDecimal ingresos, String estado) {
        @Override public String toString() { return id + " - " + cliente + " (CI " + ci + ") - " + estado; }
    }
    public record TipoGarantia(int id, String nombre, String requisitos) {
        public boolean personal() { return nombre.equalsIgnoreCase("PERSONAL"); }
        @Override public String toString() { return nombre; }
    }
    public record Garantia(int id, int solicitud, int tipo, String descripcion, String titular,
                           BigDecimal valor, String verificacion, String evidencia, String estado) {}

    public static final String SQL_DOCUMENTOS =
            "SELECT d.id_documento AS Documento,d.tipo_documento AS Tipo,d.nombre_archivo AS Archivo,"
          + "d.estado_verificacion AS Verificacion,d.fecha_carga AS Fecha,u.login AS Registro "
          + "FROM documento_solicitud d JOIN usuario u ON u.id_usuario=d.id_usuario "
          + "WHERE d.id_solicitud=? ORDER BY d.id_documento DESC";
    public static final String SQL_OBSERVACIONES =
            "SELECT a.id_auditoria AS Registro,a.fecha_hora AS Fecha,u.login AS Oficial,"
          + "a.datos_nuevos AS Observacion FROM auditoria a JOIN usuario u ON u.id_usuario=a.id_usuario "
          + "WHERE a.entidad='documento_solicitud' AND a.id_entidad=? "
          + "AND a.operacion='OBSERVACION_DOCUMENTO' ORDER BY a.id_auditoria DESC";
    public static final String SQL_GARANTIAS =
            "SELECT g.id_garantia AS Garantia,g.id_solicitud AS Solicitud,CONCAT(c.nombres,' ',c.apellidos) AS Cliente,"
          + "t.nombre AS Tipo,g.descripcion AS Descripcion,g.valor AS Valor,g.estado_verificacion AS Verificacion,"
          + "g.estado AS Estado,g.titular AS Titular,g.evidencia_verificacion AS Evidencia "
          + "FROM garantia g JOIN tipo_garantia t ON t.id_tipo_garantia=g.id_tipo_garantia "
          + "JOIN solicitud_credito s ON s.id_solicitud=g.id_solicitud JOIN cliente c ON c.id_cliente=s.id_cliente "
          + "ORDER BY g.id_garantia DESC";
    public static final String SQL_RESPALDOS =
            "SELECT id_documento AS Documento,tipo_documento AS Tipo,nombre_archivo AS Archivo,fecha_carga AS Fecha "
          + "FROM documento_garantia WHERE id_garantia=? ORDER BY id_documento DESC";

    public List<Solicitud> solicitudes() throws SQLException {
        Seguridad.comprobar("SOLICITUD_REGISTRAR");
        List<Solicitud> salida = new ArrayList<>();
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT s.id_solicitud,c.ci,CONCAT(c.nombres,' ',c.apellidos),"
                     + "c.lugar_trabajo,c.ingresos_mensuales,s.estado FROM solicitud_credito s "
                     + "JOIN cliente c ON c.id_cliente=s.id_cliente ORDER BY s.id_solicitud DESC")) {
            while (rs.next()) salida.add(new Solicitud(rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getBigDecimal(5), rs.getString(6)));
        }
        return salida;
    }

    public List<TipoGarantia> tipos() throws SQLException {
        Seguridad.comprobar("GARANTIA_REGISTRAR");
        List<TipoGarantia> salida = new ArrayList<>();
        try (Connection con = Conexion.getConexion(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_tipo_garantia,nombre,datos_requeridos FROM tipo_garantia WHERE activo=1 ORDER BY nombre")) {
            while (rs.next()) salida.add(new TipoGarantia(rs.getInt(1),rs.getString(2),rs.getString(3)));
        }
        return salida;
    }

    public int registrarDocumento(int solicitud, String login, String tipo, String archivo) throws SQLException {
        texto(tipo, 60, "Tipo de documento"); archivoDisponible(archivo);
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int usuario = UsuarioDAO.exigirRol(con, login, "OFICIAL");
                Seguridad.exigir(con,login,"SOLICITUD_REGISTRAR");
                bloquearSolicitud(con, solicitud);
                int id;
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO documento_solicitud(id_solicitud,id_usuario,tipo_documento,nombre_archivo) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1,solicitud); ps.setInt(2,usuario); ps.setString(3,tipo.trim()); ps.setString(4,archivo);
                    ps.executeUpdate(); id = clave(ps);
                }
                auditar(con,usuario,"REGISTRAR_DOCUMENTO","documento_solicitud",id,null,"Tipo: " + tipo + "; archivo: " + archivo);
                con.commit(); return id;
            } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        }
    }

    public void validarDatos(int solicitud, String login) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int usuario = UsuarioDAO.exigirRol(con,login,"OFICIAL");
                Seguridad.exigir(con,login,"SOLICITUD_REGISTRAR");
                bloquearSolicitud(con,solicitud);
                String datos;
                try (PreparedStatement ps = con.prepareStatement("SELECT c.ci,c.nombres,c.apellidos,c.lugar_trabajo,c.ingresos_mensuales "
                        + "FROM cliente c JOIN solicitud_credito s ON s.id_cliente=c.id_cliente WHERE s.id_solicitud=? LOCK IN SHARE MODE")) {
                    ps.setInt(1,solicitud);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        for (int i=1;i<=4;i++) if (rs.getString(i)==null || rs.getString(i).isBlank()) {
                            throw new IllegalArgumentException("Completa CI, nombres, apellidos, trabajo e ingresos en Registrar/Actualizar cliente antes de validar.");
                        }
                        if (rs.getBigDecimal(5)==null || rs.getBigDecimal(5).signum()<=0) throw new IllegalArgumentException("Completa los ingresos mensuales del cliente antes de validar.");
                        datos="CI: " + rs.getString(1) + "; nombre: " + rs.getString(2) + " " + rs.getString(3)
                                + "; trabajo: " + rs.getString(4) + "; ingresos: " + rs.getBigDecimal(5);
                    }
                }
                auditar(con,usuario,"VALIDAR_DATOS_CLIENTE","solicitud_credito",solicitud,null,datos);
                con.commit();
            } catch (SQLException | RuntimeException e) { con.rollback(); throw e; }
        }
    }

    public String ultimaValidacionDatos(int solicitud) throws SQLException {
        Seguridad.comprobar("SOLICITUD_REGISTRAR");
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(
                "SELECT a.fecha_hora,u.login FROM auditoria a JOIN usuario u ON u.id_usuario=a.id_usuario "
              + "WHERE a.entidad='solicitud_credito' AND a.id_entidad=? AND a.operacion='VALIDAR_DATOS_CLIENTE' ORDER BY a.id_auditoria DESC LIMIT 1")) {
            ps.setInt(1,solicitud);
            try (ResultSet rs=ps.executeQuery()) { return rs.next() ? "Ultima revision de datos: " + rs.getString(1) + " por " + rs.getString(2) : "Datos del cliente: revision aun no registrada."; }
        }
    }

    public void verificarDocumento(int documento, String login) throws SQLException {
        cambiarDocumento(documento,login,null);
    }
    public void observarDocumento(int documento, String login, String observacion) throws SQLException {
        texto(observacion,2000,"Observacion"); cambiarDocumento(documento,login,observacion.trim());
    }
    private void cambiarDocumento(int documento, String login, String observacion) throws SQLException {
        try (Connection con=Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int usuario=UsuarioDAO.exigirRol(con,login,"OFICIAL");
                Seguridad.exigir(con,login,"SOLICITUD_REGISTRAR");
                int solicitud;
                try (PreparedStatement ps=con.prepareStatement("SELECT id_solicitud FROM documento_solicitud WHERE id_documento=?")) {
                    ps.setInt(1,documento);
                    try (ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("El documento no existe."); solicitud=rs.getInt(1); }
                }
                bloquearSolicitud(con,solicitud);
                String anterior;
                try (PreparedStatement ps=con.prepareStatement("SELECT estado_verificacion,nombre_archivo FROM documento_solicitud WHERE id_documento=? FOR UPDATE")) {
                    ps.setInt(1,documento);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new IllegalArgumentException("El documento ya no existe.");
                        anterior=rs.getString(1);
                        if(observacion==null) archivoDisponible(rs.getString(2));
                    }
                }
                try(PreparedStatement ps=con.prepareStatement("UPDATE documento_solicitud SET estado_verificacion=? WHERE id_documento=?")) {
                    ps.setString(1,observacion==null?"VERIFICADO":"OBSERVADO");ps.setInt(2,documento);ps.executeUpdate();
                }
                auditar(con,usuario,observacion==null?"VERIFICAR_DOCUMENTO":"OBSERVACION_DOCUMENTO","documento_solicitud",documento,anterior,observacion==null?"VERIFICADO":observacion);
                con.commit();
            } catch(SQLException | RuntimeException e) {con.rollback();throw e;}
        }
    }

    public Garantia garantia(int id) throws SQLException {
        Seguridad.comprobar("GARANTIA_REGISTRAR");
        try(Connection con=Conexion.getConexion();PreparedStatement ps=con.prepareStatement("SELECT * FROM garantia WHERE id_garantia=?")) {
            ps.setInt(1,id);
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) throw new IllegalArgumentException("La garantia no existe.");
                return new Garantia(id,rs.getInt("id_solicitud"),rs.getInt("id_tipo_garantia"),rs.getString("descripcion"),rs.getString("titular"),rs.getBigDecimal("valor"),rs.getString("estado_verificacion"),rs.getString("evidencia_verificacion"),rs.getString("estado"));
            }
        }
    }

    public int guardarGarantia(Integer id, int solicitud, int tipo, String login, String descripcion,
            String titular, BigDecimal valor, String verificacion, String evidencia, String tipoDocumento, String archivo) throws SQLException {
        texto(descripcion,300,"Descripcion"); texto(titular,120,"Titular o garante");
        if(evidencia!=null && evidencia.length()>300) throw new IllegalArgumentException("La evidencia admite hasta 300 caracteres.");
        if(!Set.of("PENDIENTE","VALIDADA").contains(verificacion==null?"":verificacion)) throw new IllegalArgumentException("Verificacion no valida.");
        if(valor==null || valor.signum()<0 || valor.stripTrailingZeros().scale()>2 || valor.compareTo(new BigDecimal("999999999999.99"))>0) {
            throw new IllegalArgumentException("Indica un valor valido con hasta dos decimales.");
        }
        if(archivo!=null) { texto(tipoDocumento,60,"Tipo del respaldo"); archivoDisponible(archivo); }
        try(Connection con=Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int usuario=UsuarioDAO.exigirRol(con,login,"OFICIAL");
                Seguridad.exigir(con,login,"GARANTIA_REGISTRAR"); bloquearSolicitud(con,solicitud);
                boolean personal;
                try(PreparedStatement ps=con.prepareStatement("SELECT nombre FROM tipo_garantia WHERE id_tipo_garantia=? AND activo=1 LOCK IN SHARE MODE")) {
                    ps.setInt(1,tipo);try(ResultSet rs=ps.executeQuery()) {if(!rs.next()) throw new IllegalArgumentException("Selecciona un tipo de garantia activo."); personal="PERSONAL".equalsIgnoreCase(rs.getString(1));}
                }
                if(personal && valor.signum()!=0) throw new IllegalArgumentException("La garantia personal no lleva tasacion de un bien; su valor debe ser cero (No aplica).");
                if(!personal && valor.signum()<=0) throw new IllegalArgumentException("Registra el valor de la garantia, mayor que cero.");
                String anterior=null;
                if(id!=null) {
                    try(PreparedStatement ps=con.prepareStatement("SELECT id_solicitud,id_credito,estado,valor,estado_verificacion FROM garantia WHERE id_garantia=? FOR UPDATE")) {
                        ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()) {
                            if(!rs.next() || rs.getInt(1)!=solicitud) throw new IllegalArgumentException("La garantia no corresponde a esa solicitud.");
                            if(rs.getObject(2)!=null || !"REGISTRADA".equals(rs.getString(3))) throw new IllegalArgumentException("Solo se editan garantias registradas que aun no esten vinculadas a un credito.");
                            anterior="Valor: "+rs.getBigDecimal(4)+"; verificacion: "+rs.getString(5);
                        }
                    }
                    try(PreparedStatement ps=con.prepareStatement("UPDATE garantia SET id_tipo_garantia=?,descripcion=?,titular=?,valor=?,estado_verificacion=?,evidencia_verificacion=? WHERE id_garantia=?")) {
                        ps.setInt(1,tipo);ps.setString(2,descripcion.trim());ps.setString(3,titular.trim());ps.setBigDecimal(4,valor);ps.setString(5,verificacion);ps.setString(6,evidencia);ps.setInt(7,id);ps.executeUpdate();
                    }
                } else {
                    try(PreparedStatement ps=con.prepareStatement("INSERT INTO garantia(id_solicitud,id_tipo_garantia,id_usuario,descripcion,titular,valor,estado_verificacion,evidencia_verificacion) VALUES(?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1,solicitud);ps.setInt(2,tipo);ps.setInt(3,usuario);ps.setString(4,descripcion.trim());ps.setString(5,titular.trim());ps.setBigDecimal(6,valor);ps.setString(7,verificacion);ps.setString(8,evidencia);ps.executeUpdate();id=clave(ps);
                    }
                }
                if(archivo!=null) {
                    try(PreparedStatement ps=con.prepareStatement("INSERT INTO documento_garantia(id_garantia,tipo_documento,nombre_archivo) VALUES(?,?,?)")) {
                        ps.setInt(1,id);ps.setString(2,tipoDocumento.trim());ps.setString(3,archivo);ps.executeUpdate();
                    }
                }
                if("VALIDADA".equals(verificacion)) {
                    texto(evidencia,300,"Evidencia de verificacion");
                    boolean respaldo=false;
                    try(PreparedStatement ps=con.prepareStatement("SELECT nombre_archivo FROM documento_garantia WHERE id_garantia=?")) {
                        ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()) {
                            while(rs.next()) {
                                try { ArchivoDocumental.resolver(rs.getString(1)); respaldo=true; }
                                catch(IOException ignorada) { /* Puede adjuntarse un respaldo local nuevo. */ }
                            }
                        }
                    }
                    if(!respaldo) throw new IllegalArgumentException("Adjunta al menos un respaldo local antes de validar la garantia.");
                }
                auditar(con,usuario,anterior==null?"REGISTRAR_GARANTIA":"ACTUALIZAR_GARANTIA","garantia",id,anterior,
                        "Solicitud: "+solicitud+"; tipo: "+tipo+"; titular: "+titular+"; valor: "+valor+"; verificacion: "+verificacion+"; evidencia: "+evidencia+"; archivo: "+archivo);
                con.commit();return id;
            } catch(SQLException | RuntimeException e) {con.rollback();throw e;}
        }
    }

    private static void bloquearSolicitud(Connection con,int id) throws SQLException {
        BloqueoCliente.solicitud(con,id);
        try(PreparedStatement ps=con.prepareStatement("SELECT id_solicitud FROM solicitud_credito WHERE id_solicitud=? FOR UPDATE")) {
            ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()) {if(!rs.next()) throw new IllegalArgumentException("Selecciona una solicitud existente.");}
        }
    }
    private static int clave(PreparedStatement ps) throws SQLException {
        try(ResultSet rs=ps.getGeneratedKeys()) {if(!rs.next()) throw new SQLException("No se obtuvo el identificador.");return rs.getInt(1);}
    }
    private static void texto(String s,int max,String campo) {
        if(s==null || s.isBlank() || s.length()>max) throw new IllegalArgumentException(campo+" es obligatorio y admite hasta "+max+" caracteres.");
    }
    private static void archivoDisponible(String archivo) {
        texto(archivo,200,"Archivo");
        try {ArchivoDocumental.resolver(archivo);} catch(IOException e) {throw new IllegalArgumentException(e.getMessage());}
    }
    private static void auditar(Connection con,int usuario,String operacion,String entidad,int id,String antes,String despues) throws SQLException {
        try(PreparedStatement ps=con.prepareStatement("INSERT INTO auditoria(id_usuario,operacion,entidad,id_entidad,datos_anteriores,datos_nuevos,resultado) VALUES(?,?,?,?,?,?,'CORRECTO')")) {
            ps.setInt(1,usuario);ps.setString(2,operacion);ps.setString(3,entidad);ps.setInt(4,id);ps.setString(5,antes);ps.setString(6,despues);ps.executeUpdate();
        }
    }
}
