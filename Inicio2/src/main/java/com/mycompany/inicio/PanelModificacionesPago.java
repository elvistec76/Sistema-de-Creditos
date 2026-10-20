package com.mycompany.inicio;

import java.awt.*;
import javax.swing.*;

/** Bandeja del jefe para revisar, autorizar o rechazar solicitudes del cliente. */
public class PanelModificacionesPago extends JPanel {
    private final JTable tabla=new JTable();
    private final JTextArea justificacion=InterfazFormulario.texto(3),detalle=InterfazFormulario.texto(4);
    private final JCheckBox importes=new JCheckBox("Autorizo conservar capital e interes pactados, sin cargos nuevos");
    private final ModificacionPagoDAO dao=new ModificacionPagoDAO();
    public PanelModificacionesPago(){
        setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(InterfazFormulario.encabezado("Modificaciones de pago","Solicitudes de cambio de fecha o aplazamiento enviadas por el cliente. La solicitud pendiente o rechazada conserva el plan actual."),BorderLayout.NORTH);
        InterfazFormulario.tabla(tabla);add(new JScrollPane(tabla),BorderLayout.CENTER);
        detalle.setEditable(false);detalle.setText("Selecciona una solicitud y pulsa Revisar elegibilidad.\nEl aplazamiento mueve una cuota al final del plan. El jefe debe autorizar expresamente el tratamiento del importe.");
        JPanel formulario=new JPanel(new GridBagLayout());
        InterfazFormulario.campo(formulario,0,"Revision:",new JScrollPane(detalle));
        InterfazFormulario.campo(formulario,1,"Justificacion (obligatoria, max. 300):",new JScrollPane(justificacion));
        InterfazFormulario.campo(formulario,2,"Tratamiento al aprobar:",importes);
        JPanel abajo=new JPanel(new BorderLayout());abajo.add(formulario,BorderLayout.CENTER);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton revisar=new JButton("Revisar elegibilidad"),aprobar=new JButton("Aprobar cambio"),rechazar=new JButton("Rechazar"),actualizar=new JButton("Actualizar");
        botones.add(revisar);botones.add(aprobar);botones.add(rechazar);botones.add(actualizar);abajo.add(botones,BorderLayout.SOUTH);add(abajo,BorderLayout.SOUTH);
        revisar.addActionListener(e->{try{detalle.setText(dao.revisar(InterfazFormulario.id(tabla)));}catch(Exception ex){detalle.setText(ex.getMessage());}});
        aprobar.addActionListener(e->decidir(true));rechazar.addActionListener(e->decidir(false));actualizar.addActionListener(e->cargar());
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()){importes.setSelected(false);justificacion.setText("");if(tabla.getSelectedRow()>=0){int f=tabla.getSelectedRow();detalle.setText("Estado: "+DocumentacionUI.celda(tabla,f,"Estado")+" | Fecha anterior: "+DocumentacionUI.celda(tabla,f,"Anterior")+" | Propuesta: "+DocumentacionUI.celda(tabla,f,"Propuesta")+"\nMotivo del cliente: "+DocumentacionUI.celda(tabla,f,"Motivo")+"\nDecision: "+DocumentacionUI.celda(tabla,f,"Justificacion")+"\nTratamiento: "+DocumentacionUI.celda(tabla,f,"Tratamiento"));detalle.setCaretPosition(0);}}});
        cargar();
    }
    public final void cargar(){try{TablaUtil.cargarAutorizada(tabla,"MODIFICACION_DECIDIR",ModificacionPagoDAO.SQL_JEFE);InterfazFormulario.anchos(tabla,55,160,90,180,100,130,55,105,105,100,260,270,450,100);}catch(Exception e){InterfazFormulario.error(this,e);}}
    private void decidir(boolean aprobar){try{
        int id=InterfazFormulario.id(tabla);
        if(JOptionPane.showConfirmDialog(this,(aprobar?"Aprobar el cambio y generar una nueva version del cronograma":"Rechazar la solicitud y conservar el cronograma")+"?","Confirmar decision",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        dao.decidir(id,aprobar,justificacion.getText(),importes.isSelected());
        JOptionPane.showMessageDialog(this,"Decision guardada. El cliente tiene un aviso interno con el resultado.");cargar();
    }catch(Exception e){InterfazFormulario.error(this,e);}}
}
