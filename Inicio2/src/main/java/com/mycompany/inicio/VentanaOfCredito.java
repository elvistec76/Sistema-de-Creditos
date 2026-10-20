/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
public class VentanaOfCredito extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaOfCredito.class.getName());

    // {menu desplegable, nombre de la opcion (tambien nombre del panel), descripcion}
    private static final String[][] OPCIONES = {
        {"Clientes", "Registrar cliente", "CU1 - Registrar los datos de un nuevo cliente."},
        {"Clientes", "Actualizar cliente", "CU16 - Modificar los datos de un cliente existente."},
        {"Clientes", "Validar datos y documentos", "CU30 - Validar los datos y documentos presentados por el cliente."},
        {"Clientes", "Registrar observaciones", "CU31 - Registrar observaciones de la documentación."},
        {"Solicitudes", "Registrar solicitud", "CU2 - Registrar una solicitud de crédito."},
        {"Garantías", "Registrar garantía", "CU6 - Registrar la garantía de una solicitud."},
        {"Formalización", "Formalizar contrato", "CU18 - Formalizar el contrato de un crédito aprobado."},
        {"Desembolso", "Desembolsar crédito", "CU11 - Desembolsar un crédito formalizado."},
        {"Desembolso", "Desembolsos pendientes", "CU19 - Verificar desembolsos pendientes."}
    };

    private static final String SQL_SOLICITUDES =
            "SELECT s.id_solicitud AS Solicitud, c.ci AS CI, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "s.fecha_solicitud AS Fecha, s.destino AS Destino, "
          + "p.nombre AS Producto, s.monto_solicitado AS Monto, "
          + "s.plazo_meses AS Plazo, s.estado AS Estado, "
          + "CASE WHEN p.nombre IS NULL OR TRIM(p.nombre)='' "
          + "OR s.monto_solicitado IS NULL OR s.plazo_meses IS NULL THEN "
          + "CONCAT_WS(', ', CASE WHEN p.nombre IS NULL OR TRIM(p.nombre)='' THEN 'Producto' END, "
          + "CASE WHEN s.monto_solicitado IS NULL THEN 'Monto' END, "
          + "CASE WHEN s.plazo_meses IS NULL THEN 'Plazo' END) "
          + "ELSE 'Sin campos pendientes' END AS `Pendiente del analista` "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "LEFT JOIN producto_credito p ON p.id_producto = s.id_producto "
          + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC";

    private static final String SQL_GARANTIAS =
            "SELECT g.id_garantia AS Garantia, s.id_solicitud AS Solicitud, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, t.nombre AS Tipo, "
          + "g.descripcion AS Descripcion, g.valor AS Valor, "
          + "g.estado_verificacion AS Verificacion, g.estado AS Estado "
          + "FROM garantia g "
          + "JOIN solicitud_credito s ON s.id_solicitud = g.id_solicitud "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "JOIN tipo_garantia t ON t.id_tipo_garantia = g.id_tipo_garantia "
          + "ORDER BY g.id_garantia DESC";

    private static final String SQL_APROBADAS =
            "SELECT s.id_solicitud AS Solicitud, c.ci AS CI, "
          + "CONCAT(c.nombres, ' ', c.apellidos) AS Cliente, "
          + "p.nombre AS Producto, s.monto_solicitado AS Monto, "
          + "s.plazo_meses AS Plazo, s.fecha_decision AS Aprobada "
          + "FROM solicitud_credito s "
          + "JOIN cliente c ON c.id_cliente = s.id_cliente "
          + "LEFT JOIN producto_credito p ON p.id_producto = s.id_producto "
          + "WHERE s.estado = 'APROBADA' "
          + "AND NOT EXISTS (SELECT 1 FROM credito cr WHERE cr.id_solicitud = s.id_solicitud) "
          + "ORDER BY s.fecha_decision DESC";

    private final String login;
    private final CardLayout cards = new CardLayout();
    private final Map<String, Runnable> refrescos = new HashMap<>();

    public VentanaOfCredito(String login) {
        Sesion.exigirRol("OFICIAL");
        this.login = login;
        initComponents();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.setLayout(cards);

        crearPaneles();
        crearMenu();

        setTitle("Oficial de crédito - " + login);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        cards.show(jPanel1, OPCIONES[0][1]);
    }

    // Constructor vacio: solo para probar la ventana sola con Run File
    public VentanaOfCredito() {
        this("oficial");
    }

    private void crearMenu() {
        JMenu actual = null;
        String grupoActual = "";
        for (String[] op : OPCIONES) {
            if (!op[0].equals(grupoActual)) {
                actual = new JMenu(op[0]);
                jMenuBar1.add(actual);
                grupoActual = op[0];
            }
            String nombre = op[1];
            JMenuItem item = new JMenuItem(nombre);
            item.addActionListener(e -> {
                Runnable r = refrescos.get(nombre);
                if (r != null) {
                    r.run();                 // datos frescos al abrir la opcion
                }
                cards.show(jPanel1, nombre);
            });
            actual.add(item);
        }

        JMenu sesion = new JMenu("Sesión");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        cerrar.addActionListener(e -> cerrarSesion());
        sesion.add(cerrar);

        jMenuBar1.add(Box.createHorizontalGlue());
        jMenuBar1.add(sesion);
        jMenuBar1.revalidate();
        jMenuBar1.repaint();
    }

    private void crearPaneles() {
        // Clientes: el mismo panel sirve para registrar y para actualizar
        PanelClientesOficial pClientes = new PanelClientesOficial();
        jPanel1.add(pClientes, "Registrar cliente");
        PanelClientesOficial pActualizar = new PanelClientesOficial();
        jPanel1.add(pActualizar, "Actualizar cliente");
        refrescos.put("Registrar cliente", () -> pClientes.cargar());
        refrescos.put("Actualizar cliente", () -> pActualizar.cargar());

        // Reportes de solo lectura
        PanelSolicitudesOficial pSolicitudes = new PanelSolicitudesOficial(login, SQL_SOLICITUDES);
        PanelGarantiasOficial pGarantias = new PanelGarantiasOficial(login);
        PanelDocumentacionOficial pDocumentos = new PanelDocumentacionOficial(login, false);
        PanelDocumentacionOficial pObservaciones = new PanelDocumentacionOficial(login, true);
        PanelFormalizacion pAprobadas = new PanelFormalizacion(login);

        jPanel1.add(pSolicitudes, "Registrar solicitud");
        jPanel1.add(pGarantias, "Registrar garantía");
        jPanel1.add(pDocumentos, "Validar datos y documentos");
        jPanel1.add(pObservaciones, "Registrar observaciones");
        jPanel1.add(pAprobadas, "Formalizar contrato");
        PanelDesembolso pPendientes = new PanelDesembolso(login,true);
        PanelDesembolso pDesembolsar = new PanelDesembolso(login);
        jPanel1.add(pDesembolsar, "Desembolsar crédito");
        refrescos.put("Desembolsar crédito", pDesembolsar::cargar);
        jPanel1.add(pPendientes, "Desembolsos pendientes");
        refrescos.put("Registrar solicitud", () -> pSolicitudes.cargar());
        refrescos.put("Registrar garantía", () -> pGarantias.cargar());
        refrescos.put("Validar datos y documentos", () -> pDocumentos.cargar());
        refrescos.put("Registrar observaciones", () -> pObservaciones.cargar());
        refrescos.put("Formalizar contrato", () -> pAprobadas.cargar());
        refrescos.put("Desembolsos pendientes", () -> pPendientes.cargar());

        // El resto: pantalla pendiente
        for (String[] op : OPCIONES) {
            if (!refrescos.containsKey(op[1])) {
                jPanel1.add(panelBase(op[1], op[2]), op[1]);
            }
        }
    }

    private JPanel panelBase(String titulo, String descripcion) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel("<html><div style='text-align:center'><h1>" + titulo
                + "</h1><p>" + descripcion + "</p><p><i>(próximamente)</i></p></div></html>",
                SwingConstants.CENTER);
        p.add(lbl);
        return p;
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 683, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 607, Short.MAX_VALUE)
        );

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 345, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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


        java.awt.EventQueue.invokeLater(() -> new VentanaOfCredito().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
