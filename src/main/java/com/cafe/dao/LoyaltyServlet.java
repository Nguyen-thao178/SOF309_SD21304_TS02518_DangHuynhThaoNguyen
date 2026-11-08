package com.cafe.dao;

import com.cafe.model.LoyaltyPoint;
import com.cafe.model.User;
import com.cafe.service.SystemSettingsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

@WebServlet("/loyalty")
public class LoyaltyServlet extends HttpServlet {
    private final LoyaltyDAO loyaltyDAO = new LoyaltyDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request, response);
        if (user == null) return;
        loadPage(request, user);
        request.getRequestDispatcher("/WEB-INF/views/loyalty.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = currentUser(request, response);
        if (user == null) return;
        if (!"customer".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản này không thể đăng ký lại thành viên.");
            return;
        }

        String fullname = clean(request.getParameter("fullname"));
        String email = clean(request.getParameter("email"));
        String phone = clean(request.getParameter("phone"));
        String address = clean(request.getParameter("address"));
        Date birthDate = null;
        try {
            String rawBirthDate = clean(request.getParameter("birthDate"));
            if (!rawBirthDate.isEmpty()) {
                LocalDate parsed = LocalDate.parse(rawBirthDate);
                if (parsed.isAfter(LocalDate.now())) throw new IllegalArgumentException();
                birthDate = Date.valueOf(parsed);
            }
        } catch (Exception exception) {
            request.setAttribute("error", "Ngày sinh không hợp lệ.");
        }
        if (fullname.length() < 2 || fullname.length() > 120) {
            request.setAttribute("error", "Họ tên phải có từ 2 đến 120 ký tự.");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            request.setAttribute("error", "Email không hợp lệ.");
        } else if (!phone.matches("^(?:\\+84|0)[0-9]{9,10}$")) {
            request.setAttribute("error", "Số điện thoại Việt Nam không hợp lệ.");
        } else if (address.length() < 5 || address.length() > 500) {
            request.setAttribute("error", "Địa chỉ phải có từ 5 đến 500 ký tự.");
        }

        if (request.getAttribute("error") == null) {
            try {
                loyaltyDAO.registerMember(user, fullname, email, phone, birthDate, address);
                request.setAttribute("success",
                        "Đăng ký thành viên thành công. Bạn đã có thể dùng điểm làm voucher.");
            } catch (Exception exception) {
                request.setAttribute("error",
                        exception.getMessage() != null && exception.getMessage().contains("UNIQUE")
                                ? "Email đã được một tài khoản khác sử dụng."
                                : "Không thể đăng ký thành viên. Vui lòng thử lại.");
            }
        }
        loadPage(request, user);
        request.getRequestDispatcher("/WEB-INF/views/loyalty.jsp").forward(request, response);
    }

    private void loadPage(HttpServletRequest request, User user) {
        int pointValue = SystemSettingsService.getPositiveInt("loyalty_vnd_per_point", 1000);
        request.setAttribute("pointValue", pointValue);
        request.setAttribute("maxDiscountPercent", 80);
        if ("staff".equalsIgnoreCase(user.getRole())) {
            request.setAttribute("members", loyaltyDAO.getAllMembers());
            return;
        }
        LoyaltyPoint points = loyaltyDAO.getByUserId(user.getId());
        request.setAttribute("points", points);
        request.setAttribute("voucherValue", points == null ? 0 : points.getPoints() * pointValue);
        if ("member".equalsIgnoreCase(user.getRole())) {
            request.setAttribute("memberProfile", loyaltyDAO.getMemberProfile(user.getId()));
        }
    }

    private User currentUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) response.sendRedirect(request.getContextPath() + "/login");
        return user;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
