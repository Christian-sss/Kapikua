package com.projects.application.dto.command;

import java.math.BigDecimal;

public record SimularCreditoCommand(
        Long productoId,
        BigDecimal monto,
        Integer plazoMeses
) {
}
