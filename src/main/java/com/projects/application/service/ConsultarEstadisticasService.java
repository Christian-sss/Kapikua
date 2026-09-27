package com.projects.application.service;

import com.projects.application.dto.command.ConsultarEstadisticasCommand;
import com.projects.application.dto.command.PeriodoEstadistica;
import com.projects.application.dto.response.EstadisticasResponse;
import com.projects.application.dto.response.PuntoSerieResponse;
import com.projects.application.port.in.ConsultarEstadisticasUseCase;
import com.projects.application.port.out.EstadisticasQuery;
import com.projects.application.port.out.SesionContexto;
import com.projects.domain.result.Result;
import com.projects.domain.result.UsuarioError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsultarEstadisticasService implements ConsultarEstadisticasUseCase {

    private static final int LIMITE_TRANSACCIONES = 500;
    private static final int DIAS_DEL_PERIODO = 7;
    private static final int MESES_DEL_PERIODO = 6;

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private static final String[] MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Set", "Oct", "Nov", "Dic"};
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final EstadisticasQuery estadisticasQuery;
    private final SesionContexto sesionContexto;

    public ConsultarEstadisticasService(EstadisticasQuery estadisticasQuery, SesionContexto sesionContexto) {
        this.estadisticasQuery = estadisticasQuery;
        this.sesionContexto = sesionContexto;
    }

    @Override
    public Result<EstadisticasResponse> ejecutar(ConsultarEstadisticasCommand command) {

        var sesion = sesionContexto.obtenerActual();
        if (sesion.isEmpty() || !sesion.get().esAdmin()) {
            return Result.failure(UsuarioError.ACCESO_DENEGADO.name(),
                    "Solo un administrador puede consultar las estadísticas.");
        }

        var periodo = (command == null || command.periodo() == null) ? PeriodoEstadistica.DIA : command.periodo();
        var hoy = LocalDate.now();
        var inicio = (periodo == PeriodoEstadistica.DIA)
                ? hoy.minusDays(DIAS_DEL_PERIODO - 1)
                : hoy.withDayOfMonth(1).minusMonths(MESES_DEL_PERIODO - 1);
        var desde = inicio.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        var resumen = estadisticasQuery.resumenTransacciones(desde);
        var cartera = estadisticasQuery.resumenCartera();

        var serie = (periodo == PeriodoEstadistica.DIA)
                ? serieDiaria(inicio, estadisticasQuery.volumenPorDia(desde))
                : serieMensual(inicio, estadisticasQuery.volumenPorMes(desde));

        return Result.success(new EstadisticasResponse(
                periodo,
                inicio,
                OffsetDateTime.now(),
                resumen.total(),
                resumen.exitosas(),
                resumen.volumen(),
                porcentaje(BigDecimal.valueOf(resumen.exitosas()), BigDecimal.valueOf(resumen.total()), 1),
                serie,
                estadisticasQuery.volumenPorTipo(desde),
                cartera,
                porcentaje(cartera.carteraEnMora(), cartera.carteraVigente(), 2),
                estadisticasQuery.ultimasTransacciones(LIMITE_TRANSACCIONES)
        ));
    }

    // Se completan con cero los días/meses sin movimientos para que el gráfico no tenga huecos.
    private List<PuntoSerieResponse> serieDiaria(LocalDate inicio, Map<LocalDate, BigDecimal> volumen) {
        List<PuntoSerieResponse> serie = new ArrayList<>(DIAS_DEL_PERIODO);
        for (int i = 0; i < DIAS_DEL_PERIODO; i++) {
            var dia = inicio.plusDays(i);
            serie.add(new PuntoSerieResponse(dia,
                    DIAS[dia.getDayOfWeek().getValue() - 1] + " " + dia.getDayOfMonth(),
                    volumen.getOrDefault(dia, BigDecimal.ZERO)));
        }
        return serie;
    }

    private List<PuntoSerieResponse> serieMensual(LocalDate inicio, Map<LocalDate, BigDecimal> volumen) {
        List<PuntoSerieResponse> serie = new ArrayList<>(MESES_DEL_PERIODO);
        for (int i = 0; i < MESES_DEL_PERIODO; i++) {
            var mes = inicio.plusMonths(i);
            serie.add(new PuntoSerieResponse(mes,
                    MESES[mes.getMonthValue() - 1],
                    volumen.getOrDefault(mes, BigDecimal.ZERO)));
        }
        return serie;
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total, int decimales) {
        if (total.signum() == 0) {
            return null;
        }
        return parte.multiply(CIEN).divide(total, decimales, RoundingMode.HALF_EVEN);
    }
}
