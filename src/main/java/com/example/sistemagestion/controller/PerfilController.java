package com.example.sistemagestion.controller;

import com.example.sistemagestion.model.Usuario;
import com.example.sistemagestion.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "http://localhost:4200")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<?> obtenerPerfil() {
        String username = obtenerUsuarioActual();

        if (username == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Usuario no autenticado"));
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(username);

        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOptional.get();

        return ResponseEntity.ok(Map.of(
                "username", usuario.getUsername(),
                "nombreCompleto", usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : "",
                "rol", usuario.getRol(),
                "activo", usuario.isActivo()
        ));
    }

    @PutMapping
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> request) {
        String username = obtenerUsuarioActual();

        if (username == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Usuario no autenticado"));
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(username);

        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOptional.get();

        String nombreCompleto = request.get("nombreCompleto");
        String nuevaPassword = request.get("nuevaPassword");

        if (nombreCompleto == null || nombreCompleto.trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El nombre visible debe tener mínimo 3 caracteres"));
        }

        usuario.setNombreCompleto(nombreCompleto.trim());

        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            if (nuevaPassword.trim().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "La contraseña debe tener mínimo 6 caracteres"));
            }

            usuario.setPassword(passwordEncoder.encode(nuevaPassword.trim()));
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of(
                "username", usuarioGuardado.getUsername(),
                "nombreCompleto", usuarioGuardado.getNombreCompleto() != null ? usuarioGuardado.getNombreCompleto() : "",
                "rol", usuarioGuardado.getRol(),
                "activo", usuarioGuardado.isActivo()
        ));
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
}