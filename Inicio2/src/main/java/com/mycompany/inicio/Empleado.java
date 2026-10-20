package com.mycompany.inicio;

public class Empleado {
    private int id;
    private String nombre;
    private String cargo; // ANALISTA, CAJERO, OFICIAL, GERENTE, ADMINISTRADOR, GESTOR
    private int usuarioId; // referencia a Usuario

    public Empleado() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
}