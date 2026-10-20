package com.mycompany.inicio;

import java.time.LocalDate;

public class Documento {
    private int id;
    private int solicitudId;
    private String tipo; // CEDULA, COMPROBANTE_INGRESOS, ESTADO_CUENTA
    private String rutaArchivo;
    private boolean validado;
    private String observaciones;
    private LocalDate fechaCarga;

    public Documento() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSolicitudId() { return solicitudId; }
    public void setSolicitudId(int solicitudId) { this.solicitudId = solicitudId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public boolean isValidado() { return validado; }
    public void setValidado(boolean validado) { this.validado = validado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDate getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDate fechaCarga) { this.fechaCarga = fechaCarga; }
}