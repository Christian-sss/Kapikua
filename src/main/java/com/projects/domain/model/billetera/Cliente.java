package com.projects.domain.model.billetera;

import com.projects.domain.model.seguridad.Usuario;

import java.time.LocalDateTime;

public class Cliente {

    private Long id;
    private String nombres;
    private String apellidos;
    private Usuario usuario;
    private String dni;
    private String numeroCelular;
    private LocalDateTime fechaRegistro;


    public Cliente(Long id, String nombres, String apellidos, String dni, String numeroCelular, Usuario usuario, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.numeroCelular = numeroCelular;
        this.usuario = usuario;
        this.fechaRegistro = fechaRegistro;
    }

    public Cliente() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
