package com.cafe.service;

import com.cafe.dao.ProductDAO;
import com.cafe.dao.PromotionDAO;
import com.cafe.dao.PromotionItemDAO;
import com.cafe.model.CartItem;
import com.cafe.model.Product;
import com.cafe.model.Promotion;
import com.cafe.model.PromotionItem;
import com.cafe.utils.BarcodeUtil;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class CartService {
    private final ProductDAO productDAO;
    private final PromotionDAO promotionDAO;
    private final PromotionItemDAO promotionItemDAO;

    public CartService() {
        this(new ProductDAO(), new PromotionDAO(), new PromotionItemDAO());
    }

    CartService(ProductDAO productDAO, PromotionDAO promotionDAO, PromotionItemDAO promotionItemDAO) {
        this.productDAO = productDAO;
        this.promotionDAO = promotionDAO;
        this.promotionItemDAO = promotionItemDAO;
    }

    public AddResult addByProductId(HttpSession session, int productId) {
        Product product = productDAO.getById(productId);
        return addProduct(session, product);
    }

    public AddResult addByBarcode(HttpSession session, String barcode) {
        String normalizedBarcode = BarcodeUtil.normalize(barcode);
        if (normalizedBarcode == null) {
            return AddResult.failure("EMPTY_BARCODE", "Vui lòng quét hoặc nhập mã vạch.");
        }
        if (!BarcodeUtil.isValidEan13(normalizedBarcode)) {
            return AddResult.failure(
                    "INVALID_BARCODE",
                    "Barcode EAN-13 phải có đúng 13 chữ số và check digit hợp lệ."
            );
        }
        Product product;
        try {
            product = productDAO.getByBarcode(normalizedBarcode);
        } catch (SQLException e) {
            return AddResult.failure(
                    "DATABASE_ERROR",
                    "Không thể tra cứu barcode. Hãy chạy migration V006 và kiểm tra kết nối database."
            );
        }
        if (product == null) {
            return AddResult.failure(
                    "NOT_FOUND",
                    "Mã " + normalizedBarcode + " chưa được gán cho sản phẩm nào trong database."
            );
        }
        return addProduct(session, product);
    }

    @SuppressWarnings("unchecked")
    private AddResult addProduct(HttpSession session, Product product) {
        if (product == null) {
            return AddResult.failure("NOT_FOUND", "Không tìm thấy sản phẩm.");
        }
        if (product.getStock() <= 0) {
            return AddResult.failure("OUT_OF_STOCK", product.getName() + " đã hết hàng.");
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                if (item.getQuantity() >= product.getStock()) {
                    return AddResult.failure(
                            "STOCK_LIMIT",
                            "Giỏ hàng đã có toàn bộ " + product.getStock() + " sản phẩm " + product.getName() + "."
                    );
                }
                item.setQuantity(item.getQuantity() + 1);
                session.setAttribute("cart", cart);
                return AddResult.success(product, item.getQuantity(), countItems(cart));
            }
        }

        CartItem newItem = new CartItem(product, 1);
        newItem.setDiscountedPrice(calculatePrice(product));
        cart.add(newItem);
        session.setAttribute("cart", cart);
        return AddResult.success(product, 1, countItems(cart));
    }

    private double calculatePrice(Product product) {
        PromotionItem itemPromotion = promotionItemDAO.getByProductId(product.getId());
        int discountPercent;
        if (itemPromotion != null) {
            discountPercent = itemPromotion.getDiscountPercent();
        } else {
            List<Promotion> promotions = promotionDAO.getActivePromotions();
            discountPercent = promotions.stream()
                    .mapToInt(Promotion::getDiscountPercent)
                    .max()
                    .orElse(0);
        }
        return product.getPrice() * (100 - discountPercent) / 100.0;
    }

    private int countItems(List<CartItem> cart) {
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public static class AddResult {
        private final boolean success;
        private final String code;
        private final String message;
        private final Product product;
        private final int productQuantity;
        private final int cartQuantity;

        private AddResult(boolean success, String code, String message, Product product,
                          int productQuantity, int cartQuantity) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.product = product;
            this.productQuantity = productQuantity;
            this.cartQuantity = cartQuantity;
        }

        static AddResult success(Product product, int productQuantity, int cartQuantity) {
            return new AddResult(
                    true,
                    "ADDED",
                    "Đã thêm " + product.getName() + " vào giỏ hàng.",
                    product,
                    productQuantity,
                    cartQuantity
            );
        }

        static AddResult failure(String code, String message) {
            return new AddResult(false, code, message, null, 0, 0);
        }

        public boolean isSuccess() { return success; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Product getProduct() { return product; }
        public int getProductQuantity() { return productQuantity; }
        public int getCartQuantity() { return cartQuantity; }
    }
}
