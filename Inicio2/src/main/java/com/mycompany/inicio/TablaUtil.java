package com.mycompany.inicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Ayudante para llenar cualquier JTable con un SELECT.
 * Los titulos de las columnas son los "AS" que pongas en el SELECT.
 * Los "?" del SELECT se llenan con los parametros, en orden (puede no haber ninguno).
 */
public class TablaUtil {

    public static void cargarAutorizada(JTable tabla, String permiso, String sql, Object... parametros) throws SQLException {
        Seguridad.comprobar(permiso);
        cargar(tabla, sql, parametros);
    }

    public static void cargar(JTable tabla, String sql, Object... parametros) throws SQLException {

        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData info = rs.getMetaData();
                int columnas = info.getColumnCount();

                for (int i = 1; i <= columnas; i++) {
                    modelo.addColumn(info.getColumnLabel(i));
                }
                while (rs.next()) {
                    Object[] fila = new Object[columnas];
                    for (int i = 0; i < columnas; i++) {
                        fila[i] = rs.getObject(i + 1);
                    }
                    modelo.addRow(fila);
                }
            }
        }

        tabla.setModel(modelo);
        tabla.setAutoCreateRowSorter(true);   // clic en el titulo = ordenar
    }
}