package com.cafe.servlet;

import com.cafe.dao.ProductDAO;
import com.cafe.model.Product;
import com.cafe.model.User;
import com.cafe.utils.BarcodeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/products")
public class ProductManagementServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!canManageMenu(req)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        String action = req.getParameter("action");
        if ("edit".equals(action) || "create".equals(action)) {
            if ("edit".equals(action)) {
                Integer id = parsePositiveInt(req.getParameter("id"));
                if (id == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ.");
                    return;
                }
                Product p = productDAO.getById(id);
                if (p == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                req.setAttribute("product", p);
            }
            req.getRequestDispatcher("/WEB-INF/views/admin/product_form.jsp").forward(req, resp);
        } else {
            List<Product> products = productDAO.getAllProducts();
            req.setAttribute("products", products);
            req.getRequestDispatcher("/WEB-INF/views/admin/product_list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        if (!canManageMenu(req)) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        String action = req.getParameter("action");
        try {
            if ("delete".equals(action)) {
                Integer id = parsePositiveInt(req.getParameter("id"));
                if (id == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ.");
                    return;
                }
                if (!productDAO.deleteProduct(id)) {
                    resp.sendRedirect(req.getContextPath()
                            + "/admin/products?error=Không thể xóa sản phẩm đã có giao dịch");
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/admin/products?success=1");
                return;
            }

            int id = req.getParameter("id") == null || req.getParameter("id").isBlank()
                    ? 0 : requirePositiveInt(req.getParameter("id"));
            String name = requireText(req.getParameter("name"), "Tên sản phẩm");
            double price = Double.parseDouble(req.getParameter("price"));
            String description = req.getParameter("description");
            int stock = Integer.parseInt(req.getParameter("stock"));
            int soldCount = Integer.parseInt(req.getParameter("soldCount"));
            String imageUrl = requireText(req.getParameter("imageUrl"), "URL hình ảnh");
            String category = requireText(req.getParameter("category"), "Danh mục");
            String barcode = BarcodeUtil.normalize(req.getParameter("barcode"));
            if (price < 0 || stock < 0 || soldCount < 0) {
                throw new IllegalArgumentException("Giá, tồn kho và số lượng bán không được âm.");
            }

            Product p = new Product(id, name, price, description, stock, soldCount, imageUrl, category, barcode);
            if (barcode != null && !BarcodeUtil.isValidEan13(barcode)) {
                showProductForm(req, resp, p,
                        "Barcode EAN-13 phải có đúng 13 chữ số và check digit hợp lệ.");
                return;
            }
            if (productDAO.barcodeExists(barcode, id)) {
                showProductForm(req, resp, p, "Mã vạch này đã được đăng ký cho một sản phẩm khác.");
                return;
            }

            boolean saved;
            if (id == 0) {
                saved = productDAO.addProduct(p);
            } else {
                saved = productDAO.updateProduct(p);
            }
            if (!saved) {
                showProductForm(req, resp, p, "Không thể lưu sản phẩm. Vui lòng kiểm tra kết nối cơ sở dữ liệu.");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/admin/products?success=1");
        } catch (IllegalArgumentException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    private void showProductForm(HttpServletRequest req, HttpServletResponse resp, Product product, String error)
            throws ServletException, IOException {
        req.setAttribute("product", product);
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/admin/product_form.jsp").forward(req, resp);
    }

    private boolean canManageMenu(HttpServletRequest req) {
        User user = (User) req.getSession().getAttribute("user");
        return user != null && "manager".equalsIgnoreCase(user.getRole());
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private int requirePositiveInt(String value) {
        Integer parsed = parsePositiveInt(value);
        if (parsed == null) throw new IllegalArgumentException("ID sản phẩm không hợp lệ.");
        return parsed;
    }

    private String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " không được để trống.");
        }
        return value.trim();
    }
}
