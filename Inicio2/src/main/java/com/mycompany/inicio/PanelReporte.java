package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/** Muestra el resultado de un SELECT en una tabla, con boton Actualizar. */
public class PanelReporte extends JPanel {

    private final String sql;
    private final String permiso;
    private final Object[] parametros;
    private final JTable tabla = new JTable();
    private final JLabel lblTotal = new JLabel(" ");
    private final JLabel lblAviso = new JLabel();
    private boolean pendientesAnalista;
    private boolean pendientesCapacidad;

    public PanelReporte(String titulo, String sql, String permisoRequerido, Object... parametros) {
        this.permiso = permisoRequerido;
        this.sql = sql;
        this.parametros = parametros.clone();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 22f));
        JPanel cabecera = new JPanel(new BorderLayout(0, 8));
        cabecera.add(lblTitulo, BorderLayout.NORTH);
        lblAviso.setVisible(false);
        cabecera.add(lblAviso, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargar());
        abajo.add(btnActualizar);
        abajo.add(lblTotal);
        add(abajo, BorderLayout.SOUTH);

        cargar();
    }

    /** Vuelve a leer los datos desde la base. */
    public final void cargar() {
        try {
            TablaUtil.cargarAutorizada(tabla, permiso, sql, parametros);
            lblTotal.setText("Filas: " + tabla.getRowCount());
            if (pendientesAnalista) actualizarPendientesAnalista();
            if (pendientesCapacidad) actualizarPendientesCapacidad();
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Explica los campos incompletos sin cambiar los importes guardados en el modelo. */
    public void mostrarPendientesAnalista() {
        pendientesAnalista = true;
        lblAviso.setText("<html>El <b>analista</b> debe completar los datos marcados como "
                + "<b>Pendiente de análisis</b>: producto, monto y plazo.<br>"
                + "La columna <b>Pendiente del analista</b> detalla lo que falta en cada solicitud.</html>");
        lblAviso.setOpaque(true);
        lblAviso.setBackground(new java.awt.Color(255, 246, 214));
        lblAviso.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        lblAviso.setVisible(true);
        actualizarPendientesAnalista();
    }

    private void actualizarPendientesAnalista() {
        if (tabla.getColumnCount() == 0) return;
        int pendientes = 0;
        for (String nombre : new String[]{"Producto", "Monto", "Plazo"}) {
            int columna = tabla.getColumnModel().getColumnIndex(nombre);
            tabla.getColumnModel().getColumn(columna).setCellRenderer(
                    new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object valor) {
                    boolean vacio = valor == null || valor.toString().isBlank();
                    super.setValue(vacio ? "Pendiente de análisis" : valor);
                    setToolTipText(vacio ? "Este dato debe completarlo el analista." : null);
                }
            });
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(160);
        }
        int columnaPendientes = tabla.getColumnModel().getColumnIndex("Pendiente del analista");
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (!"Sin campos pendientes".equals(tabla.getValueAt(fila, columnaPendientes))) pendientes++;
        }
        tabla.getColumnModel().getColumn(columnaPendientes).setPreferredWidth(240);
        tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex("Cliente")).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex("Destino")).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex("Estado")).setPreferredWidth(140);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        lblTotal.setText("Solicitudes: " + tabla.getRowCount() + " | Con datos pendientes del analista: " + pendientes);
    }

    public void mostrarPendientesCapacidad() {
        pendientesCapacidad = true;
        lblAviso.setText("<html>El analista debe definir <b>monto y plazo</b> en <b>Solicitudes pendientes</b>. "
                + "Las celdas indican qué falta para calcular la cuota.<br>"
                + "La cuota mostrada incluye interes y corresponde a la evaluacion vigente guardada. Completa el calculo en Evaluacion de riesgo. "
                + "Los ingresos faltantes se completan en el registro del cliente.</html>");
        lblAviso.setOpaque(true);
        lblAviso.setBackground(new java.awt.Color(255, 246, 214));
        lblAviso.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        lblAviso.setVisible(true);
        actualizarPendientesCapacidad();
    }

    private static boolean sinValorPositivo(Object valor) {
        return valor == null || valor.toString().isBlank()
                || (valor instanceof Number && new java.math.BigDecimal(valor.toString()).signum() <= 0);
    }

    private void actualizarPendientesCapacidad() {
        if (tabla.getColumnCount() == 0) return;
        int pendientes = 0;
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (sinValorPositivo(valorCapacidad(fila, "Monto solicitado"))
                    || sinValorPositivo(valorCapacidad(fila, "Plazo"))) pendientes++;
        }
        for (String nombre : new String[]{"Ingresos", "Monto solicitado", "Plazo", "Cuota aprox."}) {
            var columna = tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex(nombre));
            columna.setPreferredWidth(nombre.equals("Cuota aprox.") ? 220 : 180);
            columna.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(JTable t, Object valor,
                        boolean seleccionado, boolean foco, int fila, int col) {
                    super.getTableCellRendererComponent(t, valor, seleccionado, foco, fila, col);
                    String mensaje = null;
                    if (nombre.equals("Cuota aprox.")) {
                        if (valor == null || valor.toString().isBlank()) {
                            boolean faltaMonto = sinValorPositivo(valorCapacidad(fila, "Monto solicitado"));
                            boolean faltaPlazo = sinValorPositivo(valorCapacidad(fila, "Plazo"));
                            mensaje = faltaMonto && faltaPlazo ? "Faltan monto y plazo"
                                    : faltaMonto ? "Falta definir monto"
                                    : faltaPlazo ? "Falta definir plazo" : "Pendiente de evaluacion";
                        }
                    } else if (sinValorPositivo(valor)) {
                        mensaje = nombre.equals("Ingresos") ? "Ingresos por completar"
                                : valor == null || valor.toString().isBlank()
                                ? "Pendiente del analista" : nombre + " por corregir";
                    }
                    if (mensaje != null) setText(mensaje);
                    setToolTipText(mensaje == null ? null : mensaje + (nombre.equals("Ingresos")
                            ? ". Completar en el registro del cliente."
                            : ". Completar condiciones y guardar Evaluacion de riesgo; luego actualizar este reporte."));
                    return this;
                }
            });
        }
        tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex("Cliente")).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex("CI")).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(tabla.getColumnModel().getColumnIndex("Solicitud")).setPreferredWidth(90);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        lblTotal.setText("Solicitudes: " + tabla.getRowCount() + " | Sin cuota calculable por monto/plazo: " + pendientes);
    }

    private Object valorCapacidad(int fila, String columna) {
        return tabla.getValueAt(fila, tabla.getColumnModel().getColumnIndex(columna));
    }
}
