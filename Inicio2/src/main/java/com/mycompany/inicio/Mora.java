package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Mora {
    private int id;
    private int cuotaId;
    private int diasAtraso;
    private BigDecimal montoMora;
    private LocalDate fechaDeteccion;
    private String estado = "ACTIVA"; // ACTIVA, REGULARIZADA

    public Mora() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCuotaId() { return cuotaId; }
    public void setCuotaId(int cuotaId) { this.cuotaId = cuotaId; }
    public int getDiasAtraso() { return diasAtraso; }
    public void setDiasAtraso(int diasAtraso) { this.diasAtraso = diasAtraso; }
    public BigDecimal getMontoMora() { return montoMora; }
    public void setMontoMora(BigDecimal montoMora) { this.montoMora = montoMora; }
    public LocalDate getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDate fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}