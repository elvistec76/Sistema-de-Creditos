package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/** El analista elige una solicitud, define producto, monto y plazo, y la envia al jefe. */
public class PanelEvaluacion extends JPanel {

    private static final String SQL_REGISTRADAS =
            "SELECT s.id_solicitud AS Solicitud, c.ci AS CI, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "c.ingresos_mensuales AS Ingresos, c.lugar_trabajo AS Trabajo, "
          + "s.destino AS Destino, s.fecha_solicitud AS Fecha, s.estado AS Estado "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "WHERE s.estado IN ('REGISTRADA','OBSERVADA') "
          + "ORDER BY s.fecha_solicitud, s.id_solicitud";

    private final SolicitudDAO dao = new SolicitudDAO();
    private final JTable tabla = new JTable();
    private final JComboBox<String> comboProducto = new JComboBox<>();
    private final List<Integer> idsProducto = new ArrayList<>();
    private final JLabel lblLimites = new JLabel(" ");
    private final List<String> limites = new ArrayList<>();
    private final JTextField txtMonto = new JTextField(9);
    private final JTextField txtPlazo = new JTextField(4);

    public PanelEvaluacion() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Solicitudes por evaluar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        fila.add(new JLabel("Producto:"));
        fila.add(comboProducto);
        fila.add(new JLabel("Monto:"));
        fila.add(txtMonto);
        fila.add(new JLabel("Plazo (meses):"));
        fila.add(txtPlazo);
        JButton btnEnviar = new JButton("Definir e iniciar evaluacion");
        JButton btnActualizar = new JButton("Actualizar");
        fila.add(btnEnviar);
        fila.add(btnActualizar);
        abajo.add(fila, BorderLayout.CENTER);
        abajo.add(lblLimites, BorderLayout.SOUTH);
        abajo.setBorder(BorderFactory.createTitledBorder("Definir condiciones del credito"));
        add(abajo, BorderLayout.SOUTH);

        comboProducto.addActionListener(e -> mostrarLimites());
        btnEnviar.addActionListener(e -> enviar());
        btnActualizar.addActionListener(e -> cargar());

        cargarProductos();
        cargar();
    }

    public final void cargar() {
        try {
            TablaUtil.cargarAutorizada(tabla, "EVALUACION_REALIZAR", SQL_REGISTRADAS);
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudo cargar:\n" + ex.getMessage());
        }
    }

    private void cargarProductos() {
        String sql = "SELECT id_producto, nombre, monto_minimo, monto_maximo, plazo_maximo_meses "
                   + "FROM producto_credito WHERE activo = 1 ORDER BY nombre";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Seguridad.exigir(con, Sesion.login(), "EVALUACION_REALIZAR");
            while (rs.next()) {
                idsProducto.add(rs.getInt(1));
                limites.add("Monto de " + rs.getBigDecimal(3) + " a " + rs.getBigDecimal(4)
                        + "  |  Plazo maximo: " + rs.getInt(5) + " meses");
                comboProducto.addItem(rs.getString(2));
            }
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudieron cargar los productos:\n" + ex.getMessage());
        }
        mostrarLimites();
    }

    private void mostrarLimites() {
        int i = comboProducto.getSelectedIndex();
        lblLimites.setText(i >= 0 && i < limites.size() ? "  " + limites.get(i) : " ");
    }

    private void enviar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una solicitud de la tabla.");
            return;
        }
        if (comboProducto.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "No hay productos disponibles.");
            return;
        }

        BigDecimal monto;
        int plazo;
        try {
            monto = new BigDecimal(txtMonto.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            aviso("El monto debe ser un numero.", txtMonto);
            return;
        }
        try {
            plazo = Integer.parseInt(txtPlazo.getText().trim());
        } catch (NumberFormatException e) {
            aviso("El plazo debe ser un numero entero de meses.", txtPlazo);
            return;
        }
        if (monto.signum() <= 0) {
            aviso("El monto debe ser mayor a 0.", txtMonto);
            return;
        }

        int m = tabla.convertRowIndexToModel(fila);
        int idSolicitud = Integer.parseInt(tabla.getModel().getValueAt(m, 0).toString());
        int idProducto = idsProducto.get(comboProducto.getSelectedIndex());

        try {
            dao.enviarAEvaluacion(idSolicitud, idProducto, monto, plazo);
            JOptionPane.showMessageDialog(this,
                    "Solicitud " + idSolicitud + " lista para Evaluacion de riesgo. Completa alli la capacidad, riesgo y recomendacion antes de aprobar.");
            txtMonto.setText("");
            txtPlazo.setText("");
            cargar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revisa los datos",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            error("No se pudo guardar:\n" + ex.getMessage());
        }
    }

    private void aviso(String mensaje, java.awt.Component campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        campo.requestFocus();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}