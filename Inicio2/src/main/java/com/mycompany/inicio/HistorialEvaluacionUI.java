package com.mycompany.inicio;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;

final class HistorialEvaluacionUI {
    private HistorialEvaluacionUI() {}
    static void mostrar(Component padre,int solicitud){
        try{
            String permiso;
            if("ANALISTA".equals(Sesion.rol()))permiso="EVALUACION_REALIZAR";
            else if("JEFE".equals(Sesion.rol()))permiso="CREDITO_DECIDIR";
            else throw new IllegalArgumentException("Solo el analista y el jefe revisan la evaluacion.");
            JTable t=new JTable();InterfazFormulario.tabla(t);TablaUtil.cargarAutorizada(t,permiso,EvaluacionDAO.SQL_HISTORIAL,solicitud);
            for(int i=0;i<t.getColumnCount();i++)t.getColumnModel().getColumn(i).setPreferredWidth(i>=11?300:130);
            t.setToolTipText("Desplaza la tabla horizontalmente para revisar razones, recomendacion e historial.");
            JScrollPane scroll=new JScrollPane(t);scroll.setPreferredSize(new Dimension(1050,360));
            JOptionPane.showMessageDialog(padre,scroll,"Evaluaciones de la solicitud "+solicitud,JOptionPane.PLAIN_MESSAGE);
        }catch(Exception e){InterfazFormulario.error(padre,e);}
    }
}
