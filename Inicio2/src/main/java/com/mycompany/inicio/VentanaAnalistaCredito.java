package com.mycompany.inicio;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Image;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
/**
 * NOTA: NO reemplaces todo el archivo. Conserva initComponents(), main() y las
 * variables generadas al final. Reemplaza lo que tenias de menu por esto y
 * recuerda que la clase se cierra con UNA sola llave al final del archivo.
 */
public class VentanaAnalistaCredito extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(VentanaAnalistaCredito.class.getName());

   private static final String ICO_CLIENTES    = "/com/imagen/clientes.png";
    private static final String ICO_SOLICITUDES = "/com/imagen/solicitudes.png";
    private static final String ICO_HISTORIAL   = "/com/imagen/historial.png";
    private static final String ICO_CAPACIDAD   = "/com/imagen/capacidad.png";
    private static final String ICO_RIESGO      = "/com/imagen/riesgo.png";
    private static final String ICO_SALIR       = "/com/imagen/salir.png";

    private static final String P_CLIENTES    = "Clientes";
    private static final String P_SOLICITUDES = "Solicitudes pendientes";
    private static final String P_HISTORIAL   = "Historial crediticio";
    private static final String P_CAPACIDAD   = "Capacidad de pago";
    private static final String P_RIESGO      = "Evaluación de riesgo";

    // ---------- Consultas ----------
    private static final String SQL_CLIENTES =
            "SELECT ci AS CI, CONCAT(nombres, ' ', apellidos) AS Cliente, "
          + "telefono AS Telefono, lugar_trabajo AS Trabajo, "
          + "ingresos_mensuales AS Ingresos, fecha_registro AS Registro "
          + "FROM cliente ORDER BY fecha_registro DESC, apellidos";

    private static final String SQL_SOLICITUDES =
            "SELECT s.id_solicitud AS Solicitud, c.ci AS CI, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "s.fecha_solicitud AS Fecha, s.destino AS Destino, "
          + "p.nombre AS Producto, s.monto_solicitado AS Monto, "
          + "s.plazo_meses AS Plazo, s.estado AS Estado "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "LEFT JOIN producto_credito p ON p.id_producto = s.id_producto "
          + "WHERE s.estado IN ('REGISTRADA','PENDIENTE','OBSERVADA','EN_EVALUACION') "
          + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC";

    private static final String SQL_HISTORIAL =
            "SELECT c.ci AS CI, CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "cr.numero_credito AS Credito, cr.fecha_aprobacion AS Aprobado, "
          + "cr.monto_aprobado AS Monto, cr.plazo_meses AS Plazo, "
          + "cr.saldo_capital AS `Saldo capital`, cr.estado AS Estado "
          + "FROM credito cr "
          + "JOIN solicitud_credito s ON s.id_solicitud = cr.id_solicitud "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "ORDER BY cr.fecha_aprobacion DESC";

    // Ingresos del cliente frente a lo que pide en sus solicitudes en tramite
    private static final String SQL_CAPACIDAD =
            "SELECT c.ci AS CI,CONCAT(c.nombres,' ',c.apellidos) AS Cliente,c.ingresos_mensuales AS Ingresos,"
          + "s.id_solicitud AS Solicitud,s.monto_solicitado AS `Monto solicitado`,s.plazo_meses AS Plazo,"
          + "e.cuota_proyectada AS `Cuota aprox.` FROM solicitud_credito s JOIN cliente c ON c.id_cliente=s.id_cliente "
          + "LEFT JOIN evaluacion_credito e ON e.id_solicitud=s.id_solicitud AND e.vigente=1 "
          + "WHERE s.estado IN ('REGISTRADA','PENDIENTE','OBSERVADA','EN_EVALUACION') ORDER BY s.fecha_solicitud DESC";

    private final CardLayout cards = new CardLayout();
    private PanelReporte pClientes, pHistorial, pCapacidad;
private PanelEvaluacion pSolicitudes;
    private PanelRiesgo pRiesgo;

    public VentanaAnalistaCredito() {
        Sesion.exigirRol("ANALISTA");
        initComponents();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.setLayout(cards);

        crearPaneles();
        crearMenu();

        setSize(1200, 780);
        setMinimumSize(new java.awt.Dimension(1000, 650));
        setLocationRelativeTo(null);
        cards.show(jPanel1, P_SOLICITUDES);
    }

    private Icon getIcono(String ruta) {
        java.net.URL url = getClass().getResource(ruta);
        if (url == null) {
            logger.warning("No se encontro la imagen: " + ruta);
            return null;
        }
        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void crearPaneles() {
        pClientes    = new PanelReporte("Clientes registrados", SQL_CLIENTES, "CLIENTE_CONSULTAR");
       pSolicitudes = new PanelEvaluacion();
        pHistorial   = new PanelReporte("Historial crediticio", SQL_HISTORIAL, "EVALUACION_REALIZAR");
        pCapacidad   = new PanelReporte("Capacidad de pago (solicitudes en trámite)", SQL_CAPACIDAD, "EVALUACION_REALIZAR");
        pCapacidad.mostrarPendientesCapacidad();

        jPanel1.add(pClientes, P_CLIENTES);
        jPanel1.add(pSolicitudes, P_SOLICITUDES);
        jPanel1.add(pHistorial, P_HISTORIAL);
        jPanel1.add(pCapacidad, P_CAPACIDAD);
        pRiesgo = new PanelRiesgo();
        jPanel1.add(pRiesgo, P_RIESGO);
    }

    private void crearMenu() {
        agregar(P_CLIENTES, ICO_CLIENTES, () -> pClientes.cargar());
        agregar(P_SOLICITUDES, ICO_SOLICITUDES, () -> pSolicitudes.cargar());
        agregar(P_HISTORIAL, ICO_HISTORIAL, () -> pHistorial.cargar());
        agregar(P_CAPACIDAD, ICO_CAPACIDAD, () -> pCapacidad.cargar());
        agregar(P_RIESGO, ICO_RIESGO, () -> pRiesgo.cargar());

        JMenuItem salir = new JMenuItem("Cerrar sesión", getIcono(ICO_SALIR));
        salir.addActionListener(e -> cerrarSesion());
        jMenuBar2.add(Box.createHorizontalGlue());
        jMenuBar2.add(salir);
        jMenuBar2.revalidate();
        jMenuBar2.repaint();
    }

    /** Agrega una opcion al menu: refresca los datos y muestra el panel. */
    private void agregar(String nombre, String icono, Runnable refrescar) {
        JMenuItem item = new JMenuItem(nombre, getIcono(icono));
        item.addActionListener(e -> {
            refrescar.run();
            cards.show(jPanel1, nombre);
        });
        jMenuBar2.add(item);
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
        jMenuBar2 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 987, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 587, Short.MAX_VALUE)
        );

        jMenuBar2.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        setJMenuBar(jMenuBar2);

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

        java.awt.EventQueue.invokeLater(() -> new VentanaAnalistaCredito().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
