package com.projects.domain.model.seguridad;

import com.projects.domain.result.Result;

public class Rol {

    private Long id;

    private String nombreRol;

    public Rol(Long id, String nombreRol) {
        this.id = id;
        this.nombreRol = nombreRol;
    }

    public Rol() {}


    // Business rules
    public Result<Void> validarRol(String nombreRol) {
        if(nombreRol == null ) {
            return Result.failure("","");
        }

        return Result.success();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }
}
