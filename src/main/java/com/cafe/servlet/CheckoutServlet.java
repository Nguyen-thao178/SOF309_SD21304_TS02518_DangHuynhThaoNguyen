package com.cafe.servlet;

import com.cafe.dao.OrderDAO;
import com.cafe.dao.PaymentDAO;
import com.cafe.dao.LoyaltyDAO;
import com.cafe.model.CartItem;
import com.cafe.model.LoyaltyPoint;
import com.cafe.model.Payment;
import com.cafe.model.User;
import com.cafe.payment.VNPayConfig;
import com.cafe.security.RoleAccessPolicy;
import com.cafe.service.SystemSettingsService;
import com.cafe.service.LoyaltyVoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Hiển thị trang checkout với QR
        req.setAttribute("minPickupDate", LocalDate.now().plusDays(1).toString());
        req.setAttribute("depositPercent",
                SystemSettingsService.getPositiveInt("deposit_percent", 30));
        req.setAttribute("vnpaySandbox", VNPayConfig.isSandbox());
        User user = (User) req.getSession().getAttribute("user");
        req.setAttribute("customerDepositOnly", RoleAccessPolicy.isCustomer(user));
        boolean member = user != null && "member".equalsIgnoreCase(user.getRole());
        int pointValue = SystemSettingsService.getPositiveInt("loyalty_vnd_per_point", 1000);
        LoyaltyPoint loyalty = member ? new LoyaltyDAO().getByUserId(user.getId()) : null;
        req.setAttribute("memberVoucherEnabled", member);
        req.setAttribute("loyaltyPoints", loyalty == null ? 0 : loyalty.getPoints());
        req.setAttribute("loyaltyPointValue", pointValue);
        req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (user == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập lại.");
            writeJson(resp, result);
            return;
        }
        if (cart == null || cart.isEmpty()) {
            result.put("success", false);
            result.put("message", "Giỏ hàng trống.");
            writeJson(resp, result);
            return;
        }

        String orderType = req.getParameter("orderType");
        String paymentMethod = req.getParameter("paymentMethod");
        if (!"direct".equals(orderType) && !"deposit".equals(orderType)) {
            result.put("success", false);
            result.put("message", "Hình thức giao dịch không hợp lệ.");
            writeJson(resp, result);
            return;
        }
        if (!RoleAccessPolicy.canCreateOrderType(user, orderType)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "Tài khoản khách hàng chỉ được đặt cọc trước món, không được thanh toán trực tiếp.");
            writeJson(resp, result);
            return;
        }
        if (!"cash".equals(paymentMethod) && !"vnpay".equals(paymentMethod)) {
            result.put("success", false);
            result.put("message", "Phương thức thanh toán không hợp lệ.");
            writeJson(resp, result);
            return;
        }
        if (!RoleAccessPolicy.canUsePaymentMethod(user, paymentMethod)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "Tài khoản khách hàng chỉ được thanh toán tiền cọc qua VNPay.");
            writeJson(resp, result);
            return;
        }

        Date pickupDate = null;
        if ("deposit".equals(orderType)) {
            try {
                LocalDate parsedDate = LocalDate.parse(req.getParameter("pickupDate"));
                if (!parsedDate.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException();
                }
                pickupDate = Date.valueOf(parsedDate);
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "Ngày nhận hàng phải từ ngày mai trở đi.");
                writeJson(resp, result);
                return;
            }
        }

        double grossTotal = cart.stream()
                .mapToDouble(item -> item.getDiscountedPrice() * item.getQuantity())
                .sum();
        int requestedPoints;
        try {
            String rawPoints = req.getParameter("redeemPoints");
            requestedPoints = rawPoints == null || rawPoints.isBlank()
                    ? 0 : Integer.parseInt(rawPoints);
            if (requestedPoints < 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            result.put("success", false);
            result.put("message", "Số điểm muốn dùng không hợp lệ.");
            writeJson(resp, result);
            return;
        }
        LoyaltyPoint loyalty = new LoyaltyDAO().getByUserId(user.getId());
        int availablePoints = loyalty == null ? 0 : loyalty.getPoints();
        int pointValue = SystemSettingsService.getPositiveInt("loyalty_vnd_per_point", 1000);
        var voucher = LoyaltyVoucherService.quote(user.getRole(), grossTotal, requestedPoints,
                availablePoints, pointValue);
        if (requestedPoints != voucher.pointsUsed()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Voucher không hợp lệ: chỉ Member được dùng điểm, tối đa 80% đơn và không vượt số dư.");
            writeJson(resp, result);
            return;
        }
        double total = voucher.netTotal();
        double depositRate = SystemSettingsService.getPositiveInt("deposit_percent", 30) / 100.0;
        double payableAmount = "deposit".equals(orderType)
                ? Math.round(total * depositRate)
                : total;

        if ("vnpay".equals(paymentMethod)) {
            if (!VNPayConfig.isConfigured()) {
                result.put("success", false);
                result.put("message",
                        "VNPay chưa được cấu hình trên máy chủ. Vui lòng liên hệ quản trị viên.");
                writeJson(resp, result);
                return;
            }
            try {
                Payment payment = paymentDAO.createPendingVNPayOrder(
                        user.getId(), cart, total, grossTotal, orderType, pickupDate,
                        payableAmount, voucher.pointsUsed(), voucher.discountAmount());
                session.removeAttribute("cart");
                result.put("success", true);
                result.put("redirectUrl", req.getContextPath()
                        + "/vnpay-pay?paymentId=" + payment.getId());
                result.put("message", "Đơn hàng đã được giữ. Đang chuyển đến VNPay.");
            } catch (Exception exception) {
                result.put("success", false);
                result.put("message", "Không thể khởi tạo giao dịch VNPay: "
                        + exception.getMessage());
            }
            writeJson(resp, result);
            return;
        }

        try {
            int orderId = orderDAO.createOrder(
                    user.getId(), cart, total, orderType, paymentMethod, pickupDate, payableAmount
            );
            session.removeAttribute("cart");
            result.put("success", true);
            result.put("orderId", orderId);
            result.put("redirectUrl", req.getContextPath() + "/receipt?orderId=" + orderId
                    + "&autoprint=1"
                    + ("deposit".equals(orderType) ? "&stage=deposit" : ""));
            result.put("message", "deposit".equals(orderType)
                    ? "Đã nhận tiền cọc. Đang in hóa đơn..."
                    : "Thanh toán thành công. Đang in hóa đơn...");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Không thể tạo đơn hàng: " + e.getMessage());
        }
        writeJson(resp, result);
    }

    private void writeJson(HttpServletResponse resp, Map<String, Object> data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        resp.getWriter().write(mapper.writeValueAsString(data));
    }
}
