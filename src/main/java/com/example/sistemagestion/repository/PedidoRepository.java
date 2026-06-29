package com.example.sistemagestion.repository;

import com.example.sistemagestion.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoIgnoreCase(String estado);

    List<Pedido> findByClienteContainingIgnoreCase(String cliente);

    List<Pedido> findByClienteIgnoreCase(String cliente);
}