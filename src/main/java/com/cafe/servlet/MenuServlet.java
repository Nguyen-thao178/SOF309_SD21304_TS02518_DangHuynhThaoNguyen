package com.cafe.servlet;

import com.cafe.dao.ProductDAO;
import com.cafe.dao.PromotionDAO;
import com.cafe.dao.PromotionItemDAO;
import com.cafe.model.Product;
import com.cafe.model.Promotion;
import com.cafe.model.PromotionItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/menu")
public class MenuServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private PromotionDAO promoDAO = new PromotionDAO();
    private PromotionItemDAO promoItemDAO = new PromotionItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String category = req.getParameter("category");
        List<Product> all = productDAO.getAllProducts();
        List<Product> filtered = all;
        if (category != null && !category.isEmpty()) {
            filtered = all.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        }
        req.setAttribute("productList", filtered);

        // Khuyến mãi chung
        List<Promotion> promotions = promoDAO.getActivePromotions();
        int maxDiscount = promotions.stream().mapToInt(Promotion::getDiscountPercent).max().orElse(0);
        req.setAttribute("maxDiscount", maxDiscount);

        // Danh sách sản phẩm có khuyến mãi đặc biệt (để hiển thị card)
        List<PromotionItem> promoItems = promoItemDAO.getActivePromotionItems();
        Map<Integer, Integer> promoMap = promoItems.stream()
                .collect(Collectors.toMap(PromotionItem::getProductId, PromotionItem::getDiscountPercent));
        req.setAttribute("promoMap", promoMap);

        // Top bán chạy
        List<Product> topSelling = productDAO.getTopSellingProducts(5);
        req.setAttribute("topSelling", topSelling);

        req.getRequestDispatcher("/WEB-INF/views/menu.jsp").forward(req, resp);
    }
}