package com.mycompany.inicio;

import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/** Alta, valoracion, respaldos y verificacion de garantias declaradas. */
public class PanelGarantiasOficial extends JPanel {
    private final String login;
    private final OficialDocumentacionDAO dao=new OficialDocumentacionDAO();
    private final JTable tabla=new JTable();
    private final JComboBox<OficialDocumentacionDAO.Solicitud> solicitudes=new JComboBox<>();
    private final JComboBox<OficialDocumentacionDAO.TipoGarantia> tipos=new JComboBox<>();
    private final JTextField titular=new JTextField();
    private final JTextField valor=new JTextField();
    private final JTextArea descripcion=new JTextArea(2,30);
    private final JTextArea evidencia=new JTextArea(2,30);
    private final JComboBox<String> verificacion=new JComboBox<>(new String[]{"PENDIENTE","VALIDADA"});
    private final JTextField tipoDocumento=new JTextField("RESPALDO DE GARANTIA",18);
    private final JLabel requisitos=new JLabel(" ");
    private final JLabel avisoValor=new JLabel(" ");
    private final JLabel seleccion=new JLabel("Nueva garantia");
    private final JLabel archivo=new JLabel("Ningun respaldo nuevo seleccionado");
    private Integer id;
    private Path origen;
    private boolean cargando;

