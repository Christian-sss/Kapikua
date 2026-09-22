package com.projects.domain.model.seguridad;

import java.time.LocalDateTime;

public class Usuario {

    private Long id;
    private Rol rol;
    private String email;
    private String passwordHash;
    private Boolean activo;
    private LocalDateTime fechaCreacion;


    public Usuario(Long id, Rol rol, String email, String passwordHash, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.rol = rol;
        this.email = email;
        this.passwordHash = passwordHash;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
