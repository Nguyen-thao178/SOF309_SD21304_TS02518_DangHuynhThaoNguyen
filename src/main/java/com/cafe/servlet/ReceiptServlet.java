package com.cafe.servlet;

import com.cafe.dao.OrderDAO;
import com.cafe.model.Order;
import com.cafe.model.ReceiptItem;
import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@WebServlet("/receipt")
public class ReceiptServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(req.getParameter("orderId"));
        } catch (Exception exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã hóa đơn không hợp lệ.");
            return;
        }

        try {
            boolean manager = "manager".equalsIgnoreCase(user.getRole());
            boolean staff = RoleAccessPolicy.isStaff(user);
            Order order;
            if (manager) {
                order = orderDAO.getOrderById(orderId);
            } else {
                order = orderDAO.getOrderByIdAndUserId(orderId, user.getId());
                if (order == null && staff) {
                    order = orderDAO.getCustomerDepositOrderById(orderId);
                }
            }
            if (order == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hóa đơn.");
                return;
            }

            boolean viewingCustomerOrder = staff && order.getUserId() != user.getId();
            List<ReceiptItem> items = (manager || viewingCustomerOrder)
                    ? orderDAO.getReceiptItems(orderId)
                    : orderDAO.getReceiptItems(orderId, user.getId());
            String paymentStage = "balance".equals(req.getParameter("stage"))
                    ? "balance"
                    : ("deposit".equals(order.getOrderType()) ? "deposit" : "full");

            double paidNow;
            if ("balance".equals(paymentStage)) {
                paidNow = order.getTotalAmount() - order.getDepositAmount();
            } else if ("deposit".equals(paymentStage)) {
                paidNow = order.getDepositAmount();
            } else {
                paidNow = order.getTotalAmount();
            }

            req.setAttribute("order", order);
            req.setAttribute("receiptItems", items);
            req.setAttribute("cashier", user);
            req.setAttribute("paymentStage", paymentStage);
            req.setAttribute("paidNow", Math.max(0, paidNow));
            req.setAttribute("remainingAmount",
                    Math.max(0, order.getTotalAmount() - order.getDepositAmount()));
            req.setAttribute("printedAt", new Date());
            req.setAttribute("autoPrint", "1".equals(req.getParameter("autoprint")));
            req.getRequestDispatcher("/WEB-INF/views/receipt.jsp").forward(req, resp);
        } catch (Exception exception) {
            getServletContext().log("Không thể tải hóa đơn #" + orderId, exception);
            throw new ServletException("Không thể tải dữ liệu hóa đơn.", exception);
        }
    }
}
