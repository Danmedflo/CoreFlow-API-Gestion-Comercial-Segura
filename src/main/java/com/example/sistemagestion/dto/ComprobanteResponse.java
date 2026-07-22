package com.example.sistemagestion.dto;

import com.example.sistemagestion.model.Comprobante;
import com.example.sistemagestion.model.Pedido;

public class ComprobanteResponse {

    private Comprobante comprobante;
    private Pedido pedido;

    public ComprobanteResponse() {
    }

    public ComprobanteResponse(Comprobante comprobante, Pedido pedido) {
        this.comprobante = comprobante;
        this.pedido = pedido;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}