package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private int id;
    private int cuotaId;
    private int empleadoId; // cajero que registra
    private BigDecimal montoPagado;
    private String metodo; // EFECTIVO, TRANSFERENCIA, TARJETA
    private LocalDateTime fechaPago;

    public Pago() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCuotaId() { return cuotaId; }
    public void setCuotaId(int cuotaId) { this.cuotaId = cuotaId; }
    public int getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(int empleadoId) { this.empleadoId = empleadoId; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}