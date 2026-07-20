package com.example.sistemagestion.controller;

import com.example.sistemagestion.dto.CheckoutPedidoRequest;
import com.example.sistemagestion.dto.ItemCarritoRequest;
import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.model.PedidoDetalle;
import com.example.sistemagestion.model.Producto;
import com.example.sistemagestion.repository.PedidoRepository;
import com.example.sistemagestion.repository.ProductoRepository;
import com.example.sistemagestion.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final JwtUtil jwtUtil;

    public PedidoController(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            JwtUtil jwtUtil
    ) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> listarMisPedidos() {
        String usuarioActual = obtenerUsuarioActual();

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Usuario no autenticado"));
        }

        List<Pedido> pedidos = pedidoRepository.findByClienteIgnoreCase(usuarioActual);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoRepository.findById(id);

        if (pedido.isPresent()) {
            return ResponseEntity.ok(pedido.get());
        }

        return ResponseEntity.status(404)
                .body(Map.of("mensaje", "Pedido no encontrado"));
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody Pedido pedido) {
        if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
            pedido.setEstado("PENDIENTE");
        }

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return ResponseEntity.ok(pedidoGuardado);
    }

    @Transactional
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestBody CheckoutPedidoRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioActualDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El carrito está vacío"));
        }

        Map<Long, Integer> cantidadesPorProducto = new LinkedHashMap<>();

        for (ItemCarritoRequest item : request.getItems()) {
            if (item.getProductoId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "Todos los productos del carrito deben tener identificador"));
            }

            if (item.getCantidad() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "La cantidad de cada producto debe ser mayor a cero"));
            }

            cantidadesPorProducto.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(usuarioActual);
        pedido.setFecha(LocalDate.now().toString());
        pedido.setEstado("PENDIENTE");

        List<Producto> productosActualizados = new ArrayList<>();
        double totalPedido = 0;

        for (Map.Entry<Long, Integer> entry : cantidadesPorProducto.entrySet()) {
            Long productoId = entry.getKey();
            int cantidadSolicitada = entry.getValue();

            Optional<Producto> productoOptional = productoRepository.findById(productoId);

            if (productoOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("mensaje", "Producto no encontrado en el carrito"));
            }

            Producto producto = productoOptional.get();

            if (producto.getStock() < cantidadSolicitada) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "mensaje",
                                "Stock insuficiente para el producto: " + producto.getNombre()
                        ));
            }

            double subtotal = producto.getPrecio() * cantidadSolicitada;

            PedidoDetalle detalle = new PedidoDetalle(
                    producto.getId(),
                    producto.getNombre(),
                    cantidadSolicitada,
                    producto.getPrecio(),
                    subtotal
            );

            pedido.agregarDetalle(detalle);
            totalPedido += subtotal;

            producto.setStock(producto.getStock() - cantidadSolicitada);
            productosActualizados.add(producto);
        }

        pedido.setTotal(totalPedido);

        productoRepository.saveAll(productosActualizados);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return ResponseEntity.ok(pedidoGuardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Pedido pedidoActualizado) {
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(id);

        if (pedidoOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        }

        Pedido pedido = pedidoOptional.get();
        pedido.setCliente(pedidoActualizado.getCliente());
        pedido.setFecha(pedidoActualizado.getFecha());
        pedido.setTotal(pedidoActualizado.getTotal());
        pedido.setEstado(pedidoActualizado.getEstado());

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return ResponseEntity.ok(pedidoGuardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        }

        pedidoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Pedido eliminado correctamente"));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Pedido>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(pedidoRepository.findByEstadoIgnoreCase(estado));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Pedido>> buscarPorCliente(@RequestParam String cliente) {
        return ResponseEntity.ok(pedidoRepository.findByClienteContainingIgnoreCase(cliente));
    }

    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();

        if (username == null || username.equals("anonymousUser")) {
            return null;
        }

        return username;
    }

    private String obtenerUsuarioActualDesdeToken(String authHeader) {
        String usuarioDesdeContexto = obtenerUsuarioActual();

        if (usuarioDesdeContexto != null) {
            return usuarioDesdeContexto;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        return jwtUtil.extractUsername(token);
    }
}