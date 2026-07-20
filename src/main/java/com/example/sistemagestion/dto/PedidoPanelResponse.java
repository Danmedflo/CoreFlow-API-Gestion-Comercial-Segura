package com.example.sistemagestion.dto;

public class PedidoPanelResponse {

    private long totalPedidos;
    private long pedidosPendientes;
    private long pedidosEnProceso;
    private long pedidosEntregados;
    private long pedidosCancelados;
    private double montoTotalVendido;
    private double montoPendiente;

    public PedidoPanelResponse() {
    }

    public PedidoPanelResponse(
            long totalPedidos,
            long pedidosPendientes,
            long pedidosEnProceso,
            long pedidosEntregados,
            long pedidosCancelados,
            double montoTotalVendido,
            double montoPendiente
    ) {
        this.totalPedidos = totalPedidos;
        this.pedidosPendientes = pedidosPendientes;
        this.pedidosEnProceso = pedidosEnProceso;
        this.pedidosEntregados = pedidosEntregados;
        this.pedidosCancelados = pedidosCancelados;
        this.montoTotalVendido = montoTotalVendido;
        this.montoPendiente = montoPendiente;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public long getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(long pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public long getPedidosEnProceso() {
        return pedidosEnProceso;
    }

    public void setPedidosEnProceso(long pedidosEnProceso) {
        this.pedidosEnProceso = pedidosEnProceso;
    }

    public long getPedidosEntregados() {
        return pedidosEntregados;
    }

    public void setPedidosEntregados(long pedidosEntregados) {
        this.pedidosEntregados = pedidosEntregados;
    }

    public long getPedidosCancelados() {
        return pedidosCancelados;
    }

    public void setPedidosCancelados(long pedidosCancelados) {
        this.pedidosCancelados = pedidosCancelados;
    }

    public double getMontoTotalVendido() {
        return montoTotalVendido;
    }

    public void setMontoTotalVendido(double montoTotalVendido) {
        this.montoTotalVendido = montoTotalVendido;
    }

    public double getMontoPendiente() {
        return montoPendiente;
    }

    public void setMontoPendiente(double montoPendiente) {
        this.montoPendiente = montoPendiente;
    }
}