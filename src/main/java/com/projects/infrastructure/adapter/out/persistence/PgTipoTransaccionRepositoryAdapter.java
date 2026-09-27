package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.TipoTransaccionRepository;

import java.util.Optional;

public class PgTipoTransaccionRepositoryAdapter extends BaseRepository implements TipoTransaccionRepository {

    private static final String FIND_ID_BY_CODIGO = """
            SELECT id
            FROM billetera.tipo_transaccion
            WHERE codigo = ?
            """;

    @Override
    public Optional<Long> findIdByCodigo(String codigo) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(FIND_ID_BY_CODIGO)) {
                stmt.setString(1, codigo);

                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong("id"));
                    }
                    return Optional.empty();
                }
            }
        });
    }
}
