package com.projects.application.dto.response;

import com.projects.application.dto.command.PeriodoEstadistica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Los totales, la serie y el volumen por tipo cubren solo el período (desde "desde" hasta hoy);
 * la cartera y la lista de transacciones son una foto actual, sin filtro de fecha.
 * porcentajeExito e indiceMorosidad son null cuando no hay datos sobre los cuales calcularlos.
 */
public record EstadisticasResponse(
        PeriodoEstadistica periodo,
        LocalDate desde,
        OffsetDateTime generadoEn,
        long totalTransacciones,
        long transaccionesExitosas,
        BigDecimal volumenOperado,
        BigDecimal porcentajeExito,
        List<PuntoSerieResponse> serie,
        List<VolumenTipoResponse> volumenPorTipo,
        CarteraResponse cartera,
        BigDecimal indiceMorosidad,
        List<TransaccionAdminResponse> transacciones
) {
}
