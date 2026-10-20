package com.mycompany.inicio;

public class CrearAdmin {
    public static void main(String[] args) throws Exception {
        javax.swing.JTextField usuario=new javax.swing.JTextField("admin");
        javax.swing.JPasswordField claveActual=new javax.swing.JPasswordField();
        int opcion=javax.swing.JOptionPane.showConfirmDialog(null,new Object[]{"Administrador:",usuario,"Clave actual:",claveActual,
                "Esta utilidad restablece las claves de demostracion de los trabajadores."},"Autorizacion de administrador",javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if(opcion!=javax.swing.JOptionPane.OK_OPTION)return;
        if(!"ADMIN".equals(Sesion.abrir(usuario.getText().trim(),new String(claveActual.getPassword())))){
            Sesion.cerrar();throw new IllegalArgumentException("Credenciales de administrador no validas.");
        }
        UsuarioDAO dao = new UsuarioDAO();

        dao.cambiarClave("admin", "admin123");
        dao.cambiarClave("oficial", "oficial123");
        dao.cambiarClave("analista", "analista123");
        dao.cambiarClave("jefe", "jefe123");
        dao.cambiarClave("cajero", "cajero123");
        dao.cambiarClave("gestor", "gestor123");
        dao.cambiarClave("gerente", "gerente123");

        System.out.println("Contraseñas de prueba actualizadas.");
        
       
    }
}