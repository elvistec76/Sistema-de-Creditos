package com.mycompany.inicio;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/** El oficial registra clientes nuevos y actualiza los existentes. */
public class PanelClientesOficial extends JPanel {

    private static final String SQL_CLIENTES =
            "SELECT ci AS CI, nombres AS Nombres, apellidos AS Apellidos, "
          + "telefono AS Telefono, lugar_trabajo AS Trabajo, "
          + "ingresos_mensuales AS Ingresos, fecha_registro AS Registro "
          + "FROM cliente ORDER BY fecha_registro DESC, apellidos";

    private final ClienteDAO dao = new ClienteDAO();
    private final JTable tabla = new JTable();
    private final JTextField txtCi = new JTextField(9);
    private final JTextField txtNombres = new JTextField(12);
    private final JTextField txtApellidos = new JTextField(12);
    private final JTextField txtTelefono = new JTextField(8);
    private final JTextField txtTrabajo = new JTextField(14);
    private final JTextField txtIngresos = new JTextField(8);

    public PanelClientesOficial() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Clientes");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                llenarFormulario();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Campos en una cuadrilla de 2 filas x 6 columnas (etiqueta + campo)
        JPanel campos = new JPanel(new GridLayout(2, 6, 8, 8));
        campos.add(new JLabel("CI:"));        campos.add(txtCi);
        campos.add(new JLabel("Nombres:"));   campos.add(txtNombres);
        campos.add(new JLabel("Apellidos:")); campos.add(txtApellidos);
        campos.add(new JLabel("Telefono:"));  campos.add(txtTelefono);
        campos.add(new JLabel("Trabajo (obligatorio):")); campos.add(txtTrabajo);
        txtTrabajo.setToolTipText("Indica la empresa, institucion o actividad en la que trabaja.");
        campos.add(new JLabel("Ingresos (obligatorio):")); campos.add(txtIngresos);
        txtIngresos.setToolTipText("Ingreso mensual exacto o aproximado, mayor a cero. Ejemplo: 2500 o 2500,50.");

        JButton btnRegistrar = new JButton("Registrar nuevo");
        JButton btnActualizar = new JButton("Actualizar seleccionado");
        JButton btnLimpiar = new JButton("Limpiar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnLimpiar);

        JPanel form = new JPanel(new BorderLayout(0, 6));
        form.add(campos, BorderLayout.CENTER);
        form.add(botones, BorderLayout.SOUTH);
        form.setBorder(BorderFactory.createTitledBorder(
                "Datos del cliente (selecciona una fila para editarla)"));
        add(form, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnLimpiar.addActionListener(e -> limpiar());

        cargar();
    }

    public final void cargar() {
        try {
            TablaUtil.cargarAutorizada(tabla, "CLIENTE_CONSULTAR", SQL_CLIENTES);
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudieron cargar los clientes:\n" + ex.getMessage());
        }
    }

    private void llenarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        javax.swing.table.TableModel t = tabla.getModel();
        txtCi.setText(texto(t.getValueAt(m, 0)));
        txtCi.setEditable(false);
        txtNombres.setText(texto(t.getValueAt(m, 1)));
        txtApellidos.setText(texto(t.getValueAt(m, 2)));
        txtTelefono.setText(texto(t.getValueAt(m, 3)));
        txtTrabajo.setText(texto(t.getValueAt(m, 4)));
        txtIngresos.setText(texto(t.getValueAt(m, 5)));
    }

    private static String texto(Object o) {
        return o == null ? "" : o.toString();
    }

    private void limpiar() {
        tabla.clearSelection();
        txtCi.setEditable(true);
        for (JTextField f : new JTextField[]{txtCi, txtNombres, txtApellidos,
                txtTelefono, txtTrabajo, txtIngresos}) {
            f.setText("");
        }
    }

    /** Lee y valida el formulario. Devuelve null si algo esta mal (ya avisó). */
    private Cliente leer() {
        String ci = txtCi.getText().trim().toUpperCase();
        String nombres = txtNombres.getText().trim().replaceAll("\\s+", " ");
        String apellidos = txtApellidos.getText().trim().replaceAll("\\s+", " ");
        String tel = txtTelefono.getText().trim();
        String trabajo = txtTrabajo.getText().trim();

        if (!ci.matches("[0-9]{5,10}(-[0-9A-Z]{1,2})?")) {
            aviso("CI no valido. Ejemplos: 1234567 o 1234567-1A", txtCi);
            return null;
        }
        if (nombres.isEmpty()) {
            aviso("Escribe los nombres.", txtNombres);
            return null;
        }
        if (apellidos.isEmpty()) {
            aviso("Escribe los apellidos.", txtApellidos);
            return null;
        }
        if (!tel.isEmpty() && !tel.matches("[0-9]{7,8}")) {
            aviso("El telefono debe tener 7 u 8 digitos.", txtTelefono);
            return null;
        }
        if (trabajo.isBlank()) {
            aviso("El trabajo es obligatorio. Indica la empresa, institucion o actividad en la que trabaja.", txtTrabajo);
            return null;
        }
        if (trabajo.length() > 100) {
            aviso("El trabajo admite hasta 100 caracteres.", txtTrabajo);
            return null;
        }
        String ingresosTexto = txtIngresos.getText().trim();
        if (ingresosTexto.isBlank()) {
            aviso("Los ingresos son obligatorios. Escribe el monto mensual exacto o aproximado.", txtIngresos);
            return null;
        }
        if (!ingresosTexto.matches("[0-9]+(?:[.,][0-9]{1,2})?")) {
            aviso("Escribe un ingreso valido con hasta dos decimales, sin separador de miles. Ejemplo: 2500,50.", txtIngresos);
            return null;
        }
        BigDecimal ingresos = new BigDecimal(ingresosTexto.replace(",", ".")).setScale(2);
        if (ingresos.signum() <= 0 || ingresos.precision() > 14) {
            aviso("Los ingresos deben ser mayores a 0 y no superar 999999999999,99.", txtIngresos);
            return null;
        }

        Cliente c = new Cliente();
        c.setCi(ci);
        c.setNombres(nombres);
        c.setApellidos(apellidos);
        c.setTelefono(tel);
        c.setLugarTrabajo(trabajo);
        c.setIngresosMensuales(ingresos);
        return c;
    }

    private void registrar() {
        Cliente c = leer();
        if (c == null) {
            return;
        }
        try {
            dao.registrar(c);       // inserta todos los datos en una sola operacion
            JOptionPane.showMessageDialog(this, "Cliente registrado.");
            limpiar();
            cargar();
        } catch (SQLException | IllegalArgumentException ex) {
            if (ex instanceof SQLException sqlEx && sqlEx.getErrorCode() == 1062) {
                aviso("Ya existe un cliente con ese CI.", txtCi);
            } else {
                error("No se pudo registrar:\n" + ex.getMessage());
            }
        }
    }

    private void actualizar() {
        Cliente c = leer();
        if (c == null) {
            return;
        }
        try {
            if (dao.actualizar(c) == 0) {
                JOptionPane.showMessageDialog(this,
                        "No existe un cliente con ese CI. Usa 'Registrar nuevo'.");
                return;
            }
            JOptionPane.showMessageDialog(this, "Cliente actualizado.");
            cargar();
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudo actualizar:\n" + ex.getMessage());
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
