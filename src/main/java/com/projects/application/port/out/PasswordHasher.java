package com.projects.application.port.out;
public interface PasswordHasher {

    String encriptar(String password);

    boolean validar(String passwordText, String passwordHash);



}
