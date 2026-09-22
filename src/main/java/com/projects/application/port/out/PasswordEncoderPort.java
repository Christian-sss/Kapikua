package com.projects.application.port.out;
public interface PasswordEncoderPort {

    String encriptar(String password);

    boolean validar(String passwordText, String passwordHash);



}
