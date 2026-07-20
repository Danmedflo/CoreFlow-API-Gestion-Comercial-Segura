package com.example.sistemagestion.dto;

public class ProductoPanelResponse {

    private long totalProductos;
    private long stockTotal;
    private long productosStockCritico;
    private long productosSinStock;
    private double valorInventario;

    public ProductoPanelResponse() {
    }

    public ProductoPanelResponse(
            long totalProductos,
            long stockTotal,
            long productosStockCritico,
            long productosSinStock,
            double valorInventario
    ) {
        this.totalProductos = totalProductos;
        this.stockTotal = stockTotal;
        this.productosStockCritico = productosStockCritico;
        this.productosSinStock = productosSinStock;
        this.valorInventario = valorInventario;
    }

    public long getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(long totalProductos) {
        this.totalProductos = totalProductos;
    }

    public long getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(long stockTotal) {
        this.stockTotal = stockTotal;
    }

    public long getProductosStockCritico() {
        return productosStockCritico;
    }

    public void setProductosStockCritico(long productosStockCritico) {
        this.productosStockCritico = productosStockCritico;
    }

    public long getProductosSinStock() {
        return productosSinStock;
    }

    public void setProductosSinStock(long productosSinStock) {
        this.productosSinStock = productosSinStock;
    }

    public double getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(double valorInventario) {
        this.valorInventario = valorInventario;
    }
}