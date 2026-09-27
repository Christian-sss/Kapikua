package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.SolicitudCreditoRepository;
import com.projects.domain.model.credito.SolicitudCredito;

import java.sql.Statement;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PgSolicitudCreditoRepositoryAdapter extends BaseRepository implements SolicitudCreditoRepository {

    private static final String INSERT = """
            INSERT INTO credito.solicitud_credito
                (cliente_id, producto_id, monto_solicitado, plazo_meses, estado, score_obtenido, motivo_rechazo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    @Override
    public Optional<SolicitudCredito> guardar(SolicitudCredito solicitud) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, solicitud.getClienteId());
                stmt.setLong(2, solicitud.getProductoId());
                stmt.setBigDecimal(3, solicitud.getMontoSolicitado());
                stmt.setInt(4, solicitud.getPlazoMeses());
                stmt.setString(5, solicitud.getEstado().name());
                if (solicitud.getScoreObtenido() != null) {
                    stmt.setInt(6, solicitud.getScoreObtenido());
                } else {
                    stmt.setNull(6, Types.INTEGER);
                }
                stmt.setString(7, solicitud.getMotivoRechazo());

                if (stmt.executeUpdate() == 0) {
                    return Optional.empty();
                }

                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        solicitud.setId(rs.getLong("id"));
                        solicitud.setFechaSolicitud(rs.getObject("fecha_solicitud", OffsetDateTime.class));
                        return Optional.of(solicitud);
                    }
                    return Optional.empty();
                }
            }
        });
    }
}
