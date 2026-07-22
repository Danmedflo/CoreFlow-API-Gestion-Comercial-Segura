package com.example.sistemagestion.controller;

import com.example.sistemagestion.dto.ConfirmarPagoRequest;
import com.example.sistemagestion.dto.PagoResponse;
import com.example.sistemagestion.model.Comprobante;
import com.example.sistemagestion.model.Pago;
import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.repository.ComprobanteRepository;
import com.example.sistemagestion.repository.PagoRepository;
import com.example.sistemagestion.repository.PedidoRepository;
import com.example.sistemagestion.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "http://localhost:4200")
public class PagoController {

    private static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";
    private static final double IGV_RATE = 0.18;

    private final PagoRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;
    private final JwtUtil jwtUtil;

    public PagoController(
            PagoRepository pagoRepository,
            ComprobanteRepository comprobanteRepository,
            PedidoRepository pedidoRepository,
            JwtUtil jwtUtil
    ) {
        this.pagoRepository = pagoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.pedidoRepository = pedidoRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listarPagos() {
        return ResponseEntity.ok(pagoRepository.findAll());
    }

    @GetMapping("/mis-pagos")
    public ResponseEntity<?> listarMisPagos(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
        }

        return ResponseEntity.ok(pagoRepository.findByClienteIgnoreCase(usuarioActual));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> obtenerPagoPorPedido(
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
                    .body(Map.of("mensaje", "No tienes permiso para consultar este pago"));
        }

        Optional<Pago> pago = pagoRepository.findByPedidoId(pedidoId);

        if (pago.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "El pedido todavía no tiene pago registrado"));
        }

        return ResponseEntity.ok(pago.get());
    }

    @Transactional
    @PostMapping("/confirmar/{pedidoId}")
    public ResponseEntity<?> confirmarPago(
            @PathVariable Long pedidoId,
            @RequestBody ConfirmarPagoRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);

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

        if (!pedido.getCliente().equalsIgnoreCase(usuarioActual)) {
            return ResponseEntity.status(403)
                    .body(Map.of("mensaje", "Solo el cliente del pedido puede confirmar este pago"));
        }

        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "No se puede pagar un pedido cancelado"));
        }

        if (pagoRepository.existsByPedidoId(pedidoId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Este pedido ya tiene un pago confirmado"));
        }

        String errorValidacion = validarRequestPago(request);

        if (errorValidacion != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", errorValidacion));
        }

        String metodoPago = request.getMetodoPago().trim().toUpperCase(Locale.ROOT);
        String tipoComprobante = request.getTipoComprobante().trim().toUpperCase(Locale.ROOT);

        Pago pago = new Pago(
                pedido.getId(),
                pedido.getCliente(),
                metodoPago,
                pedido.getTotal(),
                ESTADO_PAGO_CONFIRMADO,
                LocalDate.now().toString()
        );

        Pago pagoGuardado = pagoRepository.save(pago);

        double subtotal = redondear(pedido.getTotal() / (1 + IGV_RATE));
        double igv = redondear(pedido.getTotal() - subtotal);

        Comprobante comprobante = new Comprobante(
                pedido.getId(),
                pagoGuardado.getId(),
                tipoComprobante,
                generarNumeroComprobante(tipoComprobante, pagoGuardado.getId()),
                LocalDate.now().toString(),
                pedido.getCliente(),
                limpiarTexto(request.getDocumentoCliente()),
                limpiarTexto(request.getRazonSocial()),
                limpiarTexto(request.getRuc()),
                limpiarTexto(request.getDireccionFiscal()),
                subtotal,
                igv,
                pedido.getTotal()
        );

        Comprobante comprobanteGuardado = comprobanteRepository.save(comprobante);

        return ResponseEntity.ok(new PagoResponse(
                "Pago confirmado y comprobante generado correctamente",
                pagoGuardado,
                comprobanteGuardado
        ));
    }

    private String validarRequestPago(ConfirmarPagoRequest request) {
        if (request == null) {
            return "Los datos del pago son obligatorios";
        }

        if (request.getMetodoPago() == null || request.getMetodoPago().trim().isBlank()) {
            return "Selecciona un método de pago";
        }

        if (request.getTipoComprobante() == null || request.getTipoComprobante().trim().isBlank()) {
            return "Selecciona el tipo de comprobante";
        }

        String tipo = request.getTipoComprobante().trim().toUpperCase(Locale.ROOT);

        if (!"BOLETA".equals(tipo) && !"FACTURA".equals(tipo)) {
            return "El tipo de comprobante debe ser BOLETA o FACTURA";
        }

        if ("FACTURA".equals(tipo)) {
            if (request.getRazonSocial() == null || request.getRazonSocial().trim().isBlank()) {
                return "La razón social es obligatoria para factura";
            }

            if (request.getRuc() == null || request.getRuc().trim().isBlank()) {
                return "El RUC es obligatorio para factura";
            }

            if (request.getRuc().trim().length() != 11) {
                return "El RUC debe tener 11 dígitos";
            }
        }

        return null;
    }

    private String generarNumeroComprobante(String tipoComprobante, Long pagoId) {
        String serie = "FACTURA".equalsIgnoreCase(tipoComprobante) ? "F001" : "B001";
        return serie + "-" + String.format("%08d", pagoId);
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.trim();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
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