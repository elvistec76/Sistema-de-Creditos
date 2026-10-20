package com.mycompany.inicio;

/** Ventana principal; el contenido visual se organiza en PanelInicio. */
public class VentanaACredito extends javax.swing.JFrame {
    public VentanaACredito(){
        initComponents();
        panelInicio.acciones(()->abrir(new Login()),()->abrir(new ventanaNuevoCliente()),()->abrir(new LoginTrabajadores()));
        setLocationRelativeTo(null);
    }
    private void abrir(javax.swing.JFrame ventana){ventana.setLocationRelativeTo(this);ventana.setVisible(true);dispose();}
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents(){
        panelInicio = new com.mycompany.inicio.PanelInicio();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema crediticio");
        setMinimumSize(new java.awt.Dimension(980, 650));
        getContentPane().add(panelInicio, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void main(String[] args){Inicio.main(args);}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mycompany.inicio.PanelInicio panelInicio;
    // End of variables declaration//GEN-END:variables
}
