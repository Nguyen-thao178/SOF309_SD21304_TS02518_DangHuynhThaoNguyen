package com.cafe.servlet;

import com.cafe.dao.ChatHistoryDAO;
import com.cafe.dao.ProductDAO;
import com.cafe.dao.PromotionDAO;
import com.cafe.model.Product;
import com.cafe.model.Promotion;
import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import com.cafe.service.GeminiService;
import com.cafe.service.ChatCartService;
import com.cafe.service.SystemSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ChatServlet.class.getName());
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final ObjectMapper mapper = new ObjectMapper();
    private final GeminiService geminiService = new GeminiService();
    private final ProductDAO productDAO = new ProductDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final ChatHistoryDAO chatHistoryDAO = new ChatHistoryDAO();
    private final ChatCartService chatCartService = new ChatCartService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String message = req.getParameter("message");
        if (message == null || message.trim().isEmpty()) {
            writeJson(resp, false, "Vui lòng nhập tin nhắn.", "validation");
            return;
        }

        message = message.trim();
        if (message.length() > MAX_MESSAGE_LENGTH) {
            writeJson(resp, false,
                    "Tin nhắn tối đa " + MAX_MESSAGE_LENGTH + " ký tự.", "validation");
            return;
        }

        String provider = "local";
        String reply;
        User user = (User) req.getSession().getAttribute("user");
        ChatCartService.Result cartCommand = chatCartService.handle(
                message, user, req.getSession());
        if (cartCommand.handled()) {
            reply = cartCommand.message();
            provider = "cart";
            // Database reporting currently classifies deterministic actions as "local".
            saveHistory(user, message, reply, "local");
            writeJson(resp, true, reply, provider, cartCommand);
            return;
        }

        if (geminiService.isConfigured()) {
            try {
                reply = geminiService.generateReply(message, buildCafeContext(user));
                if (isGroundedReply(message, reply)) {
                    provider = "gemini";
                } else {
                    LOGGER.log(Level.INFO,
                            "Gemini reply did not contain known menu data; using local assistant");
                    reply = localReply(message, user);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Gemini request was interrupted", e);
                reply = localReply(message, user);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Gemini request failed; using local assistant", e);
                reply = localReply(message, user);
            }
        } else {
            reply = localReply(message, user);
        }
        saveHistory(user, message, reply, provider);
        writeJson(resp, true, reply, provider);
    }

    private String buildCafeContext(User user) {
        NumberFormat money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        Map<String, String> settings = SystemSettingsService.getSettings();
        StringBuilder context = new StringBuilder("""
                - Tên cửa hàng: %s
                - Hotline: %s
                - Địa chỉ: %s
                - Giờ mở cửa: %s; %s
                - Quyền thanh toán của tài khoản hiện tại: %s
                - Quá ngày nhận mà khách không nhận, đơn chuyển trạng thái "Không nhận hàng" và hàng hoàn kho.
                - Barcode hỗ trợ: EAN-13; quét tại màn hình giỏ hàng.
                """.formatted(
                settings.get("store_name"), settings.get("hotline"), settings.get("address"),
                settings.get("weekday_hours"), settings.get("weekend_hours"),
                paymentPolicyFor(user)));

        List<Product> products = productDAO.getAllProducts();
        context.append("\n- Menu từ database:\n");
        if (products.isEmpty()) {
            context.append("  Chưa tải được dữ liệu menu.\n");
        } else {
            products.stream().limit(40).forEach(product -> context
                    .append("  + ").append(product.getName())
                    .append(": ").append(money.format(product.getPrice())).append("₫")
                    .append(", tồn kho ").append(product.getStock())
                    .append(product.getBarcode() == null ? "" : ", barcode " + product.getBarcode())
                    .append('\n'));
        }

        List<Promotion> promotions = promotionDAO.getActivePromotions();
        context.append("- Khuyến mãi đang hoạt động:\n");
        if (promotions.isEmpty()) {
            context.append("  Hiện chưa có khuyến mãi được xác nhận.\n");
        } else {
            promotions.stream().limit(15).forEach(promotion -> context
                    .append("  + ").append(promotion.getTitle())
                    .append(": giảm ").append(promotion.getDiscountPercent()).append("%")
                    .append(", đến ").append(promotion.getEndDate()).append('\n'));
        }
        return context.toString();
    }

    private String localReply(String message, User user) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("menu") || lower.contains("món") || lower.contains("đồ uống")
                || lower.contains("giá") || lower.contains("bao nhiêu")
                || lower.contains("cà phê") || lower.contains("coffee")
                || lower.contains("trà") || lower.contains("bánh") || lower.contains("có gì")) {
            List<Product> products = productDAO.getAllProducts();
            if (products.isEmpty()) {
                return "Mình chưa tải được menu lúc này. Bạn vui lòng gọi "
                        + SystemSettingsService.get("hotline") + " để được hỗ trợ nhé.";
            }
            List<Product> relevantProducts = products;
            String menuLabel = "Menu hiện có";
            if (lower.contains("cà phê") || lower.contains("coffee")) {
                relevantProducts = products.stream()
                        .filter(product -> matchesProductGroup(product, "cà phê", "coffee"))
                        .toList();
                menuLabel = "Các món cà phê hiện có";
            } else if (lower.contains("trà")) {
                relevantProducts = products.stream()
                        .filter(product -> matchesProductGroup(product, "trà", "tea"))
                        .toList();
                menuLabel = "Các món trà hiện có";
            } else if (lower.contains("bánh")) {
                relevantProducts = products.stream()
                        .filter(product -> matchesProductGroup(product, "bánh", "cake"))
                        .toList();
                menuLabel = "Các món bánh hiện có";
            }
            if (relevantProducts.isEmpty()) relevantProducts = products;
            NumberFormat money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
            StringBuilder reply = new StringBuilder(menuLabel).append(": ");
            relevantProducts.stream().limit(8).forEach(product -> reply
                    .append(product.getName()).append(" (")
                    .append(money.format(product.getPrice())).append("₫), "));
            return reply.substring(0, reply.length() - 2) + ".";
        }
        if (lower.contains("cọc") || lower.contains("nhận hàng")) {
            return "Bạn có thể cọc " + SystemSettingsService.get("deposit_percent")
                    + "% và chọn ngày nhận. " + paymentPolicyFor(user) + " Nếu quá ngày mà chưa nhận, "
                    + "đơn sẽ được ghi là “Không nhận hàng” và sản phẩm được hoàn kho.";
        }
        if (lower.contains("barcode") || lower.contains("mã vạch") || lower.contains("quét")) {
            if (!SystemSettingsService.getBoolean("barcode_scanner_enabled", true)) {
                return "Chức năng quét barcode đang tạm tắt theo cấu hình của quán.";
            }
            return "Chidori hỗ trợ barcode EAN-13. Tại Giỏ hàng, hãy đặt con trỏ vào ô quét rồi quét mã; đúng sản phẩm sẽ tự được thêm vào cart.";
        }
        if (lower.contains("khuyến mãi") || lower.contains("giảm giá")) {
            List<Promotion> promotions = promotionDAO.getActivePromotions();
            return promotions.isEmpty()
                    ? "Hiện chưa có khuyến mãi được xác nhận trong hệ thống."
                    : "Khuyến mãi nổi bật: " + promotions.get(0).getTitle()
                    + " – giảm " + promotions.get(0).getDiscountPercent() + "%.";
        }
        if (lower.contains("liên hệ") || lower.contains("hotline") || lower.contains("địa chỉ")
                || lower.contains("mở cửa")) {
            Map<String, String> settings = SystemSettingsService.getSettings();
            return settings.get("store_name") + ": " + settings.get("address")
                    + " · Hotline " + settings.get("hotline") + ". Mở cửa "
                    + settings.get("weekday_hours") + "; " + settings.get("weekend_hours") + ".";
        }
        if (lower.contains("thanh toán") || lower.contains("vnpay") || lower.contains("tiền mặt")) {
            return paymentPolicyFor(user);
        }
        if (lower.contains("cảm ơn") || lower.contains("chào")) {
            return "Chào bạn! ☕ Mình có thể hỗ trợ menu, giá món, barcode, thanh toán, đặt cọc và thông tin Chidori Coffee.";
        }
        return "Mình chỉ hỗ trợ thông tin và dịch vụ của Chidori Coffee thôi ạ. Bạn muốn xem menu, giá món, khuyến mãi hay tình trạng đơn hàng?";
    }

    private boolean matchesProductGroup(Product product, String vietnameseKeyword,
                                        String englishKeyword) {
        String name = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
        String category = product.getCategory() == null
                ? "" : product.getCategory().toLowerCase(Locale.ROOT);
        return name.contains(vietnameseKeyword) || category.contains(vietnameseKeyword)
                || name.contains(englishKeyword) || category.contains(englishKeyword);
    }

    private boolean isGroundedReply(String message, String reply) {
        String question = message.toLowerCase(Locale.ROOT);
        boolean asksAboutMenu = question.contains("menu") || question.contains("món")
                || question.contains("đồ uống") || question.contains("giá")
                || question.contains("bao nhiêu") || question.contains("cà phê")
                || question.contains("coffee") || question.contains("trà")
                || question.contains("bánh") || question.contains("có gì");
        if (!asksAboutMenu) return true;

        String normalizedReply = reply.toLowerCase(Locale.ROOT);
        return productDAO.getAllProducts().stream()
                .map(Product::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedReply::contains);
    }

    private String paymentPolicyFor(User user) {
        if (RoleAccessPolicy.isCustomer(user)) {
            return "Tài khoản Customer chỉ được đặt cọc và thanh toán tiền cọc qua VNPay; không được thanh toán trực tiếp hoặc bằng tiền mặt.";
        }
        if (RoleAccessPolicy.isStaff(user)) {
            return "Tài khoản Staff có thể thanh toán trực tiếp hoặc tạo đơn cọc bằng tiền mặt/VNPay.";
        }
        return "Tài khoản hiện tại không có chức năng tạo hoặc thanh toán đơn hàng.";
    }

    private void writeJson(HttpServletResponse resp, boolean success, String message, String provider)
            throws IOException {
        writeJson(resp, success, message, provider, null);
    }

    private void writeJson(HttpServletResponse resp, boolean success, String message,
                           String provider, ChatCartService.Result cartCommand)
            throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("success", success);
        data.put("message", message);
        data.put("provider", provider);
        if (cartCommand != null && cartCommand.success() && cartCommand.product() != null) {
            Map<String, Object> cartAction = new HashMap<>();
            cartAction.put("added", true);
            cartAction.put("productId", cartCommand.product().getId());
            cartAction.put("productName", cartCommand.product().getName());
            cartAction.put("productImage", cartCommand.product().getImageUrl());
            cartAction.put("addedQuantity", cartCommand.addedQuantity());
            cartAction.put("productQuantity", cartCommand.productQuantity());
            cartAction.put("cartQuantity", cartCommand.cartQuantity());
            data.put("cartAction", cartAction);
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        mapper.writeValue(resp.getWriter(), data);
    }

    private void saveHistory(User user, String question, String answer, String provider) {
        if (user == null) return;
        try {
            chatHistoryDAO.save(user.getId(), question, answer, provider);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not save chat history", e);
        }
    }
}
