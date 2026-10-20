/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.inicio;

import java.awt.Color;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author linux
 */
public class LoginTrabajadores extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginTrabajadores.class.getName());

    private static final String PH_USUARIO = "ingrese su nombre de usuario";
    private static final String PH_ROL = "ej: Administrador, Analista, etc";

    // {texto que ve la persona, rol en la base de datos}
    private static final String[][] ROLES = {
        {"Administrador", "ADMIN"},
        {"Oficial de crédito", "OFICIAL"},
        {"Analista", "ANALISTA"},
        {"Jefe de créditos", "JEFE"},
        {"Cajero", "CAJERO"},
        {"Gestor de cobranza", "GESTOR"},
        {"Gerente", "GERENTE"}
    };

    public LoginTrabajadores() {
        initComponents();
        setLocationRelativeTo(null);

        contrasenaTrabajadores.setText("");          // quita el "jPasswordField1" del diseño
        ponerAyuda(IDusuario, PH_USUARIO);
        ponerAyuda(rolTXT, PH_ROL);

        // que ningun campo tenga el foco al abrir (si no, se borra el texto de ayuda)
        backg.setFocusable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                backg.requestFocusInWindow();
            }
        });
    }

    // =================== LOGICA ===================

    /** Texto gris que desaparece al escribir y vuelve si el campo queda vacio. */
    private void ponerAyuda(javax.swing.JTextField campo, String ayuda) {
        campo.setForeground(Color.GRAY);
        campo.setText(ayuda);
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().equals(ayuda)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campo.getText().trim().isEmpty()) {
                    campo.setForeground(Color.GRAY);
                    campo.setText(ayuda);
                }
            }
        });
    }

    /** Quita tildes, espacios sobrantes y mayusculas para comparar textos. */
    private static String limpiar(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase();
    }

    /** Convierte lo escrito ("Analista", "admin", "jefe"...) en el rol de la base de datos. */
    private String rolDesdeTexto(String texto) {
        String t = limpiar(texto);
        if (t.isEmpty() || texto.equals(PH_ROL)) {
            return null;
        }
        for (String[] r : ROLES) {
            String etiqueta = limpiar(r[0]);
            if (t.equals(limpiar(r[1])) || t.equals(etiqueta)
                    || (t.length() >= 4 && etiqueta.startsWith(t))) {
                return r[1];
            }
        }
        return null;
    }

    private void aviso(String mensaje, java.awt.Component campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Iniciar sesion",
                JOptionPane.WARNING_MESSAGE);
        campo.requestFocus();
    }

    /** Valida usuario, clave y rol contra la base de datos y abre la ventana que corresponde. */
    private void ingresar() {
        String login = IDusuario.getText().trim().toLowerCase();
        String clave = new String(contrasenaTrabajadores.getPassword());
        String rolElegido = rolDesdeTexto(rolTXT.getText());

        if (login.isEmpty() || login.equals(PH_USUARIO.toLowerCase())) {
            aviso("Escribe tu usuario.", IDusuario);
            return;
        }
        if (rolElegido == null) {
            aviso("Escribe un rol válido. Ej: Administrador, Analista, Cajero.", rolTXT);
            return;
        }
        if (clave.isEmpty()) {
            aviso("Escribe tu contraseña.", contrasenaTrabajadores);
            return;
        }

        try {
            String rol = Sesion.abrir(login, clave);

            if (rol == null) {
                aviso("Usuario o contraseña incorrectos, o cuenta desactivada.", contrasenaTrabajadores);
                contrasenaTrabajadores.setText("");
                return;
            }
            if (rol.equals("CLIENTE")) {
                Sesion.cerrar();
                aviso("Los clientes ingresan desde el login de clientes.", IDusuario);
                return;
            }
            if (!rol.equals(rolElegido)) {
                Sesion.cerrar();
                aviso("Ese usuario no tiene el rol escrito.", rolTXT);
                return;
            }

            switch (rol) {
                case "ADMIN":
                    new VentanaAdministrador(login).setVisible(true);
                    this.dispose();
                    break;
                case "OFICIAL":
                    new VentanaOfCredito(login).setVisible(true);
                    this.dispose();
                    break;
                case "ANALISTA":
                    new VentanaAnalistaCredito().setVisible(true);
                    this.dispose();
                    break;
                case "GERENTE":
                    new VentanaGerente(login).setVisible(true);
                    this.dispose();
                    break;
                              case "GESTOR":
                    new VentanaGestor(login).setVisible(true);
                    this.dispose();
                    break;      
                                 case "CAJERO":
                    new VentanaCajero(login).setVisible(true);
                    this.dispose();
                    break;
                case "JEFE":
                    new VentanaJefeCredi(login).setVisible(true);
                    this.dispose();
                    break;
                // Aqui agregaras: CAJERO, GESTOR
                 
                // Aqui agregaras: JEFE, CAJERO, GESTOR
                default:
                    JOptionPane.showMessageDialog(this,
                            "Ingresaste como " + rol + ".\nEsa ventana todavia no esta disponible.",
                            "Iniciar sesion", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException | IllegalArgumentException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con la base de datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backg = new javax.swing.JPanel();
        titulo1 = new javax.swing.JLabel();
        titulo2 = new javax.swing.JLabel();
        fondolb = new javax.swing.JLabel();
        lbIniSess = new javax.swing.JLabel();
        lblogo = new javax.swing.JLabel();
        logo = new javax.swing.JLabel();
        lbCOntrasena = new javax.swing.JLabel();
        LbUsuario = new javax.swing.JLabel();
        rolTXT = new javax.swing.JTextField();
        contrasenaTrabajadores = new javax.swing.JPasswordField();
        LbUsuario1 = new javax.swing.JLabel();
        IDusuario = new javax.swing.JTextField();
        botonEntrar = new javax.swing.JButton();
        btnVolverInicio = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        backg.setBackground(new java.awt.Color(255, 255, 255));
        backg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        titulo1.setFont(new java.awt.Font("Adwaita Mono", 1, 36)); // NOI18N
        titulo1.setText("para Trabajadores");
        backg.add(titulo1, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 280, -1, -1));

        titulo2.setFont(new java.awt.Font("Adwaita Mono", 1, 36)); // NOI18N
        titulo2.setText("Inicio de Seccion ");
        titulo2.setAlignmentY(1.0F);
        backg.add(titulo2, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 170, 400, 110));

        fondolb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/imagen/te.png"))); // NOI18N
        backg.add(fondolb, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, -100, 490, 890));

        lbIniSess.setFont(new java.awt.Font("DialogInput", 1, 36)); // NOI18N
        lbIniSess.setText("Iniciar Sesion");
        backg.add(lbIniSess, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 340, 70));

        lblogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/imagen/logo1.png"))); // NOI18N
        backg.add(lblogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 80, 70));

        logo.setText("Logo");
        backg.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, -1, -1));

        lbCOntrasena.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        lbCOntrasena.setText("Contraseña");
        backg.add(lbCOntrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 490, -1, -1));

        LbUsuario.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        LbUsuario.setText("Rol");
        backg.add(LbUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 380, -1, -1));

        rolTXT.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        rolTXT.setForeground(new java.awt.Color(153, 153, 153));
        rolTXT.setText("ej: Administrador , Analsita , etc");
        rolTXT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                rolTXTMousePressed(evt);
            }
        });
        rolTXT.addActionListener(this::rolTXTActionPerformed);
        backg.add(rolTXT, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 420, 430, -1));

        contrasenaTrabajadores.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        contrasenaTrabajadores.setText("jPasswordField1");
        contrasenaTrabajadores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                contrasenaTrabajadoresMousePressed(evt);
            }
        });
        contrasenaTrabajadores.addActionListener(this::contrasenaTrabajadoresActionPerformed);
        backg.add(contrasenaTrabajadores, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 550, 430, 40));

        LbUsuario1.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        LbUsuario1.setText("Usuario (login)");
        backg.add(LbUsuario1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, -1, -1));

        IDusuario.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        IDusuario.setForeground(new java.awt.Color(153, 153, 153));
        IDusuario.setText("ingrese su nombre de usuario");
        IDusuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                IDusuarioMousePressed(evt);
            }
        });
        IDusuario.addActionListener(this::IDusuarioActionPerformed);
        backg.add(IDusuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 300, 430, -1));

        botonEntrar.setText("entrar");
        botonEntrar.addActionListener(this::botonEntrarActionPerformed);
        backg.add(botonEntrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 650, 190, 70));

        btnVolverInicio.setText("Volver al inicio");
        btnVolverInicio.addActionListener(this::btnVolverInicioActionPerformed);
        backg.add(btnVolverInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 650, 200, 70));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backg, javax.swing.GroupLayout.PREFERRED_SIZE, 1039, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(backg, javax.swing.GroupLayout.PREFERRED_SIZE, 779, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rolTXTMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rolTXTMousePressed

    }//GEN-LAST:event_rolTXTMousePressed

    private void contrasenaTrabajadoresMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contrasenaTrabajadoresMousePressed


    }//GEN-LAST:event_contrasenaTrabajadoresMousePressed

    private void IDusuarioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IDusuarioMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_IDusuarioMousePressed

    private void rolTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rolTXTActionPerformed
