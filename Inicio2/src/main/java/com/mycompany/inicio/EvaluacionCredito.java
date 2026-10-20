package com.mycompany.inicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EvaluacionCredito {
    private int id;
    private int solicitudId;
    private int empleadoId; // analista que evalua
    private BigDecimal ingresosMensuales;
    private BigDecimal gastosMensuales;
    private BigDecimal capacidadPago;
    private String nivelRiesgo; // BAJO, MEDIO, ALTO
    private String observaciones;
    private LocalDateTime fecha;

    public EvaluacionCredito() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSolicitudId() { return solicitudId; }
    public void setSolicitudId(int solicitudId) { this.solicitudId = solicitudId; }
    public int getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(int empleadoId) { this.empleadoId = empleadoId; }
    public BigDecimal getIngresosMensuales() { return ingresosMensuales; }
    public void setIngresosMensuales(BigDecimal ingresosMensuales) { this.ingresosMensuales = ingresosMensuales; }
    public BigDecimal getGastosMensuales() { return gastosMensuales; }
    public void setGastosMensuales(BigDecimal gastosMensuales) { this.gastosMensuales = gastosMensuales; }
    public BigDecimal getCapacidadPago() { return capacidadPago; }
    public void setCapacidadPago(BigDecimal capacidadPago) { this.capacidadPago = capacidadPago; }
    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}