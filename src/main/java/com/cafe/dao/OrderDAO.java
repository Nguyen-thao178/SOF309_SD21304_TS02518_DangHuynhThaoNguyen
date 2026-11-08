package com.cafe.dao;

import com.cafe.model.CartItem;
import com.cafe.model.Order;
import com.cafe.model.ReceiptItem;
import com.cafe.service.SystemSettingsService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    public static final double DEFAULT_DEPOSIT_RATE = 0.30;

    public int createOrder(int userId, List<CartItem> cart, double total) throws SQLException {
        return createOrder(userId, cart, total, "direct", "cash", null, total);
    }

    public int createOrder(int userId, List<CartItem> cart, double total, String orderType,
                           String paymentMethod, java.sql.Date pickupDate, double paidAmount)
            throws SQLException {
        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        PreparedStatement psUpdateProduct = null;
        ResultSet generatedKeys = null;
        int orderId = -1;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra tồn kho
            for (CartItem item : cart) {
                if (!checkStock(conn, item.getProduct().getId(), item.getQuantity())) {
                    throw new SQLException("Sản phẩm " + item.getProduct().getName() + " không đủ hàng.");
                }
            }

            boolean deposit = "deposit".equals(orderType);
            if (deposit && pickupDate == null) {
                throw new SQLException("Đơn cọc phải có ngày nhận hàng.");
            }
            String status = deposit ? "deposit_pending" : "completed";
            String pickupStatus = deposit ? "pending" : null;
            double depositAmount = deposit ? paidAmount : 0;

            String sqlOrder = "INSERT INTO orders " +
                    "(user_id, total_amount, status, order_type, payment_method, deposit_amount, pickup_date, pickup_status, completed_at, gross_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setDouble(2, total);
            psOrder.setString(3, status);
            psOrder.setString(4, orderType);
            psOrder.setString(5, paymentMethod);
            psOrder.setDouble(6, depositAmount);
            psOrder.setDate(7, pickupDate);
            psOrder.setString(8, pickupStatus);
            psOrder.setTimestamp(9, deposit ? null : new Timestamp(System.currentTimeMillis()));
            psOrder.setDouble(10, total);
            psOrder.executeUpdate();

            generatedKeys = psOrder.getGeneratedKeys();
            if (generatedKeys.next()) orderId = generatedKeys.getInt(1);
            else throw new SQLException("Không lấy được order ID.");

            String sqlDetail = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            psDetail = conn.prepareStatement(sqlDetail);
            for (CartItem item : cart) {
                psDetail.setInt(1, orderId);
                psDetail.setInt(2, item.getProduct().getId());
                psDetail.setInt(3, item.getQuantity());
                psDetail.setDouble(4, item.getDiscountedPrice()); // giá đã khuyến mãi
                psDetail.addBatch();
            }
            psDetail.executeBatch();

            String sqlUpdateProduct = deposit
                    ? "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?"
                    : "UPDATE products SET sold_count = sold_count + ?, stock = stock - ? WHERE id = ? AND stock >= ?";
            psUpdateProduct = conn.prepareStatement(sqlUpdateProduct);
            for (CartItem item : cart) {
                if (deposit) {
                    psUpdateProduct.setInt(1, item.getQuantity());
                    psUpdateProduct.setInt(2, item.getProduct().getId());
                    psUpdateProduct.setInt(3, item.getQuantity());
                } else {
                    psUpdateProduct.setInt(1, item.getQuantity());
                    psUpdateProduct.setInt(2, item.getQuantity());
                    psUpdateProduct.setInt(3, item.getProduct().getId());
                    psUpdateProduct.setInt(4, item.getQuantity());
                }
                psUpdateProduct.addBatch();
            }
            int[] stockResults = psUpdateProduct.executeBatch();
            for (int result : stockResults) {
                if (result == 0) {
                    throw new SQLException("Tồn kho đã thay đổi. Vui lòng kiểm tra lại giỏ hàng.");
                }
            }

            conn.commit();
            if (!deposit) {
                LoyaltyDAO loyaltyDAO = new LoyaltyDAO();
                int pointsEarned = (int) (total /
                        SystemSettingsService.getPositiveInt("loyalty_vnd_per_point", 1000));
                loyaltyDAO.addPoints(userId, pointsEarned, total);
            }
            return orderId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (psOrder != null) psOrder.close();
            if (psDetail != null) psDetail.close();
            if (psUpdateProduct != null) psUpdateProduct.close();
            if (generatedKeys != null) generatedKeys.close();
            if (conn != null) conn.close();
        }
    }

    private boolean checkStock(Connection conn, int productId, int requestedQuantity) throws SQLException {
        String sql = "SELECT stock FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock") >= requestedQuantity;
            return false;
        }
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) orders.add(mapOrder(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderByIdAndUserId(int orderId, int userId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapOrder(rs) : null;
            }
        }
    }

    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapOrder(rs) : null;
            }
        }
    }

    public Order getCustomerDepositOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.*, u.fullname AS customer_name, u.username AS customer_username " +
                "FROM orders o INNER JOIN users u ON u.id = o.user_id " +
                "WHERE o.id = ? AND o.order_type = 'deposit' " +
                "AND LOWER(u.role) IN ('customer', 'member')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order order = mapOrder(rs);
                order.setCustomerName(rs.getString("customer_name"));
                order.setCustomerUsername(rs.getString("customer_username"));
                return order;
            }
        }
    }

    public List<ReceiptItem> getReceiptItems(int orderId, int userId) throws SQLException {
        List<ReceiptItem> items = new ArrayList<>();
        String sql = "SELECT p.name, oi.quantity, oi.price " +
                "FROM order_items oi " +
                "INNER JOIN orders o ON o.id = oi.order_id " +
                "INNER JOIN products p ON p.id = oi.product_id " +
                "WHERE oi.order_id = ? AND o.user_id = ? ORDER BY oi.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new ReceiptItem(
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    ));
                }
            }
        }
        return items;
    }

    public List<ReceiptItem> getReceiptItems(int orderId) throws SQLException {
        List<ReceiptItem> items = new ArrayList<>();
        String sql = "SELECT p.name, oi.quantity, oi.price " +
                "FROM order_items oi " +
                "INNER JOIN products p ON p.id = oi.product_id " +
                "WHERE oi.order_id = ? ORDER BY oi.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new ReceiptItem(
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    ));
                }
            }
        }
        return items;
    }

    public List<Order> getDepositOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? AND order_type = 'deposit' " +
                "ORDER BY CASE WHEN status = 'deposit_pending' THEN 0 ELSE 1 END, pickup_date, order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getCustomerDepositOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.fullname AS customer_name, u.username AS customer_username " +
                "FROM orders o INNER JOIN users u ON u.id = o.user_id " +
                "WHERE o.order_type = 'deposit' AND LOWER(u.role) IN ('customer', 'member') " +
                "ORDER BY CASE WHEN o.status = 'deposit_pending' THEN 0 ELSE 1 END, " +
                "o.pickup_date, o.order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setCustomerName(rs.getString("customer_name"));
                order.setCustomerUsername(rs.getString("customer_username"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int countPendingDeposits(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? " +
                "AND order_type = 'deposit' AND status = 'deposit_pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int expireOverdueDeposits() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             CallableStatement statement = conn.prepareCall("{call dbo.usp_expire_overdue_deposits}")) {
            boolean hasResults = statement.execute();
            while (!hasResults && statement.getUpdateCount() != -1) {
                hasResults = statement.getMoreResults();
            }
            if (hasResults) {
                try (ResultSet rs = statement.getResultSet()) {
                    return rs.next() ? rs.getInt("expired_count") : 0;
                }
            }
            return 0;
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setOrderType(rs.getString("order_type"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setDepositAmount(rs.getDouble("deposit_amount"));
        order.setPickupDate(rs.getDate("pickup_date"));
        order.setPickupStatus(rs.getString("pickup_status"));
        return order;
    }
}
