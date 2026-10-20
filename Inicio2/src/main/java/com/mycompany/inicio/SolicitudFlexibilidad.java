package com.mycompany.inicio;

import java.time.LocalDate;

public class SolicitudFlexibilidad {
    private int id;
    private int cuotaId;
    private String tipo; // CAMBIO_FECHA, APLAZAMIENTO
    private LocalDate nuevaFecha;
    private String motivo;
    private String estado = "PENDIENTE"; // PENDIENTE, APROBADA, RECHAZADA
    private LocalDate fechaSolicitud;

    public SolicitudFlexibilidad() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCuotaId() { return cuotaId; }
    public void setCuotaId(int cuotaId) { this.cuotaId = cuotaId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDate getNuevaFecha() { return nuevaFecha; }
    public void setNuevaFecha(LocalDate nuevaFecha) { this.nuevaFecha = nuevaFecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}