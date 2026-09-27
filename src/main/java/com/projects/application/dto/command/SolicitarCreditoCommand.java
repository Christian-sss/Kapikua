package com.projects.application.dto.command;

import java.math.BigDecimal;

public record SolicitarCreditoCommand(
        Long clienteId,
        Long productoId,
        BigDecimal monto,
        Integer plazoMeses
) {
}
