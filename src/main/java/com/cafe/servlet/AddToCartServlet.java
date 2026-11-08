package com.cafe.servlet;

import com.cafe.service.CartService;
import com.cafe.service.CartService.AddResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/add-to-cart")
public class AddToCartServlet extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Vui lòng thêm sản phẩm bằng biểu mẫu bảo mật.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        int productId;
        try {
            productId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/menu?error=invalidproduct");
            return;
        }

        AddResult result = cartService.addByProductId(req.getSession(), productId);
        if (!result.isSuccess()) {
            resp.sendRedirect(req.getContextPath() + "/menu?error=" + result.getCode().toLowerCase());
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/menu");
    }
}
