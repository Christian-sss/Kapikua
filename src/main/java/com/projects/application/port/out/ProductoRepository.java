package com.projects.application.port.out;

import com.projects.domain.model.credito.ProductoCrediticio;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    List<ProductoCrediticio> listar();

    Optional<ProductoCrediticio> findById(Long id);

}
