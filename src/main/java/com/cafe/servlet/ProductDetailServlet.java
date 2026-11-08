package com.cafe.servlet;

import com.cafe.dao.ProductDAO;
import com.cafe.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/product")
public class ProductDetailServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        try {
            int id = Integer.parseInt(idStr);
            Product p = productDAO.getById(id);
            if (p == null) {
                resp.sendRedirect(req.getContextPath() + "/menu");
                return;
            }
            req.setAttribute("product", p);
            req.getRequestDispatcher("/WEB-INF/views/product.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/menu");
        }
    }
}