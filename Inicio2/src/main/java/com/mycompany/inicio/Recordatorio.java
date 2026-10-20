package com.mycompany.inicio;

import java.time.LocalDateTime;

public class Recordatorio {
    private int id;
    private int clienteId;
    private Integer cuotaId; // puede ser null (ej. notificacion de resultado de solicitud)
    private String tipo; // PAGO_PROXIMO, RESULTADO_SOLICITUD, MORA
    private String mensaje;
    private LocalDateTime fechaEnvio;
    private boolean enviado;

    public Recordatorio() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public Integer getCuotaId() { return cuotaId; }
    public void setCuotaId(Integer cuotaId) { this.cuotaId = cuotaId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public boolean isEnviado() { return enviado; }
    public void setEnviado(boolean enviado) { this.enviado = enviado; }
}