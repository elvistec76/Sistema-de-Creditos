package com.mycompany.inicio;

import java.awt.*;
import javax.swing.*;

/** Permite corregir solicitudes aprobadas que aun no tengan un credito. */
public class PanelDecisionesTomadas extends JPanel {
    private final String jefe,sql;
    private final JTable tabla=new JTable();
    public PanelDecisionesTomadas(String jefe,String sql){
        this.jefe=jefe;this.sql=sql;setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JLabel titulo=new JLabel("Decisiones tomadas");titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,22f));add(titulo,BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);add(new JScrollPane(tabla),BorderLayout.CENTER);
        JPanel pie=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton devolver=new JButton("Devolver aprobada a analisis");JButton actualizar=new JButton("Actualizar");pie.add(devolver);pie.add(actualizar);add(pie,BorderLayout.SOUTH);
        JButton revisar=new JButton("Ver evaluacion"),ratificar=new JButton("Ratificar evaluacion");pie.add(revisar);pie.add(ratificar);
        revisar.addActionListener(e->{try{HistorialEvaluacionUI.mostrar(this,InterfazFormulario.id(tabla));}catch(Exception ex){DocumentacionUI.error(this,ex);}});
        ratificar.addActionListener(e->{try{int id=InterfazFormulario.id(tabla);String motivo=JOptionPane.showInputDialog(this,"Justifica la ratificacion de la evaluacion actual (no cambia monto, tasa ni plazo):");if(motivo==null)return;new DecisionDAO().ratificarEvaluacion(id,jefe,motivo);JOptionPane.showMessageDialog(this,"Evaluacion favorable actual ratificada con auditoria.");cargar();}catch(Exception ex){DocumentacionUI.error(this,ex);}});
        actualizar.addActionListener(e->cargar());devolver.addActionListener(e->devolver());cargar();
    }
    public final void cargar(){try{TablaUtil.cargarAutorizada(tabla,"CREDITO_DECIDIR",sql);}catch(Exception e){DocumentacionUI.error(this,e);}}
    private void devolver(){try{int fila=tabla.getSelectedRow();if(fila<0)throw new IllegalArgumentException("Selecciona una solicitud aprobada sin credito preparado.");
        int id=((Number)DocumentacionUI.celda(tabla,fila,"Solicitud")).intValue();String motivo=JOptionPane.showInputDialog(this,"Motivo para devolver la solicitud "+id+" al analista:");if(motivo==null)return;
        new DecisionDAO().devolverAAnalisis(id,jefe,motivo);JOptionPane.showMessageDialog(this,"Solicitud observada: el analista puede corregirla y enviarla nuevamente.");cargar();
    }catch(Exception e){DocumentacionUI.error(this,e);}}
}
