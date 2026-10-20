/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.inicio;

import java.sql.SQLException;
import javax.swing.JOptionPane;
 
public class VentanaCliente extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaCliente.class.getName());


    private String ci;     // CI del cliente que esta viendo la ventana
    private javax.swing.JMenuItem menuAvisos;

    public VentanaCliente(String ci) {
        Sesion.exigirRol("CLIENTE");
        Sesion.exigirIdentidad(ci);
        initComponents();
        setLocationRelativeTo(null);
        this.ci = ci;
        cargarTodo();
        javax.swing.JMenuBar barraAvisos=new javax.swing.JMenuBar();
        menuAvisos=new javax.swing.JMenuItem("Mis avisos");
        menuAvisos.addActionListener(e->mostrarAvisos(false));
        barraAvisos.add(menuAvisos);
        javax.swing.JMenuItem flexibilidad=new javax.swing.JMenuItem("Cambios de fecha y aplazamientos");
        flexibilidad.addActionListener(e->{
            javax.swing.JDialog dialogo=new javax.swing.JDialog(this,"Mis alternativas de pago",true);
            dialogo.setContentPane(new PanelFlexibilidadCliente(this.ci));dialogo.setSize(1050,680);
            dialogo.setMinimumSize(new java.awt.Dimension(850,600));dialogo.setLocationRelativeTo(this);dialogo.setVisible(true);
            cargarTodo();
        });
        barraAvisos.add(flexibilidad);setJMenuBar(barraAvisos);
        javax.swing.SwingUtilities.invokeLater(()->mostrarAvisos(true));
    }

    // Constructor vacio: solo para probar la ventana sola con Run File
    public VentanaCliente() {
        this("1234567");   // pon un CI que exista en tu BD
    }

    private static final String SQL_SOLICITUDES =
            "SELECT s.id_solicitud AS Solicitud, p.nombre AS Producto, "
          + "s.monto_solicitado AS Monto, s.plazo_meses AS Plazo, "
          + "s.fecha_solicitud AS Fecha, s.estado AS Estado, "
          + "s.motivo_decision AS Motivo "
          + "FROM solicitud_credito s "
          + "JOIN cliente cl ON cl.id_cliente = s.id_cliente "
          + "LEFT JOIN producto_credito p ON p.id_producto = s.id_producto "
          + "WHERE cl.ci = ? "
          + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC";

    // IMPORTANTE: Estado debe quedar como ULTIMA columna (lo usa resumenPagos)
    private static final String SQL_PAGOS =
            "SELECT cr.numero_credito AS Credito, q.numero AS Cuota, "
          + "q.fecha_vencimiento AS Vence, q.monto AS Monto, q.saldo AS Saldo, "
          + "GREATEST(DATEDIFF(CURDATE(),q.fecha_vencimiento),0) AS `Dias atraso`, "
          + "CASE WHEN q.fecha_vencimiento<CURDATE() THEN 'VENCIDA' "
          + "WHEN q.fecha_vencimiento<=DATE_ADD(CURDATE(),INTERVAL 7 DAY) THEN 'POR_VENCER' ELSE 'PENDIENTE' END AS Estado "
          + "FROM cuota q "
          + "JOIN cronograma_pago cp ON cp.id_cronograma = q.id_cronograma AND cp.vigente = 1 "
          + "JOIN credito cr ON cr.id_credito = cp.id_credito "
          + "JOIN solicitud_credito s ON s.id_solicitud = cr.id_solicitud "
          + "JOIN cliente cl ON cl.id_cliente = s.id_cliente "
          + "WHERE cl.ci = ? AND q.saldo>0 AND cr.estado IN ('VIGENTE','EN_MORA') "
          + "ORDER BY q.fecha_vencimiento";

    private static final String SQL_HISTORIAL =
            "SELECT cr.numero_credito AS Credito, cr.fecha_aprobacion AS Aprobado, "
          + "cr.monto_aprobado AS Monto, cr.plazo_meses AS Plazo, "
          + "cr.saldo_capital AS `Saldo capital`, cr.estado AS Estado "
          + "FROM credito cr "
          + "JOIN solicitud_credito s ON s.id_solicitud = cr.id_solicitud "
          + "JOIN cliente cl ON cl.id_cliente = s.id_cliente "
          + "WHERE cl.ci = ? "
          + "ORDER BY cr.fecha_aprobacion DESC";

    /** Llena el nombre y las 3 tablas. Se llama al abrir y al pulsar Actualizar. */
    private void cargarTodo() {
        try {
            Seguridad.comprobar("CRONOGRAMA_CONSULTAR");
            Cliente c = new ClienteDAO().buscarPorCi(ci);
            if (c != null) {
                lblBienvenida.setText("Bienvenido(a), " + c.getNombreCompleto()
                        + "  (CI " + c.getCi() + ")");
            }
            TablaUtil.cargar(jTable1, SQL_SOLICITUDES, ci);
            TablaUtil.cargar(tblPagos, SQL_PAGOS, ci);
            TablaUtil.cargar(tblHistorial, SQL_HISTORIAL, ci);
            ordenarColumnas(jTable1, new int[]{75,130,110,70,105,170,300});
            ordenarColumnas(tblPagos, new int[]{170,65,125,120,120,100,150});
            ordenarColumnas(tblHistorial, new int[]{170,175,130,80,150,150});
            resumenPagos();
        } catch (SQLException | IllegalArgumentException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ordenarColumnas(javax.swing.JTable tabla, int[] anchos) {
        tabla.setRowHeight(24);
        for (int i=0;i<Math.min(anchos.length,tabla.getColumnCount());i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /** Cuenta cuantas cuotas faltan y cuantas estan vencidas. */
    private void resumenPagos() {
        int total = tblPagos.getRowCount();
        int colEstado = tblPagos.getColumnCount() - 1;   // la ultima columna es Estado
        int vencidas = 0;
        for (int i = 0; i < total; i++) {
            if ("VENCIDA".equals(tblPagos.getValueAt(i, colEstado))) {
                vencidas++;
            }
        }
        lblResumen.setText("Cuotas por pagar: " + total + "     |     Vencidas: " + vencidas);
    }

    private void mostrarAvisos(boolean soloNuevos) {
        try {
            AvisosClienteDAO dao=new AvisosClienteDAO();
            var todos=dao.listar(ci);
            long nuevos=todos.stream().filter(AvisosClienteDAO.Aviso::nuevo).count();
            menuAvisos.setText("Mis avisos ("+nuevos+" nuevos)");
            var mostrados=todos.stream().filter(a->!soloNuevos||a.nuevo()).toList();
            if(mostrados.isEmpty()) {
                if(!soloNuevos)JOptionPane.showMessageDialog(this,"No tienes avisos registrados.");
                return;
            }
            StringBuilder texto=new StringBuilder();
            for(var a:mostrados)texto.append(a.fecha()).append(" | ").append(a.tipo().replace('_',' '))
                    .append(a.nuevo()?" | NUEVO":" | MOSTRADO").append("\n").append(a.mensaje()).append("\n\n");
            javax.swing.JTextArea contenido=new javax.swing.JTextArea(texto.toString(),16,65);
            contenido.setEditable(false);contenido.setLineWrap(true);contenido.setWrapStyleWord(true);contenido.setCaretPosition(0);
            JOptionPane.showMessageDialog(this,new javax.swing.JScrollPane(contenido),"Mis avisos",JOptionPane.INFORMATION_MESSAGE);
            dao.marcarMostrados(ci,mostrados.stream().filter(AvisosClienteDAO.Aviso::nuevo).map(AvisosClienteDAO.Aviso::id).toList());
            long restantes=dao.listar(ci).stream().filter(AvisosClienteDAO.Aviso::nuevo).count();
            menuAvisos.setText("Mis avisos ("+restantes+" nuevos)");
        }catch(SQLException | IllegalArgumentException e){JOptionPane.showMessageDialog(this,e.getMessage(),"No se pudieron consultar los avisos",JOptionPane.WARNING_MESSAGE);}
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblBienvenida = new javax.swing.JLabel();
        lblResumen = new javax.swing.JLabel();
        tblSolicitudes = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblPagos = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();
        btnSolicitar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnCerrarSession = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/imagen/te.png"))); // NOI18N
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, -120, 100, 850));
        lblBienvenida.setFont(lblBienvenida.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        jPanel1.add(lblBienvenida, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 20, 976, 32));

        lblResumen.setText("Resumen: ");
        jPanel1.add(lblResumen, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 58, 976, 28));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblSolicitudes.setViewportView(jTable1);

        jPanel1.add(tblSolicitudes, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 138, 976, 140));

        tblPagos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tblPagos);

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 324, 976, 140));

        tblHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblHistorial);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 510, 976, 140));

        btnSolicitar.setBackground(new java.awt.Color(204, 255, 204));
        btnSolicitar.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        btnSolicitar.setText("Solicitar Credito");
        btnSolicitar.setActionCommand("");
        btnSolicitar.addActionListener(this::btnSolicitarActionPerformed);
        jPanel1.add(btnSolicitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 682, 300, 52));

        btnActualizar.setBackground(new java.awt.Color(204, 255, 204));
        btnActualizar.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(this::btnActualizarActionPerformed);
        jPanel1.add(btnActualizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 682, 300, 52));

        btnCerrarSession.setBackground(new java.awt.Color(204, 255, 204));
        btnCerrarSession.setFont(new java.awt.Font("sansserif", 0, 18)); // NOI18N
        btnCerrarSession.setText("Cerrar Sesion");
        btnCerrarSession.setActionCommand("Cerrar Sesion");
        btnCerrarSession.addActionListener(this::btnCerrarSessionActionPerformed);
        jPanel1.add(btnCerrarSession, new org.netbeans.lib.awtextra.AbsoluteConstraints(696, 682, 300, 52));

        jLabel1.setText("Cuotas pendientes de pago");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 294, 976, 24));

        jLabel3.setText("Historial de tus creditos");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 480, 976, 24));

        jLabel4.setText("Solicitudes de credito realizadas");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 108, 976, 24));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 765, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSolicitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSolicitarActionPerformed
Cliente c = null;
        try {
            c = new ClienteDAO().buscarPorCi(ci);
        } catch (SQLException | IllegalArgumentException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        String nombre = (c != null) ? c.getNombreCompleto() : "";
        new SolicitudCredito(nombre, ci).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnSolicitarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        cargarTodo();
        mostrarAvisos(true);
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnCerrarSessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSessionActionPerformed
        int op = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            new Inicio().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnCerrarSessionActionPerformed

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

        /* Create and display the form */
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
        java.awt.EventQueue.invokeLater(() -> new VentanaCliente().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnCerrarSession;
    private javax.swing.JButton btnSolicitar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblResumen;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTable tblPagos;
    private javax.swing.JScrollPane tblSolicitudes;
    // End of variables declaration//GEN-END:variables
}
