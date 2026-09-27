package com.projects.application.dto.response;

import java.math.BigDecimal;

public record BilleteraResponse(
        Long id,
        Long clienteId,
        BigDecimal saldo,
        String estado
) {
}
