package com.example.sistemagestion.dto;

public class ComprarPedidoRequest {

    private String cliente;
    private Long productoId;
    private int cantidad;

    public ComprarPedidoRequest() {
    }

    public ComprarPedidoRequest(String cliente, Long productoId, int cantidad) {
        this.cliente = cliente;
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}