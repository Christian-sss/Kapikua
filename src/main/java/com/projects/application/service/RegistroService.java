package com.projects.application.service;


import com.projects.application.dto.response.UsuarioRegistradoResponse;
import com.projects.application.port.in.RegistrarUsuarioUseCase;
import com.projects.application.port.out.ClienteRepository;
import com.projects.application.port.out.PasswordEncoderPort;
import com.projects.application.port.out.UsuarioRepository;
import com.projects.domain.model.billetera.Cliente;
import com.projects.domain.model.seguridad.Usuario;

public class RegistroService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public RegistroService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, PasswordEncoderPort passwordEncoderPort) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public UsuarioRegistradoResponse ejecutar(RegistrarUsuarioUseCase usuarioUseCase) {

        var usuario = new Usuario();

        var cliente = new Cliente();






        return null;


    }
}
