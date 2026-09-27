package com.projects.application.port.out;
import com.projects.domain.model.seguridad.Usuario;
import java.util.Optional;

public interface UsuarioRepository {


     Optional<Usuario> save(Usuario usuario);

     boolean existsByEmail(String email);

     Optional<Usuario> findByEmail(String email);


}
