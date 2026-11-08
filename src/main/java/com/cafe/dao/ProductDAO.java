package com.cafe.dao;

import com.cafe.model.Product;
import com.cafe.utils.BarcodeUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Product getById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Product getByBarcode(String barcode) throws SQLException {
        String normalizedBarcode = BarcodeUtil.normalize(barcode);
        if (normalizedBarcode == null) {
            return null;
        }

        String sql = "SELECT id, name, price, description, stock, sold_count, image_url, category, barcode " +
                "FROM products WHERE barcode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedBarcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        }
        return null;
    }

    public boolean barcodeExists(String barcode, int excludedProductId) {
        String normalizedBarcode = BarcodeUtil.normalize(barcode);
        if (normalizedBarcode == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM products WHERE barcode = ? AND id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedBarcode);
            ps.setInt(2, excludedProductId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public List<Product> searchByName(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapProduct(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 🔥 Phương thức mới: lấy top N sản phẩm bán chạy nhất
    public List<Product> getTopSellingProducts(int limit) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM products ORDER BY sold_count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getString("description"),
            rs.getInt("stock"),
            rs.getInt("sold_count"),
            rs.getString("image_url"),
            rs.getString("category"),
            rs.getString("barcode")
        );
    }
    
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, price, description, stock, sold_count, image_url, category, barcode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setString(3, product.getDescription());
            ps.setInt(4, product.getStock());
            ps.setInt(5, product.getSoldCount());
            ps.setString(6, product.getImageUrl());
            ps.setString(7, product.getCategory());
            ps.setString(8, BarcodeUtil.normalize(product.getBarcode()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, price=?, description=?, stock=?, sold_count=?, image_url=?, category=?, barcode=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setString(3, product.getDescription());
            ps.setInt(4, product.getStock());
            ps.setInt(5, product.getSoldCount());
            ps.setString(6, product.getImageUrl());
            ps.setString(7, product.getCategory());
            ps.setString(8, BarcodeUtil.normalize(product.getBarcode()));
            ps.setInt(9, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

}
