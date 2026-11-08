package com.cafe.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class DatabaseConfig {
    private static final Properties LOCAL = loadLocal();

    public static final String URL = configured(
            "CAFE_DB_URL", "db.url",
            "jdbc:sqlserver://localhost:1433;databaseName=CafeDB;encrypt=true;trustServerCertificate=true");
    public static final String USER = configured("CAFE_DB_USER", "db.user", "sa");
    public static final String PASSWORD = configured("CAFE_DB_PASSWORD", "db.password", "");

    private DatabaseConfig() {
    }

    private static String configured(String environmentName, String propertyName,
                                     String defaultValue) {
        String value = System.getenv(environmentName);
        if (value == null || value.isBlank()) value = System.getProperty(environmentName);
        if (value == null || value.isBlank()) value = LOCAL.getProperty(propertyName);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static Properties loadLocal() {
        Properties properties = new Properties();
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null || catalinaBase.isBlank()) return properties;
        Path path = Path.of(catalinaBase, "conf", "chidori-db.properties");
        if (!Files.isRegularFile(path)) return properties;
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException ignored) {
            // Connection code returns a safe database error if configuration is unavailable.
        }
        return properties;
    }
}
