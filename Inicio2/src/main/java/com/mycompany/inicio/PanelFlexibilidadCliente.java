package com.mycompany.inicio;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

/** Cliente: consulta sus cuotas, solicita un cambio y revisa la respuesta del jefe. */
public class PanelFlexibilidadCliente extends JPanel {
    private final String login;
    private final JTable cuotas=new JTable(),solicitudes=new JTable();
    private final JComboBox<String> tipo=new JComboBox<>(new String[]{"CAMBIO_FECHA","APLAZAMIENTO"});
    private final JTextField fecha=new JTextField();
    private final JTextArea motivo=InterfazFormulario.texto(3);
    private final JLabel reglas=new JLabel();
    public PanelFlexibilidadCliente(String login){
        this.login=login;Sesion.exigirRol("CLIENTE");Sesion.exigirIdentidad(login);
        setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(InterfazFormulario.encabezado("Solicitar cambio de fecha o aplazamiento","Selecciona una cuota propia pendiente. Hasta que el jefe apruebe, debes cumplir la fecha original."),BorderLayout.NORTH);
        InterfazFormulario.tabla(cuotas);InterfazFormulario.tabla(solicitudes);
        JTabbedPane pestanas=new JTabbedPane();pestanas.addTab("Mis cuotas",new JScrollPane(cuotas));pestanas.addTab("Mis solicitudes y respuestas",new JScrollPane(solicitudes));add(pestanas,BorderLayout.CENTER);
        JPanel campos=new JPanel(new GridBagLayout());
        InterfazFormulario.campo(campos,0,"Tipo de solicitud:",tipo);InterfazFormulario.campo(campos,1,"Fecha propuesta (AAAA-MM-DD):",fecha);
        InterfazFormulario.campo(campos,2,"Motivo (obligatorio, max. 300):",new JScrollPane(motivo));
        InterfazFormulario.campo(campos,3,"Condiciones:",reglas);
        JPanel abajo=new JPanel(new BorderLayout());abajo.add(campos,BorderLayout.CENTER);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton enviar=new JButton("Enviar solicitud al jefe"),actualizar=new JButton("Actualizar");botones.add(enviar);botones.add(actualizar);abajo.add(botones,BorderLayout.SOUTH);add(abajo,BorderLayout.SOUTH);
        enviar.addActionListener(e->{try{
            int id=new ModificacionPagoDAO().solicitar(InterfazFormulario.id(cuotas),(String)tipo.getSelectedItem(),LocalDate.parse(fecha.getText().trim()),motivo.getText());
            JOptionPane.showMessageDialog(this,"Solicitud "+id+" enviada al jefe. La presentacion no cambia aun tus vencimientos.");fecha.setText("");motivo.setText("");cargar();pestanas.setSelectedIndex(1);
        }catch(java.time.format.DateTimeParseException ex){InterfazFormulario.error(this,new IllegalArgumentException("Escribe la fecha como AAAA-MM-DD."));}catch(Exception ex){InterfazFormulario.error(this,ex);}});
        actualizar.addActionListener(e->cargar());cargar();
    }
    public final void cargar(){try{
        Sesion.exigirIdentidad(login);Seguridad.comprobar("MODIFICACION_SOLICITAR");
        TablaUtil.cargar(cuotas,ModificacionPagoDAO.SQL_CUOTAS_CLIENTE,login);TablaUtil.cargar(solicitudes,ModificacionPagoDAO.SQL_MIS_SOLICITUDES,login);
        InterfazFormulario.anchos(cuotas,60,180,60,120,120,300);InterfazFormulario.anchos(solicitudes,60,180,130,160,120,110,300,450);
        try(var c=Conexion.getConexion()){
            int dias=ReglasCredito.parametro(c,"DIAS_MIN_ANTICIPACION_MOD",366),max=ReglasCredito.parametro(c,"MAX_APLAZAMIENTOS_ANIO",100);
            reglas.setText("<html>Anticipacion minima: "+dias+" dias. Maximo "+max+" aplazamientos aprobados en 12 meses.<br>Sin cuotas vencidas ni pagos en la cuota elegida. Aplazamiento: fecha posterior a la ultima cuota, hasta un mes despues.<br>Solo se ofrece conservar los importes pactados, sin cargos nuevos, sujeto a autorizacion del jefe.</html>");
        }
    }catch(Exception e){InterfazFormulario.error(this,e);}}
}
