package com.example.sistemagestion.repository;

import com.example.sistemagestion.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    Optional<Comprobante> findByPedidoId(Long pedidoId);

    Optional<Comprobante> findByPagoId(Long pagoId);

    List<Comprobante> findByClienteIgnoreCase(String cliente);

    boolean existsByPedidoId(Long pedidoId);
}