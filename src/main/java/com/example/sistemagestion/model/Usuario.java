package com.example.sistemagestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String rol;
    private boolean activo;
    private String nombreCompleto;

    public Usuario() {
    }

    public Usuario(Long id, String username, String password, String rol, boolean activo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public Usuario(Long id, String username, String password, String rol, boolean activo, String nombreCompleto) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
        this.nombreCompleto = nombreCompleto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
}