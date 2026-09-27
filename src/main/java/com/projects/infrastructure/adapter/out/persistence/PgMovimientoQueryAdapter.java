package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.dto.response.MovimientoHistorialResponse;
import com.projects.application.port.out.MovimientoQuery;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PgMovimientoQueryAdapter extends BaseRepository implements MovimientoQuery {

    private static final String BUSCAR_POR_BILLETERA = """
            SELECT t.id AS transaccion_id, t.fecha, tt.codigo AS tipo_codigo,
                   m.signo, m.monto, m.saldo_posterior, t.estado
            FROM billetera.movimiento m
            JOIN billetera.transaccion t ON t.id = m.transaccion_id
            JOIN billetera.tipo_transaccion tt ON tt.id = t.tipo_id
            WHERE m.billetera_id = ?
            ORDER BY t.fecha DESC
            """;

    @Override
    public List<MovimientoHistorialResponse> buscarPorBilleteraId(Long billeteraId) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(BUSCAR_POR_BILLETERA)) {
                stmt.setLong(1, billeteraId);

                try (var rs = stmt.executeQuery()) {
                    List<MovimientoHistorialResponse> resultado = new ArrayList<>();
                    while (rs.next()) {
                        resultado.add(new MovimientoHistorialResponse(
                                rs.getLong("transaccion_id"),
                                rs.getObject("fecha", OffsetDateTime.class),
                                rs.getString("tipo_codigo"),
                                rs.getString("signo").charAt(0),
                                rs.getBigDecimal("monto"),
                                rs.getBigDecimal("saldo_posterior"),
                                rs.getString("estado")
                        ));
                    }
                    return resultado;
                }
            }
        });
    }
}
