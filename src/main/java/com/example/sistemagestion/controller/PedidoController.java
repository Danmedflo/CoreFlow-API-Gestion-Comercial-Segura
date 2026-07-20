package com.example.sistemagestion.controller;

import com.example.sistemagestion.dto.ActualizarEstadoPedidoRequest;
import com.example.sistemagestion.dto.ActualizarPedidoOperativoRequest;
import com.example.sistemagestion.dto.CheckoutPedidoRequest;
import com.example.sistemagestion.dto.ItemCarritoRequest;
import com.example.sistemagestion.dto.PedidoPanelResponse;
import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.model.PedidoDetalle;
import com.example.sistemagestion.model.Producto;
import com.example.sistemagestion.repository.PedidoRepository;
import com.example.sistemagestion.repository.ProductoRepository;
import com.example.sistemagestion.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
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

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_EN_PROCESO = "EN_PROCESO";
    private static final String ESTADO_ENTREGADO = "ENTREGADO";
    private static final String ESTADO_CANCELADO = "CANCELADO";

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

    @GetMapping("/panel/resumen")
    public ResponseEntity<PedidoPanelResponse> obtenerResumenPanel() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        long totalPedidos = pedidos.size();

        long pendientes = pedidos.stream()
                .filter(pedido -> ESTADO_PENDIENTE.equals(normalizarEstado(pedido.getEstado())))
                .count();

        long enProceso = pedidos.stream()
                .filter(pedido -> ESTADO_EN_PROCESO.equals(normalizarEstado(pedido.getEstado())))
                .count();

        long entregados = pedidos.stream()
                .filter(pedido -> ESTADO_ENTREGADO.equals(normalizarEstado(pedido.getEstado())))
                .count();

        long cancelados = pedidos.stream()
                .filter(pedido -> ESTADO_CANCELADO.equals(normalizarEstado(pedido.getEstado())))
                .count();

        double montoTotalVendido = pedidos.stream()
                .filter(pedido -> ESTADO_ENTREGADO.equals(normalizarEstado(pedido.getEstado())))
                .mapToDouble(Pedido::getTotal)
                .sum();

        double montoPendiente = pedidos.stream()
                .filter(pedido -> {
                    String estado = normalizarEstado(pedido.getEstado());
                    return ESTADO_PENDIENTE.equals(estado) || ESTADO_EN_PROCESO.equals(estado);
                })
                .mapToDouble(Pedido::getTotal)
                .sum();

        PedidoPanelResponse resumen = new PedidoPanelResponse(
                totalPedidos,
                pendientes,
                enProceso,
                entregados,
                cancelados,
                montoTotalVendido,
                montoPendiente
        );

        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> listarMisPedidos(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
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
            pedido.setEstado(ESTADO_PENDIENTE);
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
        String usuarioActual = obtenerUsuarioDesdeToken(authHeader);

        if (usuarioActual == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Token inválido o usuario no autenticado"));
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El carrito está vacío"));
        }

        Map<Long, Integer> cantidadesPorProducto = consolidarItems(request.getItems());

        if (cantidadesPorProducto.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El carrito no contiene productos válidos"));
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(usuarioActual);
        pedido.setFecha(LocalDate.now().toString());
        pedido.setEstado(ESTADO_PENDIENTE);

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
                        .body(Map.of("mensaje", "Stock insuficiente para el producto: " + producto.getNombre()));
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

    @Transactional
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoPedidoRequest request
    ) {
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(id);

        if (pedidoOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        }

        if (request == null || request.getEstado() == null || request.getEstado().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El nuevo estado del pedido es obligatorio"));
        }

        Pedido pedido = pedidoOptional.get();

        String estadoActual = normalizarEstado(pedido.getEstado());
        String nuevoEstado = normalizarEstado(request.getEstado());

        if (!esEstadoValido(nuevoEstado)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Estado no válido para el pedido"));
        }

        if (estadoActual.equals(nuevoEstado)) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "El pedido ya se encuentra en ese estado",
                    "pedido", pedido
            ));
        }

        if (esEstadoFinal(estadoActual)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "No se puede modificar un pedido " + estadoActual));
        }

        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Transición de estado no permitida: " + estadoActual + " → " + nuevoEstado));
        }

        if (ESTADO_CANCELADO.equals(nuevoEstado)) {
            devolverStockAlInventario(pedido);
        }

        pedido.setEstado(nuevoEstado);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Estado del pedido actualizado correctamente",
                "pedido", pedidoGuardado
        ));
    }

    @Transactional
    @PutMapping("/{id}/operativo")
    public ResponseEntity<?> actualizarPedidoOperativo(
            @PathVariable Long id,
            @RequestBody ActualizarPedidoOperativoRequest request
    ) {
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(id);

        if (pedidoOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        }

        Pedido pedido = pedidoOptional.get();
        String estadoActual = normalizarEstado(pedido.getEstado());

        if (esEstadoFinal(estadoActual)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "No se puede editar el contenido de un pedido " + estadoActual));
        }

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El pedido debe tener al menos un producto"));
        }

        Map<Long, Integer> nuevasCantidades = consolidarItems(request.getItems());

        if (nuevasCantidades.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El pedido debe tener productos válidos"));
        }

        Map<Long, Producto> productosSolicitados = new LinkedHashMap<>();

        for (Long productoId : nuevasCantidades.keySet()) {
            Optional<Producto> productoOptional = productoRepository.findById(productoId);

            if (productoOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("mensaje", "Producto no encontrado: " + productoId));
            }

            productosSolicitados.put(productoId, productoOptional.get());
        }

        Map<Long, Integer> stockDisponible = new LinkedHashMap<>();

        for (Map.Entry<Long, Producto> entry : productosSolicitados.entrySet()) {
            stockDisponible.put(entry.getKey(), Math.max(entry.getValue().getStock(), 0));
        }

        if (pedido.getDetalles() != null) {
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                Long productoId = detalle.getProductoId();

                if (productoId != null && stockDisponible.containsKey(productoId)) {
                    stockDisponible.put(
                            productoId,
                            stockDisponible.get(productoId) + detalle.getCantidad()
                    );
                }
            }
        }

        for (Map.Entry<Long, Integer> entry : nuevasCantidades.entrySet()) {
            Long productoId = entry.getKey();
            int cantidadSolicitada = entry.getValue();
            int stockActualDisponible = stockDisponible.getOrDefault(productoId, 0);

            if (cantidadSolicitada > stockActualDisponible) {
                Producto producto = productosSolicitados.get(productoId);

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "mensaje",
                                "Stock insuficiente para " + producto.getNombre() +
                                        ". Disponible: " + stockActualDisponible
                        ));
            }
        }

        List<Producto> productosActualizados = new ArrayList<>();

        if (pedido.getDetalles() != null) {
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                if (detalle.getProductoId() == null) {
                    continue;
                }

                Optional<Producto> productoOptional = productoRepository.findById(detalle.getProductoId());

                if (productoOptional.isPresent()) {
                    Producto producto = productoOptional.get();
                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productosActualizados.add(producto);
                }
            }
        }

        pedido.limpiarDetalles();

        double nuevoTotal = 0;

        for (Map.Entry<Long, Integer> entry : nuevasCantidades.entrySet()) {
            Long productoId = entry.getKey();
            int nuevaCantidad = entry.getValue();

            Producto producto = productosSolicitados.get(productoId);

            producto.setStock(producto.getStock() - nuevaCantidad);

            double subtotal = producto.getPrecio() * nuevaCantidad;

            PedidoDetalle nuevoDetalle = new PedidoDetalle(
                    producto.getId(),
                    producto.getNombre(),
                    nuevaCantidad,
                    producto.getPrecio(),
                    subtotal
            );

            pedido.agregarDetalle(nuevoDetalle);
            nuevoTotal += subtotal;

            if (!productosActualizados.contains(producto)) {
                productosActualizados.add(producto);
            }
        }

        pedido.setTotal(nuevoTotal);

        productoRepository.saveAll(productosActualizados);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Contenido del pedido actualizado correctamente",
                "pedido", pedidoGuardado
        ));
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

    private Map<Long, Integer> consolidarItems(List<ItemCarritoRequest> items) {
        Map<Long, Integer> cantidadesPorProducto = new LinkedHashMap<>();

        for (ItemCarritoRequest item : items) {
            if (item.getProductoId() == null || item.getCantidad() <= 0) {
                continue;
            }

            cantidadesPorProducto.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        return cantidadesPorProducto;
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return ESTADO_PENDIENTE;
        }

        return estado.trim().toUpperCase();
    }

    private boolean esEstadoValido(String estado) {
        return ESTADO_PENDIENTE.equals(estado)
                || ESTADO_EN_PROCESO.equals(estado)
                || ESTADO_ENTREGADO.equals(estado)
                || ESTADO_CANCELADO.equals(estado);
    }

    private boolean esEstadoFinal(String estado) {
        return ESTADO_ENTREGADO.equals(estado) || ESTADO_CANCELADO.equals(estado);
    }

    private boolean esTransicionValida(String estadoActual, String nuevoEstado) {
        if (ESTADO_PENDIENTE.equals(estadoActual)) {
            return ESTADO_EN_PROCESO.equals(nuevoEstado)
                    || ESTADO_CANCELADO.equals(nuevoEstado);
        }

        if (ESTADO_EN_PROCESO.equals(estadoActual)) {
            return ESTADO_ENTREGADO.equals(nuevoEstado)
                    || ESTADO_CANCELADO.equals(nuevoEstado);
        }

        return false;
    }

    private void devolverStockAlInventario(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            return;
        }

        List<Producto> productosActualizados = new ArrayList<>();

        for (PedidoDetalle detalle : pedido.getDetalles()) {
            if (detalle.getProductoId() == null) {
                continue;
            }

            Optional<Producto> productoOptional = productoRepository.findById(detalle.getProductoId());

            if (productoOptional.isPresent()) {
                Producto producto = productoOptional.get();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productosActualizados.add(producto);
            }
        }

        productoRepository.saveAll(productosActualizados);
    }
}