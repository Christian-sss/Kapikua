package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.UsuarioRepository;
import com.projects.domain.model.seguridad.Usuario;

import java.sql.Statement;
import java.util.Optional;

public class PgUsuarioRepositoryAdapter extends BaseRepository implements UsuarioRepository {

    private static final String EXISTS_EMAIL = """
            
                    SELECT 1
                    FROM seguridad.usuario u
                    WHERE u.correo = ?
            
            """;


    private static final String SAVE_USUARIO = """
            
            
                INSERT INTO seguridad.usuario (rol_id, correo, password_hash, activo)
                VALUES (?,?, ?,?);
            
            """;

    @Override
    public Optional<Usuario> save(Usuario usuario) {

        return ejecutar(conn -> {

            try (var stmt = conn.prepareStatement(SAVE_USUARIO, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1, usuario.getRolId());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getPasswordHash());
                stmt.setBoolean(4, usuario.getActivo());

                int filaAfectada = stmt.executeUpdate();

                if (filaAfectada == 0) {
                    return Optional.empty();
                }

                try (var rs = stmt.getGeneratedKeys()) {

                    if (rs.next()) {
                        usuario.setId(rs.getLong("id"));
                        return Optional.of(usuario);
                    }


                    return Optional.empty();

                }
            }

        });


    }

    @Override
    public boolean existsByEmail(String email) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(EXISTS_EMAIL)) {
                stmt.setString(1, email);
                try (var rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
}