    public PanelGarantiasOficial(String login) {
        this.login=login;setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JPanel arriba=new JPanel(new BorderLayout());JLabel titulo=new JLabel("Registrar y valorar garantias");titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,22f));arriba.add(titulo,BorderLayout.NORTH);
        arriba.add(new JLabel("Selecciona una garantia para completarla o pulsa Nueva garantia. Los respaldos se conservan en archivos locales."),BorderLayout.SOUTH);add(arriba,BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);JScrollPane lista=new JScrollPane(tabla);
        JPanel form=new JPanel(new BorderLayout(6,6));
        JPanel campos=new JPanel(new GridBagLayout());int fila=0;
        agregar(campos,fila++,"Solicitud / cliente:",solicitudes);agregar(campos,fila++,"Tipo de garantia:",tipos);agregar(campos,fila++,"Titular / garante (obligatorio):",titular);
        agregar(campos,fila++,"Descripcion (max. 300):",new JScrollPane(descripcion));
        agregar(campos,fila++,"Valor del bien (Bs):",valor);agregar(campos,fila++,"Verificacion:",verificacion);
        agregar(campos,fila++,"Evidencia de verificacion (max. 300):",new JScrollPane(evidencia));
        agregar(campos,fila++,"Tipo del respaldo nuevo:",tipoDocumento);
        descripcion.setLineWrap(true);descripcion.setWrapStyleWord(true);evidencia.setLineWrap(true);evidencia.setWrapStyleWord(true);
        JPanel notas=new JPanel(new GridLayout(3,1));notas.add(seleccion);notas.add(requisitos);notas.add(avisoValor);form.add(notas,BorderLayout.NORTH);form.add(campos,BorderLayout.CENTER);
        JPanel abajo=new JPanel(new BorderLayout());
        JPanel adjunto=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton elegir=new JButton("Seleccionar respaldo");adjunto.add(elegir);adjunto.add(archivo);abajo.add(adjunto,BorderLayout.NORTH);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton nuevo=new JButton("Nueva garantia");JButton guardar=new JButton("Guardar garantia");JButton respaldos=new JButton("Ver respaldos");JButton actualizar=new JButton("Actualizar");
        botones.add(nuevo);botones.add(guardar);botones.add(respaldos);botones.add(actualizar);abajo.add(botones,BorderLayout.SOUTH);add(abajo,BorderLayout.SOUTH);
        JScrollPane formulario=new JScrollPane(form);formulario.setBorder(BorderFactory.createTitledBorder("Datos y valoracion de la garantia"));
        JSplitPane division=new JSplitPane(JSplitPane.VERTICAL_SPLIT,lista,formulario);division.setResizeWeight(0.4);division.setDividerLocation(180);add(division,BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&!cargando&&tabla.getSelectedRow()>=0)seleccionar();});
        tipos.addActionListener(e->{if(!cargando)mostrarTipo();});
        elegir.addActionListener(e->{Path p=DocumentacionUI.seleccionar(this);if(p!=null){origen=p;archivo.setText(p.getFileName().toString());}});
        nuevo.addActionListener(e->limpiar());guardar.addActionListener(e->guardar());respaldos.addActionListener(e->respaldos());actualizar.addActionListener(e->cargar());
        cargar();
    }
    private static void agregar(JPanel panel,int fila,String texto,Component campo) {
        GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=fila;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(3,8,3,8);panel.add(new JLabel(texto),g);
        g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(campo,g);
    }
    public final void cargar() {
        Integer anterior=id;
        try {
            cargando=true;solicitudes.removeAllItems();for(var s:dao.solicitudes())solicitudes.addItem(s);
            tipos.removeAllItems();for(var t:dao.tipos())tipos.addItem(t);
            TablaUtil.cargarAutorizada(tabla,"GARANTIA_REGISTRAR",OficialDocumentacionDAO.SQL_GARANTIAS);
            int valorCol=tabla.getColumnModel().getColumnIndex("Valor");
            tabla.getColumnModel().getColumn(valorCol).setCellRenderer(new DefaultTableCellRenderer(){
                @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foco,int f,int c) {
                    Component celda=super.getTableCellRendererComponent(t,v,sel,foco,f,c);
                    String tipo=String.valueOf(DocumentacionUI.celda(t,f,"Tipo"));
                    if("PERSONAL".equalsIgnoreCase(tipo))setText("No aplica (personal)");
                    else if(v==null || (v instanceof BigDecimal numero && numero.signum()==0))setText("Pendiente de valoracion");
                    return celda;
                }
            });
            tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for(int c=0;c<tabla.getColumnCount();c++)tabla.getColumnModel().getColumn(c).setPreferredWidth(c==2||c==4||c==9?220:150);
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}finally{cargando=false;}
        limpiar();
        if(anterior!=null)seleccionarId(anterior);
    }
    private void seleccionarId(int buscado) {
        for(int f=0;f<tabla.getRowCount();f++)if(((Number)DocumentacionUI.celda(tabla,f,"Garantia")).intValue()==buscado){tabla.setRowSelectionInterval(f,f);return;}
    }
    private void seleccionar() {
        try {
            int n=((Number)DocumentacionUI.celda(tabla,tabla.getSelectedRow(),"Garantia")).intValue();
            var g=dao.garantia(n);id=g.id();
            cargando=true;
            for(int i=0;i<solicitudes.getItemCount();i++)if(solicitudes.getItemAt(i).id()==g.solicitud())solicitudes.setSelectedIndex(i);
            boolean existe=false;
            for(int i=0;i<tipos.getItemCount();i++)if(tipos.getItemAt(i).id()==g.tipo()){tipos.setSelectedIndex(i);existe=true;}
            if(!existe)tipos.setSelectedIndex(-1);
            solicitudes.setEnabled(false);titular.setText(g.titular());descripcion.setText(g.descripcion());evidencia.setText(g.evidencia());verificacion.setSelectedItem(g.verificacion());
            origen=null;archivo.setText("Sin respaldo nuevo; pulsa Ver respaldos para consultar los existentes");seleccion.setText("Editando garantia "+id+" | Estado: "+g.estado());
            mostrarTipo();valor.setText(g.valor().toPlainString());
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}finally{cargando=false;}
    }
    private void mostrarTipo() {
        var tipo=(OficialDocumentacionDAO.TipoGarantia)tipos.getSelectedItem();boolean personal=tipo!=null&&tipo.personal();
        valor.setEnabled(!personal);if(personal)valor.setText("0.00");
        requisitos.setText("Datos requeridos: "+(tipo==null?"Selecciona un tipo activo.":tipo.requisitos()));
        avisoValor.setText(personal?"Personal: no se tasa un bien; completa los datos del garante y su respaldo.":"Valor obligatorio mayor que cero. Para VALIDADA: evidencia y al menos un respaldo adjunto.");
    }
    private void limpiar() {
        tabla.clearSelection();id=null;solicitudes.setEnabled(true);titular.setText("");descripcion.setText("");valor.setText("");evidencia.setText("");
        verificacion.setSelectedItem("PENDIENTE");origen=null;archivo.setText("Ningun respaldo nuevo seleccionado");seleccion.setText("Nueva garantia");mostrarTipo();
    }
    private void guardar() {
        String copia=null;
        try {
            var s=(OficialDocumentacionDAO.Solicitud)solicitudes.getSelectedItem();var t=(OficialDocumentacionDAO.TipoGarantia)tipos.getSelectedItem();
            if(s==null||t==null)throw new IllegalArgumentException("Selecciona solicitud y tipo de garantia activo.");
            String textoValor=valor.getText().trim();
            if(!textoValor.matches("[0-9]+(?:[.,][0-9]{1,2})?"))throw new IllegalArgumentException("Escribe un valor con hasta dos decimales, sin separador de miles.");
            BigDecimal importe=new BigDecimal(textoValor.replace(',','.'));
            if(origen!=null)copia=ArchivoDocumental.copiar(origen,"garantias");
            int guardada=dao.guardarGarantia(id,s.id(),t.id(),login,descripcion.getText(),titular.getText(),importe,String.valueOf(verificacion.getSelectedItem()),evidencia.getText(),tipoDocumento.getText(),copia);
            copia=null;id=guardada;cargar();JOptionPane.showMessageDialog(this,"Garantia "+guardada+" guardada correctamente.");
        }catch(Exception e){DocumentacionUI.limpiarCopia(this,copia);DocumentacionUI.error(this,e);}
    }
    private void respaldos() {
        try {
            if(id==null)throw new IllegalArgumentException("Selecciona una garantia registrada.");
            JTable documentos=new JTable();TablaUtil.cargarAutorizada(documentos,"GARANTIA_REGISTRAR",OficialDocumentacionDAO.SQL_RESPALDOS,id);
            JPanel panel=new JPanel(new BorderLayout());panel.add(new JScrollPane(documentos),BorderLayout.CENTER);
            JButton abrir=new JButton("Abrir respaldo seleccionado");panel.add(abrir,BorderLayout.SOUTH);
            abrir.addActionListener(e->{int f=documentos.getSelectedRow();if(f>=0)DocumentacionUI.abrir(panel,String.valueOf(DocumentacionUI.celda(documentos,f,"Archivo")));});
            panel.setPreferredSize(new Dimension(850,300));JOptionPane.showMessageDialog(this,panel,"Respaldos de garantia "+id,JOptionPane.PLAIN_MESSAGE);
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
}
