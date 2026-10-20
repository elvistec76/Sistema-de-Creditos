package com.mycompany.inicio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.*;

/** Cuotas vencidas o proximas a vencer y registro manual del seguimiento. */
public class PanelGestionCobranza extends JPanel {
    private final String login;
    private final JLabel anticipacion=new JLabel();
    private final JTable tabla = new JTable();
    private final JComboBox<String> tipo = new JComboBox<>(new String[]{"COBRANZA", "PREVENTIVA"});
    private final JComboBox<String> medio = new JComboBox<>(new String[]{"TELEFONO", "PRESENCIAL", "CORREO", "MENSAJE"});
    private final JTextField resultado = new JTextField();
    private final JTextArea observaciones = new JTextArea(3, 35);

    public PanelGestionCobranza(String login) {
        this.login = login;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel cabecera=InterfazFormulario.encabezado("Seguimiento preventivo y cobranza","El gestor contacta al cliente, registra el resultado y ofrece alternativas. Los cobros se realizan en caja; los cambios de fecha los decide el jefe.<br>Preventiva: cuotas proximas a vencer. Cobranza: cuotas vencidas con saldo. Los avisos son internos, visibles al iniciar sesion el cliente.");
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        arriba.add(new JLabel("Seguimiento:")); arriba.add(tipo);
        JButton actualizar = new JButton("Actualizar"); arriba.add(actualizar);
        arriba.add(anticipacion);
        cabecera.add(arriba,BorderLayout.SOUTH);add(cabecera, BorderLayout.NORTH);
        InterfazFormulario.tabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel campos = new JPanel(new GridLayout(2, 2, 8, 8));
        campos.add(new JLabel("Medio utilizado:")); campos.add(medio);
        campos.add(new JLabel("Resultado (max. 200 caracteres):")); campos.add(resultado);
        observaciones.setLineWrap(true); observaciones.setWrapStyleWord(true);
        JScrollPane notas = new JScrollPane(observaciones);
        notas.setBorder(BorderFactory.createTitledBorder("Alternativa ofrecida y observaciones (obligatorio, max. 500)"));
        JPanel abajo = new JPanel(new BorderLayout(6, 6));
        abajo.add(campos, BorderLayout.NORTH); abajo.add(notas, BorderLayout.CENTER);
        JButton registrar = new JButton("Registrar gestion realizada");
        JButton aviso=new JButton("Generar aviso interno al cliente");JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));acciones.add(registrar);acciones.add(aviso);
        aviso.addActionListener(e->{try{int id=new GestionCobranzaDAO().avisar(cuotaSeleccionada());JOptionPane.showMessageDialog(this,"Alerta "+id+": aviso interno disponible. Se evita duplicarlo el mismo dia.");cargar();}catch(Exception ex){InterfazFormulario.error(this,ex);}});
        abajo.add(acciones, BorderLayout.SOUTH); add(abajo, BorderLayout.SOUTH);
        actualizar.addActionListener(e -> cargar()); tipo.addActionListener(e -> cargar());
        registrar.addActionListener(e -> registrar());
        cargar();
    }

    public final void cargar() {
        try {
            Seguridad.comprobar("COBRANZA_GESTIONAR");
            try(var c=Conexion.getConexion()){anticipacion.setText("Preventiva: vencimientos de hoy a "+ReglasCredito.parametro(c,"DIAS_RECORDATORIO",366)+" dias");}
            TablaUtil.cargarAutorizada(tabla, "COBRANZA_GESTIONAR", GestionCobranzaDAO.SQL_SEGUIMIENTO, tipo.getSelectedItem(), tipo.getSelectedItem());
            InterfazFormulario.anchos(tabla,55,160,95,210,100,60,110,110,85,75,115);
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo cargar el seguimiento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona una cuota del credito."); return; }
        int id = ((Number) tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), 0)).intValue();
        try {
            Sesion.exigirIdentidad(login);
            new GestionCobranzaDAO().registrarCuota(cuotaSeleccionada(), (String) medio.getSelectedItem(), resultado.getText(), observaciones.getText());
            resultado.setText(""); observaciones.setText("");
            JOptionPane.showMessageDialog(this, "Gestion registrada en el historial.");
            cargar();
        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo registrar", JOptionPane.WARNING_MESSAGE);
        }
    }
    private int cuotaSeleccionada(){
        if(tabla.getSelectedRow()<0)throw new IllegalArgumentException("Selecciona la cuota que vas a atender.");
        return ((Number)tabla.getModel().getValueAt(tabla.convertRowIndexToModel(tabla.getSelectedRow()),9)).intValue();
    }
}
