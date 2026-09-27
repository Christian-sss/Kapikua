package com.projects.application.service;

import com.projects.application.dto.command.RetirarSaldoCommand;
import com.projects.application.dto.response.RetiroResponse;
import com.projects.application.port.in.RetirarSaldoUseCase;
import com.projects.application.port.out.BilleteraRepository;
import com.projects.application.port.out.MovimientoRepository;
import com.projects.application.port.out.TipoTransaccionRepository;
import com.projects.application.port.out.TransaccionRepository;
import com.projects.application.port.out.TransactionManager;
import com.projects.domain.model.EstadoTransaccion;
import com.projects.domain.model.billetera.Movimiento;
import com.projects.domain.model.billetera.Transaccion;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class RetirarSaldoService implements RetirarSaldoUseCase {

    private static final String CODIGO_TIPO_RETIRO = "RETIRO";
    private static final BigDecimal MONTO_MINIMO = new BigDecimal("10.00");
    private static final Pattern PATRON_CUENTA = Pattern.compile("^\\d{10,20}$");

    private final BilleteraRepository billeteraRepository;
    private final TransaccionRepository transaccionRepository;
    private final MovimientoRepository movimientoRepository;
    private final TipoTransaccionRepository tipoTransaccionRepository;
    private final TransactionManager transactionManager;

    public RetirarSaldoService(BilleteraRepository billeteraRepository,
                                TransaccionRepository transaccionRepository,
                                MovimientoRepository movimientoRepository,
                                TipoTransaccionRepository tipoTransaccionRepository,
                                TransactionManager transactionManager) {
        this.billeteraRepository = billeteraRepository;
        this.transaccionRepository = transaccionRepository;
        this.movimientoRepository = movimientoRepository;
        this.tipoTransaccionRepository = tipoTransaccionRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Result<RetiroResponse> ejecutar(RetirarSaldoCommand command) {

        if (command == null || command.clienteId() == null
                || command.banco() == null || command.banco().isBlank()
                || command.numeroCuenta() == null || command.monto() == null) {
            return Result.failure("COMANDO_INVALIDO", "Los datos del retiro son obligatorios.");
        }

        var numeroCuenta = command.numeroCuenta().trim();

        // 1. Formato de cuenta y monto mínimo: reglas propias del retiro, no de la billetera.
        if (!PATRON_CUENTA.matcher(numeroCuenta).matches()) {
            return Result.failure(BilleteraError.CUENTA_INVALIDA.name(),
                    "El número de cuenta o CCI debe tener entre 10 y 20 dígitos numéricos.");
        }

        if (command.monto().compareTo(MONTO_MINIMO) < 0) {
            return Result.failure(BilleteraError.MONTO_MINIMO_NO_ALCANZADO.name(),
                    "El monto mínimo de retiro es S/ " + MONTO_MINIMO + ".");
        }

        var billeteraActual = billeteraRepository.findByClienteId(command.clienteId());
        if (billeteraActual.isEmpty()) {
            return Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(), "No se encontró tu billetera.");
        }

        Long idBilletera = billeteraActual.get().getId();
        String referencia = command.banco().trim() + " ****" + numeroCuenta.substring(numeroCuenta.length() - 4);

        var tipoId = tipoTransaccionRepository.findIdByCodigo(CODIGO_TIPO_RETIRO)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el tipo de transacción " + CODIGO_TIPO_RETIRO + " en billetera.tipo_transaccion."));

        return transactionManager.enTransaccion(() -> {

            var billetera = billeteraRepository.findByIdParaActualizar(idBilletera)
                    .orElseThrow(() -> new IllegalStateException("La billetera " + idBilletera + " ya no existe."));

            // 2. billetera.withDraw(monto): la entidad valida estado, monto y saldo.
            var retiro = billetera.withDraw(command.monto());
            if (retiro.isFailure()) {
                return Result.failure(retiro.getError().get());
            }

            // 3. actualizarSaldo
            billeteraRepository.actualizarSaldo(billetera);

            // 4. Guardar la transacción RETIRO con referencia, y su movimiento (-)
            var transaccionGuardada = transaccionRepository.guardar(new Transaccion(
                    null, tipoId, command.monto(), BigDecimal.ZERO, EstadoTransaccion.EXITOSA, null, referencia
            )).orElseThrow(() -> new IllegalStateException("No se pudo registrar la transacción."));

            movimientoRepository.guardar(new Movimiento(
                    null, transaccionGuardada.getId(), billetera.getId(), '-',
                    command.monto(), billetera.getSaldo()
            ));

            return Result.success(new RetiroResponse(
                    transaccionGuardada.getId(),
                    command.monto(),
                    billetera.getSaldo(),
                    referencia
            ));
        });
    }
}
