package com.cafe.servlet;

import com.cafe.dao.SystemSettingsDAO;
import com.cafe.model.User;
import com.cafe.service.SystemSettingsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/admin/settings")
public class SettingsServlet extends HttpServlet {
    private SystemSettingsDAO settingsDAO = new SystemSettingsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        req.setAttribute("settings", SystemSettingsService.getSettings());
        req.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        Map<String, String> submitted = new LinkedHashMap<>();
        for (String key : SystemSettingsService.EDITABLE_KEYS) {
            submitted.put(key, req.getParameter(key));
        }
        SystemSettingsService.ValidationResult validation =
                SystemSettingsService.validate(submitted);
        if (!validation.valid()) {
            redirect(resp, req, "error", validation.message());
            return;
        }
        if (!settingsDAO.updateSettings(validation.values(), user.getId())) {
            redirect(resp, req, "error", "Không thể lưu cấu hình vào database.");
            return;
        }
        SystemSettingsService.invalidate();
        redirect(resp, req, "success", "Đã áp dụng cấu hình cho toàn hệ thống.");
    }

    private void redirect(HttpServletResponse response, HttpServletRequest request,
                          String parameter, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/settings?" + parameter + "="
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
