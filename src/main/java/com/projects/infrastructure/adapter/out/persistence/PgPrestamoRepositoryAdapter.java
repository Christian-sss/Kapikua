package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.PrestamoRepository;
import com.projects.domain.model.EstadoPrestamo;
import com.projects.domain.model.credito.Prestamo;

import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PgPrestamoRepositoryAdapter extends BaseRepository implements PrestamoRepository {

    private static final String INSERT = """
            INSERT INTO credito.prestamo (solicitud_id, monto_desembolsado, tcea, saldo_capital, estado)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID_PARA_ACTUALIZAR = """
            SELECT id, solicitud_id, monto_desembolsado, tcea, saldo_capital, estado, fecha_desembolso
            FROM credito.prestamo
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String ACTUALIZAR = """
            UPDATE credito.prestamo
            SET saldo_capital = ?, estado = ?
            WHERE id = ?
            """;

    @Override
    public Optional<Prestamo> guardar(Prestamo prestamo) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, prestamo.getSolicitudId());
                stmt.setBigDecimal(2, prestamo.getMontoDesembolsado());
                stmt.setBigDecimal(3, prestamo.getTcea());
                stmt.setBigDecimal(4, prestamo.getSaldoCapital());
                stmt.setString(5, prestamo.getEstado().name());

                if (stmt.executeUpdate() == 0) {
                    return Optional.empty();
                }

                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        prestamo.setId(rs.getLong("id"));
                        prestamo.setFechaDesembolso(rs.getObject("fecha_desembolso", OffsetDateTime.class));
                        return Optional.of(prestamo);
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public Optional<Prestamo> findByIdParaActualizar(Long id) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(FIND_BY_ID_PARA_ACTUALIZAR)) {
                stmt.setLong(1, id);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new Prestamo(
                            rs.getLong("id"),
                            rs.getLong("solicitud_id"),
                            rs.getBigDecimal("monto_desembolsado"),
                            rs.getBigDecimal("tcea"),
                            rs.getBigDecimal("saldo_capital"),
                            EstadoPrestamo.valueOf(rs.getString("estado")),
                            rs.getObject("fecha_desembolso", OffsetDateTime.class)
                    ));
                }
            }
        });
    }

    @Override
    public void actualizar(Prestamo prestamo) {
        ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(ACTUALIZAR)) {
                stmt.setBigDecimal(1, prestamo.getSaldoCapital());
                stmt.setString(2, prestamo.getEstado().name());
                stmt.setLong(3, prestamo.getId());
                stmt.executeUpdate();
                return null;
            }
        });
    }
}
