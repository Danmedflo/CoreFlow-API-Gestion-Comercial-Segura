package com.example.sistemagestion.config;

import com.example.sistemagestion.model.Pedido;
import com.example.sistemagestion.model.Producto;
import com.example.sistemagestion.model.Usuario;
import com.example.sistemagestion.repository.PedidoRepository;
import com.example.sistemagestion.repository.ProductoRepository;
import com.example.sistemagestion.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ProductoRepository productoRepository,
                           PedidoRepository pedidoRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            productoRepository.save(new Producto(null, "Laptop", 2500.0, 10, "Tecnología"));
            productoRepository.save(new Producto(null, "Mouse", 80.0, 25, "Accesorios"));
            productoRepository.save(new Producto(null, "Teclado", 150.0, 15, "Accesorios"));
        }

        if (pedidoRepository.count() == 0) {
            pedidoRepository.save(new Pedido(null, "Carlos Perez", "2026-04-21", 500.0, "PENDIENTE"));
            pedidoRepository.save(new Pedido(null, "Ana Torres", "2026-04-22", 320.0, "ENTREGADO"));
        }

        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRol("ADMIN");
            admin.setActivo(true);

            usuarioRepository.save(admin);
        }
    }
}