package com.cafe.security;

import com.cafe.model.User;

import java.util.Locale;
import java.util.Set;

/**
 * Central role/route policy used by the authentication filter and login flow.
 * Keeping this matrix in one place prevents JSP visibility rules from becoming
 * the only access-control layer.
 */
public final class RoleAccessPolicy {
    private static final Set<String> COMMON_PATHS = Set.of(
            "/logout", "/change-password", "/chat"
    );

    private static final Set<String> STAFF_PATHS = Set.of(
            "/home", "/menu", "/promotion", "/search", "/product",
            "/add-to-cart", "/cart", "/cart/scan", "/update-cart", "/remove-cart",
            "/checkout", "/vnpay-pay", "/history", "/export-history", "/receipt",
            "/deposit-orders", "/loyalty"
    );

    private static final Set<String> MANAGER_PATHS = Set.of(
            "/history", "/export-history", "/receipt",
            "/admin/users", "/admin/products", "/admin/report",
            "/admin/report/chat-history/print"
    );

    private static final Set<String> ADMIN_PATHS = Set.of(
            "/admin/report", "/admin/report/chat-history/print",
            "/admin/contacts", "/admin/settings"
    );

    private static final Set<String> CUSTOMER_PATHS = Set.of(
            "/home", "/menu", "/promotion", "/search", "/product",
            "/add-to-cart", "/cart", "/update-cart", "/remove-cart",
            "/checkout", "/vnpay-pay", "/history", "/export-history", "/receipt",
            "/deposit-orders", "/loyalty"
    );

    private RoleAccessPolicy() {
    }

    public static boolean canAccess(User user, String path) {
        if (user == null || path == null) return false;
        String normalizedPath = normalizePath(path);
        if (COMMON_PATHS.contains(normalizedPath)) return true;

        String role = user.getRole() == null
                ? ""
                : user.getRole().trim().toLowerCase(Locale.ROOT);
        return switch (role) {
            case "staff" -> STAFF_PATHS.contains(normalizedPath);
            case "manager" -> MANAGER_PATHS.contains(normalizedPath);
            case "admin" -> ADMIN_PATHS.contains(normalizedPath);
            case "customer", "member" -> CUSTOMER_PATHS.contains(normalizedPath);
            default -> false;
        };
    }

    public static String landingPath(User user) {
        if (user == null || user.getRole() == null) return "/login";
        return switch (user.getRole().trim().toLowerCase(Locale.ROOT)) {
            case "admin" -> "/admin/report";
            case "manager" -> "/history";
            case "staff", "customer", "member" -> "/home";
            default -> "/login";
        };
    }

    public static boolean isCustomer(User user) {
        String role = normalizedRole(user);
        return "customer".equals(role) || "member".equals(role);
    }

    public static boolean isStaff(User user) {
        return "staff".equals(normalizedRole(user));
    }

    /** Customers may reserve products, but may never create a direct-payment order. */
    public static boolean canCreateOrderType(User user, String orderType) {
        if (isCustomer(user)) return "deposit".equals(orderType);
        return isStaff(user) && ("direct".equals(orderType) || "deposit".equals(orderType));
    }

    /** Customers pay their deposit online; cash remains a counter-only option for staff. */
    public static boolean canUsePaymentMethod(User user, String paymentMethod) {
        if (isCustomer(user)) return "vnpay".equals(paymentMethod);
        return isStaff(user) && ("cash".equals(paymentMethod) || "vnpay".equals(paymentMethod));
    }

    /** Only counter staff may collect the remaining balance of a customer deposit. */
    public static boolean canProcessCustomerDeposit(User user) {
        return isStaff(user);
    }

    private static String normalizedRole(User user) {
        if (user == null || user.getRole() == null) return "";
        return user.getRole().trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePath(String path) {
        String normalized = path.trim();
        if (normalized.isEmpty()) return "/";
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) normalized = normalized.substring(0, queryIndex);
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
