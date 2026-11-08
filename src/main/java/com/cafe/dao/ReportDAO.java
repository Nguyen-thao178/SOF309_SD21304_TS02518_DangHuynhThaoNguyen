package com.cafe.dao;

import java.sql.*;
import java.util.*;

public class ReportDAO {

    public double getTotalRevenueByDate(String date) {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) FROM orders WHERE CAST(order_date AS DATE) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenueByMonth(int year, int month) {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) FROM orders WHERE YEAR(order_date) = ? AND MONTH(order_date) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getTopProducts(int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT TOP (?) p.name, SUM(oi.quantity) as total_qty " +
                     "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                     "GROUP BY p.name ORDER BY total_qty DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Double> getDailyRevenue(int year, int month) {
        Map<String, Double> daily = new LinkedHashMap<>();
        String sql = "SELECT DAY(order_date) as day, SUM(total_amount) as revenue " +
                     "FROM orders WHERE YEAR(order_date) = ? AND MONTH(order_date) = ? " +
                     "GROUP BY DAY(order_date) ORDER BY day";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                daily.put(String.valueOf(rs.getInt("day")), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return daily;
    }
}