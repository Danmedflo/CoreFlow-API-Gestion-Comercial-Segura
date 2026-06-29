package com.example.sistemagestion.controller;

import com.example.sistemagestion.dto.ComprarPedidoRequest;
import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.model.Producto;
import com.example.sistemagestion.repository.PedidoRepository;
import com.example.sistemagestion.repository.ProductoRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoController(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(pedidoRepository.findAll());
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
    public ResponseEntity<?> crear(@RequestBody Pedido pedido) {
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return ResponseEntity.ok(pedidoGuardado);
    }

    @Transactional
    @PostMapping("/comprar")
    public ResponseEntity<?> comprarProducto(@RequestBody ComprarPedidoRequest request) {
        if (request.getCliente() == null || request.getCliente().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El cliente es obligatorio"));
        }

        if (request.getProductoId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El producto es obligatorio"));
        }

        if (request.getCantidad() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La cantidad debe ser mayor a cero"));
        }

        Optional<Producto> productoOptional = productoRepository.findById(request.getProductoId());

        if (productoOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Producto no encontrado"));
        }

        Producto producto = productoOptional.get();

        if (producto.getStock() < request.getCantidad()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Stock insuficiente para realizar el pedido"));
        }

        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);

        Pedido pedido = new Pedido();
        pedido.setCliente(request.getCliente().trim());
        pedido.setFecha(LocalDate.now().toString());
        pedido.setEstado("PENDIENTE");

        pedido.setProductoId(producto.getId());
        pedido.setProductoNombre(producto.getNombre());
        pedido.setCantidad(request.getCantidad());
        pedido.setPrecioUnitario(producto.getPrecio());
        pedido.setTotal(producto.getPrecio() * request.getCantidad());

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

        pedido.setProductoId(pedidoActualizado.getProductoId());
        pedido.setProductoNombre(pedidoActualizado.getProductoNombre());
        pedido.setCantidad(pedidoActualizado.getCantidad());
        pedido.setPrecioUnitario(pedidoActualizado.getPrecioUnitario());

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
}