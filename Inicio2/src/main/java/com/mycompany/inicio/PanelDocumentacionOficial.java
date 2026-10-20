package com.mycompany.inicio;

import java.awt.*;
import java.nio.file.Path;
import java.sql.SQLException;
import javax.swing.*;

/** Dos tarjetas independientes para validar documentos y registrar observaciones. */
public class PanelDocumentacionOficial extends JPanel {
    private final String login;
    private final boolean observaciones;
    private final OficialDocumentacionDAO dao=new OficialDocumentacionDAO();
    private final JComboBox<OficialDocumentacionDAO.Solicitud> solicitudes=new JComboBox<>();
    private final JTextArea datos=new JTextArea(3,70);
    private final JLabel revision=new JLabel(" ");
    private final JTable documentos=new JTable();
    private final JTable historial=new JTable();
    private final JTextArea observacion=new JTextArea(4,55);
    private final JComboBox<String> tipo=new JComboBox<>(new String[]{"CI","COMPROBANTE DE INGRESOS","DOMICILIO","OTRO"});
    private final JLabel archivo=new JLabel("Ningun archivo seleccionado");
    private Path origen;
    private boolean cargando;

    public PanelDocumentacionOficial(String login,boolean observaciones) {
        this.login=login;this.observaciones=observaciones;
        archivo.setPreferredSize(new Dimension(250,25));
        setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JPanel arriba=new JPanel(new BorderLayout(5,5));
        JLabel titulo=new JLabel(observaciones?"Registrar observaciones de documentos":"Validar datos y documentos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,22f));arriba.add(titulo,BorderLayout.NORTH);
        JPanel seleccion=new JPanel(new BorderLayout(8,8));seleccion.add(new JLabel("Solicitud / cliente:"),BorderLayout.WEST);seleccion.add(solicitudes,BorderLayout.CENTER);
        JButton actualizar=new JButton("Actualizar");seleccion.add(actualizar,BorderLayout.EAST);arriba.add(seleccion,BorderLayout.CENTER);
        JPanel info=new JPanel(new BorderLayout());datos.setEditable(false);datos.setOpaque(false);datos.setLineWrap(true);datos.setWrapStyleWord(true);info.add(datos,BorderLayout.CENTER);info.add(revision,BorderLayout.SOUTH);arriba.add(info,BorderLayout.SOUTH);add(arriba,BorderLayout.NORTH);
        documentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane lista=new JScrollPane(documentos);lista.setBorder(BorderFactory.createTitledBorder("Documentos de la solicitud: selecciona una fila para revisar o anotar"));
        if(observaciones) {
            JScrollPane notas=new JScrollPane(historial);notas.setBorder(BorderFactory.createTitledBorder("Historial de observaciones del documento seleccionado"));
            JSplitPane centro=new JSplitPane(JSplitPane.VERTICAL_SPLIT,lista,notas);centro.setResizeWeight(0.6);add(centro,BorderLayout.CENTER);
            JPanel abajo=new JPanel(new BorderLayout(6,6));observacion.setLineWrap(true);observacion.setWrapStyleWord(true);
            JScrollPane entrada=new JScrollPane(observacion);entrada.setBorder(BorderFactory.createTitledBorder("Observacion (obligatoria, hasta 2000 caracteres)"));abajo.add(entrada,BorderLayout.CENTER);
            JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton guardar=new JButton("Registrar observacion");JButton abrir=new JButton("Abrir documento");botones.add(guardar);botones.add(abrir);abajo.add(botones,BorderLayout.SOUTH);add(abajo,BorderLayout.SOUTH);
            guardar.addActionListener(e->observar());abrir.addActionListener(e->abrir());
        } else {
            add(lista,BorderLayout.CENTER);
            JPanel abajo=new JPanel(new BorderLayout(6,6));
            JPanel carga=new JPanel(new FlowLayout(FlowLayout.LEFT));tipo.setEditable(true);carga.add(new JLabel("Tipo:"));carga.add(tipo);
            JButton seleccionar=new JButton("Seleccionar archivo");JButton guardar=new JButton("Registrar documento");carga.add(seleccionar);carga.add(archivo);carga.add(guardar);abajo.add(carga,BorderLayout.NORTH);
            JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton abrir=new JButton("Abrir documento");JButton verificar=new JButton("Marcar documento verificado");JButton validar=new JButton("Validar datos del cliente");acciones.add(abrir);acciones.add(verificar);acciones.add(validar);abajo.add(acciones,BorderLayout.SOUTH);add(abajo,BorderLayout.SOUTH);
            seleccionar.addActionListener(e->{Path p=DocumentacionUI.seleccionar(this);if(p!=null){origen=p;archivo.setText(p.getFileName().toString());archivo.setToolTipText(p.toString());}});
            guardar.addActionListener(e->registrar());abrir.addActionListener(e->abrir());verificar.addActionListener(e->verificar());validar.addActionListener(e->validarDatos());
        }
        solicitudes.addActionListener(e->{if(!cargando){origen=null;archivo.setText("Ningun archivo seleccionado");observacion.setText("");mostrarSolicitud();}});
        documentos.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting() && observaciones)cargarHistorial();});
        actualizar.addActionListener(e->cargar());cargar();
    }

    public final void cargar() {
        var actual=(OficialDocumentacionDAO.Solicitud)solicitudes.getSelectedItem();
        try {
            cargando=true;solicitudes.removeAllItems();
            for(var s:dao.solicitudes())solicitudes.addItem(s);
            if(actual!=null)for(int i=0;i<solicitudes.getItemCount();i++)if(solicitudes.getItemAt(i).id()==actual.id())solicitudes.setSelectedIndex(i);
            mostrarSolicitud();
        } catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);} finally{cargando=false;}
    }
    private OficialDocumentacionDAO.Solicitud solicitud() {
        var s=(OficialDocumentacionDAO.Solicitud)solicitudes.getSelectedItem();
        if(s==null)throw new IllegalArgumentException("Selecciona una solicitud.");return s;
    }
    private void mostrarSolicitud() {
        if(solicitudes.getSelectedItem()==null){datos.setText("No hay solicitudes registradas.");documentos.setModel(new javax.swing.table.DefaultTableModel());return;}
        try {
            var s=solicitud();datos.setText("Cliente: "+s.cliente()+" | CI: "+s.ci()+"\nTrabajo: "+(s.trabajo()==null||s.trabajo().isBlank()?"PENDIENTE DE COMPLETAR":s.trabajo())+" | Ingresos mensuales: "+s.ingresos()+" | Estado de la solicitud: "+s.estado());
            revision.setText(dao.ultimaValidacionDatos(s.id()));TablaUtil.cargarAutorizada(documentos,"SOLICITUD_REGISTRAR",OficialDocumentacionDAO.SQL_DOCUMENTOS,s.id());
            if(observaciones)cargarHistorial();
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
    private int documento() {
        int fila=documentos.getSelectedRow();if(fila<0)throw new IllegalArgumentException("Selecciona un documento de la tabla.");
        return ((Number)DocumentacionUI.celda(documentos,fila,"Documento")).intValue();
    }
    private void cargarHistorial() {
        try {TablaUtil.cargarAutorizada(historial,"SOLICITUD_REGISTRAR",OficialDocumentacionDAO.SQL_OBSERVACIONES,documentos.getSelectedRow()<0?-1:documento());}
        catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
    private void registrar() {
        String copia=null;
        try {
            int id=solicitud().id();if(origen==null)throw new IllegalArgumentException("Selecciona el documento que deseas adjuntar.");
            copia=ArchivoDocumental.copiar(origen,"solicitudes");
            int documento=dao.registrarDocumento(id,login,String.valueOf(tipo.getSelectedItem()),copia);
            copia=null;origen=null;archivo.setText("Ningun archivo seleccionado");mostrarSolicitud();
            JOptionPane.showMessageDialog(this,"Documento "+documento+" registrado. Su verificacion queda PENDIENTE.");
        }catch(Exception e){DocumentacionUI.limpiarCopia(this,copia);DocumentacionUI.error(this,e);}
    }
    private void abrir() {
        try{documento();DocumentacionUI.abrir(this,String.valueOf(DocumentacionUI.celda(documentos,documentos.getSelectedRow(),"Archivo")));}
        catch(IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
    private void verificar() {
        try {
            int id=documento();
            if(JOptionPane.showConfirmDialog(this,"Confirma que revisaste el contenido y los datos de este documento.","Verificar documento",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.verificarDocumento(id,login);mostrarSolicitud();
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
    private void validarDatos() {
        try {
            if(JOptionPane.showConfirmDialog(this,"Confirma que revisaste los datos del cliente seleccionado.","Validar datos",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.validarDatos(solicitud().id(),login);mostrarSolicitud();
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
    private void observar() {
        try {
            dao.observarDocumento(documento(),login,observacion.getText());observacion.setText("");
            int id=documento();mostrarSolicitud();
            for(int f=0;f<documentos.getRowCount();f++)if(((Number)DocumentacionUI.celda(documentos,f,"Documento")).intValue()==id)documentos.setRowSelectionInterval(f,f);
            cargarHistorial();JOptionPane.showMessageDialog(this,"Observacion registrada. El documento queda OBSERVADO.");
        }catch(SQLException | IllegalArgumentException e){DocumentacionUI.error(this,e);}
    }
}
