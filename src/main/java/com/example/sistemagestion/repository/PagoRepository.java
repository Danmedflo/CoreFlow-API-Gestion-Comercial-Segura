package com.example.sistemagestion.repository;

import com.example.sistemagestion.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    boolean existsByPedidoId(Long pedidoId);

    List<Pago> findByClienteIgnoreCase(String cliente);

    List<Pago> findByEstadoPagoIgnoreCase(String estadoPago);
}