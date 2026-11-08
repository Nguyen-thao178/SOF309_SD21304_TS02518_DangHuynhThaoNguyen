package com.cafe.servlet;

import com.cafe.dao.ContactDAO;
import com.cafe.model.Contact;
import com.cafe.model.User;
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

@WebServlet("/admin/contacts")
public class ContactServlet extends HttpServlet {
    private static final Set<String> POSITIONS =
            Set.of("owner", "manager", "employee", "other");
    private final ContactDAO contactDAO = new ContactDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            String action = req.getParameter("action");
            if ("create".equals(action)) {
                req.getRequestDispatcher("/WEB-INF/views/admin/contact_form.jsp")
                        .forward(req, resp);
                return;
            }
            if ("edit".equals(action)) {
                Integer id = positiveInt(req.getParameter("id"));
                if (id == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID liên hệ không hợp lệ.");
                    return;
                }
                Contact contact = contactDAO.findById(id);
                if (contact == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                req.setAttribute("contact", contact);
                req.getRequestDispatcher("/WEB-INF/views/admin/contact_form.jsp")
                        .forward(req, resp);
                return;
            }
            List<Contact> contacts = contactDAO.getAllContacts();
            req.setAttribute("contacts", contacts);
            req.getRequestDispatcher("/WEB-INF/views/contact.jsp").forward(req, resp);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải danh sách liên hệ.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        try {
            if ("delete".equals(action)) {
                Integer id = positiveInt(req.getParameter("id"));
                if (id == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID liên hệ không hợp lệ.");
                    return;
                }
                if (!contactDAO.delete(id)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                redirect(req, resp, "success", "Đã xóa thông tin liên hệ.");
                return;
            }

            boolean update = "update".equals(action);
            if (!update && !"create".equals(action)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
                return;
            }
            Contact contact = readContact(req, update);
            String error = validate(contact);
            if (error != null) {
                req.setAttribute("contact", contact);
                req.setAttribute("error", error);
                req.getRequestDispatcher("/WEB-INF/views/admin/contact_form.jsp")
                        .forward(req, resp);
                return;
            }
            boolean saved = update ? contactDAO.update(contact) : contactDAO.insert(contact);
            if (!saved) {
                resp.sendError(update ? HttpServletResponse.SC_NOT_FOUND
                        : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            redirect(req, resp, "success", update
                    ? "Đã cập nhật thông tin liên hệ."
                    : "Đã thêm thông tin liên hệ.");
        } catch (IllegalArgumentException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (SQLException exception) {
            throw new ServletException("Không thể lưu thông tin liên hệ.", exception);
        }
    }

    private Contact readContact(HttpServletRequest request, boolean update) {
        Contact contact = new Contact();
        if (update) {
            Integer id = positiveInt(request.getParameter("id"));
            if (id == null) throw new IllegalArgumentException("ID liên hệ không hợp lệ.");
            contact.setId(id);
        }
        contact.setName(trim(request.getParameter("name")));
        contact.setPosition(trim(request.getParameter("position")));
        contact.setPhone(trim(request.getParameter("phone")));
        contact.setEmail(trim(request.getParameter("email")));
        contact.setAddress(trim(request.getParameter("address")));
        contact.setNotes(trim(request.getParameter("notes")));
        return contact;
    }

    private String validate(Contact contact) {
        if (contact.getName() == null || contact.getName().length() > 120) {
            return "Tên liên hệ là bắt buộc và tối đa 120 ký tự.";
        }
        if (!POSITIONS.contains(contact.getPosition())) {
            return "Chức vụ không hợp lệ.";
        }
        if (contact.getPhone() != null && contact.getPhone().length() > 30) {
            return "Số điện thoại tối đa 30 ký tự.";
        }
        if (contact.getEmail() != null && (contact.getEmail().length() > 254
                || !contact.getEmail().matches("^[A-Za-z0-9+_.-]+@[^\\s@]+$"))) {
            return "Email không hợp lệ.";
        }
        if (contact.getAddress() != null && contact.getAddress().length() > 500) {
            return "Địa chỉ tối đa 500 ký tự.";
        }
        if (contact.getNotes() != null && contact.getNotes().length() > 1000) {
            return "Ghi chú tối đa 1000 ký tự.";
        }
        return null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    private Integer positiveInt(String value) {
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

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String parameter, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/contacts?" + parameter + "="
                + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
