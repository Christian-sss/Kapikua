package com.projects.application.service;

import com.projects.application.dto.response.ProductoCrediticioResponse;
import com.projects.application.port.in.ListarProductosUseCase;
import com.projects.application.port.out.ProductoRepository;
import com.projects.domain.result.Result;

import java.util.List;

public class ListarProductosService implements ListarProductosUseCase {

    private final ProductoRepository productoRepository;

    public ListarProductosService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public Result<List<ProductoCrediticioResponse>> ejecutar() {
        return Result.success(productoRepository.listar().stream()
                .map(producto -> new ProductoCrediticioResponse(
                        producto.getId(),
                        producto.getNombre(),
                        producto.getTipo().name(),
                        producto.getMontoMinimo(),
                        producto.getMontoMaximo(),
                        producto.getPlazoMinimoMeses(),
                        producto.getPlazoMaximoMeses(),
                        producto.getTasaInteresAnual()
                ))
                .toList());
    }
}
