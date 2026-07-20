package com.example.sistemagestion.dto;

import java.util.List;

public class CheckoutPedidoRequest {

    private List<ItemCarritoRequest> items;

    public CheckoutPedidoRequest() {
    }

    public CheckoutPedidoRequest(List<ItemCarritoRequest> items) {
        this.items = items;
    }

    public List<ItemCarritoRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemCarritoRequest> items) {
        this.items = items;
    }
}