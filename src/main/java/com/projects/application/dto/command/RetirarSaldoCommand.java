package com.projects.application.dto.command;

import java.math.BigDecimal;

public record RetirarSaldoCommand(
        Long clienteId,
        String banco,
        String numeroCuenta,
        BigDecimal monto
) {
}
