package com.mycompany.inicio;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/** Ventana propia del gestor de cobranza. */
public class VentanaGestor extends JFrame {
    public VentanaGestor(String login) {
        Sesion.exigirRol("GESTOR");Sesion.exigirIdentidad(login);
        setTitle("Gestor de cobranza - " + login);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir(); }
        });
        PanelGestionCobranza seguimiento = new PanelGestionCobranza(login);
        PanelReporte historial = new PanelReporte("Gestiones realizadas por " + login,
                GestionCobranzaDAO.SQL_HISTORIAL, "COBRANZA_GESTIONAR", login);
        java.awt.CardLayout cards=new java.awt.CardLayout();JPanel contenido=new JPanel(cards);
        contenido.add(seguimiento,"Seguimiento");contenido.add(historial,"Mi historial");setContentPane(contenido);
        JMenuBar menu=new JMenuBar();
        JMenuItem seguir=new JMenuItem("Seguimiento y alertas"),anteriores=new JMenuItem("Mi historial");
        seguir.addActionListener(e->{seguimiento.cargar();cards.show(contenido,"Seguimiento");});
        anteriores.addActionListener(e->{historial.cargar();cards.show(contenido,"Mi historial");});
        menu.add(seguir);menu.add(anteriores);menu.add(Box.createHorizontalGlue());
        JMenuItem cerrar = new JMenuItem("Cerrar sesion");
        cerrar.addActionListener(e -> salir()); menu.add(cerrar); setJMenuBar(menu);
        setSize(1200, 750);setMinimumSize(new java.awt.Dimension(1000,650)); setLocationRelativeTo(null);
    }

    private void salir() {
        if (JOptionPane.showConfirmDialog(this, "Cerrar sesion?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose(); new Inicio().setVisible(true);
        }
    }
}
