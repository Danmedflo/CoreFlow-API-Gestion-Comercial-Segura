package com.example.sistemagestion.controller;

import com.example.sistemagestion.dto.ProductoPanelResponse;
import com.example.sistemagestion.model.Producto;
import com.example.sistemagestion.repository.ProductoRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoController {

    private static final int LIMITE_STOCK_CRITICO = 5;

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoRepository.findAll());
    }

    @GetMapping("/panel/resumen")
    public ResponseEntity<ProductoPanelResponse> obtenerResumenPanel() {
        List<Producto> productos = productoRepository.findAll();

        long totalProductos = productos.size();

        long stockTotal = productos.stream()
                .mapToLong(producto -> Math.max(producto.getStock(), 0))
                .sum();

        long productosStockCritico = productos.stream()
                .filter(producto -> producto.getStock() > 0 && producto.getStock() <= LIMITE_STOCK_CRITICO)
                .count();

        long productosSinStock = productos.stream()
                .filter(producto -> producto.getStock() <= 0)
                .count();

        double valorInventario = productos.stream()
                .mapToDouble(producto -> Math.max(producto.getPrecio(), 0) * Math.max(producto.getStock(), 0))
                .sum();

        ProductoPanelResponse resumen = new ProductoPanelResponse(
                totalProductos,
                stockTotal,
                productosStockCritico,
                productosSinStock,
                valorInventario
        );

        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/panel/stock-critico")
    public ResponseEntity<List<Producto>> listarStockCritico(
            @RequestParam(defaultValue = "5") int limite
    ) {
        int limiteSeguro = Math.max(limite, 0);

        List<Producto> productos = productoRepository.findAll()
                .stream()
                .filter(producto -> producto.getStock() <= limiteSeguro)
                .sorted(Comparator.comparingInt(Producto::getStock))
                .toList();

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Producto> producto = productoRepository.findById(id);

        if (producto.isPresent()) {
            return ResponseEntity.ok(producto.get());
        }

        return ResponseEntity.status(404)
                .body(Map.of("mensaje", "Producto no encontrado"));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        String errorValidacion = validarProducto(producto);

        if (errorValidacion != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", errorValidacion));
        }

        Producto productoGuardado = productoRepository.save(producto);
        return ResponseEntity.ok(productoGuardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        Optional<Producto> productoOptional = productoRepository.findById(id);

        if (productoOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Producto no encontrado"));
        }

        String errorValidacion = validarProducto(productoActualizado);

        if (errorValidacion != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", errorValidacion));
        }

        Producto producto = productoOptional.get();

        producto.setNombre(productoActualizado.getNombre().trim());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setStock(productoActualizado.getStock());
        producto.setCategoria(productoActualizado.getCategoria().trim());
        producto.setDescripcion(productoActualizado.getDescripcion());

        Producto productoGuardado = productoRepository.save(producto);

        return ResponseEntity.ok(productoGuardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Producto no encontrado"));
        }

        productoRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoRepository.findByNombreContainingIgnoreCase(nombre));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(productoRepository.findByCategoriaIgnoreCase(categoria));
    }

    private String validarProducto(Producto producto) {
        if (producto == null) {
            return "Los datos del producto son obligatorios.";
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isBlank()) {
            return "El nombre del producto es obligatorio.";
        }

        if (producto.getCategoria() == null || producto.getCategoria().trim().isBlank()) {
            return "La categoría del producto es obligatoria.";
        }

        if (producto.getPrecio() <= 0) {
            return "El precio del producto debe ser mayor a cero.";
        }

        if (producto.getStock() < 0) {
            return "El stock del producto no puede ser negativo.";
        }

        return null;
    }
}