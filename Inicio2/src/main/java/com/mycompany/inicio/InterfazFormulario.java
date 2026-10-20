package com.mycompany.inicio;

import java.awt.*;
import javax.swing.*;

/** Distribucion comun de los formularios; conserva el tema y los colores de Swing. */
final class InterfazFormulario {
    private InterfazFormulario() {}
    static JPanel encabezado(String titulo, String ayuda) {
        JPanel p=new JPanel(new BorderLayout(0,6));
        JLabel t=new JLabel(titulo);t.setFont(t.getFont().deriveFont(Font.BOLD,22f));
        p.add(t,BorderLayout.NORTH);p.add(new JLabel("<html>"+ayuda+"</html>"),BorderLayout.CENTER);
        return p;
    }
    static void tabla(JTable t) {
        t.setRowHeight(25);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getTableHeader().setReorderingAllowed(false);
    }
    static void anchos(JTable t,int... anchos) {
        for(int i=0;i<Math.min(anchos.length,t.getColumnCount());i++)t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
    }
    static void campo(JPanel p,int fila,String titulo,JComponent c) {
        GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=fila;g.anchor=GridBagConstraints.WEST;
        g.insets=new Insets(4,6,4,12);p.add(new JLabel(titulo),g);
        g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(c,g);
    }
    static JTextArea texto(int filas) {
        JTextArea a=new JTextArea(filas,35);a.setLineWrap(true);a.setWrapStyleWord(true);return a;
    }
    static int id(JTable t) {
        if(t.getSelectedRow()<0)throw new IllegalArgumentException("Selecciona un registro de la tabla.");
        return ((Number)t.getModel().getValueAt(t.convertRowIndexToModel(t.getSelectedRow()),0)).intValue();
    }
    static void error(Component p,Exception e){DocumentacionUI.error(p,e);}
}
