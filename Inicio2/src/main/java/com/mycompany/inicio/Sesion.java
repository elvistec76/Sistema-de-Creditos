package com.mycompany.inicio;

import java.sql.SQLException;

/** Una sesion autenticada para esta ejecucion de escritorio. */
public final class Sesion {
    private record Identidad(String login, String rol) {}
    private static volatile Identidad actual;
    private Sesion() {}

    public static String abrir(String login, String clave) throws SQLException {
        cerrar();
        String rol = new UsuarioDAO().autenticar(login, clave);
        if (rol != null) actual = new Identidad(login, rol);
        return rol;
    }

    public static void cerrar() { actual = null; }

    public static String login() {
        Identidad identidad = actual;
        if (identidad == null) throw new IllegalArgumentException("Inicia sesion desde Inicio antes de realizar esta operacion.");
        return identidad.login();
    }

    public static String rol() {
        login(); return actual.rol();
    }

    public static void exigirIdentidad(String login) {
        if (login == null || !login().equals(login))
            throw new IllegalArgumentException("La operacion debe pertenecer al usuario de la sesion actual.");
    }

    public static void exigirRol(String rol) {
        Identidad identidad = actual;
        if (identidad == null || !identidad.rol().equals(rol))
            throw new IllegalArgumentException("Inicia sesion con el rol " + rol + " desde Inicio.");
    }
}