contrasenaTrabajadores.requestFocus(); 
    }//GEN-LAST:event_rolTXTActionPerformed

    private void botonEntrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonEntrarActionPerformed
        ingresar();
    }//GEN-LAST:event_botonEntrarActionPerformed

    private void btnVolverInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverInicioActionPerformed
        new Inicio().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnVolverInicioActionPerformed

    private void IDusuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IDusuarioActionPerformed
    rolTXT.requestFocus(); 
    }//GEN-LAST:event_IDusuarioActionPerformed

    private void contrasenaTrabajadoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contrasenaTrabajadoresActionPerformed
        ingresar(); 
    }//GEN-LAST:event_contrasenaTrabajadoresActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new LoginTrabajadores().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField IDusuario;
    private javax.swing.JLabel LbUsuario;
    private javax.swing.JLabel LbUsuario1;
    private javax.swing.JPanel backg;
    private javax.swing.JButton botonEntrar;
    private javax.swing.JButton btnVolverInicio;
    private javax.swing.JPasswordField contrasenaTrabajadores;
    private javax.swing.JLabel fondolb;
    private javax.swing.JLabel lbCOntrasena;
    private javax.swing.JLabel lbIniSess;
    private javax.swing.JLabel lblogo;
    private javax.swing.JLabel logo;
    private javax.swing.JTextField rolTXT;
    private javax.swing.JLabel titulo1;
    private javax.swing.JLabel titulo2;
    // End of variables declaration//GEN-END:variables
}
