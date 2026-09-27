package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * origen/destino son el celular del cliente, la referencia externa (ej. "BCP ****1234")
 * o "KAPIKUA" cuando la contraparte es la propia plataforma (desembolsos).
 */
public record TransaccionAdminResponse(
        Long transaccionId,
        OffsetDateTime fecha,
        String origen,
        String destino,
        String tipo,
        BigDecimal monto,
        String estado
) {
}
