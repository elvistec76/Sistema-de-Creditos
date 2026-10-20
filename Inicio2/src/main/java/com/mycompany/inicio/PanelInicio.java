package com.mycompany.inicio;

import java.awt.*;
import javax.swing.*;

/** Portada adaptable con un acceso de clientes y otro de trabajadores. */
public class PanelInicio extends JPanel {
    private Runnable verClientes=()->{},registrarCliente=()->{},trabajadores=()->{};
    public PanelInicio(){
        setLayout(new BorderLayout());setPreferredSize(new Dimension(1050,660));
        JPanel contenido=new JPanel(new GridBagLayout());contenido.setBorder(BorderFactory.createEmptyBorder(35,45,35,45));
        GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;g.anchor=GridBagConstraints.WEST;
        JLabel curso=new JLabel("SISTEMA CREDITICIO");curso.setFont(curso.getFont().deriveFont(Font.BOLD,14f));g.gridy=0;g.insets=new Insets(0,0,24,0);contenido.add(curso,g);
        JLabel titulo=new JLabel("<html>Bienvenido al<br>sistema crediticio</html>");titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,34f));g.gridy++;g.insets=new Insets(0,0,16,0);contenido.add(titulo,g);
        JLabel ayuda=new JLabel("<html>Consulta tus solicitudes, creditos y cuotas.<br>Selecciona el acceso que necesitas para continuar.</html>");ayuda.setFont(ayuda.getFont().deriveFont(15f));g.gridy++;g.insets=new Insets(0,0,32,0);contenido.add(ayuda,g);
        JButton clientes=new JButton("Clientes  \u25be");clientes.setBackground(new Color(204,255,204));clientes.setFont(clientes.getFont().deriveFont(Font.BOLD,20f));clientes.setPreferredSize(new Dimension(380,64));
        JPopupMenu opciones=new JPopupMenu();JMenuItem ver=new JMenuItem("Ver clientes"),nuevo=new JMenuItem("Registrar nuevo cliente");
        ver.addActionListener(e->verClientes.run());nuevo.addActionListener(e->registrarCliente.run());opciones.add(ver);opciones.add(nuevo);
        clientes.addActionListener(e->{opciones.setPopupSize(clientes.getWidth(),80);opciones.show(clientes,0,clientes.getHeight());});
        g.gridy++;g.insets=new Insets(0,0,10,0);contenido.add(clientes,g);
        JLabel detalle=new JLabel("<html>Ver clientes: ingresa a tu cuenta para consultar tus datos.<br>Registrar nuevo cliente: crea tu acceso al sistema.</html>");g.gridy++;g.insets=new Insets(0,0,30,0);contenido.add(detalle,g);
        JButton sistema=new JButton("Sistema (trabajadores)");sistema.setFont(sistema.getFont().deriveFont(Font.BOLD,18f));sistema.setPreferredSize(new Dimension(380,58));sistema.addActionListener(e->trabajadores.run());
        g.gridy++;g.insets=new Insets(0,0,10,0);contenido.add(sistema,g);
        JLabel roles=new JLabel("Acceso del personal segun el rol asignado.");g.gridy++;g.insets=new Insets(0,0,0,0);contenido.add(roles,g);
        add(contenido,BorderLayout.CENTER);
        Image imagen=new ImageIcon(getClass().getResource("/com/imagen/te.png")).getImage();
        JPanel ilustracion=new JPanel(){@Override protected void paintComponent(Graphics original){
            super.paintComponent(original);Graphics2D dibujo=(Graphics2D)original.create();
            dibujo.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int iw=imagen.getWidth(this),ih=imagen.getHeight(this);if(iw>0&&ih>0){double escala=Math.max((double)getWidth()/iw,(double)getHeight()/ih);
                int w=(int)Math.ceil(iw*escala),h=(int)Math.ceil(ih*escala);dibujo.drawImage(imagen,(getWidth()-w)/2,getHeight()-h,w,h,this);}
            dibujo.dispose();
        }};
        ilustracion.setPreferredSize(new Dimension(370,660));add(ilustracion,BorderLayout.EAST);
    }
    public void acciones(Runnable ver,Runnable registrar,Runnable sistema){verClientes=ver;registrarCliente=registrar;trabajadores=sistema;}
}
