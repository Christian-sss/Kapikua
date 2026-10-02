package com.projects.infrastructure.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public final class PostressqlConexion {

    private static final String ARCHIVO = "config.properties";

    private static final Properties CONFIG = cargar();

    private PostressqlConexion() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                valor("KAPIKUA_DB_URL", "db.url"),
                valor("KAPIKUA_DB_USER", "db.user"),
                valor("KAPIKUA_DB_PASSWORD", "db.password"));
    }

    // La variable de entorno tiene prioridad; si no existe, se usa config.properties.
    private static String valor(String variableEntorno, String clave) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = CONFIG.getProperty(clave);
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Falta '" + clave + "' en " + ARCHIVO + " (o la variable de entorno " + variableEntorno + ").");
        }
        return valor.trim();
    }

    // Busca config.properties junto al jar y, si no está, en la carpeta de trabajo (el IDE).
    private static Properties cargar() {
        var props = new Properties();
        for (Path carpeta : List.of(carpetaDelJar(), Path.of(""))) {
            Path archivo = carpeta.resolve(ARCHIVO);
            if (Files.isRegularFile(archivo)) {
                try (var in = Files.newInputStream(archivo)) {
                    props.load(in);
                    return props;
                } catch (IOException ignorada) {
                    // Se intenta con la siguiente ubicación.
                }
            }
        }
        return props;
    }

    private static Path carpetaDelJar() {
        try {
            return Path.of(PostressqlConexion.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException | RuntimeException e) {
            return Path.of("");
        }
    }
}
