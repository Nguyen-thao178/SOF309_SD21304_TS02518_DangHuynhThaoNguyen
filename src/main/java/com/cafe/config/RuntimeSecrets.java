package com.cafe.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads runtime-only secrets without placing them in the application WAR.
 * Environment variables and JVM properties take precedence over the optional
 * Tomcat file: ${catalina.base}/conf/chidori-secrets.properties.
 */
public final class RuntimeSecrets {
    private static final Properties FILE_VALUES = loadFileValues();

    private RuntimeSecrets() {
    }

    public static String first(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (isBlank(value)) value = System.getProperty(name);
            if (isBlank(value)) value = FILE_VALUES.getProperty(name);
            if (!isBlank(value)) return value.trim();
        }
        return null;
    }

    public static String valueOrDefault(String name, String defaultValue) {
        String value = first(name);
        return value == null ? defaultValue : value;
    }

    private static Properties loadFileValues() {
        Properties values = new Properties();
        Path file = configuredFile();
        if (file == null || !Files.isRegularFile(file)) return values;

        try (InputStream input = Files.newInputStream(file)) {
            values.load(input);
        } catch (IOException exception) {
            System.err.println("Không thể đọc cấu hình bí mật của Chidori từ Tomcat/conf.");
        }
        return values;
    }

    private static Path configuredFile() {
        String explicit = System.getenv("CHIDORI_SECRETS_FILE");
        if (isBlank(explicit)) explicit = System.getProperty("CHIDORI_SECRETS_FILE");
        if (!isBlank(explicit)) return Path.of(explicit.trim());

        String catalinaBase = System.getProperty("catalina.base");
        if (isBlank(catalinaBase)) return null;
        return Path.of(catalinaBase, "conf", "chidori-secrets.properties");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
