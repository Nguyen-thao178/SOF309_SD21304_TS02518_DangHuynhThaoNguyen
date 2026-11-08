package com.cafe.servlet;

import com.cafe.dao.UserDAO;
import com.cafe.model.User;
import com.cafe.utils.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@WebServlet("/admin/users")
public class UserManagementServlet extends HttpServlet {
    private static final Set<String> MANAGER_ASSIGNABLE_ROLES = Set.of("staff");
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User actor = currentUser(request);
        if (!canManageUsers(actor)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        prepareRoleContext(request, actor);
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                request.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                        .forward(request, response);
                return;
            }
            if ("edit".equals(action)) {
                Integer id = parsePositiveInt(request.getParameter("id"));
                if (id == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID người dùng không hợp lệ.");
                    return;
                }
                User user = userDAO.findById(id);
                if (user == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                if (!canManageTarget(actor, user)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Bạn chỉ có thể quản lý tài khoản cấp dưới.");
                    return;
                }
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                        .forward(request, response);
                return;
            }
            List<User> users = userDAO.findAll().stream()
                    .filter(user -> "staff".equalsIgnoreCase(user.getRole()))
                    .toList();
            request.setAttribute("users", users);
            request.getRequestDispatcher("/WEB-INF/views/admin/user_list.jsp")
                    .forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải dữ liệu người dùng.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        User actor = currentUser(request);
        if (!canManageUsers(actor)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        prepareRoleContext(request, actor);
        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                deleteUser(request, response);
                return;
            }
            if ("update".equals(action)) {
                saveUser(request, response, true);
                return;
            }
            if ("create".equals(action)) {
                saveUser(request, response, false);
                return;
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
        } catch (SQLException exception) {
            redirectWithError(request, response, exception.getMessage());
        }
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response,
                          boolean update) throws SQLException, IOException, ServletException {
        Integer id = update ? parsePositiveInt(request.getParameter("id")) : null;
        if (update && id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID người dùng không hợp lệ.");
            return;
        }

        String username = trim(request.getParameter("username"));
        String fullname = trim(request.getParameter("fullname"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String role = trim(request.getParameter("role"));
        User actor = currentUser(request);
        Set<String> allowedRoles = allowedRolesFor(actor);
        if (username == null || fullname == null || email == null
                || !email.matches("^[A-Za-z0-9+_.-]+@[^\\s@]+$")
                || !allowedRoles.contains(role)
                || (!update && (password == null || password.length() < 8))
                || (password != null && !password.isBlank() && password.length() < 8)) {
            request.setAttribute("error",
                    "Vui lòng nhập đủ dữ liệu; mật khẩu tối thiểu 8 ký tự và role phải hợp lệ.");
            request.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                    .forward(request, response);
            return;
        }

        if (!update && userDAO.findByUsername(username) != null) {
            request.setAttribute("error", "Username đã tồn tại.");
            request.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                    .forward(request, response);
            return;
        }
        User emailOwner = userDAO.findByEmail(email);
        if (emailOwner != null && (!update || emailOwner.getId() != id)) {
            request.setAttribute("error", "Email đã được sử dụng.");
            request.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp")
                    .forward(request, response);
            return;
        }

        User user = new User();
        if (update) {
            User existing = userDAO.findById(id);
            if (existing == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!canManageTarget(actor, existing)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Không thể thay đổi tài khoản ngang cấp hoặc cấp trên.");
                return;
            }
            user.setId(id);
            user.setUsername(existing.getUsername());
            user.setFullname(fullname);
            user.setEmail(email);
            user.setRole(role);
            userDAO.update(user);
            if (password != null && !password.isBlank()) {
                userDAO.updatePassword(id, PasswordUtil.hash(password));
            }
        } else {
            user.setUsername(username);
            user.setFullname(fullname);
            user.setEmail(email);
            user.setRole(role);
            user.setPasswordHash(PasswordUtil.hash(password));
            userDAO.insert(user);
        }
        response.sendRedirect(request.getContextPath() + "/admin/users?success=1");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInt(request.getParameter("id"));
        User current = (User) request.getSession().getAttribute("user");
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID người dùng không hợp lệ.");
            return;
        }
        if (current.getId() == id) {
            redirectWithError(request, response, "Không thể tự xóa tài khoản đang đăng nhập.");
            return;
        }
        User target = userDAO.findById(id);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!canManageTarget(current, target)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn chỉ có thể xóa tài khoản cấp dưới.");
            return;
        }
        userDAO.deleteById(id);
        response.sendRedirect(request.getContextPath() + "/admin/users?success=1");
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response,
                                   String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/users?error="
                + URLEncoder.encode(message == null ? "Không thể xử lý yêu cầu." : message,
                StandardCharsets.UTF_8));
    }

    private User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

    private boolean canManageUsers(User user) {
        return user != null && "manager".equalsIgnoreCase(user.getRole());
    }

    private Set<String> allowedRolesFor(User actor) {
        return MANAGER_ASSIGNABLE_ROLES;
    }

    private boolean canManageTarget(User actor, User target) {
        if (actor == null || target == null) return false;
        return "manager".equalsIgnoreCase(actor.getRole())
                && "staff".equalsIgnoreCase(target.getRole());
    }

    private void prepareRoleContext(HttpServletRequest request, User actor) {
        request.setAttribute("managerLimited", true);
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private String trim(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
}
