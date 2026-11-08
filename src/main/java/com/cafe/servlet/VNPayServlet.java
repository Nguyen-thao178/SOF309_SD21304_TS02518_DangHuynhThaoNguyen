package com.cafe.servlet;

import com.cafe.dao.PaymentDAO;
import com.cafe.model.Payment;
import com.cafe.model.User;
import com.cafe.payment.VNPayCallbackService;
import com.cafe.payment.VNPayConfig;
import com.cafe.security.RoleAccessPolicy;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

@WebServlet("/vnpay-pay")
public class VNPayServlet extends HttpServlet {
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!VNPayConfig.isConfigured()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "VNPay chưa được cấu hình trên máy chủ.");
            return;
        }

        long paymentId;
        try {
            paymentId = Long.parseLong(request.getParameter("paymentId"));
        } catch (Exception exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Payment ID không hợp lệ.");
            return;
        }

        try {
            Payment payment = paymentDAO.getPendingPaymentForUser(paymentId, user.getId());
            if (payment == null && RoleAccessPolicy.isStaff(user)) {
                payment = paymentDAO.getPendingCustomerBalancePaymentForStaff(paymentId);
            }
            if (payment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy giao dịch đang chờ.");
                return;
            }
            if (RoleAccessPolicy.isCustomer(user)
                    && !"deposit".equals(payment.getOrderType())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Tài khoản khách hàng chỉ được thanh toán tiền cọc.");
                return;
            }

            String transactionReference = payment.getTransactionReference();
            if (transactionReference == null || transactionReference.isBlank()) {
                transactionReference = VNPayConfig.TMN_CODE + "-"
                        + payment.getId() + "-"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                if (!paymentDAO.attachTransactionReference(payment.getId(), transactionReference)) {
                    response.sendError(HttpServletResponse.SC_CONFLICT,
                            "Giao dịch đang được xử lý. Vui lòng tải lại lịch sử.");
                    return;
                }
            }

            Map<String, String> parameters = buildParameters(request, payment,
                    transactionReference);
            String hashData = VNPayCallbackService.buildHashData(parameters);
            String secureHash = VNPayConfig.hmacSHA512(VNPayConfig.SECRET_KEY, hashData);
            response.sendRedirect(VNPayConfig.PAY_URL + "?" + hashData
                    + "&vnp_SecureHash=" + secureHash);
        } catch (Exception exception) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tạo giao dịch VNPay: " + exception.getMessage());
        }
    }

    private Map<String, String> buildParameters(HttpServletRequest request, Payment payment,
                                                 String transactionReference) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("vnp_Version", VNPayConfig.VERSION);
        parameters.put("vnp_Command", VNPayConfig.COMMAND);
        parameters.put("vnp_TmnCode", VNPayConfig.TMN_CODE);
        parameters.put("vnp_Amount", String.valueOf(Math.round(payment.getAmount() * 100)));
        parameters.put("vnp_CurrCode", VNPayConfig.CURRENCY);
        parameters.put("vnp_TxnRef", transactionReference);
        parameters.put("vnp_OrderInfo", "Thanh toan " + payment.getPaymentStage()
                + " don hang " + payment.getOrderId());
        parameters.put("vnp_OrderType", "other");
        parameters.put("vnp_Locale", VNPayConfig.LOCALE);
        if (!VNPayConfig.BANK_CODE.isBlank()) {
            parameters.put("vnp_BankCode", VNPayConfig.BANK_CODE);
        }
        parameters.put("vnp_ReturnUrl", VNPayConfig.RETURN_URL);
        parameters.put("vnp_IpAddr", request.getRemoteAddr());

        TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone);
        parameters.put("vnp_CreateDate", formatter.format(calendar.getTime()));
        calendar.add(Calendar.MINUTE, 15);
        parameters.put("vnp_ExpireDate", formatter.format(calendar.getTime()));
        return parameters;
    }
}
