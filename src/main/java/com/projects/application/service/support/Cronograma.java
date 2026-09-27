package com.projects.application.service.support;

import java.math.BigDecimal;
import java.util.List;

/**
 * tea está en porcentaje (45.00 = 45 %); tem es una fracción sin redondear (0.0314...).
 * Sin comisiones ni seguros la TCEA es igual a la TEA (decisión de la Unidad 1).
 */
public record Cronograma(
        BigDecimal tea,
        BigDecimal tem,
        BigDecimal cuotaFija,
        BigDecimal totalIntereses,
        BigDecimal totalAPagar,
        List<CuotaProyectada> cuotas
) {

    public BigDecimal tcea() {
        return tea;
    }
}
