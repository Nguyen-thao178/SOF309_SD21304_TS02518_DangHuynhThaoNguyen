package com.cafe.payment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayConfig {
    public static final String VERSION = "2.1.0";
    public static final String COMMAND = "pay";
    public static final String CURRENCY = "VND";
    public static final String LOCALE = "vn";

    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    public static final String TMN_CODE =
            configuredValue("VNPAY_TMN_CODE", "vnpay.tmnCode", "");
    public static final String SECRET_KEY =
            configuredValue("VNPAY_HASH_SECRET", "vnpay.hashSecret", "");
    public static final String RETURN_URL =
            configuredValue("VNPAY_RETURN_URL", "vnpay.returnUrl",
                    "http://localhost:8080/ChidoriManagementSystem/vnpay-return");
    public static final String IPN_URL =
            configuredValue("VNPAY_IPN_URL", "vnpay.ipnUrl",
                    "http://localhost:8080/ChidoriManagementSystem/vnpay-ipn");
    public static final String PAY_URL =
            configuredValue("VNPAY_PAY_URL", "vnpay.payUrl",
                    "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
    public static final String BANK_CODE =
            configuredValue("VNPAY_BANK_CODE", "vnpay.bankCode",
                    PAY_URL.contains("sandbox.vnpayment.vn") ? "NCB" : "");

    private VNPayConfig() {
    }

    public static boolean isConfigured() {
        return !TMN_CODE.isBlank() && !SECRET_KEY.isBlank();
    }

    public static boolean isSandbox() {
        return PAY_URL.contains("sandbox.vnpayment.vn");
    }

    public static void requireConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "VNPay chưa được cấu hình. Cần VNPAY_TMN_CODE và VNPAY_HASH_SECRET.");
        }
    }

    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] hash = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    private static String configuredValue(String environmentName, String propertyName,
                                          String defaultValue) {
        String value = System.getenv(environmentName);
        if (value == null || value.isBlank()) {
            value = System.getProperty(environmentName);
        }
        if (value == null || value.isBlank()) {
            value = LOCAL_CONFIG.getProperty(propertyName);
        }
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static Properties loadLocalConfig() {
        Properties properties = new Properties();
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null || catalinaBase.isBlank()) {
            return properties;
        }

        Path configPath = Path.of(catalinaBase, "conf", "chidori-vnpay.properties");
        if (!Files.isRegularFile(configPath)) {
            return properties;
        }
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException ignored) {
            // The payment servlet reports a safe configuration error when values are unavailable.
        }
        return properties;
    }
}
