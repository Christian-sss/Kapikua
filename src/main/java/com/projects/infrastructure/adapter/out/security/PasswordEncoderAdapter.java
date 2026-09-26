package com.projects.infrastructure.adapter.out.security;


import com.projects.application.port.out.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoderAdapter implements PasswordHasher {


    private static final int COSTO_HASH = 12;


    @Override
    public String encriptar(String password) {
        return BCrypt.hashpw(password,BCrypt.gensalt(COSTO_HASH));
    }

    @Override
    public boolean validar(String passwordText, String passwordHash) {

        if (passwordText == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        return BCrypt.checkpw(passwordText,passwordHash);
    }
}
