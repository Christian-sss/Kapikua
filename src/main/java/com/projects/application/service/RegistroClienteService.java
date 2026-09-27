package com.projects.application.service;


import com.projects.application.dto.command.RegistrarClienteCommand;
import com.projects.application.dto.response.ClienteRegistradoResponse;
import com.projects.application.port.in.RegistrarClienteUseCase;
import com.projects.application.port.out.*;
import com.projects.domain.model.seguridad.Usuario;
import com.projects.domain.result.ClienteError;
import com.projects.domain.result.Result;
import com.projects.domain.model.billetera.*;
import com.projects.domain.result.UsuarioError;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class RegistroClienteService implements RegistrarClienteUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordHasher passwordHasher;
    private final BilleteraRepository billeteraRepository;
    private final RolRepository rolRepository;
    private final TransactionManager transactionManager;

    public RegistroClienteService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository , PasswordHasher passwordHasher, BilleteraRepository billeteraRepository, RolRepository rolRepository, TransactionManager transactionManager) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordHasher = passwordHasher;
        this.billeteraRepository = billeteraRepository;
        this.rolRepository = rolRepository;
        this.transactionManager = transactionManager;
    }

    @Override
    public Result<ClienteRegistradoResponse> ejecutar(RegistrarClienteCommand command) {


        if (command == null) {

            return Result.failure("COMANDO_INVALIDO", "the command cannnot data nulls.");
        }


        OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);

        var validacionCliente = Cliente.validarDatos(
                command.nombres(),
                command.apellidos(),
                command.dni(),
                command.celular(),
                command.fechaNacimiento(),
                dateTime);

        if (validacionCliente.isFailure()) {
            return Result.failure(validacionCliente.getError().get());
        }


        var validacionUsuario = Usuario.validarDatos(
                command.email(),
                command.password());


        if (validacionUsuario.isFailure()) {
            return Result.failure(validacionUsuario.getError().get());
        }


        var passwordHash = passwordHasher.encriptar(command.password());

        return transactionManager.enTransaccion(()-> {

            if(usuarioRepository.existsByEmail(command.email().trim().toLowerCase())) {
                return Result.failure(UsuarioError.EMAIL_DUPLICADO.name(), "El email no debe ser duplicado.");
            }

            if(clienteRepository.existsByCelular(command.celular().trim())) {

                return Result.failure(ClienteError.CELULAR_DUPLICADO.name(), "Celular duplicado, ya existe.");
            }

            if(clienteRepository.existsByDni(command.dni().trim())) {
                return Result.failure(ClienteError.DNI_DUPLICADO.name(), "El dni ya existe.");
            }



            var rolUsuario = rolRepository
                    .findByNombre("CLIENTE")
                    .orElseThrow(
                            () -> new IllegalArgumentException("")
                    );

            var usuarioResult = Usuario.crear(
                    command.email(),
                    passwordHash,
                    rolUsuario.getId(),
                    dateTime);

            if(usuarioResult.isFailure()) {
                return Result.failure(usuarioResult.getError().get());
            }

            var usuarioSaved = usuarioRepository.save(usuarioResult.getValue().get());


            var clienteResult = Cliente.crear(
                    usuarioSaved.get().getId(),
                    command.nombres(),
                    command.apellidos(),
                    command.dni(),
                    command.celular(),
                    command.fechaNacimiento(),
                    dateTime
                    );

            if(clienteResult.isFailure()) {
                return Result.failure(clienteResult.getError().get());
            }

            var clienteSaved = clienteRepository.save(clienteResult.getValue().get());

            var billeteraResult = Billetera.abrirPara(
                    clienteSaved.get().getId(),
                    dateTime
                    );

            if (billeteraResult.isFailure()) {
                return Result.failure(billeteraResult.getError().get());
            }

            billeteraRepository.save(billeteraResult.getValue().get());

            return Result.success(new ClienteRegistradoResponse(
                    clienteSaved.get().getId(),
                    usuarioSaved.get().getEmail(),
                    "Successfull"
            ));

        });


    }


}
