package com.mycompany.inicio;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

/** Registro manual de una entrega de fondos con referencia y confirmacion expresa. */
public class PanelDesembolso extends JPanel {
    private final String login;
    private final boolean pendientes;
    private final CicloCreditoDAO dao=new CicloCreditoDAO();
    private final JTable tabla=new JTable();
    private final JTextField prevista=new JTextField(LocalDate.now().toString(),11);
    private final JTextField destino=new JTextField(22);
    private final JTextField referencia=new JTextField(24);

    public PanelDesembolso(String login){
        this(login,false);
    }
    public PanelDesembolso(String login,boolean pendientes){
        this.login=login;this.pendientes=pendientes;setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JPanel arriba=new JPanel(new BorderLayout(0,8));JLabel titulo=new JLabel(pendientes?"Desembolsos pendientes":"Desembolso y generacion de cuotas");titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,22f));arriba.add(titulo,BorderLayout.NORTH);
        arriba.add(new JLabel("Registra la entrega realizada y su comprobante. Al confirmar se crean las cuotas y el credito queda habilitado en caja."),BorderLayout.SOUTH);add(arriba,BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);tabla.setRowHeight(24);tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);add(new JScrollPane(tabla),BorderLayout.CENTER);
        JPanel abajo=new JPanel(new GridLayout(0,1,4,4));JPanel fila=new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel("Fecha prevista (AAAA-MM-DD):"));fila.add(prevista);fila.add(new JLabel("Cuenta o destino de entrega:"));fila.add(destino);
        JButton solicitar=new JButton("Solicitar desembolso");fila.add(solicitar);abajo.add(fila);
        JPanel entrega=new JPanel(new FlowLayout(FlowLayout.LEFT));entrega.add(new JLabel("Referencia del comprobante de entrega:"));entrega.add(referencia);
        JButton confirmar=new JButton("Confirmar entrega y generar cuotas");entrega.add(confirmar);abajo.add(entrega);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton plan=new JButton("Ver plan de pagos");JButton rechazar=new JButton("Rechazar operacion pendiente");JButton actualizar=new JButton("Actualizar");
        botones.add(plan);botones.add(rechazar);botones.add(actualizar);abajo.add(botones);
        abajo.add(new JLabel("La confirmacion registra una entrega manual; no realiza una transferencia bancaria. Los vencimientos se calculan desde la fecha real."));add(abajo,BorderLayout.SOUTH);
        solicitar.addActionListener(e->solicitar());confirmar.addActionListener(e->confirmar());rechazar.addActionListener(e->rechazar());plan.addActionListener(e->plan());actualizar.addActionListener(e->cargar());cargar();
    }
    public final void cargar(){try{TablaUtil.cargarAutorizada(tabla,"DESEMBOLSO_REGISTRAR",pendientes?CicloCreditoDAO.SQL_PENDIENTES:CicloCreditoDAO.SQL_DESEMBOLSOS);int[] anchos={55,240,100,180,110,120,85,190,100,160,180,220};for(int i=0;i<anchos.length;i++)tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);}catch(Exception e){DocumentacionUI.error(this,e);}}
    private Object celda(String columna){int fila=tabla.getSelectedRow();if(fila<0)throw new IllegalArgumentException("Selecciona un credito de la tabla.");return DocumentacionUI.celda(tabla,fila,columna);}
    private int credito(){return ((Number)celda("ID")).intValue();}
    private int operacion(){Object id=celda("Operacion");if(!(id instanceof Number))throw new IllegalArgumentException("Primero solicita el desembolso del credito.");return ((Number)id).intValue();}
    private void solicitar(){try{int id=credito();LocalDate fecha=LocalDate.parse(prevista.getText().trim());if(JOptionPane.showConfirmDialog(this,"Solicitar desembolso para "+celda("Credito")+" por Bs "+celda("Monto")+"?", "Solicitar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        int op=dao.solicitarDesembolso(id,login,fecha,destino.getText());JOptionPane.showMessageDialog(this,"Operacion "+op+" solicitada. Confirma solo cuando se haya entregado el dinero.");cargar();}catch(Exception e){DocumentacionUI.error(this,e);}}
    private void confirmar(){try{
        int id=credito();int op=operacion();String ref=referencia.getText().trim();if(ref.isEmpty())throw new IllegalArgumentException("Indica la referencia del comprobante de entrega realizada.");
        mostrarPlanActual();
        if(JOptionPane.showConfirmDialog(this,"Confirmas que se entregaron Bs "+celda("Monto")+" al cliente "+celda("Cliente")+"?\nCredito: "+celda("Credito")+"\nReferencia: "+ref+"\nSe registrara el egreso y se crearan las cuotas del plan mostrado.","Confirmar entrega de fondos",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        dao.confirmarDesembolso(op,login,ref);referencia.setText("");JOptionPane.showMessageDialog(this,"Entrega confirmada. Credito vigente y cuotas disponibles para cliente, cajero y seguimiento del gestor segun vencimiento.");cargar();
    }catch(Exception e){DocumentacionUI.error(this,e);}}
    private void rechazar(){try{int op=operacion();String motivo=JOptionPane.showInputDialog(this,"Motivo del rechazo de la operacion "+op+":");if(motivo==null)return;dao.rechazarDesembolso(op,login,motivo);cargar();}catch(Exception e){DocumentacionUI.error(this,e);}}
    private void plan(){try{mostrarPlanActual();}catch(Exception e){DocumentacionUI.error(this,e);}}
    private void mostrarPlanActual() throws java.sql.SQLException {PanelFormalizacion.mostrarPlan(this,dao.planCredito(credito(),CicloCreditoDAO.fechaActual()),dao.resumenCredito(credito())+".\nCronograma guardado si existe; en otro caso, simulacion de entrega hoy. Cuotas mensuales, tasa anual / 12. FIJA: frances; GRADUAL: capital constante. Sin cargos adicionales.");}
}
