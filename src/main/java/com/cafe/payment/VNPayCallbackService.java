package com.cafe.payment;

import com.cafe.dao.PaymentDAO;
import com.cafe.model.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VNPayCallbackService {
    private final PaymentDAO paymentDAO;

    public VNPayCallbackService() {
        this(new PaymentDAO());
    }

    VNPayCallbackService(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }

    public CallbackResult process(HttpServletRequest request) {
        if (!VNPayConfig.isConfigured()) {
            return CallbackResult.error("99", "VNPay chưa được cấu hình.", null);
        }

        Map<String, String> fields = readFields(request);
        String suppliedHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        if (suppliedHash == null || !isValidSignature(fields, suppliedHash)) {
            return CallbackResult.error("97", "Chữ ký VNPay không hợp lệ.", null);
        }

        String transactionReference = fields.get("vnp_TxnRef");
        if (transactionReference == null || transactionReference.isBlank()) {
            return CallbackResult.error("01", "Thiếu mã giao dịch VNPay.", null);
        }

        try {
            Payment payment = paymentDAO.getByTransactionReference(transactionReference);
            if (payment == null) {
                return CallbackResult.error("01", "Không tìm thấy giao dịch.", null);
            }

            long callbackAmount;
            try {
                callbackAmount = Long.parseLong(fields.getOrDefault("vnp_Amount", "-1"));
            } catch (NumberFormatException exception) {
                return CallbackResult.error("04", "Số tiền VNPay không hợp lệ.", payment);
            }
            long expectedAmount = Math.round(payment.getAmount() * 100);
            if (callbackAmount != expectedAmount) {
                return CallbackResult.error("04", "Số tiền VNPay không khớp đơn hàng.", payment);
            }

            if ("paid".equals(payment.getStatus())) {
                return CallbackResult.success("02", "Giao dịch đã được xác nhận trước đó.", payment);
            }
            if (!"pending".equals(payment.getStatus())) {
                return CallbackResult.error("02", "Giao dịch không còn chờ thanh toán.", payment);
            }

            String responseCode = fields.get("vnp_ResponseCode");
            String transactionStatus = fields.get("vnp_TransactionStatus");
            boolean successful = "00".equals(responseCode)
                    && (transactionStatus == null || "00".equals(transactionStatus));
            if (!successful) {
                paymentDAO.failVNPayPayment(transactionReference);
                return CallbackResult.error("00",
                        "Thanh toán không thành công. Mã lỗi: " + responseCode, payment);
            }

            Payment completed = paymentDAO.completeVNPayPayment(transactionReference);
            return CallbackResult.success("00", "Thanh toán thành công.", completed);
        } catch (SQLException exception) {
            return CallbackResult.error("99", "Không thể cập nhật giao dịch: "
                    + exception.getMessage(), null);
        }
    }

    public static Map<String, String> readFields(HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            fields.put(name, request.getParameter(name));
        }
        return fields;
    }

    public static String buildHashData(Map<String, String> fields) {
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        StringBuilder data = new StringBuilder();
        for (String name : names) {
            String value = fields.get(name);
            if (value == null || value.isEmpty()) continue;
            if (!data.isEmpty()) data.append('&');
            data.append(encode(name)).append('=').append(encode(value));
        }
        return data.toString();
    }

    private boolean isValidSignature(Map<String, String> fields, String suppliedHash) {
        try {
            String expectedHash = VNPayConfig.hmacSHA512(
                    VNPayConfig.SECRET_KEY, buildHashData(fields));
            return MessageDigest.isEqual(
                    expectedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII),
                    suppliedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        } catch (Exception exception) {
            return false;
        }
    }

    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record CallbackResult(boolean success, String responseCode, String message,
                                 Payment payment) {
        static CallbackResult success(String responseCode, String message, Payment payment) {
            return new CallbackResult(true, responseCode, message, payment);
        }

        static CallbackResult error(String responseCode, String message, Payment payment) {
            return new CallbackResult(false, responseCode, message, payment);
        }
    }
}
