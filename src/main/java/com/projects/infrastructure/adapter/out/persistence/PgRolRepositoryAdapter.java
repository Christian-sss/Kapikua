package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.RolRepository;
import com.projects.domain.model.seguridad.Rol;
import java.sql.ResultSet;
import java.util.Optional;

public class PgRolRepositoryAdapter extends BaseRepository implements RolRepository {

    private static final String BUSCAR_ROL = """
            
            
            SELECT r.id,r.nombre_rol
            FROM seguridad.rol r\s
            WHERE r.nombre_rol = ?

            """;

    private static final String BUSCAR_ROL_POR_ID = """
            SELECT r.id, r.nombre_rol
            FROM seguridad.rol r
            WHERE r.id = ?
            """;

    @Override
    public Optional<Rol> findById(Long id) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(BUSCAR_ROL_POR_ID)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new Rol(rs.getLong("id"), rs.getString("nombre_rol")));
                    }
                    return Optional.empty();
                }
            }
        });
    }


    @Override
    public Optional<Rol> findByNombre(String nombreRol) {



        return ejecutar( conn -> {

            try(var stmt = conn.prepareStatement(BUSCAR_ROL)) {
                stmt.setString(1,nombreRol);

                try(var rs = stmt.executeQuery()) {
                    if(rs.next()) {
                        return Optional.of( new Rol(
                                rs.getLong("id"),
                                rs.getString("nombre_rol")
                                )
                        );
                    }

                    return Optional.empty();
                }
            }

        }

        );




    }
}
