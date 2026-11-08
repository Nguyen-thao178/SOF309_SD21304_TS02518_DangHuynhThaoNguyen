package com.cafe.servlet;

import com.cafe.dao.OrderDAO;
import com.cafe.dao.PaymentDAO;
import com.cafe.model.Order;
import com.cafe.model.Payment;
import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/deposit-orders")
public class DepositOrdersServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            orderDAO.expireOverdueDeposits();
        } catch (Exception e) {
            getServletContext().log("Không thể cập nhật đơn cọc quá hạn", e);
        }

        boolean staffView = RoleAccessPolicy.isStaff(user);
        List<Order> orders = staffView
                ? orderDAO.getCustomerDepositOrders()
                : orderDAO.getDepositOrdersByUserId(user.getId());
        req.setAttribute("staffDepositView", staffView);
        req.setAttribute("depositOrders", orders);
        req.getRequestDispatcher("/WEB-INF/views/deposit_orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!RoleAccessPolicy.canProcessCustomerDeposit(user)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Khách hàng chỉ được xem đơn cọc của mình.");
            return;
        }

        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            String paymentMethod = req.getParameter("balancePaymentMethod");
            Payment payment = paymentDAO.createBalancePaymentForCustomerOrder(
                    orderId, paymentMethod);
            if ("vnpay".equals(paymentMethod)) {
                resp.sendRedirect(req.getContextPath() + "/vnpay-pay?paymentId="
                        + payment.getId());
            } else {
                resp.sendRedirect(req.getContextPath() + "/receipt?orderId=" + orderId
                        + "&stage=balance&autoprint=1");
            }
        } catch (Exception e) {
            getServletContext().log("Không thể xác nhận nhận hàng", e);
            resp.sendRedirect(req.getContextPath() + "/deposit-orders?error=update");
        }
    }
}
