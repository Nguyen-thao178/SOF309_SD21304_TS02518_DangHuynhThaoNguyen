package com.cafe.servlet;

import com.cafe.dao.PromotionDAO;
import com.cafe.model.Promotion;
import com.cafe.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/promotion")
public class PromotionServlet extends HttpServlet {
    private PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<Promotion> promotions = promotionDAO.getActivePromotions();
        req.setAttribute("promotions", promotions);
        req.getRequestDispatcher("/WEB-INF/views/promotion.jsp").forward(req, resp);
    }
}