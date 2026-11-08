package com.cafe.servlet;

import com.cafe.dao.UserDAO;
import com.cafe.model.User;
import com.cafe.utils.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register-member")
public class MemberRegisterServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String fullname = req.getParameter("fullname");
        String email = req.getParameter("email");

        // Validate
        if (username == null || username.trim().isEmpty()) {
            req.setAttribute("error", "Username không được để trống.");
            req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
            return;
        }
        if (password == null || password.length() < 8) {
            req.setAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
            return;
        }
        if (fullname == null || fullname.trim().isEmpty()) {
            req.setAttribute("error", "Họ tên không được để trống.");
            req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
            return;
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            req.setAttribute("error", "Email không hợp lệ.");
            req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
            return;
        }

        try {
            // Kiểm tra username đã tồn tại
            if (userDAO.findByUsername(username) != null) {
                req.setAttribute("error", "Tên đăng nhập đã tồn tại.");
                req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
                return;
            }

            // Kiểm tra email đã tồn tại
            if (userDAO.findByEmail(email) != null) {
                req.setAttribute("error", "Email đã được sử dụng.");
                req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
                return;
            }

            // Tạo đối tượng User
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setFullname(fullname);
            user.setEmail(email);
            user.setRole("customer"); // Mặc định là khách hàng

            // Lưu vào database
            int id = userDAO.insert(user);
            if (id > 0) {
                // Đăng ký thành công
                req.setAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            } else {
                req.setAttribute("error", "Đăng ký thất bại, vui lòng thử lại.");
                req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/register_member.jsp").forward(req, resp);
        }
    }
}
