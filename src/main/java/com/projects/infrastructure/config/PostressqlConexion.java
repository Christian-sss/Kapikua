package com.projects.infrastructure.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostressqlConexion {
/*
    private static final String URL = "jdbc:postgresql://localhost:5432/kapikua_db";
    private static final String USER = "chris";
    private static final String PASS = "1234";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL,USER,PASS);


    }
*/

    private static final String URL =
            "jdbc:postgresql://caboose.proxy.rlwy.net:48941/railway";

    private static final String USER = "postgres";

    private static final String PASS = "OMeXLeCopMQQzldJZQGjMefIejFoYEak";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }





}
