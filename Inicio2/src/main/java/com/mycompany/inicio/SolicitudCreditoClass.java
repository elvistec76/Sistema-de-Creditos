package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SolicitudCreditoClass {
    private int id;
    private int clienteId;
    private BigDecimal monto;
    private int plazoMeses;
    private String estado = "PENDIENTE"; // PENDIENTE, EN_EVALUACION, APROBADA, RECHAZADA
    private LocalDateTime fecha;

    public SolicitudCreditoClass() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}