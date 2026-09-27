package com.projects.application.service;

import com.projects.application.dto.command.SimularCreditoCommand;
import com.projects.application.dto.response.SimulacionCreditoResponse;
import com.projects.application.port.in.SimularCreditoUseCase;
import com.projects.application.port.out.ProductoRepository;
import com.projects.application.service.support.CalculadoraCronograma;
import com.projects.application.service.support.RangoProducto;
import com.projects.domain.result.CreditoError;
import com.projects.domain.result.Result;

import java.time.LocalDate;

public class SimularCreditoService implements SimularCreditoUseCase {

    private final ProductoRepository productoRepository;
    private final CalculadoraCronograma calculadoraCronograma;

    public SimularCreditoService(ProductoRepository productoRepository, CalculadoraCronograma calculadoraCronograma) {
        this.productoRepository = productoRepository;
        this.calculadoraCronograma = calculadoraCronograma;
    }

    @Override
    public Result<SimulacionCreditoResponse> ejecutar(SimularCreditoCommand command) {

        if (command == null || command.productoId() == null || command.monto() == null || command.plazoMeses() == null) {
            return Result.failure("COMANDO_INVALIDO", "El producto, el monto y el plazo son obligatorios.");
        }

        var productoActual = productoRepository.findById(command.productoId());
        if (productoActual.isEmpty()) {
            return Result.failure(CreditoError.PRODUCTO_NO_ENCONTRADO.name(), "No se encontró el producto crediticio.");
        }
        var producto = productoActual.get();

        var rango = RangoProducto.validar(producto, command.monto(), command.plazoMeses());
        if (rango.isFailure()) {
            return Result.failure(rango.getError().get());
        }

        var cronograma = calculadoraCronograma.generar(
                command.monto(), producto.getTasaInteresAnual(), command.plazoMeses(), LocalDate.now());

        return Result.success(new SimulacionCreditoResponse(
                producto.getId(),
                producto.getNombre(),
                command.monto(),
                command.plazoMeses(),
                cronograma
        ));
    }
}
