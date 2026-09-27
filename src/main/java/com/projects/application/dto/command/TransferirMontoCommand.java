package com.projects.application.dto.command;

import java.math.BigDecimal;

public record TransferirMontoCommand(
        Long clienteOrigenId,
        String celularDestino,
        BigDecimal monto
) {
}
