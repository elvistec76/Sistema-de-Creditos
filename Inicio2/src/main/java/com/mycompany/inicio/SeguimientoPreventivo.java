package com.mycompany.inicio;

import java.time.LocalDateTime;

public class SeguimientoPreventivo {
    private int id;
    private int creditoId;
    private int empleadoId; // gestor de cobranza
    private String medioContacto; // LLAMADA, MENSAJE, VISITA
    private String resultado;
    private LocalDateTime fecha;

    public SeguimientoPreventivo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreditoId() { return creditoId; }
    public void setCreditoId(int creditoId) { this.creditoId = creditoId; }
    public int getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(int empleadoId) { this.empleadoId = empleadoId; }
    public String getMedioContacto() { return medioContacto; }
    public void setMedioContacto(String medioContacto) { this.medioContacto = medioContacto; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}