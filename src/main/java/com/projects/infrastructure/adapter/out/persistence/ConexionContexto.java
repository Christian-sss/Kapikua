package com.projects.infrastructure.adapter.out.persistence;

import java.sql.Connection;

public class ConexionContexto {


    private static final ThreadLocal<Connection> ACTUAL = new ThreadLocal<>();


    private ConexionContexto() {
    }


    public static void cambiar(Connection connection) {
        ACTUAL.set(connection);
    }


    public static Connection get() {
        return ACTUAL.get();
    }

    public static void clear() {
        ACTUAL.remove();
    }

}
