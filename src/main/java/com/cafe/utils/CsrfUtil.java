package com.cafe.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfUtil {
    public static final String SESSION_KEY = "csrfToken";
    public static final String PARAMETER = "_csrf";
    public static final String HEADER = "X-CSRF-Token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    public static String ensureToken(HttpSession session) {
        Object current = session.getAttribute(SESSION_KEY);
        if (current instanceof String token && !token.isBlank()) return token;
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_KEY, token);
        return token;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object expected = session.getAttribute(SESSION_KEY);
        String supplied = request.getHeader(HEADER);
        if (supplied == null || supplied.isBlank()) supplied = request.getParameter(PARAMETER);
        if (!(expected instanceof String expectedToken) || supplied == null) return false;
        return MessageDigest.isEqual(expectedToken.getBytes(), supplied.getBytes());
    }
}
