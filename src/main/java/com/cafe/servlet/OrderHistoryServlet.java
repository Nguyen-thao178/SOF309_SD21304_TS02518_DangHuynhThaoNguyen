package com.cafe.servlet;

import com.cafe.dao.OrderDAO;
import com.cafe.model.Order;
import com.cafe.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/history")
public class OrderHistoryServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            orderDAO.expireOverdueDeposits();
        } catch (Exception e) {
            getServletContext().log("Không thể cập nhật đơn cọc quá hạn", e);
        }
        boolean viewingAllOrders = "manager".equalsIgnoreCase(user.getRole());
        List<Order> orders = viewingAllOrders
                ? orderDAO.getAllOrders()
                : orderDAO.getOrdersByUserId(user.getId());
        req.setAttribute("viewingAllOrders", viewingAllOrders);
        req.setAttribute("orderList", orders);
        req.getRequestDispatcher("/WEB-INF/views/history.jsp").forward(req, resp);
    }
}
