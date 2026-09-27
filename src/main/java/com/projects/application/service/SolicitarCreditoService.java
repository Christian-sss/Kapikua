package com.projects.application.service;

import com.projects.application.dto.command.SolicitarCreditoCommand;
import com.projects.application.dto.response.SolicitudCreditoResponse;
import com.projects.application.port.in.SolicitarCreditoUseCase;
import com.projects.application.port.out.BilleteraRepository;
import com.projects.application.port.out.CuotaRepository;
import com.projects.application.port.out.HistorialCrediticioQuery;
import com.projects.application.port.out.MovimientoRepository;
import com.projects.application.port.out.PrestamoRepository;
import com.projects.application.port.out.ProductoRepository;
import com.projects.application.port.out.SolicitudCreditoRepository;
import com.projects.application.port.out.TipoTransaccionRepository;
import com.projects.application.port.out.TransaccionRepository;
import com.projects.application.port.out.TransactionManager;
import com.projects.application.service.support.CalculadoraCronograma;
import com.projects.application.service.support.RangoProducto;
import com.projects.domain.model.EstadoCuota;
import com.projects.domain.model.EstadoPrestamo;
import com.projects.domain.model.EstadoSolicitudCredito;
import com.projects.domain.model.EstadoTransaccion;
import com.projects.domain.model.TipoProductoCrediticio;
import com.projects.domain.model.billetera.Movimiento;
import com.projects.domain.model.billetera.Transaccion;
import com.projects.domain.model.credito.Cuota;
import com.projects.domain.model.credito.Prestamo;
import com.projects.domain.model.credito.SolicitudCredito;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.CreditoError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

public class SolicitarCreditoService implements SolicitarCreditoUseCase {

    private static final String CODIGO_TIPO_DESEMBOLSO = "DESEMBOLSO_CREDITO";

    private final ProductoRepository productoRepository;
    private final BilleteraRepository billeteraRepository;
    private final HistorialCrediticioQuery historialCrediticioQuery;
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final PrestamoRepository prestamoRepository;
    private final CuotaRepository cuotaRepository;
    private final TransaccionRepository transaccionRepository;
    private final MovimientoRepository movimientoRepository;
    private final TipoTransaccionRepository tipoTransaccionRepository;
    private final TransactionManager transactionManager;
    private final CalculadoraCronograma calculadoraCronograma;

    public SolicitarCreditoService(ProductoRepository productoRepository,
                                    BilleteraRepository billeteraRepository,
                                    HistorialCrediticioQuery historialCrediticioQuery,
                                    SolicitudCreditoRepository solicitudCreditoRepository,
                                    PrestamoRepository prestamoRepository,
                                    CuotaRepository cuotaRepository,
                                    TransaccionRepository transaccionRepository,
                                    MovimientoRepository movimientoRepository,
                                    TipoTransaccionRepository tipoTransaccionRepository,
                                    TransactionManager transactionManager,
                                    CalculadoraCronograma calculadoraCronograma) {
        this.productoRepository = productoRepository;
        this.billeteraRepository = billeteraRepository;
        this.historialCrediticioQuery = historialCrediticioQuery;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.prestamoRepository = prestamoRepository;
        this.cuotaRepository = cuotaRepository;
        this.transaccionRepository = transaccionRepository;
        this.movimientoRepository = movimientoRepository;
        this.tipoTransaccionRepository = tipoTransaccionRepository;
        this.transactionManager = transactionManager;
        this.calculadoraCronograma = calculadoraCronograma;
    }

    @Override
    public Result<SolicitudCreditoResponse> ejecutar(SolicitarCreditoCommand command) {

        if (command == null || command.clienteId() == null || command.productoId() == null
                || command.monto() == null || command.plazoMeses() == null) {
            return Result.failure("COMANDO_INVALIDO", "Los datos de la solicitud son obligatorios.");
        }

        var productoActual = productoRepository.findById(command.productoId());
        if (productoActual.isEmpty()) {
            return Result.failure(CreditoError.PRODUCTO_NO_ENCONTRADO.name(), "No se encontró el producto crediticio.");
        }
        var producto = productoActual.get();

        // 1. Monto y plazo contra el producto.
        var rango = RangoProducto.validar(producto, command.monto(), command.plazoMeses());
        if (rango.isFailure()) {
            return Result.failure(rango.getError().get());
        }

        var billeteraActual = billeteraRepository.findByClienteId(command.clienteId());
        if (billeteraActual.isEmpty()) {
            return Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(), "No se encontró tu billetera.");
        }
        Long idBilletera = billeteraActual.get().getId();

        var tipoId = tipoTransaccionRepository.findIdByCodigo(CODIGO_TIPO_DESEMBOLSO)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el tipo de transacción " + CODIGO_TIPO_DESEMBOLSO + " en billetera.tipo_transaccion."));

