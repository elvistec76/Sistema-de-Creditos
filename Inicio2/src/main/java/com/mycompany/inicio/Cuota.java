package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cuota {
    private int id;
    private int cronogramaId;
    private int numero;
    private LocalDate fechaVencimiento;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal montoTotal;
    private String estado = "PENDIENTE"; // PENDIENTE, PAGADA, VENCIDA

    public Cuota() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCronogramaId() { return cronogramaId; }
    public void setCronogramaId(int cronogramaId) { this.cronogramaId = cronogramaId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public BigDecimal getCapital() { return capital; }
    public void setCapital(BigDecimal capital) { this.capital = capital; }
    public BigDecimal getInteres() { return interes; }
    public void setInteres(BigDecimal interes) { this.interes = interes; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}