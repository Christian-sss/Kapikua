package com.projects.application.service;

import com.projects.application.dto.command.PagarCuotaCommand;
import com.projects.application.dto.response.PagoCuotaResponse;
import com.projects.application.port.in.PagarCuotaUseCase;
import com.projects.application.port.out.BilleteraRepository;
import com.projects.application.port.out.CuotaRepository;
import com.projects.application.port.out.MovimientoRepository;
import com.projects.application.port.out.PagoRepository;
import com.projects.application.port.out.PrestamoQuery;
import com.projects.application.port.out.PrestamoRepository;
import com.projects.application.port.out.TipoTransaccionRepository;
import com.projects.application.port.out.TransaccionRepository;
import com.projects.application.port.out.TransactionManager;
import com.projects.domain.model.EstadoCuota;
import com.projects.domain.model.EstadoPrestamo;
import com.projects.domain.model.EstadoTransaccion;
import com.projects.domain.model.billetera.Movimiento;
import com.projects.domain.model.billetera.Transaccion;
import com.projects.domain.model.credito.Pago;
import com.projects.domain.model.credito.PagoDetalle;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.CreditoError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;

/**
 * Unidad 1: paga solo la próxima cuota completa (RFC-20 a RFC-22). El monto se aplica en el
 * orden mora → interés → capital; como la cuota se paga entera, cada concepto se cubre completo.
 */
public class PagarCuotaService implements PagarCuotaUseCase {

    private static final String CODIGO_TIPO_PAGO = "PAGO_CREDITO";

    private final PrestamoQuery prestamoQuery;
    private final PrestamoRepository prestamoRepository;
    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;
    private final BilleteraRepository billeteraRepository;
    private final TransaccionRepository transaccionRepository;
    private final MovimientoRepository movimientoRepository;
    private final TipoTransaccionRepository tipoTransaccionRepository;
    private final TransactionManager transactionManager;

