package com.projects.application.service;

import com.projects.application.dto.command.GenerarComprobanteCommand;
import com.projects.application.dto.response.ComprobanteFilaResponse;
import com.projects.application.dto.response.ComprobanteResponse;
import com.projects.application.port.in.GenerarComprobanteUseCase;
import com.projects.application.port.out.ComprobanteQuery;
import com.projects.application.port.out.ReportePdfPort;
import com.projects.domain.result.Result;
import com.projects.domain.result.TransaccionError;

import java.math.BigDecimal;
import java.util.List;

public class GenerarComprobanteService implements GenerarComprobanteUseCase {

    private final ComprobanteQuery comprobanteQuery;
    private final ReportePdfPort reportePdfPort;

    public GenerarComprobanteService(ComprobanteQuery comprobanteQuery, ReportePdfPort reportePdfPort) {
        this.comprobanteQuery = comprobanteQuery;
        this.reportePdfPort = reportePdfPort;
    }

    @Override
    public Result<byte[]> ejecutar(GenerarComprobanteCommand command) {

        if (command == null || command.transaccionId() == null || command.clienteSolicitanteId() == null) {
            return Result.failure("COMANDO_INVALIDO", "La transacción y el cliente son obligatorios.");
        }

        var filas = comprobanteQuery.buscarPorTransaccionId(command.transaccionId());

        if (filas.isEmpty()) {
            return Result.failure(TransaccionError.TRANSACCION_NO_ENCONTRADA.name(),
                    "No se encontró la transacción.");
        }

        boolean participa = filas.stream()
                .anyMatch(fila -> fila.clienteId().equals(command.clienteSolicitanteId()));

        if (!participa) {
            return Result.failure(TransaccionError.SIN_PERMISO.name(),
                    "No tienes acceso al comprobante de esta transacción.");
        }

        var comprobante = construir(filas, command.clienteSolicitanteId());

        return Result.success(reportePdfPort.generarComprobante(comprobante));
    }

    private ComprobanteResponse construir(List<ComprobanteFilaResponse> filas, Long clienteSolicitanteId) {

        var primera = filas.get(0);
        String origen = null;
        String destino = null;
        BigDecimal saldoSolicitante = null;

        for (var fila : filas) {
            String nombreCompleto = fila.nombres() + " " + fila.apellidos();

            if (fila.signo() == '-') {
                origen = nombreCompleto;
            } else {
                destino = nombreCompleto;
            }

            if (fila.clienteId().equals(clienteSolicitanteId)) {
                saldoSolicitante = fila.saldoPosterior();
            }
        }

        // RETIRO y DESEMBOLSO_CREDITO solo tienen un movimiento: la contraparte que falta
        // se toma de la referencia guardada en la transacción ("BCP ****1234", "Préstamo #5").
        if (destino == null && primera.referencia() != null) {
            destino = primera.referencia();
        }
        if (origen == null && primera.referencia() != null) {
            origen = "KAPIKUA - " + primera.referencia();
        }

        return new ComprobanteResponse(
                primera.transaccionId(),
                primera.fecha(),
                primera.tipoCodigo(),
                primera.montoTransaccion(),
                primera.estado(),
                origen,
                destino,
                saldoSolicitante
        );
    }
}
