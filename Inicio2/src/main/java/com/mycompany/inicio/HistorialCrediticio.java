package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HistorialCrediticio {
    private int id;
    private int clienteId;
    private String entidad;
    private BigDecimal montoOtorgado;
    private String estado; // AL_DIA, EN_MORA, CANCELADO
    private LocalDate fechaConsulta;

    public HistorialCrediticio() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public BigDecimal getMontoOtorgado() { return montoOtorgado; }
    public void setMontoOtorgado(BigDecimal montoOtorgado) { this.montoOtorgado = montoOtorgado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDate fechaConsulta) { this.fechaConsulta = fechaConsulta; }
}