    public PagarCuotaService(PrestamoQuery prestamoQuery,
                             PrestamoRepository prestamoRepository,
                             CuotaRepository cuotaRepository,
                             PagoRepository pagoRepository,
                             BilleteraRepository billeteraRepository,
                             TransaccionRepository transaccionRepository,
                             MovimientoRepository movimientoRepository,
                             TipoTransaccionRepository tipoTransaccionRepository,
                             TransactionManager transactionManager) {
        this.prestamoQuery = prestamoQuery;
        this.prestamoRepository = prestamoRepository;
        this.cuotaRepository = cuotaRepository;
        this.pagoRepository = pagoRepository;
        this.billeteraRepository = billeteraRepository;
        this.transaccionRepository = transaccionRepository;
        this.movimientoRepository = movimientoRepository;
        this.tipoTransaccionRepository = tipoTransaccionRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Result<PagoCuotaResponse> ejecutar(PagarCuotaCommand command) {

        if (command == null || command.clienteId() == null || command.prestamoId() == null) {
            return Result.failure("COMANDO_INVALIDO", "El cliente y el préstamo son obligatorios.");
        }

        var duenio = prestamoQuery.buscarClienteIdDelPrestamo(command.prestamoId());
        if (duenio.isEmpty()) {
            return Result.failure(CreditoError.PRESTAMO_NO_ENCONTRADO.name(), "No se encontró el préstamo.");
        }
        if (!duenio.get().equals(command.clienteId())) {
            return Result.failure(CreditoError.SIN_PERMISO.name(), "No puedes pagar un préstamo que no es tuyo.");
        }

        var billeteraActual = billeteraRepository.findByClienteId(command.clienteId());
        if (billeteraActual.isEmpty()) {
            return Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(), "No se encontró tu billetera.");
        }
        Long idBilletera = billeteraActual.get().getId();

        var tipoId = tipoTransaccionRepository.findIdByCodigo(CODIGO_TIPO_PAGO)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el tipo de transacción " + CODIGO_TIPO_PAGO + " en billetera.tipo_transaccion."));

        return transactionManager.enTransaccion(() -> {

            // Mismo orden de bloqueo que SolicitarCreditoService (billetera primero): dos clics seguidos
            // en "Pagar" se serializan y el segundo ya encuentra la siguiente cuota.
            var billetera = billeteraRepository.findByIdParaActualizar(idBilletera)
                    .orElseThrow(() -> new IllegalStateException("La billetera " + idBilletera + " ya no existe."));

            var prestamo = prestamoRepository.findByIdParaActualizar(command.prestamoId())
                    .orElseThrow(() -> new IllegalStateException("El préstamo " + command.prestamoId() + " ya no existe."));

            if (prestamo.getEstado() != EstadoPrestamo.ACTIVO && prestamo.getEstado() != EstadoPrestamo.EN_MORA) {
                return Result.failure(CreditoError.PRESTAMO_NO_VIGENTE.name(),
                        "Este préstamo está " + prestamo.getEstado().name() + " y ya no tiene cuotas por pagar.");
            }

            // 1. Primera cuota no pagada.
            var cuotaActual = cuotaRepository.buscarPrimeraNoPagadaParaActualizar(prestamo.getId());
            if (cuotaActual.isEmpty()) {
                return Result.failure(CreditoError.SIN_CUOTAS_PENDIENTES.name(), "El préstamo no tiene cuotas pendientes.");
            }
            var cuota = cuotaActual.get();
            var total = cuota.getMora().add(cuota.getInteres()).add(cuota.getCapital());

            // 2. Débito: la entidad valida estado, monto y saldo.
            var retiro = billetera.withDraw(total);
            if (retiro.isFailure()) {
                return Result.failure(retiro.getError().get());
            }
            billeteraRepository.actualizarSaldo(billetera);

            // 3. Transacción PAGO_CREDITO y su movimiento (-).
            var transaccion = transaccionRepository.guardar(new Transaccion(
                    null, tipoId, total, BigDecimal.ZERO, EstadoTransaccion.EXITOSA, null,
                    "Préstamo #" + prestamo.getId() + " · Cuota " + cuota.getNumero()
            )).orElseThrow(() -> new IllegalStateException("No se pudo registrar la transacción del pago."));

            movimientoRepository.guardar(new Movimiento(
                    null, transaccion.getId(), billetera.getId(), '-', total, billetera.getSaldo()
            ));

            // 4. Pago y su detalle por concepto.
            var pago = pagoRepository.guardar(new Pago(null, transaccion.getId(), total, null))
                    .orElseThrow(() -> new IllegalStateException("No se pudo registrar el pago."));

            pagoRepository.guardarDetalle(new PagoDetalle(
                    null, pago.getId(), cuota.getId(), cuota.getCapital(), cuota.getInteres(), cuota.getMora()
            ));

            // 5. Cuota PAGADA y el capital sale del saldo del préstamo.
            cuota.setEstado(EstadoCuota.PAGADA);
            cuotaRepository.actualizarEstado(cuota);
            prestamo.setSaldoCapital(prestamo.getSaldoCapital().subtract(cuota.getCapital()));

            // 6. Última cuota → PAGADO. Si estaba EN_MORA y ya no le quedan cuotas vencidas, vuelve a ACTIVO.
            if (!cuotaRepository.existeNoPagada(prestamo.getId())) {
                prestamo.setEstado(EstadoPrestamo.PAGADO);
            } else if (prestamo.getEstado() == EstadoPrestamo.EN_MORA && !cuotaRepository.existeVencida(prestamo.getId())) {
                prestamo.setEstado(EstadoPrestamo.ACTIVO);
            }
            prestamoRepository.actualizar(prestamo);

            return Result.success(new PagoCuotaResponse(
                    transaccion.getId(),
                    prestamo.getId(),
                    cuota.getNumero(),
                    total,
                    cuota.getMora(),
                    cuota.getInteres(),
                    cuota.getCapital(),
                    billetera.getSaldo(),
                    prestamo.getSaldoCapital(),
                    prestamo.getEstado().name()
            ));
        });
    }
}
