package com.cafe.service;

import com.cafe.dao.SystemSettingsDAO;
import com.cafe.model.SystemSetting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SystemSettingsService {
    public static final Set<String> EDITABLE_KEYS = Set.of(
            "store_name", "store_tagline", "hotline", "address",
            "weekday_hours", "weekend_hours", "social_links", "currency",
            "deposit_percent", "loyalty_vnd_per_point", "barcode_scanner_enabled"
    );

    private static final Map<String, String> DEFAULTS = defaults();
    private static final long CACHE_MILLIS = 30_000L;
    private static volatile Map<String, String> cachedSettings = DEFAULTS;
    private static volatile long cacheExpiresAt;

    private SystemSettingsService() {
    }

    public static Map<String, String> getSettings() {
        long now = System.currentTimeMillis();
        if (now < cacheExpiresAt) return cachedSettings;
        synchronized (SystemSettingsService.class) {
            if (now < cacheExpiresAt) return cachedSettings;
            LinkedHashMap<String, String> loaded = new LinkedHashMap<>(DEFAULTS);
            List<SystemSetting> databaseSettings = new SystemSettingsDAO().getAll();
            for (SystemSetting setting : databaseSettings) {
                if (setting.getKey() != null && setting.getValue() != null) {
                    loaded.put(setting.getKey(), setting.getValue());
                }
            }
            loaded.put("currency_symbol", currencySymbol(loaded.get("currency")));
            cachedSettings = Collections.unmodifiableMap(loaded);
            cacheExpiresAt = now + CACHE_MILLIS;
            return cachedSettings;
        }
    }

    public static void invalidate() {
        cacheExpiresAt = 0L;
    }

    public static String get(String key) {
        return getSettings().getOrDefault(key, DEFAULTS.getOrDefault(key, ""));
    }

    public static int getPositiveInt(String key, int fallback) {
        try {
            int value = Integer.parseInt(get(key));
            return value > 0 ? value : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static boolean getBoolean(String key, boolean fallback) {
        String value = get(key);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    public static ValidationResult validate(Map<String, String> submitted) {
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        for (String key : EDITABLE_KEYS) {
            String value = submitted.get(key);
            if (value == null) return ValidationResult.error("Thiếu cấu hình: " + key + ".");
            normalized.put(key, value.trim());
        }

        if (!lengthBetween(normalized.get("store_name"), 2, 100))
            return ValidationResult.error("Tên cửa hàng phải từ 2 đến 100 ký tự.");
        if (!lengthBetween(normalized.get("store_tagline"), 2, 200))
            return ValidationResult.error("Mô tả cửa hàng phải từ 2 đến 200 ký tự.");
        if (!lengthBetween(normalized.get("address"), 5, 300))
            return ValidationResult.error("Địa chỉ phải từ 5 đến 300 ký tự.");
        if (!normalized.get("hotline").matches("[0-9+(). -]{3,30}"))
            return ValidationResult.error("Số hotline không hợp lệ.");
        if (!lengthBetween(normalized.get("weekday_hours"), 3, 80)
                || !lengthBetween(normalized.get("weekend_hours"), 3, 80))
            return ValidationResult.error("Giờ mở cửa không hợp lệ.");
        if (normalized.get("social_links").length() > 200)
            return ValidationResult.error("Thông tin mạng xã hội tối đa 200 ký tự.");

        String currency = normalized.get("currency").toUpperCase(Locale.ROOT);
        if (!Set.of("VND", "USD", "EUR").contains(currency))
            return ValidationResult.error("Đơn vị tiền tệ không được hỗ trợ.");
        normalized.put("currency", currency);

        Integer deposit = parseInteger(normalized.get("deposit_percent"));
        if (deposit == null || deposit < 1 || deposit > 90)
            return ValidationResult.error("Phần trăm tiền cọc phải từ 1 đến 90.");
        Integer loyalty = parseInteger(normalized.get("loyalty_vnd_per_point"));
        if (loyalty == null || loyalty < 100 || loyalty > 1_000_000)
            return ValidationResult.error("Số tiền cho một điểm phải từ 100 đến 1.000.000.");
        if (!Set.of("true", "false").contains(normalized.get("barcode_scanner_enabled").toLowerCase(Locale.ROOT)))
            return ValidationResult.error("Trạng thái máy quét barcode không hợp lệ.");
        normalized.put("barcode_scanner_enabled",
                normalized.get("barcode_scanner_enabled").toLowerCase(Locale.ROOT));

        return new ValidationResult(true, Collections.unmodifiableMap(normalized), null);
    }

    private static boolean lengthBetween(String value, int min, int max) {
        return value != null && value.length() >= min && value.length() <= max;
    }

    private static Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String currencySymbol(String currency) {
        if ("USD".equalsIgnoreCase(currency)) return "$";
        if ("EUR".equalsIgnoreCase(currency)) return "€";
        return "₫";
    }

    private static Map<String, String> defaults() {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        values.put("store_name", "Chidori Coffee");
        values.put("store_tagline", "Thương hiệu cà phê rang xay nguyên chất");
        values.put("hotline", "1900 1234");
        values.put("address", "123 Đường Cà Phê, Quận 1, TP.HCM");
        values.put("weekday_hours", "Thứ 2 - Thứ 6: 7:00 - 21:00");
        values.put("weekend_hours", "Thứ 7 - CN: 8:00 - 22:00");
        values.put("social_links", "Facebook | Instagram | Tiktok");
        values.put("currency", "VND");
        values.put("currency_symbol", "₫");
        values.put("deposit_percent", "30");
        values.put("loyalty_vnd_per_point", "1000");
        values.put("barcode_scanner_enabled", "true");
        return Collections.unmodifiableMap(values);
    }

    public record ValidationResult(boolean valid, Map<String, String> values, String message) {
        static ValidationResult error(String message) {
            return new ValidationResult(false, Map.of(), message);
        }
    }
}
