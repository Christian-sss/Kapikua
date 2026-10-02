package com.projects.application.service;

import com.projects.application.dto.command.IniciarSesionCommand;
import com.projects.application.dto.response.SesionIniciadaResponse;
import com.projects.application.port.in.IniciarSesionUseCase;
import com.projects.application.port.out.PasswordHasher;
import com.projects.application.port.out.RolRepository;
import com.projects.application.port.out.SesionContexto;
import com.projects.application.port.out.SesionRepository;
import com.projects.application.port.out.UsuarioRepository;
import com.projects.domain.model.seguridad.Rol;
import com.projects.domain.result.Result;
import com.projects.domain.result.UsuarioError;

import java.util.UUID;

public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordHasher passwordHasher;
    private final SesionContexto sesionContexto;
    private final SesionRepository sesionRepository;

    public IniciarSesionService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                                PasswordHasher passwordHasher, SesionContexto sesionContexto,
                                SesionRepository sesionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordHasher = passwordHasher;
        this.sesionContexto = sesionContexto;
        this.sesionRepository = sesionRepository;
    }

    @Override
    public Result<SesionIniciadaResponse> iniciarSesion(IniciarSesionCommand command) {

        if (command == null || command.email() == null || command.email().trim().isEmpty()
                || command.password() == null || command.password().isEmpty()) {
            return Result.failure("COMANDO_INVALIDO", "El email y la contraseña son obligatorios.");
        }

        var emailNormalizado = command.email().trim().toLowerCase();

        var usuarioEncontrado = usuarioRepository.findByEmail(emailNormalizado);


        if (usuarioEncontrado.isEmpty()) {
            return Result.failure(UsuarioError.CREDENCIALES_INVALIDAS.name(), "Email o contraseña incorrectos.");
        }

        var usuario = usuarioEncontrado.get();

        if (!passwordHasher.validar(command.password(), usuario.getPasswordHash())) {
            return Result.failure(UsuarioError.CREDENCIALES_INVALIDAS.name(), "Email o contraseña incorrectos.");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return Result.failure(UsuarioError.USUARIO_INACTIVO.name(), "La cuenta se encuentra inactiva.");
        }

        var rol = rolRepository.findById(usuario.getRolId())
                .map(Rol::getNombreRol)
                .orElseThrow();

        var token = UUID.randomUUID().toString();
        if (!sesionRepository.registrar(usuario.getId(), token)) {
            return Result.failure(UsuarioError.SESION_YA_ACTIVA.name(),
                    "Esta cuenta ya tiene una sesión abierta en otro equipo o ventana.");
        }

        var sesion = new SesionIniciadaResponse(usuario.getId(), usuario.getEmail(), usuario.getRolId(), rol, token);
        sesionContexto.iniciar(sesion);

        return Result.success(sesion);
    }
}
