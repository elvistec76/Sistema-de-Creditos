package com.mycompany.inicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CronogramaPagos {
    private int id;
    private int creditoId;
    private LocalDate fechaGeneracion;
    private List<Cuota> cuotas = new ArrayList<>();

    public CronogramaPagos() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreditoId() { return creditoId; }
    public void setCreditoId(int creditoId) { this.creditoId = creditoId; }
    public LocalDate getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDate fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public List<Cuota> getCuotas() { return cuotas; }
    public void setCuotas(List<Cuota> cuotas) { this.cuotas = cuotas; }
}