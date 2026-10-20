package com.mycompany.inicio;

import java.math.BigDecimal;

public class Garantia {
    private int id;
    private int solicitudId;
    private String tipo; // HIPOTECARIA, PRENDARIA, PERSONAL
    private String descripcion;
    private BigDecimal valorEstimado;

    public Garantia() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSolicitudId() { return solicitudId; }
    public void setSolicitudId(int solicitudId) { this.solicitudId = solicitudId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(BigDecimal valorEstimado) { this.valorEstimado = valorEstimado; }
}