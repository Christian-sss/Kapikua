package com.projects.application.port.out;

import com.projects.application.dto.response.CarteraResponse;
import com.projects.application.dto.response.ResumenTransaccionesResponse;
import com.projects.application.dto.response.TransaccionAdminResponse;
import com.projects.application.dto.response.VolumenTipoResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Puerto de solo lectura para el panel de administración. Los volúmenes cuentan solo
 * transacciones EXITOSA.
 */
public interface EstadisticasQuery {

    ResumenTransaccionesResponse resumenTransacciones(OffsetDateTime desde);

    /** Clave: el día (o el primer día del mes) en la zona horaria de la sesión. */
    Map<LocalDate, BigDecimal> volumenPorDia(OffsetDateTime desde);

    Map<LocalDate, BigDecimal> volumenPorMes(OffsetDateTime desde);

    List<VolumenTipoResponse> volumenPorTipo(OffsetDateTime desde);

    CarteraResponse resumenCartera();

    List<TransaccionAdminResponse> ultimasTransacciones(int limite);

}
