package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.MovimientoRepository;
import com.projects.domain.model.billetera.Movimiento;

import java.sql.Statement;
import java.util.Optional;

public class PgMovimientoRepositoryAdapter extends BaseRepository implements MovimientoRepository {

    private static final String INSERT = """
            INSERT INTO billetera.movimiento (transaccion_id, billetera_id, signo, monto, saldo_posterior)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Override
    public Optional<Movimiento> guardar(Movimiento movimiento) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, movimiento.getTransaccionId());
                stmt.setLong(2, movimiento.getBilleteraId());
                stmt.setString(3, movimiento.getSigno().toString());
                stmt.setBigDecimal(4, movimiento.getMonto());
                stmt.setBigDecimal(5, movimiento.getSaldoPosterior());

                int filas = stmt.executeUpdate();

                if (filas == 0) {
                    return Optional.empty();
                }

                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setId(rs.getLong("id"));
                        return Optional.of(movimiento);
                    }
                    return Optional.empty();
                }
            }
        });
    }
}
