package com.cafe.servlet;

import com.cafe.model.CartItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/update-cart")
public class UpdateCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        String quantityStr = req.getParameter("quantity");
        if (idStr == null || quantityStr == null) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        int productId;
        int quantity;
        try {
            productId = Integer.parseInt(idStr);
            quantity = Integer.parseInt(quantityStr);
            if (productId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "ID hoặc số lượng sản phẩm không hợp lệ.");
            return;
        }
        HttpSession session = req.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (item.getProduct().getId() == productId) {
                    if (quantity <= 0) {
                        cart.remove(item);
                    } else {
                        item.setQuantity(Math.min(quantity, item.getProduct().getStock()));
                    }
                    break;
                }
            }
            session.setAttribute("cart", cart);
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}
