package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.BilleteraRepository;
import com.projects.domain.model.billetera.Billetera;

import java.sql.Statement;
import java.util.Optional;

public class PgBilleteraRepositoryAdapter extends BaseRepository implements BilleteraRepository {


    private static final String SAVE_BILLETERA = """
            
            INSERT INTO billetera.billetera (cliente_id, saldo, estado)
            VALUES ( ?,?,? );

            """;


    @Override
    public Optional<Billetera> save(Billetera billetera) {

        return ejecutar(conn -> {

            try(var stmt = conn.prepareStatement(SAVE_BILLETERA, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1,billetera.getClienteId());
                stmt.setBigDecimal(2,billetera.getSaldo());
                stmt.setString(3,billetera.getEstadoBilletera().name());


                int filaAfectada = stmt.executeUpdate();

                if(filaAfectada == 0) {
                    return Optional.empty();
                }


                try (var rs = stmt.getGeneratedKeys()) {

                    if(rs.next()) {

                        billetera.setId(rs.getLong("id"));
                        return Optional.of(billetera);

                    }

                    return Optional.empty();

                }
            }
        });

    }
}
