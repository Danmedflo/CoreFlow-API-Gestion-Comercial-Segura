package com.example.sistemagestion.config;

import java.util.List;

import com.example.sistemagestion.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain checkoutSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/pedidos/checkout")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/pagos/confirmar/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/mis-pagos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/pedido/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/comprobantes/mis-comprobantes").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comprobantes/pedido/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comprobantes/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/comprobantes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/productos/panel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/detalle/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/cancelar").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}