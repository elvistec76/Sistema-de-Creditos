package com.mycompany.inicio;

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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/** Panel para administrar a los usuarios de UN rol (crear, activar/desactivar, cambiar clave). */
public class PanelUsuariosRol extends JPanel {

    private final String rol;          // nombre del rol en la BD (ej: "CAJERO")
    private final String adminLogin;   // quien esta usando el panel
    private final UsuarioDAO dao = new UsuarioDAO();

    private final JTable tabla = new JTable();
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtLogin  = new JTextField(10);
    private final JPasswordField txtClave = new JPasswordField(10);
    private final JTextField txtLimite = new JTextField("0", 8);

    public PanelUsuariosRol(String rol, String adminLogin) {
        this.rol = rol;
        this.adminLogin = adminLogin;
        construir();
        cargar();
    }

    private void construir() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Usuarios con rol " + rol);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);

        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ----- Formulario de creacion -----
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.add(new JLabel("Nombre completo:"));
        form.add(txtNombre);
        form.add(new JLabel("Usuario:"));
        form.add(txtLogin);
        form.add(new JLabel("Clave:"));
        form.add(txtClave);
        form.add(new JLabel("Limite aprobacion:"));
        form.add(txtLimite);
        txtLimite.setEnabled(rol.equals("JEFE"));   // solo el jefe aprueba creditos

        JButton btnCrear = new JButton("Crear " + rol);
        JButton btnEstado = new JButton("Activar / Desactivar");
        JButton btnClave = new JButton("Restablecer clave");
        JButton btnActualizar = new JButton("Actualizar");
        form.add(btnCrear);
        form.add(btnEstado);
        form.add(btnClave);
        form.add(btnActualizar);

        form.setBorder(BorderFactory.createTitledBorder("Nuevo usuario / acciones"));
        add(form, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crear());
        btnEstado.addActionListener(e -> alternarEstado());
        btnClave.addActionListener(e -> restablecerClave());
        btnActualizar.addActionListener(e -> cargar());
    }

    /** Vuelve a leer la tabla desde la base de datos. */
    public final void cargar() {
        try {
            TablaUtil.cargarAutorizada(tabla, "USUARIOS_GESTIONAR", UsuarioDAO.SQL_POR_ROL, rol);
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudieron cargar los usuarios:\n" + ex.getMessage());
        }
    }

    // =================== ACCIONES ===================

    private void crear() {
        String nombre = txtNombre.getText().trim().replaceAll("\\s+", " ");
        String login  = txtLogin.getText().trim().toLowerCase();
        String clave  = new String(txtClave.getPassword());

        if (nombre.split(" ").length < 2) {
            aviso("Escribe nombre y apellido.", txtNombre);
            return;
        }
        if (!login.matches("[a-z0-9._]{3,40}")) {
            aviso("Usuario no valido: de 3 a 40 caracteres, solo letras, numeros, punto o guion bajo.", txtLogin);
            return;
        }
        if (clave.length() < 6) {
            aviso("La clave debe tener al menos 6 caracteres.", txtClave);
            return;
        }

        BigDecimal limite = BigDecimal.ZERO;
        if (rol.equals("JEFE")) {
            try {
                limite = new BigDecimal(txtLimite.getText().trim().replace(",", "."));
                if (limite.signum() < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                aviso("El limite de aprobacion debe ser un numero mayor o igual a 0.", txtLimite);
                return;
            }
        }

        try {
            dao.crearTrabajador(nombre, login, clave, rol, limite);
            JOptionPane.showMessageDialog(this,
                    "Usuario creado.\n\nUsuario: " + login + "\nRol: " + rol,
                    "Listo", JOptionPane.INFORMATION_MESSAGE);
            txtNombre.setText("");
            txtLogin.setText("");
            txtClave.setText("");
            if (rol.equals("JEFE")) {
                txtLimite.setText("0");
            }
            cargar();
        } catch (SQLException | IllegalArgumentException ex) {
            if (ex instanceof SQLException sqlEx && sqlEx.getErrorCode() == 1062) {
                aviso("Ya existe un usuario con ese nombre de usuario.", txtLogin);
            } else {
                error("No se pudo crear el usuario:\n" + ex.getMessage());
            }
        }
    }

    private void alternarEstado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.");
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        String login = tabla.getModel().getValueAt(m, 0).toString();
        boolean estaInactivo = "INACTIVO".equals(tabla.getModel().getValueAt(m, 3));

        if (login.equals(adminLogin)) {
            JOptionPane.showMessageDialog(this, "No puedes desactivar tu propia cuenta.");
            return;
        }

        int op = JOptionPane.showConfirmDialog(this,
                (estaInactivo ? "¿Activar a " : "¿Desactivar a ") + login + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.cambiarEstado(login, estaInactivo);
            cargar();
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudo cambiar el estado:\n" + ex.getMessage());
        }
    }

    private void restablecerClave() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.");
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        String login = tabla.getModel().getValueAt(m, 0).toString();

        JPasswordField pf = new JPasswordField(15);
        int op = JOptionPane.showConfirmDialog(this,
                new Object[]{"Nueva clave para " + login + ":", pf},
                "Restablecer clave", JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) {
            return;
        }
        String nueva = new String(pf.getPassword());
        if (nueva.length() < 6) {
            JOptionPane.showMessageDialog(this, "La clave debe tener al menos 6 caracteres.");
            return;
        }
        try {
            dao.cambiarClave(login, nueva);
            JOptionPane.showMessageDialog(this, "Clave actualizada.");
        } catch (SQLException | IllegalArgumentException ex) {
            error("No se pudo cambiar la clave:\n" + ex.getMessage());
        }
    }

    // =================== AYUDANTES ===================

    private void aviso(String mensaje, java.awt.Component campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        campo.requestFocus();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}