package com.mycompany.inicio;
public class Inicio {
 
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(Inicio::abrirPrincipal);
    }

    private static void abrirPrincipal() {
        Sesion.cerrar();
        new VentanaACredito().setVisible(true);
    }
 
    /**
     * Permite usar new Inicio().setVisible(true) desde cualquier ventana
     * (por ejemplo al cerrar sesion) y vuelve a abrir la ventana principal.
     */
    public void setVisible(boolean b) {
        if (b) {
            abrirPrincipal();
        }
    }
}