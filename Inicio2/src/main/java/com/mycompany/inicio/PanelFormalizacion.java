package com.mycompany.inicio;

import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Flujo explicito: preparar registro, adjuntar contrato firmado y formalizar. */
public class PanelFormalizacion extends JPanel {
    private final String login;
    private final CicloCreditoDAO dao = new CicloCreditoDAO();
    private final JTable tabla = new JTable();
    private final JTextField firma = new JTextField(LocalDate.now().toString(), 11);
    private final JLabel archivo = new JLabel("Selecciona el contrato firmado en PDF o imagen.");
    private Path origen;

    public PanelFormalizacion(String login) {
        this.login=login;
        setLayout(new BorderLayout(10,10)); setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JLabel titulo=new JLabel("Formalizar contrato de una solicitud aprobada"); titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,22f));
        JPanel arriba=new JPanel(new BorderLayout(0,8)); arriba.add(titulo,BorderLayout.NORTH);
        arriba.add(new JLabel("1. Revisa condiciones y documentos.  2. Prepara el credito.  3. Adjunta el contrato firmado y formaliza."),BorderLayout.SOUTH);
        add(arriba,BorderLayout.NORTH); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setRowHeight(24); add(new JScrollPane(tabla),BorderLayout.CENTER);
        JPanel pie=new JPanel(new GridLayout(0,1,4,4));
        JPanel fila=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton condiciones=new JButton("Ver condiciones y cuotas"); JButton preparar=new JButton("Preparar credito y contrato"); JButton actualizar=new JButton("Actualizar");
        fila.add(condiciones);fila.add(preparar);fila.add(actualizar);pie.add(fila);
        JPanel firmaFila=new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton elegir=new JButton("Seleccionar contrato firmado"); JButton formalizar=new JButton("Formalizar firma"); JButton ver=new JButton("Ver contrato firmado");
        firmaFila.add(new JLabel("Firma (AAAA-MM-DD):")); firmaFila.add(firma);firmaFila.add(elegir);firmaFila.add(formalizar);firmaFila.add(ver);pie.add(firmaFila);pie.add(archivo);
        pie.add(new JLabel("El registro del contrato conserva tus condiciones. El desembolso se solicita despues de formalizar la firma."));add(pie,BorderLayout.SOUTH);
        condiciones.addActionListener(e->condiciones());preparar.addActionListener(e->preparar());actualizar.addActionListener(e->cargar());
        elegir.addActionListener(e->{Path elegido=DocumentacionUI.seleccionar(this);if(elegido!=null){origen=elegido;archivo.setText(elegido.getFileName().toString());}});
        formalizar.addActionListener(e->formalizar());ver.addActionListener(e->verContrato());
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()){origen=null;archivo.setText("Selecciona el contrato firmado para la solicitud elegida.");}});
        cargar();
    }
    public final void cargar(){try{TablaUtil.cargarAutorizada(tabla,"CONTRATO_FORMALIZAR",CicloCreditoDAO.SQL_APROBADAS);tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);int[] anchos={75,105,185,120,105,70,240,240,135};for(int i=0;i<anchos.length;i++)tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);}catch(Exception e){DocumentacionUI.error(this,e);}}
    private int solicitud(){int fila=tabla.getSelectedRow();if(fila<0)throw new IllegalArgumentException("Selecciona una solicitud aprobada.");return ((Number)DocumentacionUI.celda(tabla,fila,"Solicitud")).intValue();}
    private void condiciones(){try{var c=dao.condiciones(solicitud());mostrarPlan(this,Amortizacion.generar(c.monto(),c.tasa(),c.meses(),c.tipo(),CicloCreditoDAO.fechaActual()),
                "Cliente: "+c.cliente()+" | Monto: Bs "+c.monto()+" | Tasa nominal anual: "+c.tasa()+"% | "+c.tipo()+" | "+c.meses()+" meses.\n"
                +"Simulacion con desembolso hoy. Tasa mensual = anual / 12; fechas mensuales; sin seguros, comisiones ni penalidades.");}catch(Exception e){DocumentacionUI.error(this,e);}}
    private void preparar(){try{int id=solicitud();if(JOptionPane.showConfirmDialog(this,"Preparar el credito y contrato de la solicitud "+id+"?\nEsto todavia no registra una entrega de dinero.","Preparar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        var ct=dao.preparar(id,login);JOptionPane.showMessageDialog(this,"Contrato "+ct.numero()+" preparado.\nAdjunta el contrato firmado para formalizarlo.");cargar();}catch(Exception e){DocumentacionUI.error(this,e);}}
    private void formalizar(){String copia=null;try{
        int id=solicitud();LocalDate fecha=LocalDate.parse(firma.getText().trim());if(origen==null)throw new IllegalArgumentException("Selecciona el contrato firmado.");
        var ct=dao.preparacion(id);
        if(JOptionPane.showConfirmDialog(this,"Confirmas que el contrato "+ct.numero()+" fue firmado el "+fecha+"?", "Formalizar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        copia=ArchivoDocumental.copiar(origen,"contratos");dao.formalizar(ct.credito(),login,fecha,copia);copia=null;origen=null;
        JOptionPane.showMessageDialog(this,"Contrato formalizado. Continua en Desembolso > Desembolsar credito.");cargar();
    }catch(Exception e){DocumentacionUI.limpiarCopia(this,copia);DocumentacionUI.error(this,e);}}
    private void verContrato(){try{
        Seguridad.comprobar("CONTRATO_FORMALIZAR");
        try(var con=Conexion.getConexion();var ps=con.prepareStatement("SELECT ct.referencia_documental FROM contrato ct JOIN credito cr ON cr.id_credito=ct.id_credito WHERE cr.id_solicitud=? ORDER BY ct.version DESC LIMIT 1")){
            ps.setInt(1,solicitud());try(var rs=ps.executeQuery()){if(!rs.next()||rs.getString(1)==null)throw new IllegalArgumentException("El contrato aun no tiene un respaldo firmado.");DocumentacionUI.abrir(this,rs.getString(1));}}
    }catch(Exception e){DocumentacionUI.error(this,e);}}
    static void mostrarPlan(Component padre,List<Amortizacion.CuotaPlan> plan,String detalle){
        DefaultTableModel modelo=new DefaultTableModel(new String[]{"Cuota","Vencimiento","Capital Bs","Interes Bs","Total Bs","Capital restante Bs"},0){@Override public boolean isCellEditable(int f,int c){return false;}};
        java.math.BigDecimal interes=java.math.BigDecimal.ZERO;java.math.BigDecimal total=java.math.BigDecimal.ZERO;
        for(var q:plan){modelo.addRow(new Object[]{q.numero(),q.vence(),q.capital(),q.interes(),q.monto(),q.saldoCapital()});interes=interes.add(q.interes());total=total.add(q.monto());}
        JTextArea cabecera=new JTextArea(detalle);cabecera.setEditable(false);cabecera.setLineWrap(true);cabecera.setWrapStyleWord(true);cabecera.setOpaque(false);
        JTable cuotas=new JTable(modelo);cuotas.setRowHeight(24);JPanel p=new JPanel(new BorderLayout(6,6));p.add(cabecera,BorderLayout.NORTH);p.add(new JScrollPane(cuotas),BorderLayout.CENTER);
        p.add(new JLabel("Cuotas: "+plan.size()+" | Total intereses: Bs "+interes+" | Total a pagar: Bs "+total+". Ultima cuota ajustada por redondeo."),BorderLayout.SOUTH);
        p.setPreferredSize(new Dimension(850,430));JOptionPane.showMessageDialog(padre,p,"Plan mensual de pagos",JOptionPane.PLAIN_MESSAGE);
    }
}
