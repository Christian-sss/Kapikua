package com.projects.application.port.out;

import com.projects.application.dto.response.ComprobanteResponse;
import com.projects.application.dto.response.EstadisticasResponse;

public interface ReportePdfPort {

    byte[] generarComprobante(ComprobanteResponse comprobante);

    byte[] generarReporteEstadisticas(EstadisticasResponse estadisticas);

}
