package com.cafe.servlet;

import com.cafe.dao.UserDAO;
import com.cafe.model.User;
import com.cafe.utils.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            // Chưa đăng nhập → redirect login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Kiểm tra timeout (nếu cần) – có thể bỏ qua
        request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate input
        if (oldPassword == null || oldPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
            request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu mới và xác nhận không khớp!");
            request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
            return;
        }

        if (newPassword.length() < 8) {
            request.setAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự!");
            request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
            return;
        }

        try {
            int userId = user.getId();
            String currentHash = userDAO.getPasswordHash(userId);

            if (currentHash == null) {
                request.setAttribute("error", "Không tìm thấy thông tin người dùng!");
                request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
                return;
            }

            if (!PasswordUtil.verify(oldPassword, currentHash)) {
                request.setAttribute("error", "Mật khẩu cũ không đúng!");
                request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
                return;
            }

            String newPasswordHash = PasswordUtil.hash(newPassword);
            boolean success = userDAO.updatePassword(userId, newPasswordHash);

            if (success) {
                request.setAttribute("message", "Đổi mật khẩu thành công!");
            } else {
                request.setAttribute("error", "Đổi mật khẩu thất bại. Vui lòng thử lại!");
            }
            request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/changePassword.jsp").forward(request, response);
        }
    }
}
