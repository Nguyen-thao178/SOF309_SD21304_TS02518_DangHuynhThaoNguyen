package com.cafe.servlet;

import com.cafe.dao.ProductDAO;
import com.cafe.dao.PromotionDAO;
import com.cafe.model.Product;
import com.cafe.model.Promotion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private PromotionDAO promoDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<Product> all = productDAO.getAllProducts();
        List<Product> featured = all.stream().limit(8).toList();
        req.setAttribute("featuredProducts", featured);

        List<Promotion> promotions = promoDAO.getActivePromotions();
        int maxDiscount = promotions.stream().mapToInt(Promotion::getDiscountPercent).max().orElse(0);
        req.setAttribute("maxDiscount", maxDiscount);

        List<Product> topSelling = productDAO.getTopSellingProducts(5);
        req.setAttribute("topSelling", topSelling);

        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}