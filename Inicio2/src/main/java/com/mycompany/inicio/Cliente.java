package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cliente {
    private String ci;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String lugarTrabajo;
    private BigDecimal ingresosMensuales;
    private LocalDateTime fechaRegistro;

    public Cliente() {}

    public String getCi() { return ci; }
    public void setCi(String ci) { this.ci = ci; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNombreCompleto() { return (nombres + " " + apellidos).trim(); }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getLugarTrabajo() { return lugarTrabajo; }
    public void setLugarTrabajo(String lugarTrabajo) { this.lugarTrabajo = lugarTrabajo; }

    public BigDecimal getIngresosMensuales() { return ingresosMensuales; }
    public void setIngresosMensuales(BigDecimal ingresosMensuales) { this.ingresosMensuales = ingresosMensuales; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}