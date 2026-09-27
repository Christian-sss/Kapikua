package com.projects.application.dto.response;

import java.math.BigDecimal;

/**
 * La cartera vigente suma el saldo de capital de los préstamos ACTIVO y EN_MORA.
 */
public record CarteraResponse(
        BigDecimal carteraVigente,
        BigDecimal carteraEnMora,
        long prestamosVigentes,
        long prestamosEnMora
) {
}
