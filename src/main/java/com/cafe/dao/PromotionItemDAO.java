package com.cafe.dao;

import com.cafe.model.PromotionItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionItemDAO {
    public List<PromotionItem> getActivePromotionItems() {
        List<PromotionItem> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion_items WHERE status='active' AND start_date <= CAST(GETDATE() AS DATE) AND end_date >= CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PromotionItem p = new PromotionItem();
                p.setId(rs.getInt("id"));
                p.setProductId(rs.getInt("product_id"));
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setStartDate(rs.getDate("start_date"));
                p.setEndDate(rs.getDate("end_date"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Lấy khuyến mãi cho một sản phẩm cụ thể
    public PromotionItem getByProductId(int productId) {
        String sql = "SELECT * FROM promotion_items WHERE product_id = ? AND status='active' AND start_date <= CAST(GETDATE() AS DATE) AND end_date >= CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PromotionItem p = new PromotionItem();
                p.setId(rs.getInt("id"));
                p.setProductId(rs.getInt("product_id"));
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setStartDate(rs.getDate("start_date"));
                p.setEndDate(rs.getDate("end_date"));
                p.setStatus(rs.getString("status"));
                return p;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}