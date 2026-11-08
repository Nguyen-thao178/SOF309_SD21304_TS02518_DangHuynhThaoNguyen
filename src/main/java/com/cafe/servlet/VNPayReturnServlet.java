package com.cafe.servlet;

import com.cafe.model.Payment;
import com.cafe.model.User;
import com.cafe.payment.VNPayCallbackService;
import com.cafe.payment.VNPayCallbackService.CallbackResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/vnpay-return")
public class VNPayReturnServlet extends HttpServlet {
    private final VNPayCallbackService callbackService = new VNPayCallbackService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        CallbackResult result = callbackService.process(request);
        Payment payment = result.payment();
        if (result.success() && payment != null) {
            HttpSession session = request.getSession(false);
            User user = session == null ? null : (User) session.getAttribute("user");
            if (user != null && user.getId() == payment.getUserId()) {
                response.sendRedirect(request.getContextPath() + "/receipt?orderId="
                        + payment.getOrderId() + "&autoprint=1"
                        + ("deposit".equals(payment.getPaymentStage()) ? "&stage=deposit" : "")
                        + ("balance".equals(payment.getPaymentStage()) ? "&stage=balance" : ""));
                return;
            }
            request.setAttribute("paymentStatus", "success");
            request.setAttribute("message", result.message()
                    + " Mã đơn #" + payment.getOrderId() + ".");
        } else {
            request.setAttribute("paymentStatus", "failed");
            request.setAttribute("message", result.message());
        }
        request.getRequestDispatcher("/WEB-INF/views/payment_result.jsp")
                .forward(request, response);
    }
}
