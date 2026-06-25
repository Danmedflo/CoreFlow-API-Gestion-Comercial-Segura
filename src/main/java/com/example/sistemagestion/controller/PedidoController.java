package com.example.sistemagestion.controller;

import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.repository.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidoController {

    private final PedidoRepository pedidoRepository;

    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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
    public ResponseEntity<Pedido> crear(@RequestBody Pedido pedido) {
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
}