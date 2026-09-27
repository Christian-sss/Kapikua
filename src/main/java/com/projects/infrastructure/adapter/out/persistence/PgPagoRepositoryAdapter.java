package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.PagoRepository;
import com.projects.domain.model.credito.Pago;
import com.projects.domain.model.credito.PagoDetalle;

import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PgPagoRepositoryAdapter extends BaseRepository implements PagoRepository {

    private static final String INSERT_PAGO = """
            INSERT INTO credito.pago (transaccion_id, monto_total)
            VALUES (?, ?)
            """;

    private static final String INSERT_DETALLE = """
            INSERT INTO credito.pago_detalle (pago_id, cuota_id, aplicado_capital, aplicado_interes, aplicado_mora)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Override
    public Optional<Pago> guardar(Pago pago) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT_PAGO, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, pago.getTransaccionId());
                stmt.setBigDecimal(2, pago.getMontoTotal());

                if (stmt.executeUpdate() == 0) {
                    return Optional.empty();
                }

                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pago.setId(rs.getLong("id"));
                        pago.setFecha(rs.getObject("fecha", OffsetDateTime.class));
                        return Optional.of(pago);
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public void guardarDetalle(PagoDetalle detalle) {
        ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT_DETALLE, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, detalle.getPagoId());
                stmt.setLong(2, detalle.getCuotaId());
                stmt.setBigDecimal(3, detalle.getCapitalAplicado());
                stmt.setBigDecimal(4, detalle.getInteresAplicado());
                stmt.setBigDecimal(5, detalle.getMoraAplicado());
                stmt.executeUpdate();

                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        detalle.setId(rs.getLong("id"));
                    }
                }
                return null;
            }
        });
    }
}
