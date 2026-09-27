package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Una fila cruda de la consulta del comprobante: una por cada movimiento de la transacción
 * (1 para depósito/retiro, 2 para una transferencia). GenerarComprobanteService las combina
 * en un único ComprobanteResponse.
 */
public record ComprobanteFilaResponse(
        Long transaccionId,
        OffsetDateTime fecha,
        String tipoCodigo,
        BigDecimal montoTransaccion,
        String estado,
        String referencia,
        Long clienteId,
        String nombres,
        String apellidos,
        Character signo,
        BigDecimal saldoPosterior
) {
}
