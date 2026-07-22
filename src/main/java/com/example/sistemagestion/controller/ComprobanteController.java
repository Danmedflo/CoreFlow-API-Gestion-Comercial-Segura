package com.example.sistemagestion.controller;

import com.example.sistemagestion.dto.ComprobanteResponse;
import com.example.sistemagestion.model.Comprobante;
import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.repository.ComprobanteRepository;
import com.example.sistemagestion.repository.PedidoRepository;
import com.example.sistemagestion.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comprobantes")
@CrossOrigin(origins = "http://localhost:4200")
public class ComprobanteController {

    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;
    private final JwtUtil jwtUtil;

    public ComprobanteController(
            ComprobanteRepository comprobanteRepository,
            PedidoRepository pedidoRepository,
            JwtUtil jwtUtil
    ) {
        this.comprobanteRepository = comprobanteRepository;
        this.pedidoRepository = pedidoRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<Comprobante>> listarComprobantes() {
        return ResponseEntity.ok(comprobanteRepository.findAll());
    }

    @GetMapping("/mis-comprobantes")
    public ResponseEntity<?> listarMisComprobantes(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
        }

        return ResponseEntity.ok(comprobanteRepository.findByClienteIgnoreCase(usuarioActual));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);
        String rolActual = obtenerRolDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
        }

        Optional<Comprobante> comprobanteOptional = comprobanteRepository.findById(id);

        if (comprobanteOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Comprobante no encontrado"));
        }

        Comprobante comprobante = comprobanteOptional.get();

        if (!esAdmin(rolActual) && !comprobante.getCliente().equalsIgnoreCase(usuarioActual)) {
            return ResponseEntity.status(403)
                    .body(Map.of("mensaje", "No tienes permiso para ver este comprobante"));
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(comprobante.getPedidoId());

        return ResponseEntity.ok(new ComprobanteResponse(
                comprobante,
                pedidoOptional.orElse(null)
        ));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> obtenerPorPedido(
            @PathVariable Long pedidoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);
        String rolActual = obtenerRolDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);

        if (pedidoOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        }

        Pedido pedido = pedidoOptional.get();

        if (!esAdmin(rolActual) && !pedido.getCliente().equalsIgnoreCase(usuarioActual)) {
            return ResponseEntity.status(403)
                    .body(Map.of("mensaje", "No tienes permiso para ver este comprobante"));
        }

        Optional<Comprobante> comprobanteOptional = comprobanteRepository.findByPedidoId(pedidoId);

        if (comprobanteOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Este pedido todavía no tiene comprobante"));
        }

        return ResponseEntity.ok(new ComprobanteResponse(
                comprobanteOptional.get(),
                pedido
        ));
    }

    private String obtenerUsuarioDesdeToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        return jwtUtil.extractUsername(token);
    }

    private String obtenerRolDesdeToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        return jwtUtil.extractRol(token);
    }

    private boolean esAdmin(String rol) {
        if (rol == null) {
            return false;
        }

        return "ADMIN".equalsIgnoreCase(rol.replace("ROLE_", "").trim());
    }
}