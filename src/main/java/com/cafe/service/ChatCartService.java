package com.cafe.service;

import com.cafe.dao.ProductDAO;
import com.cafe.model.Product;
import com.cafe.model.User;
import com.cafe.security.RoleAccessPolicy;
import com.cafe.service.CartService.AddResult;
import jakarta.servlet.http.HttpSession;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns explicit chat commands into safe, server-side cart updates. */
public class ChatCartService {
    private static final int MAX_CHAT_QUANTITY = 10;
    private static final Pattern ACTION_QUANTITY_PATTERN = Pattern.compile(
            "(?:them|mua|dat(?: mon)?|lay|goi)\\s+(\\d{1,2})(?:\\s+(?:ly|coc|cai|phan))?\\b"
    );
    private static final Pattern POLITE_QUANTITY_PATTERN = Pattern.compile(
            "cho (?:toi|minh) (?:dat|them|lay|goi)?\\s*(\\d{1,2})(?:\\s+(?:ly|coc|cai|phan))?\\b"
    );

    private final ProductDAO productDAO;
    private final CartService cartService;

    public ChatCartService() {
        this(new ProductDAO(), new CartService());
    }

    ChatCartService(ProductDAO productDAO, CartService cartService) {
        this.productDAO = productDAO;
        this.cartService = cartService;
    }

    public Result handle(String message, User user, HttpSession session) {
        String normalizedMessage = normalize(message);
        if (!isAddCommand(normalizedMessage)) return Result.notHandled();

        if (!RoleAccessPolicy.isCustomer(user) && !RoleAccessPolicy.isStaff(user)) {
            return Result.failure("Tài khoản hiện tại không có chức năng giỏ hàng.");
        }

        List<Product> products = productDAO.getAllProducts();
        Product product = products.stream()
                .filter(item -> normalizedMessage.contains(normalize(item.getName())))
                .max(Comparator.comparingInt(item -> normalize(item.getName()).length()))
                .orElse(null);
        if (product == null) {
            return Result.failure("Mình chưa nhận ra tên món. Bạn hãy nhập rõ, ví dụ: “Thêm cà phê sữa vào giỏ”.");
        }

        int requestedQuantity = extractQuantity(normalizedMessage);
        AddResult lastResult = null;
        int addedQuantity = 0;
        for (int index = 0; index < requestedQuantity; index++) {
            lastResult = cartService.addByProductId(session, product.getId());
            if (!lastResult.isSuccess()) break;
            addedQuantity++;
        }

        if (addedQuantity == 0) {
            return Result.failure(lastResult == null
                    ? "Không thể thêm món vào giỏ hàng."
                    : lastResult.getMessage());
        }

        String nextAction = RoleAccessPolicy.isCustomer(user)
                ? "Mở Giỏ và bấm Đặt hàng khi bạn đã chọn xong nhé."
                : "Bạn có thể mở Giỏ để tiếp tục thanh toán.";
        String messageText = "Đã thêm " + addedQuantity + " " + product.getName()
                + " vào giỏ. Giỏ hiện có " + lastResult.getCartQuantity() + " món. "
                + nextAction;
        if (addedQuantity < requestedQuantity && lastResult != null) {
            messageText += " " + lastResult.getMessage();
        }

        return Result.success(messageText, product, addedQuantity,
                lastResult.getProductQuantity(), lastResult.getCartQuantity());
    }

    private boolean isAddCommand(String message) {
        return message.startsWith("them ")
                || message.startsWith("mua ")
                || message.startsWith("dat ")
                || message.startsWith("dat mon ")
                || message.startsWith("lay ")
                || message.startsWith("goi ")
                || message.contains("cho toi dat ")
                || message.contains("cho minh dat ")
                || message.contains("cho toi lay ")
                || message.contains("cho minh lay ")
                || message.matches("^cho (?:toi|minh) (?:mot|\\d{1,2}) (?:ly |coc |phan |cai )?.+")
                || ((message.contains("them") || message.contains("cho"))
                && message.contains("vao gio"));
    }

    private int extractQuantity(String message) {
        Matcher matcher = ACTION_QUANTITY_PATTERN.matcher(message);
        if (!matcher.find()) {
            matcher = POLITE_QUANTITY_PATTERN.matcher(message);
        }
        if (!matcher.find(0)) return 1;
        try {
            return Math.max(1, Math.min(MAX_CHAT_QUANTITY,
                    Integer.parseInt(matcher.group(1))));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    public record Result(boolean handled, boolean success, String message, Product product,
                         int addedQuantity, int productQuantity, int cartQuantity) {
        static Result notHandled() {
            return new Result(false, false, null, null, 0, 0, 0);
        }

        static Result failure(String message) {
            return new Result(true, false, message, null, 0, 0, 0);
        }

        static Result success(String message, Product product, int addedQuantity,
                              int productQuantity, int cartQuantity) {
            return new Result(true, true, message, product, addedQuantity,
                    productQuantity, cartQuantity);
        }
    }
}
