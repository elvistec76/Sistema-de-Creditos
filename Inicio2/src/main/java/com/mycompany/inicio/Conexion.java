package com.mycompany.inicio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {

    private static final String URL  = "jdbc:mariadb://localhost:3306/creditos_db";
    private static final String USER = "creditos";
    private static final String PASS = "1234"; // la clave que pusiste al crear el usuario

    /** Abre una conexion nueva. Usala con try-with-resources para que se cierre sola. */
    public static Connection getConexion() throws SQLException {
        // Las propiedades permiten pruebas aisladas sin editar la conexion habitual.
        return DriverManager.getConnection(
                System.getProperty("creditos.db.url", URL),
                System.getProperty("creditos.db.user", USER),
                System.getProperty("creditos.db.password", PASS));
    }

    /** Prueba rapida: clic derecho en este archivo > Run File (Shift + F6). */
    public static void main(String[] args) {
        try (Connection con = getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuario")) {

            System.out.println("Conexion exitosa a la base: " + con.getCatalog());
            if (rs.next()) {
                System.out.println("Usuarios encontrados: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }
}
