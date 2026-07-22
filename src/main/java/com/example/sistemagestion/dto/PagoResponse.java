package com.example.sistemagestion.dto;

import com.example.sistemagestion.model.Comprobante;
import com.example.sistemagestion.model.Pago;

public class PagoResponse {

    private String mensaje;
    private Pago pago;
    private Comprobante comprobante;

    public PagoResponse() {
    }

    public PagoResponse(String mensaje, Pago pago, Comprobante comprobante) {
        this.mensaje = mensaje;
        this.pago = pago;
        this.comprobante = comprobante;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }
}