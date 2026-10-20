package com.mycompany.inicio;
 
import java.math.BigDecimal;
 
/** Un producto de credito (tabla producto_credito): Consumo, Vivienda, etc. */
public class Producto {
    private int id;
    private String nombre;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private int plazoMaximo;          // en meses
    private boolean exigeGarantia;
 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(BigDecimal montoMinimo) { this.montoMinimo = montoMinimo; }
    public BigDecimal getMontoMaximo() { return montoMaximo; }
    public void setMontoMaximo(BigDecimal montoMaximo) { this.montoMaximo = montoMaximo; }
    public int getPlazoMaximo() { return plazoMaximo; }
    public void setPlazoMaximo(int plazoMaximo) { this.plazoMaximo = plazoMaximo; }
    public boolean isExigeGarantia() { return exigeGarantia; }
    public void setExigeGarantia(boolean exigeGarantia) { this.exigeGarantia = exigeGarantia; }
}
 