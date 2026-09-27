package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.dto.response.CuotaCronogramaResponse;
import com.projects.application.dto.response.PrestamoResumenResponse;
import com.projects.application.port.out.PrestamoQuery;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PgPrestamoQueryAdapter extends BaseRepository implements PrestamoQuery {

    private static final String BUSCAR_POR_CLIENTE = """
            SELECT p.id, pc.nombre, pc.tipo, p.monto_desembolsado, p.saldo_capital, p.tcea,
                   p.estado, p.fecha_desembolso,
                   (SELECT COUNT(*) FROM credito.cuota c WHERE c.prestamo_id = p.id AND c.estado = 'PAGADA') AS pagadas,
                   (SELECT COUNT(*) FROM credito.cuota c WHERE c.prestamo_id = p.id) AS totales,
                   prox.fecha_vencimiento AS proximo_vencimiento,
                   prox.capital + prox.interes + prox.mora AS proxima_cuota
            FROM credito.prestamo p
            JOIN credito.solicitud_credito s ON s.id = p.solicitud_id
            JOIN credito.producto_crediticio pc ON pc.id = s.producto_id
            LEFT JOIN LATERAL (
                SELECT c.fecha_vencimiento, c.capital, c.interes, c.mora
                FROM credito.cuota c
                WHERE c.prestamo_id = p.id AND c.estado <> 'PAGADA'
                ORDER BY c.numero
                LIMIT 1
            ) prox ON TRUE
            WHERE s.cliente_id = ?
            ORDER BY p.fecha_desembolso DESC
            """;

    private static final String CLIENTE_DEL_PRESTAMO = """
            SELECT s.cliente_id
            FROM credito.prestamo p
            JOIN credito.solicitud_credito s ON s.id = p.solicitud_id
            WHERE p.id = ?
            """;

    private static final String CUOTAS = """
            SELECT numero, fecha_vencimiento, capital, interes, mora, estado
            FROM credito.cuota
            WHERE prestamo_id = ?
            ORDER BY numero
            """;

    @Override
    public List<PrestamoResumenResponse> buscarPorClienteId(Long clienteId) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(BUSCAR_POR_CLIENTE)) {
                stmt.setLong(1, clienteId);
                try (var rs = stmt.executeQuery()) {
                    List<PrestamoResumenResponse> prestamos = new ArrayList<>();
                    while (rs.next()) {
                        prestamos.add(new PrestamoResumenResponse(
                                rs.getLong("id"),
                                rs.getString("nombre"),
                                rs.getString("tipo"),
                                rs.getBigDecimal("monto_desembolsado"),
                                rs.getBigDecimal("saldo_capital"),
                                rs.getBigDecimal("tcea"),
                                rs.getString("estado"),
                                rs.getObject("fecha_desembolso", OffsetDateTime.class),
                                rs.getInt("pagadas"),
                                rs.getInt("totales"),
                                rs.getObject("proximo_vencimiento", LocalDate.class),
                                rs.getBigDecimal("proxima_cuota")
                        ));
                    }
                    return prestamos;
                }
            }
        });
    }

    @Override
    public Optional<Long> buscarClienteIdDelPrestamo(Long prestamoId) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(CLIENTE_DEL_PRESTAMO)) {
                stmt.setLong(1, prestamoId);
                try (var rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getLong("cliente_id")) : Optional.empty();
                }
            }
        });
    }

    @Override
    public List<CuotaCronogramaResponse> buscarCuotas(Long prestamoId) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(CUOTAS)) {
                stmt.setLong(1, prestamoId);
                try (var rs = stmt.executeQuery()) {
                    List<CuotaCronogramaResponse> cuotas = new ArrayList<>();
                    while (rs.next()) {
                        cuotas.add(new CuotaCronogramaResponse(
                                rs.getInt("numero"),
                                rs.getObject("fecha_vencimiento", LocalDate.class),
                                rs.getBigDecimal("capital"),
                                rs.getBigDecimal("interes"),
                                rs.getBigDecimal("mora"),
                                rs.getString("estado")
                        ));
                    }
                    return cuotas;
                }
            }
        });
    }
}
