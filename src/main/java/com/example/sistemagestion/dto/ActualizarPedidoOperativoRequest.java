package com.example.sistemagestion.dto;

import java.util.List;

public class ActualizarPedidoOperativoRequest {

    private List<ItemCarritoRequest> items;

    public ActualizarPedidoOperativoRequest() {
    }

    public ActualizarPedidoOperativoRequest(List<ItemCarritoRequest> items) {
        this.items = items;
    }

    public List<ItemCarritoRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemCarritoRequest> items) {
        this.items = items;
    }
}