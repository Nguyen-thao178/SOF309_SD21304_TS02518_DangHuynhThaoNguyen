package com.cafe.utils;

public final class BarcodeUtil {
    private BarcodeUtil() {
    }

    public static String normalize(String barcode) {
        if (barcode == null) {
            return null;
        }

        String normalized = barcode
                .replaceAll("[\\x00-\\x1F\\x7F]", "")
                .trim();

        // AIM symbology identifier, for example ]E0 for EAN/UPC scanners.
        if (normalized.matches("^][A-Za-z][0-9].*")) {
            normalized = normalized.substring(3);
        }

        normalized = normalized.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static boolean isValidEan13(String barcode) {
        String normalized = normalize(barcode);
        if (normalized == null || !normalized.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;
        for (int index = 0; index < 12; index++) {
            int digit = normalized.charAt(index) - '0';
            sum += index % 2 == 0 ? digit : digit * 3;
        }
        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        return expectedCheckDigit == normalized.charAt(12) - '0';
    }
}
