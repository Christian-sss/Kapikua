package com.projects.application.service.support;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaProyectada(
        int numero,
        LocalDate fechaVencimiento,
        BigDecimal cuota,
        BigDecimal interes,
        BigDecimal capital,
        BigDecimal saldo
) {
}