        return transactionManager.enTransaccion(() -> {

            // Se bloquea la billetera antes de evaluar: dos solicitudes simultáneas del mismo cliente
            // quedan en fila aquí, y la segunda ya ve en los filtros el préstamo creado por la primera.
            var billetera = billeteraRepository.findByIdParaActualizar(idBilletera)
                    .orElseThrow(() -> new IllegalStateException("La billetera " + idBilletera + " ya no existe."));

            // 2. Filtros de evaluación.
            var rechazo = evaluar(command.clienteId(), producto.getTipo());

            // 3. Rechazada: se guarda con su motivo y se confirma (no es un error del sistema).
            if (rechazo.isPresent()) {
                var solicitud = guardarSolicitud(command, EstadoSolicitudCredito.RECHAZADA, rechazo.get().name());

                return Result.success(new SolicitudCreditoResponse(
                        solicitud.getId(),
                        EstadoSolicitudCredito.RECHAZADA.name(),
                        rechazo.get().name(),
                        mensajeRechazo(rechazo.get(), producto.getTipo()),
                        null, null, null, null, null
                ));
            }

            // 4. Aprobada: solicitud, cronograma, préstamo, cuotas y desembolso.
            var solicitud = guardarSolicitud(command, EstadoSolicitudCredito.APROBADA, null);

            var cronograma = calculadoraCronograma.generar(
                    command.monto(), producto.getTasaInteresAnual(), command.plazoMeses(), LocalDate.now());

            var prestamo = prestamoRepository.guardar(new Prestamo(
                    null, solicitud.getId(), command.monto(), cronograma.tcea(),
                    command.monto(), EstadoPrestamo.ACTIVO, null
            )).orElseThrow(() -> new IllegalStateException("No se pudo registrar el préstamo."));

            cuotaRepository.guardarTodas(cronograma.cuotas().stream()
                    .map(cuota -> new Cuota(
                            null, prestamo.getId(), cuota.numero(), cuota.fechaVencimiento(),
                            cuota.capital(), cuota.interes(), BigDecimal.ZERO, EstadoCuota.PENDIENTE))
                    .toList());

            var deposito = billetera.deposit(command.monto());
            if (deposito.isFailure()) {
                return Result.failure(deposito.getError().get());
            }

            billeteraRepository.actualizarSaldo(billetera);

            var transaccion = transaccionRepository.guardar(new Transaccion(
                    null, tipoId, command.monto(), BigDecimal.ZERO, EstadoTransaccion.EXITOSA,
                    null, "Préstamo #" + prestamo.getId()
            )).orElseThrow(() -> new IllegalStateException("No se pudo registrar la transacción de desembolso."));

            movimientoRepository.guardar(new Movimiento(
                    null, transaccion.getId(), billetera.getId(), '+',
                    command.monto(), billetera.getSaldo()
            ));

            return Result.success(new SolicitudCreditoResponse(
                    solicitud.getId(),
                    EstadoSolicitudCredito.APROBADA.name(),
                    null,
                    String.format(Locale.US, "¡Crédito aprobado! Se abonaron S/ %,.2f a tu billetera.", command.monto()),
                    prestamo.getId(),
                    transaccion.getId(),
                    command.monto(),
                    cronograma.cuotaFija(),
                    billetera.getSaldo()
            ));
        });
    }

    private Optional<CreditoError> evaluar(Long clienteId, TipoProductoCrediticio tipo) {

        if (historialCrediticioQuery.tienePrestamoEnMora(clienteId)) {
            return Optional.of(CreditoError.PRESTAMO_EN_MORA);
        }

        if (historialCrediticioQuery.tienePrestamoActivo(clienteId, tipo)) {
            return Optional.of(CreditoError.LIMITE_PRESTAMOS);
        }

        if (tipo == TipoProductoCrediticio.CREDITO_PERSONAL
                && !historialCrediticioQuery.tienePrestamoPagado(clienteId, TipoProductoCrediticio.MICROCREDITO)) {
            return Optional.of(CreditoError.REQUIERE_MICROCREDITO_PAGADO);
        }

        return Optional.empty();
    }

    private String mensajeRechazo(CreditoError motivo, TipoProductoCrediticio tipo) {
        return switch (motivo) {
            case PRESTAMO_EN_MORA ->
                    "Tienes un préstamo en mora. Regulariza tus pagos antes de solicitar un nuevo crédito.";
            case LIMITE_PRESTAMOS -> "Ya tienes un " + (tipo == TipoProductoCrediticio.MICROCREDITO
                    ? "microcrédito" : "crédito personal") + " activo. Termina de pagarlo antes de solicitar otro.";
            case REQUIERE_MICROCREDITO_PAGADO ->
                    "Para acceder a un crédito personal primero debes haber pagado un microcrédito.";
            default -> "Tu solicitud no cumple los requisitos del producto.";
        };
    }

    private SolicitudCredito guardarSolicitud(SolicitarCreditoCommand command, EstadoSolicitudCredito estado,
                                              String motivoRechazo) {
        return solicitudCreditoRepository.guardar(new SolicitudCredito(
                null, command.clienteId(), command.productoId(), command.monto(), command.plazoMeses(),
                estado, null, motivoRechazo, null
        )).orElseThrow(() -> new IllegalStateException("No se pudo registrar la solicitud de crédito."));
    }
}
