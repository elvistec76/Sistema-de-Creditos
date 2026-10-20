/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
public class VentanaJefeCredi extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaJefeCredi.class.getName());
private static final String P_DECIDIR       = "Por decidir";
    private static final String P_DECIDIDAS     = "Decisiones tomadas";
    private static final String P_MODIFICACIONES = "Modificaciones de pago";

    private static final String SQL_DECIDIDAS =
            "SELECT s.id_solicitud AS Solicitud, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "s.monto_solicitado AS Monto, s.estado AS Decision, "
          + "s.fecha_decision AS Fecha, u.nombre_completo AS Decidio, "
          + "s.motivo_decision AS Motivo "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "JOIN usuario u ON u.id_usuario = s.id_usuario_decision "
          + "ORDER BY s.fecha_decision DESC";

    private final String jefeLogin;
    private final CardLayout cards = new CardLayout();
    private PanelDecisiones pDecidir;
    private PanelDecisionesTomadas pDecididas;
    private PanelModificacionesPago pModificaciones;

    public VentanaJefeCredi(String jefeLogin) {
        Sesion.exigirRol("JEFE");
        this.jefeLogin = jefeLogin;
        initComponents();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.setLayout(cards);

        crearPaneles();
        jMenuBar1.removeAll();
        crearMenu();

        setSize(1200, 750);
        setMinimumSize(new java.awt.Dimension(1000,650));
        setLocationRelativeTo(null);
        cards.show(jPanel1, P_DECIDIR);
    }

    // Constructor vacio: solo para probar la ventana sola con Run File
    public VentanaJefeCredi() {
        this("jefe");
    }

    private void crearPaneles() {
        pDecidir   = new PanelDecisiones(jefeLogin);
        pDecididas = new PanelDecisionesTomadas(jefeLogin, SQL_DECIDIDAS);

        pModificaciones = new PanelModificacionesPago();

        jPanel1.add(pDecidir, P_DECIDIR);
        jPanel1.add(pDecididas, P_DECIDIDAS);
        jPanel1.add(pModificaciones, P_MODIFICACIONES);
    }

    private void crearMenu() {
        agregarOpcion(P_DECIDIR, () -> pDecidir.cargar());
        agregarOpcion(P_DECIDIDAS, () -> pDecididas.cargar());
        agregarOpcion(P_MODIFICACIONES, () -> pModificaciones.cargar());

        JMenuItem salir = new JMenuItem("Cerrar sesión");
        salir.addActionListener(e -> cerrarSesion());
        jMenuBar1.add(Box.createHorizontalGlue());
        jMenuBar1.add(salir);
        jMenuBar1.revalidate();
        jMenuBar1.repaint();
    }

    private void agregarOpcion(String nombre, Runnable refrescar) {
        JMenuItem item = new JMenuItem(nombre);
        item.addActionListener(e -> {
            refrescar.run();
            cards.show(jPanel1, nombre);
        });
        jMenuBar1.add(item);
    }

    private void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            new Inicio().setVisible(true);
            this.dispose();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 487, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 472, Short.MAX_VALUE)
        );

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        java.awt.EventQueue.invokeLater(() -> new VentanaJefeCredi().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
