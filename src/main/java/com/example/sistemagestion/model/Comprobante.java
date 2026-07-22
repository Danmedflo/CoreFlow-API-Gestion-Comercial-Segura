package com.example.sistemagestion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "comprobantes")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;
    private Long pagoId;

    private String tipoComprobante;
    private String numeroComprobante;
    private String fechaEmision;

    private String cliente;
    private String documentoCliente;

    private String razonSocial;
    private String ruc;
    private String direccionFiscal;

    private double subtotal;
    private double igv;
    private double total;

    public Comprobante() {
    }

    public Comprobante(
            Long pedidoId,
            Long pagoId,
            String tipoComprobante,
            String numeroComprobante,
            String fechaEmision,
            String cliente,
            String documentoCliente,
            String razonSocial,
            String ruc,
            String direccionFiscal,
            double subtotal,
            double igv,
            double total
    ) {
        this.pedidoId = pedidoId;
        this.pagoId = pagoId;
        this.tipoComprobante = tipoComprobante;
        this.numeroComprobante = numeroComprobante;
        this.fechaEmision = fechaEmision;
        this.cliente = cliente;
        this.documentoCliente = documentoCliente;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.direccionFiscal = direccionFiscal;
        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDireccionFiscal() {
        return direccionFiscal;
    }

    public void setDireccionFiscal(String direccionFiscal) {
        this.direccionFiscal = direccionFiscal;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getIgv() {
        return igv;
    }

    public void setIgv(double igv) {
        this.igv = igv;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}