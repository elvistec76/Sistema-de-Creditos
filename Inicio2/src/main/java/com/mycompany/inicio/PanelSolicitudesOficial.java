package com.mycompany.inicio;

import java.awt.*;
import javax.swing.*;

/** Registro presencial y consulta de solicitudes con datos pendientes del analista. */
public class PanelSolicitudesOficial extends JPanel {
    private final PanelReporte reporte;
    private final String login;
    public PanelSolicitudesOficial(String login,String sql){
        this.login=login;setLayout(new BorderLayout());
        reporte=new PanelReporte("Solicitudes registradas",sql,"SOLICITUD_REGISTRAR");reporte.mostrarPendientesAnalista();add(reporte,BorderLayout.CENTER);
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton nueva=new JButton("Registrar nueva solicitud");acciones.add(nueva);add(acciones,BorderLayout.SOUTH);nueva.addActionListener(e->registrar());
    }
    public void cargar(){reporte.cargar();}
    private void registrar(){JTextField ci=new JTextField(14);JTextArea destino=new JTextArea(3,35);destino.setLineWrap(true);destino.setWrapStyleWord(true);
        int opcion=JOptionPane.showConfirmDialog(this,new Object[]{"CI del cliente registrado:",ci,"Uso del dinero (hasta 150 caracteres):",new JScrollPane(destino),"El analista completara producto, monto y plazo."},"Nueva solicitud",JOptionPane.OK_CANCEL_OPTION);
        if(opcion!=JOptionPane.OK_OPTION)return;
        try{int id=new SolicitudDAO().registrarOficial(ci.getText(),destino.getText(),login);JOptionPane.showMessageDialog(this,"Solicitud "+id+" registrada para analisis.");cargar();}catch(Exception e){DocumentacionUI.error(this,e);}
    }
}
