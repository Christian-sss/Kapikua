package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.CuotaRepository;
import com.projects.domain.model.EstadoCuota;
import com.projects.domain.model.credito.Cuota;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PgCuotaRepositoryAdapter extends BaseRepository implements CuotaRepository {

    private static final String INSERT = """
            INSERT INTO credito.cuota (prestamo_id, numero, fecha_vencimiento, capital, interes, mora, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String PRIMERA_NO_PAGADA_PARA_ACTUALIZAR = """
            SELECT id, prestamo_id, numero, fecha_vencimiento, capital, interes, mora, estado
            FROM credito.cuota
            WHERE prestamo_id = ? AND estado <> 'PAGADA'
            ORDER BY numero
            LIMIT 1
            FOR UPDATE
            """;

    private static final String ACTUALIZAR_ESTADO = """
            UPDATE credito.cuota
            SET estado = ?
            WHERE id = ?
            """;

    private static final String EXISTE_CON_ESTADO = """
            SELECT EXISTS (SELECT 1 FROM credito.cuota WHERE prestamo_id = ? AND estado = ANY (?))
            """;

    @Override
    public void guardarTodas(List<Cuota> cuotas) {

        ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT)) {
                for (var cuota : cuotas) {
                    stmt.setLong(1, cuota.getPrestamoId());
                    stmt.setInt(2, cuota.getNumero());
                    stmt.setObject(3, cuota.getFechaVencimiento());
                    stmt.setBigDecimal(4, cuota.getCapital());
                    stmt.setBigDecimal(5, cuota.getInteres());
                    stmt.setBigDecimal(6, cuota.getMora());
                    stmt.setString(7, cuota.getEstado().name());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                return null;
            }
        });
    }

    @Override
    public Optional<Cuota> buscarPrimeraNoPagadaParaActualizar(Long prestamoId) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(PRIMERA_NO_PAGADA_PARA_ACTUALIZAR)) {
                stmt.setLong(1, prestamoId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new Cuota(
                            rs.getLong("id"),
                            rs.getLong("prestamo_id"),
                            rs.getInt("numero"),
                            rs.getObject("fecha_vencimiento", LocalDate.class),
                            rs.getBigDecimal("capital"),
                            rs.getBigDecimal("interes"),
                            rs.getBigDecimal("mora"),
                            EstadoCuota.valueOf(rs.getString("estado"))
                    ));
                }
            }
        });
    }

    @Override
    public void actualizarEstado(Cuota cuota) {
        ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(ACTUALIZAR_ESTADO)) {
                stmt.setString(1, cuota.getEstado().name());
                stmt.setLong(2, cuota.getId());
                stmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public boolean existeNoPagada(Long prestamoId) {
        return existeConEstado(prestamoId, EstadoCuota.PENDIENTE, EstadoCuota.VENCIDA);
    }

    @Override
    public boolean existeVencida(Long prestamoId) {
        return existeConEstado(prestamoId, EstadoCuota.VENCIDA);
    }

    private boolean existeConEstado(Long prestamoId, EstadoCuota... estados) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(EXISTE_CON_ESTADO)) {
                stmt.setLong(1, prestamoId);
                var nombres = Arrays.stream(estados).map(Enum::name).toArray(String[]::new);
                stmt.setArray(2, conn.createArrayOf("varchar", nombres));
                try (var rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getBoolean(1);
                }
            }
        });
    }
}
