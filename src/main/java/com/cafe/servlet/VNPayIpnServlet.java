package com.cafe.servlet;

import com.cafe.payment.VNPayCallbackService;
import com.cafe.payment.VNPayCallbackService.CallbackResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/vnpay-ipn")
public class VNPayIpnServlet extends HttpServlet {
    private final VNPayCallbackService callbackService = new VNPayCallbackService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        CallbackResult result = callbackService.process(request);
        Map<String, String> body = new LinkedHashMap<>();
        body.put("RspCode", result.responseCode());
        body.put("Message", result.message());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        mapper.writeValue(response.getWriter(), body);
    }
}
