package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.TransaccionRepository;
import com.projects.domain.model.billetera.Transaccion;

import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PgTransaccionRepositoryAdapter extends BaseRepository implements TransaccionRepository {

    private static final String INSERT = """
            INSERT INTO billetera.transaccion (tipo_id, monto, comision, estado, referencia)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Override
    public Optional<Transaccion> guardar(Transaccion transaccion) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, transaccion.getTipoId());
                stmt.setBigDecimal(2, transaccion.getMonto());
                stmt.setBigDecimal(3, transaccion.getComision());
                stmt.setString(4, transaccion.getEstado().name());
                stmt.setString(5, transaccion.getReferencia());

                int filas = stmt.executeUpdate();

                if (filas == 0) {
                    return Optional.empty();
                }

                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaccion.setId(rs.getLong("id"));
                        transaccion.setFecha(rs.getObject("fecha", OffsetDateTime.class));
                        return Optional.of(transaccion);
                    }
                    return Optional.empty();
                }
            }
        });
    }
}
