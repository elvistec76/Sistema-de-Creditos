package com.mycompany.inicio;

public class Usuario {
    private int id;
    private String usuario;
    private String contrasena;
    private String rol; // CLIENTE, ADMINISTRADOR, ANALISTA, CAJERO, OFICIAL, GERENTE, GESTOR

    public Usuario() {}

    public Usuario(int id, String usuario, String contrasena, String rol) {
        this.id = id;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
