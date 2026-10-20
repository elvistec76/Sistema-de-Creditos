package com.mycompany.inicio;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.*;

/** Formulario del analista con tabla, calculos y evaluaciones versionadas. */
public class PanelRiesgo extends JPanel {
    private final JTable tabla=new JTable();
    private final JTextField gastos=new JTextField("0.00"),externas=new JTextField("0.00"),referencia=new JTextField(),vigencia=new JTextField();
    private final JComboBox<String> riesgo=new JComboBox<>(new String[]{"BAJO","MEDIO","ALTO"});
    private final JTextArea razones=InterfazFormulario.texto(2),recomendacion=InterfazFormulario.texto(2),evidencia=InterfazFormulario.texto(2),historial=InterfazFormulario.texto(2);
    private final JTextArea calculo=InterfazFormulario.texto(5);
    private final EvaluacionDAO dao=new EvaluacionDAO();
    public PanelRiesgo(){
        setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(InterfazFormulario.encabezado("Evaluacion de pagos y riesgo","1. Selecciona una solicitud en evaluacion. 2. Revisa respaldos y calcula. 3. Guarda la evaluacion para el jefe."),BorderLayout.NORTH);
        InterfazFormulario.tabla(tabla);
        JPanel capacidad=new JPanel(new GridBagLayout()),clasificacion=new JPanel(new GridBagLayout()),antecedentes=new JPanel(new GridBagLayout());
        InterfazFormulario.campo(capacidad,0,"Gastos mensuales (Bs):",gastos);
        InterfazFormulario.campo(capacidad,1,"Cuotas externas mensuales (Bs):",externas);
        InterfazFormulario.campo(capacidad,2,"Criterio:",new JLabel("<html>Las cuotas internas se calculan automaticamente; no las repitas en externas.<br>Cuota maxima = maximo de 0 y (ingresos - gastos - cuotas internas - externas) x porcentaje de la politica.</html>"));
        calculo.setEditable(false);calculo.setText("Selecciona una solicitud y pulsa Calcular capacidad.\nSi faltan condiciones, completalas en Solicitudes pendientes.");
        InterfazFormulario.campo(capacidad,3,"Calculo:",new JScrollPane(calculo));
        InterfazFormulario.campo(clasificacion,0,"Nivel de riesgo:",riesgo);
        InterfazFormulario.campo(clasificacion,1,"Razones del riesgo (max. 500):",new JScrollPane(razones));
        InterfazFormulario.campo(clasificacion,2,"Recomendacion (max. 500):",new JScrollPane(recomendacion));
        InterfazFormulario.campo(clasificacion,3,"Observaciones / evidencia (max. 500):",new JScrollPane(evidencia));
        InterfazFormulario.campo(antecedentes,0,"Referencia del historial manual:",referencia);
        InterfazFormulario.campo(antecedentes,1,"Historial valido hasta (AAAA-MM-DD):",vigencia);
        InterfazFormulario.campo(antecedentes,2,"Resultado del historial manual:",new JScrollPane(historial));
        InterfazFormulario.campo(antecedentes,3,"Alcance:",new JLabel("<html>Registro manual de un historial revisado y respaldado por el analista.<br>No hay consulta en linea. Sin historial vigente, el resultado queda pendiente.</html>"));
        JTabbedPane detalle=new JTabbedPane();detalle.addTab("1. Capacidad de pago",new JScrollPane(capacidad));
        detalle.addTab("2. Riesgo y recomendacion",new JScrollPane(clasificacion));detalle.addTab("3. Historial de riesgo (manual)",new JScrollPane(antecedentes));
        JSplitPane division=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(tabla),detalle);
        division.setResizeWeight(.35);division.setDividerLocation(190);division.setBorder(null);add(division,BorderLayout.CENTER);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton calcular=new JButton("Calcular capacidad"),guardar=new JButton("Guardar evaluacion"),actualizar=new JButton("Actualizar");
        JButton ver=new JButton("Ver evaluaciones anteriores");ver.addActionListener(e->{try{HistorialEvaluacionUI.mostrar(this,InterfazFormulario.id(tabla));}catch(Exception ex){InterfazFormulario.error(this,ex);}});
        botones.add(calcular);botones.add(guardar);botones.add(ver);botones.add(actualizar);add(botones,BorderLayout.SOUTH);
        calcular.addActionListener(e->calcular());guardar.addActionListener(e->guardar());actualizar.addActionListener(e->cargar());
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()){gastos.setText("0.00");externas.setText("0.00");razones.setText("");recomendacion.setText("");evidencia.setText("");referencia.setText("");vigencia.setText("");historial.setText("");calculo.setText("Revisa los respaldos del cliente antes de guardar una nueva version.");}});
        cargar();
    }
    public final void cargar(){try{TablaUtil.cargarAutorizada(tabla,"EVALUACION_REALIZAR",EvaluacionDAO.SQL_SOLICITUDES);InterfazFormulario.anchos(tabla,85,100,210,130,110,70,150,100);}catch(Exception e){InterfazFormulario.error(this,e);}}
    private BigDecimal numero(JTextField f){try{return new BigDecimal(f.getText().trim().replace(',','.'));}catch(NumberFormatException e){throw new IllegalArgumentException("Gastos y cuotas externas deben ser importes numericos; usa 0 si no existen.");}}
    private void calcular(){try{calculo.setText(dao.calcular(InterfazFormulario.id(tabla),numero(gastos),numero(externas)).resumen());}catch(Exception e){InterfazFormulario.error(this,e);}}
    private void guardar(){try{
        int solicitud=InterfazFormulario.id(tabla);
        LocalDate fecha=vigencia.getText().isBlank()?null:LocalDate.parse(vigencia.getText().trim());
        int id=dao.guardar(solicitud,new EvaluacionDAO.Datos(numero(gastos),numero(externas),(String)riesgo.getSelectedItem(),razones.getText(),recomendacion.getText(),evidencia.getText(),referencia.getText(),fecha,historial.getText()));
        JOptionPane.showMessageDialog(this,"Evaluacion "+id+" guardada. Revisa el resultado en la tabla; el jefe toma la decision final.");cargar();
    }catch(java.time.format.DateTimeParseException e){InterfazFormulario.error(this,new IllegalArgumentException("La vigencia debe tener formato AAAA-MM-DD."));}catch(Exception e){InterfazFormulario.error(this,e);}}
}
