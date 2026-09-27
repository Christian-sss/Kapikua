package com.projects.application.port.out;

import com.projects.domain.model.seguridad.Rol;

import java.util.Optional;

public interface RolRepository {

    Optional<Rol> findByNombre(String nombreRol);

    Optional<Rol> findById(Long id);

}
