/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.inicio;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.Box;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
public class VentanaGerente extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaGerente.class.getName());
 private static final String P_RESUMEN     = "Resumen";
    private static final String P_SOLICITUDES = "Solicitudes";
    private static final String P_POR_ESTADO  = "Solicitudes por estado";
    private static final String P_CARTERA     = "Cartera de créditos";
    private static final String P_VENCIDAS    = "Cuotas vencidas";
    private static final String P_MOROSOS     = "Morosos";

    private static final String SQL_SOLICITUDES =
            "SELECT s.id_solicitud AS Solicitud, c.ci AS CI, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "s.fecha_solicitud AS Fecha, s.monto_solicitado AS Monto, "
          + "s.plazo_meses AS Plazo, s.destino AS Destino, s.estado AS Estado "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC";

    private static final String SQL_POR_ESTADO =
            "SELECT estado AS Estado, COUNT(*) AS Solicitudes, "
          + "COALESCE(SUM(monto_solicitado), 0) AS `Monto solicitado` "
          + "FROM solicitud_credito GROUP BY estado ORDER BY COUNT(*) DESC";

    // Estas tres usan las vistas que ya trae tu base de datos
    private static final String SQL_CARTERA  = "SELECT * FROM v_estado_credito";
    private static final String SQL_VENCIDAS = "SELECT * FROM v_cuotas_vencidas";
    private static final String SQL_MOROSOS  = "SELECT * FROM v_morosos";

    private final String gerenteLogin;
    private final CardLayout cards = new CardLayout();
    private PanelResumenGerente pResumen;
    private PanelReporte pSolicitudes, pPorEstado, pCartera, pVencidas, pMorosos;

    public VentanaGerente(String gerenteLogin) {
        Sesion.exigirRol("GERENTE");
        this.gerenteLogin = gerenteLogin;
        initComponents();

        // jPanel1 pasa a ser el area de contenido y ocupa toda la ventana
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.setLayout(cards);

        crearPaneles();
        crearMenu();

        setSize(1100, 650);
        setLocationRelativeTo(null);
        cards.show(jPanel1, P_RESUMEN);
    }

    // Constructor vacio: solo para probar la ventana sola con Run File
    public VentanaGerente() {
        this("gerente");
    }

    private void crearPaneles() {
        pResumen     = new PanelResumenGerente(gerenteLogin);
        pSolicitudes = new PanelReporte("Todas las solicitudes", SQL_SOLICITUDES, "REPORTE_GENERAR");
        pPorEstado   = new PanelReporte("Solicitudes por estado", SQL_POR_ESTADO, "REPORTE_GENERAR");
        pCartera     = new PanelReporte("Cartera de créditos", SQL_CARTERA, "REPORTE_GENERAR");
        pVencidas    = new PanelReporte("Cuotas vencidas", SQL_VENCIDAS, "REPORTE_GENERAR");
        pMorosos     = new PanelReporte("Clientes morosos", SQL_MOROSOS, "REPORTE_GENERAR");

        jPanel1.add(pResumen, P_RESUMEN);
        jPanel1.add(pSolicitudes, P_SOLICITUDES);
        jPanel1.add(pPorEstado, P_POR_ESTADO);
        jPanel1.add(pCartera, P_CARTERA);
        jPanel1.add(pVencidas, P_VENCIDAS);
        jPanel1.add(pMorosos, P_MOROSOS);
    }

    private void crearMenu() {
        agregarOpcion(P_RESUMEN, () -> pResumen.cargar());
        agregarOpcion(P_SOLICITUDES, () -> pSolicitudes.cargar());
        agregarOpcion(P_POR_ESTADO, () -> pPorEstado.cargar());
        agregarOpcion(P_CARTERA, () -> pCartera.cargar());
        agregarOpcion(P_VENCIDAS, () -> pVencidas.cargar());
        agregarOpcion(P_MOROSOS, () -> pMorosos.cargar());

        JMenuItem mniSalir = new JMenuItem("Cerrar sesión");
        mniSalir.addActionListener(e -> cerrarSesion());
        jMenuBar1.add(Box.createHorizontalGlue());   // empuja "Cerrar sesion" a la derecha
        jMenuBar1.add(mniSalir);
        jMenuBar1.revalidate();
        jMenuBar1.repaint();
    }

    /** Agrega una opcion al menu: refresca los datos y muestra el panel. */
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1025, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 627, Short.MAX_VALUE)
        );

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
        java.awt.EventQueue.invokeLater(() -> new VentanaGerente().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
