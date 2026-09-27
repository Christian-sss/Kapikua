package com.projects.application.service;

import com.projects.application.dto.command.ConsultarEstadisticasCommand;
import com.projects.application.port.in.ConsultarEstadisticasUseCase;
import com.projects.application.port.in.GenerarReporteEstadisticasUseCase;
import com.projects.application.port.out.ReportePdfPort;
import com.projects.domain.result.Result;

/**
 * Reutiliza ConsultarEstadisticasUseCase: el reporte muestra exactamente los mismos números que
 * la pantalla y hereda su verificación de rol ADMIN.
 */
public class GenerarReporteEstadisticasService implements GenerarReporteEstadisticasUseCase {

    private final ConsultarEstadisticasUseCase consultarEstadisticasUseCase;
    private final ReportePdfPort reportePdfPort;

    public GenerarReporteEstadisticasService(ConsultarEstadisticasUseCase consultarEstadisticasUseCase,
                                              ReportePdfPort reportePdfPort) {
        this.consultarEstadisticasUseCase = consultarEstadisticasUseCase;
        this.reportePdfPort = reportePdfPort;
    }

    @Override
    public Result<byte[]> ejecutar(ConsultarEstadisticasCommand command) {
        return consultarEstadisticasUseCase.ejecutar(command).map(reportePdfPort::generarReporteEstadisticas);
    }
}
