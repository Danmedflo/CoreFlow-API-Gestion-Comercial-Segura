package com.example.sistemagestion.dto;

public class ActualizarEstadoPedidoRequest {

    private String estado;

    public ActualizarEstadoPedidoRequest() {
    }

    public ActualizarEstadoPedidoRequest(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}