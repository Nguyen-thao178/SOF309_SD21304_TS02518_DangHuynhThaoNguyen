package com.cafe.utils;

import com.cafe.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {

    // ⭐ Đã đổi từ "loggedInUser" → "user" để đồng bộ với các servlet
    private static final String SESSION_USER_KEY = "user";
    private static final String SESSION_LOGIN_TIME = "loginTime";
    private static final String SESSION_LAST_ACTIVITY = "lastActivityTime";

    public static void setUser(HttpServletRequest request, User user) {
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_USER_KEY, user);
        session.setAttribute(SESSION_LOGIN_TIME, System.currentTimeMillis());
        session.setAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
    }

    public static User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session == null) ? null : (User) session.getAttribute(SESSION_USER_KEY);
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return getUser(request) != null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User user = getUser(request);
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    public static boolean isManager(HttpServletRequest request) {
        User user = getUser(request);
        return user != null && "manager".equalsIgnoreCase(user.getRole());
    }

    public static boolean isStaff(HttpServletRequest request) {
        User user = getUser(request);
        return user != null && "staff".equalsIgnoreCase(user.getRole());
    }

    public static boolean isCustomer(HttpServletRequest request) {
        User user = getUser(request);
        return user != null && "customer".equalsIgnoreCase(user.getRole());
    }

    public static boolean isManagerOrAdmin(HttpServletRequest request) {
        return isManager(request) || isAdmin(request);
    }

    public static boolean isEmployee(HttpServletRequest request) {
        return isStaff(request) || isManager(request) || isAdmin(request);
    }

    public static boolean isSessionTimeout(HttpServletRequest request, int timeoutMinutes) {
        HttpSession session = request.getSession(false);
        if (session == null) return true;
        Long lastActivity = (Long) session.getAttribute(SESSION_LAST_ACTIVITY);
        if (lastActivity == null) return true;
        long inactiveTime = System.currentTimeMillis() - lastActivity;
        return inactiveTime > timeoutMinutes * 60 * 1000L;
    }

    public static void updateLastActivity(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
        }
    }

    public static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_USER_KEY);
            session.removeAttribute(SESSION_LOGIN_TIME);
            session.removeAttribute(SESSION_LAST_ACTIVITY);
            session.invalidate();
        }
    }

    public static int getCurrentUserId(HttpServletRequest request) {
        User user = getUser(request);
        return user != null ? user.getId() : -1;
    }

    public static String getCurrentRole(HttpServletRequest request) {
        User user = getUser(request);
        return user != null ? user.getRole() : null;
    }
}