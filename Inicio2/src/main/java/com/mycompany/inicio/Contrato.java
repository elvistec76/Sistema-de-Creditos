package com.mycompany.inicio;

import java.time.LocalDate;

public class Contrato {
    private int id;
    private int creditoId;
    private String numero;
    private LocalDate fechaGeneracion;
    private LocalDate fechaFirma;
    private String estado = "GENERADO"; // GENERADO, FORMALIZADO

    public Contrato() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreditoId() { return creditoId; }
    public void setCreditoId(int creditoId) { this.creditoId = creditoId; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDate getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDate fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public LocalDate getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(LocalDate fechaFirma) { this.fechaFirma = fechaFirma; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}