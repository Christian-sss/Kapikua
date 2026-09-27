package com.projects.application.service.support;

import com.projects.domain.model.credito.ProductoCrediticio;
import com.projects.domain.result.CreditoError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Regla compartida por la simulación y la solicitud: el monto y el plazo deben caer dentro
 * del rango del producto.
 */
public final class RangoProducto {

    private RangoProducto() {
    }

    public static Result<Void> validar(ProductoCrediticio producto, BigDecimal monto, int plazoMeses) {

        if (monto.scale() > 2) {
            return Result.failure(CreditoError.FUERA_DE_RANGO.name(),
                    "El monto admite como máximo 2 decimales.");
        }

        if (monto.compareTo(producto.getMontoMinimo()) < 0 || monto.compareTo(producto.getMontoMaximo()) > 0) {
            return Result.failure(CreditoError.FUERA_DE_RANGO.name(), String.format(Locale.US,
                    "El monto debe estar entre S/ %,.2f y S/ %,.2f.",
                    producto.getMontoMinimo(), producto.getMontoMaximo()));
        }

        if (plazoMeses < producto.getPlazoMinimoMeses() || plazoMeses > producto.getPlazoMaximoMeses()) {
            return Result.failure(CreditoError.FUERA_DE_RANGO.name(), String.format(
                    "El plazo debe estar entre %d y %d meses.",
                    producto.getPlazoMinimoMeses(), producto.getPlazoMaximoMeses()));
        }

        return Result.success();
    }
}
