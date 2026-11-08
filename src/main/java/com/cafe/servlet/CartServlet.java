package com.cafe.servlet;

import com.cafe.dao.OrderDAO;
import com.cafe.model.User;
import com.cafe.service.SystemSettingsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user != null) {
            req.setAttribute("pendingDepositCount", orderDAO.countPendingDeposits(user.getId()));
        }
        req.setAttribute("barcodeScannerEnabled",
                SystemSettingsService.getBoolean("barcode_scanner_enabled", true));
        req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, resp);
    }
}
