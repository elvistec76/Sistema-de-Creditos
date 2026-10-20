package com.mycompany.inicio;

public class ParametroSistema {
    private int id;
    private String clave; // ej. TASA_INTERES, DIAS_GRACIA, MONTO_MAXIMO
    private String valor;
    private String descripcion;

    public ParametroSistema() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}