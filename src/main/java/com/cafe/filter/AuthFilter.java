package com.cafe.filter;

import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import com.cafe.service.SystemSettingsService;
import com.cafe.utils.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        boolean isAsset = uri.startsWith(contextPath + "/assets");
        if (!isAsset) {
            CsrfUtil.ensureToken(req.getSession());
            req.setAttribute("appSettings", SystemSettingsService.getSettings());
        }

        boolean isPublic = uri.startsWith(contextPath + "/login")
                || isAsset
                || uri.startsWith(contextPath + "/register-member")
                || uri.startsWith(contextPath + "/oauth-login")
                || uri.startsWith(contextPath + "/oauth-callback")
                || uri.startsWith(contextPath + "/vnpay-return")
                || uri.startsWith(contextPath + "/vnpay-ipn")
                || uri.equals(contextPath + "/")
                || uri.equals(contextPath + "/index.jsp");

        boolean trustedCallback = uri.startsWith(contextPath + "/oauth-callback")
                || uri.startsWith(contextPath + "/vnpay-return")
                || uri.startsWith(contextPath + "/vnpay-ipn");
        if ("POST".equalsIgnoreCase(req.getMethod()) && !trustedCallback
                && !CsrfUtil.isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Yêu cầu đã hết hạn hoặc thiếu CSRF token.");
            return;
        }

        if (isPublic) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }

        String requestPath = uri.substring(contextPath.length());
        if (!RoleAccessPolicy.canAccess(user, requestPath)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản của bạn không có quyền thực hiện chức năng này.");
            return;
        }

        req.setAttribute("homePath", contextPath + RoleAccessPolicy.landingPath(user));

        chain.doFilter(req, resp);
    }
}
