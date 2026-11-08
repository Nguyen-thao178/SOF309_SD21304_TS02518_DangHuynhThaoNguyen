package com.cafe.servlet;

import com.cafe.service.CartService;
import com.cafe.service.CartService.AddResult;
import com.cafe.service.SystemSettingsService;
import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/cart/scan")
public class BarcodeCartServlet extends HttpServlet {
    private final CartService cartService = new CartService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User user = (User) req.getSession().getAttribute("user");
        if (!RoleAccessPolicy.isStaff(user)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), Map.of(
                    "success", false,
                    "code", "SCANNER_FORBIDDEN",
                    "message", "Tài khoản Customer chỉ được chọn món và bấm Đặt hàng; không được quét mã."
            ));
            return;
        }

        if (!SystemSettingsService.getBoolean("barcode_scanner_enabled", true)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(), Map.of(
                    "success", false,
                    "code", "SCANNER_DISABLED",
                    "message", "Chức năng quét barcode đang bị tắt trong Cài đặt hệ thống."
            ));
            return;
        }

        String barcode = req.getParameter("barcode");
        AddResult result = cartService.addByBarcode(req.getSession(), barcode);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", result.isSuccess());
        body.put("code", result.getCode());
        body.put("message", result.getMessage());

        if (result.isSuccess()) {
            body.put("productId", result.getProduct().getId());
            body.put("productName", result.getProduct().getName());
            body.put("productQuantity", result.getProductQuantity());
            body.put("cartQuantity", result.getCartQuantity());
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if ("NOT_FOUND".equals(result.getCode())) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else if ("EMPTY_BARCODE".equals(result.getCode())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if ("INVALID_BARCODE".equals(result.getCode())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if ("DATABASE_ERROR".equals(result.getCode())) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        }

        objectMapper.writeValue(resp.getWriter(), body);
    }
}
