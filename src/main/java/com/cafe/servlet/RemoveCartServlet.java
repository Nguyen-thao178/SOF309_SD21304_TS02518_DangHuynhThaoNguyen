package com.cafe.servlet;

import com.cafe.model.CartItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@WebServlet("/remove-cart")
public class RemoveCartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Vui lòng xóa sản phẩm bằng biểu mẫu bảo mật.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        int productId;
        try {
            productId = Integer.parseInt(idStr);
            if (productId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ.");
            return;
        }
        HttpSession session = req.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            Iterator<CartItem> it = cart.iterator();
            while (it.hasNext()) {
                if (it.next().getProduct().getId() == productId) {
                    it.remove();
                    break;
                }
            }
            session.setAttribute("cart", cart);
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }
}
