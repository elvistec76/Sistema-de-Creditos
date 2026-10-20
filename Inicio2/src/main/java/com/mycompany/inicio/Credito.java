package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Credito {
    private int id;
    private int solicitudId;
    private BigDecimal montoAprobado;
    private BigDecimal tasaInteres;
    private int plazoMeses;
    private String estado = "APROBADO"; // APROBADO, DESEMBOLSADO, VIGENTE, EN_MORA, CANCELADO
    private LocalDate fechaDesembolso;

    public Credito() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSolicitudId() { return solicitudId; }
    public void setSolicitudId(int solicitudId) { this.solicitudId = solicitudId; }
    public BigDecimal getMontoAprobado() { return montoAprobado; }
    public void setMontoAprobado(BigDecimal montoAprobado) { this.montoAprobado = montoAprobado; }
    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal tasaInteres) { this.tasaInteres = tasaInteres; }
    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(LocalDate fechaDesembolso) { this.fechaDesembolso = fechaDesembolso; }
}