package com.projects.application.service;

import com.projects.application.dto.command.TransferirMontoCommand;
import com.projects.application.dto.response.TransferenciaResponse;
import com.projects.application.port.in.TransferirMontoUseCase;
import com.projects.application.port.out.BilleteraRepository;
import com.projects.application.port.out.ClienteRepository;
import com.projects.application.port.out.MovimientoRepository;
import com.projects.application.port.out.TipoTransaccionRepository;
import com.projects.application.port.out.TransaccionRepository;
import com.projects.application.port.out.TransactionManager;
import com.projects.domain.model.EstadoTransaccion;
import com.projects.domain.model.billetera.Movimiento;
import com.projects.domain.model.billetera.Transaccion;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.ClienteError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;

public class TransferirService implements TransferirMontoUseCase {

    private static final String CODIGO_TIPO_TRANSFERENCIA = "TRANSFERENCIA";

    private final BilleteraRepository billeteraRepository;
    private final ClienteRepository clienteRepository;
    private final TransaccionRepository transaccionRepository;
    private final MovimientoRepository movimientoRepository;
    private final TipoTransaccionRepository tipoTransaccionRepository;
    private final TransactionManager transactionManager;

    public TransferirService(BilleteraRepository billeteraRepository,
                              ClienteRepository clienteRepository,
                              TransaccionRepository transaccionRepository,
                              MovimientoRepository movimientoRepository,
                              TipoTransaccionRepository tipoTransaccionRepository,
                              TransactionManager transactionManager) {
        this.billeteraRepository = billeteraRepository;
        this.clienteRepository = clienteRepository;
        this.transaccionRepository = transaccionRepository;
        this.movimientoRepository = movimientoRepository;
        this.tipoTransaccionRepository = tipoTransaccionRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Result<TransferenciaResponse> ejecutar(TransferirMontoCommand command) {

        if (command == null || command.clienteOrigenId() == null
                || command.celularDestino() == null || command.celularDestino().isBlank()
                || command.monto() == null) {
            return Result.failure("COMANDO_INVALIDO", "Los datos de la transferencia son obligatorios.");
        }

        var billeteraOrigenActual = billeteraRepository.findByClienteId(command.clienteOrigenId());
        if (billeteraOrigenActual.isEmpty()) {
            return Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(), "No se encontró tu billetera.");
        }

        var clienteDestinoActual = clienteRepository.findByCelular(command.celularDestino().trim());
        if (clienteDestinoActual.isEmpty()) {
            return Result.failure(ClienteError.DESTINATARIO_NO_ENCONTRADO.name(),
                    "No existe un cliente KAPIKUA con ese número de celular.");
        }
        var clienteDestino = clienteDestinoActual.get();

        if (clienteDestino.getId().equals(command.clienteOrigenId())) {
            return Result.failure(ClienteError.DESTINATARIO_INVALIDO.name(),
                    "No puedes transferirte dinero a ti mismo.");
        }

        var billeteraDestinoActual = billeteraRepository.findByClienteId(clienteDestino.getId());
        if (billeteraDestinoActual.isEmpty()) {
            return Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(),
                    "El destinatario no tiene una billetera activa.");
        }

        Long idOrigen = billeteraOrigenActual.get().getId();
        Long idDestino = billeteraDestinoActual.get().getId();

        var tipoId = tipoTransaccionRepository.findIdByCodigo(CODIGO_TIPO_TRANSFERENCIA)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el tipo de transacción " + CODIGO_TIPO_TRANSFERENCIA + " en billetera.tipo_transaccion."));

        return transactionManager.enTransaccion(() -> {

            // Se bloquean en orden ascendente de id para que dos transferencias cruzadas
            // (A->B y B->A) al mismo tiempo no terminen en deadlock.
            Long idMenor = Math.min(idOrigen, idDestino);
            Long idMayor = Math.max(idOrigen, idDestino);

            var billeteraA = billeteraRepository.findByIdParaActualizar(idMenor)
                    .orElseThrow(() -> new IllegalStateException("La billetera " + idMenor + " ya no existe."));
            var billeteraB = billeteraRepository.findByIdParaActualizar(idMayor)
                    .orElseThrow(() -> new IllegalStateException("La billetera " + idMayor + " ya no existe."));

            var billeteraOrigen = billeteraA.getId().equals(idOrigen) ? billeteraA : billeteraB;
            var billeteraDestino = billeteraA.getId().equals(idDestino) ? billeteraA : billeteraB;

            var retiro = billeteraOrigen.withDraw(command.monto());
            if (retiro.isFailure()) {
                return Result.failure(retiro.getError().get());
            }

            var deposito = billeteraDestino.deposit(command.monto());
            if (deposito.isFailure()) {
                return Result.failure(deposito.getError().get());
            }

            billeteraRepository.actualizarSaldo(billeteraOrigen);
            billeteraRepository.actualizarSaldo(billeteraDestino);

            var transaccionGuardada = transaccionRepository.guardar(new Transaccion(
                    null, tipoId, command.monto(), BigDecimal.ZERO, EstadoTransaccion.EXITOSA, null, null
            )).orElseThrow(() -> new IllegalStateException("No se pudo registrar la transacción."));

            movimientoRepository.guardar(new Movimiento(
                    null, transaccionGuardada.getId(), billeteraOrigen.getId(), '-',
                    command.monto(), billeteraOrigen.getSaldo()
            ));

            movimientoRepository.guardar(new Movimiento(
                    null, transaccionGuardada.getId(), billeteraDestino.getId(), '+',
                    command.monto(), billeteraDestino.getSaldo()
            ));

            var primerNombre = clienteDestino.getNombres().trim().split("\\s+")[0];
            var inicialApellido = clienteDestino.getApellidos().trim().charAt(0);

            return Result.success(new TransferenciaResponse(
                    transaccionGuardada.getId(),
                    command.monto(),
                    billeteraOrigen.getSaldo(),
                    primerNombre + " " + inicialApellido + "***",
                    clienteDestino.getNumeroCelular()
            ));
        });
    }
}
