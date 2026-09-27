package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaCronogramaResponse(
        Integer numero,
        LocalDate fechaVencimiento,
        BigDecimal capital,
        BigDecimal interes,
        BigDecimal mora,
        String estado
) {

    public BigDecimal total() {
        return capital.add(interes).add(mora);
    }
}
