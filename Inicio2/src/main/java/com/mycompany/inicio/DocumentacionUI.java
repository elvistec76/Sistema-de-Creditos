package com.mycompany.inicio;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Ayudas de escritorio compartidas por los formularios documentales. */
final class DocumentacionUI {
    private DocumentacionUI() {}
    static Path seleccionar(Component padre) {
        JFileChooser selector=new JFileChooser();
        selector.setDialogTitle("Seleccionar respaldo (hasta 20 MB)");
        selector.setAcceptAllFileFilterUsed(false);
        selector.setFileFilter(new FileNameExtensionFilter("Documentos e imagenes", "pdf","png","jpg","jpeg","docx"));
        return selector.showOpenDialog(padre)==JFileChooser.APPROVE_OPTION ? selector.getSelectedFile().toPath() : null;
    }
    static void abrir(Component padre,String referencia) {
        try {
            if(!Desktop.isDesktopSupported()) throw new IOException("No se puede abrir un visor de documentos en este equipo.");
            Desktop.getDesktop().open(ArchivoDocumental.resolver(referencia).toFile());
        } catch(IOException | UnsupportedOperationException | SecurityException e) {error(padre,e);}
    }
    static void error(Component padre,Exception e) {
        JOptionPane.showMessageDialog(padre,e.getMessage(),"No se pudo completar la operacion",JOptionPane.WARNING_MESSAGE);
    }
    static void limpiarCopia(Component padre,String referencia) {
        if(referencia==null) return;
        try {ArchivoDocumental.descartarCopia(referencia);}
        catch(IOException e) {JOptionPane.showMessageDialog(padre,"El registro no se guardo. No se pudo retirar la copia local: "+e.getMessage());}
    }
    static Object celda(JTable tabla,int fila,String columna) {
        return tabla.getValueAt(fila,tabla.getColumnModel().getColumnIndex(columna));
    }
}
