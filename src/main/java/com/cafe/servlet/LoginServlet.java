package com.cafe.servlet;

import com.cafe.dao.UserDAO;
import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import com.cafe.utils.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            resp.sendRedirect(req.getContextPath() + RoleAccessPolicy.landingPath(user));
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        try {
            // Lấy user theo username
            User user = userDAO.findByUsername(username);

            if (user != null) {
                // Hash mật khẩu nhập vào và so sánh với hash trong DB
                if (PasswordUtil.verify(password, user.getPasswordHash())) {
                    // Đăng nhập thành công
                    HttpSession session = req.getSession();
                    req.changeSessionId();
                    if (PasswordUtil.needsUpgrade(user.getPasswordHash())) {
                        String upgradedHash = PasswordUtil.hash(password);
                        userDAO.updatePassword(user.getId(), upgradedHash);
                        user.setPasswordHash(upgradedHash);
                    }
                    session.setAttribute("user", user);
                    resp.sendRedirect(req.getContextPath() + RoleAccessPolicy.landingPath(user));
                    return;
                }
            }

            // Sai username hoặc password
            req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
