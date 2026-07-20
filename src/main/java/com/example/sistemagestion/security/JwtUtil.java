package com.example.sistemagestion.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "mi_clave_secreta_para_spring_con_jwt_1234567890_segura";

    // Token válido por 24 horas para evitar cierres durante la exposición.
    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24;

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", normalizarRol(rol))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(limpiarToken(token)).getSubject();
    }

    public String extractRol(String token) {
        String rol = getClaims(limpiarToken(token)).get("rol", String.class);
        return normalizarRol(rol);
    }

    public boolean validateToken(String token) {
        try {
            String tokenLimpio = limpiarToken(token);

            if (tokenLimpio == null || tokenLimpio.isBlank()) {
                return false;
            }

            Claims claims = getClaims(tokenLimpio);
            Date expiration = claims.getExpiration();
            String username = claims.getSubject();

            return username != null
                    && !username.isBlank()
                    && expiration != null
                    && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String limpiarToken(String token) {
        if (token == null) {
            return null;
        }

        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            return null;
        }

        return rol.replace("ROLE_", "").trim().toUpperCase();
    }
}