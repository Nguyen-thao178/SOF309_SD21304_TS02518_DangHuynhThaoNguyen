package com.cafe.dao;

import com.cafe.model.CartItem;
import com.cafe.model.Payment;
import com.cafe.service.SystemSettingsService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Owns the payment lifecycle. VNPay orders are persisted and inventory is
 * reserved before the customer leaves the application.
 */
public class PaymentDAO {

    public Payment createPendingVNPayOrder(int userId, List<CartItem> cart, double total,
                                           String orderType, Date pickupDate, double payable)
            throws SQLException {
        return createPendingVNPayOrder(userId, cart, total, total, orderType, pickupDate,
                payable, 0, 0);
    }

    public Payment createPendingVNPayOrder(int userId, List<CartItem> cart, double total,
                                           double grossAmount, String orderType, Date pickupDate,
                                           double payable, int loyaltyPointsUsed,
                                           double loyaltyDiscountAmount) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockAndValidateStock(connection, cart);
                boolean deposit = "deposit".equals(orderType);
                if (deposit && pickupDate == null) {
                    throw new SQLException("Đơn cọc phải có ngày nhận hàng.");
                }
                LoyaltyDAO.redeemPoints(connection, userId, loyaltyPointsUsed);

                int orderId;
                String orderSql = """
                        INSERT INTO orders
                            (user_id, total_amount, status, order_type, payment_method,
                             deposit_amount, pickup_date, pickup_status, gross_amount,
                             loyalty_points_used, loyalty_discount_amount)
                        VALUES (?, ?, 'pending', ?, 'vnpay', ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement statement =
                             connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, userId);
                    statement.setDouble(2, total);
                    statement.setString(3, orderType);
                    statement.setDouble(4, deposit ? payable : 0);
                    statement.setDate(5, pickupDate);
                    statement.setString(6, deposit ? "pending" : null);
                    statement.setDouble(7, grossAmount);
                    statement.setInt(8, loyaltyPointsUsed);
                    statement.setDouble(9, loyaltyDiscountAmount);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được order ID.");
                        orderId = keys.getInt(1);
                    }
                }

                insertItems(connection, orderId, cart);
                reserveInventory(connection, cart);

                Payment payment = findPaymentByOrderAndStage(
                        connection, orderId, deposit ? "deposit" : "full");
                if (payment == null) {
                    throw new SQLException("Không tạo được payment pending cho đơn VNPay.");
                }
                connection.commit();
                return payment;
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException(exception);
            }
        }
    }

    public Payment createBalancePaymentForCustomerOrder(int orderId, String paymentMethod)
            throws SQLException {
        if (!"cash".equals(paymentMethod) && !"vnpay".equals(paymentMethod)) {
            throw new SQLException("Phương thức thanh toán phần còn lại không hợp lệ.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                double balance;
                int customerUserId;
                String lockSql = """
                        SELECT o.total_amount - o.deposit_amount AS balance, o.user_id
                        FROM orders o WITH (UPDLOCK, ROWLOCK)
                        INNER JOIN users u ON u.id = o.user_id
                        WHERE o.id = ? AND o.order_type = 'deposit'
                          AND o.status = 'deposit_pending'
                          AND LOWER(u.role) IN ('customer', 'member')
                        """;
                try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
                    statement.setInt(1, orderId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Đơn cọc không còn ở trạng thái chờ nhận.");
                        }
                        balance = result.getDouble("balance");
                        customerUserId = result.getInt("user_id");
                    }
                }

                String duplicateSql = """
                        SELECT COUNT(*) FROM payments
                        WHERE order_id = ? AND payment_stage = 'balance'
                          AND status IN ('pending', 'paid')
                        """;
                try (PreparedStatement statement = connection.prepareStatement(duplicateSql)) {
                    statement.setInt(1, orderId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next() && result.getInt(1) > 0) {
                            throw new SQLException("Phần tiền còn lại đã được tạo hoặc thanh toán.");
                        }
                    }
                }

                long paymentId;
                String insertSql = """
                        INSERT INTO payments
                            (order_id, payment_stage, payment_method, amount, status, paid_at)
                        VALUES (?, 'balance', ?, ?, ?, ?)
                        """;
                boolean cash = "cash".equals(paymentMethod);
                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, orderId);
                    statement.setString(2, paymentMethod);
                    statement.setDouble(3, balance);
                    statement.setString(4, cash ? "paid" : "pending");
                    statement.setTimestamp(5, cash ? new Timestamp(System.currentTimeMillis()) : null);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được payment ID.");
                        paymentId = keys.getLong(1);
                    }
                }

                if (cash) completePickup(connection, orderId);
                Payment payment = findPaymentById(connection, paymentId);
                connection.commit();
                if (cash) addLoyaltyAfterCommit(customerUserId, payment.getOrderTotal());
                return payment;
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException(exception);
            }
        }
    }

    public Payment getPendingPaymentForUser(long paymentId, int userId) throws SQLException {
        String sql = paymentSelect() + " WHERE p.id = ? AND o.user_id = ? AND p.status = 'pending'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPayment(result) : null;
            }
        }
    }

    public Payment getPendingCustomerBalancePaymentForStaff(long paymentId) throws SQLException {
        String sql = paymentSelect() + " INNER JOIN users u ON u.id = o.user_id " +
                "WHERE p.id = ? AND p.payment_stage = 'balance' AND p.status = 'pending' " +
                "AND o.order_type = 'deposit' AND LOWER(u.role) IN ('customer', 'member')";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPayment(result) : null;
            }
        }
    }

    public Payment getByTransactionReference(String transactionReference) throws SQLException {
        String sql = paymentSelect() + " WHERE p.transaction_reference = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transactionReference);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPayment(result) : null;
            }
        }
    }

    public boolean attachTransactionReference(long paymentId, String reference) throws SQLException {
        String sql = """
                UPDATE payments SET transaction_reference = ?
                WHERE id = ? AND status = 'pending' AND transaction_reference IS NULL
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);
            statement.setLong(2, paymentId);
            return statement.executeUpdate() == 1;
        }
    }

    public Payment completeVNPayPayment(String transactionReference) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Payment payment = lockByReference(connection, transactionReference);
                if (payment == null) throw new SQLException("Không tìm thấy giao dịch VNPay.");
                if ("paid".equals(payment.getStatus())) {
                    connection.commit();
                    return payment;
                }
                if (!"pending".equals(payment.getStatus())) {
                    throw new SQLException("Giao dịch VNPay không còn chờ xử lý.");
                }

                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE payments SET status = 'paid', paid_at = SYSDATETIME()
                        WHERE id = ? AND status = 'pending'
                        """)) {
                    statement.setLong(1, payment.getId());
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Giao dịch đã được xử lý bởi yêu cầu khác.");
                    }
                }

                if ("balance".equals(payment.getPaymentStage())) {
                    completePickup(connection, payment.getOrderId());
                } else {
                    completeInitialOrder(connection, payment);
                }
                connection.commit();
                addLoyaltyAfterCommit(payment.getUserId(),
                        "deposit".equals(payment.getPaymentStage())
                                ? 0 : payment.getOrderTotal());
                payment.setStatus("paid");
                return payment;
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException(exception);
            }
        }
    }

    public void failVNPayPayment(String transactionReference) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Payment payment = lockByReference(connection, transactionReference);
                if (payment == null || !"pending".equals(payment.getStatus())) {
                    connection.rollback();
                    return;
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE payments SET status = 'failed' WHERE id = ? AND status = 'pending'")) {
                    statement.setLong(1, payment.getId());
                    statement.executeUpdate();
                }
                if (!"balance".equals(payment.getPaymentStage())) {
                    restoreInventory(connection, payment.getOrderId());
                    try (PreparedStatement statement = connection.prepareStatement("""
                            UPDATE orders SET status = 'cancelled', cancelled_at = SYSDATETIME(),
                                cancellation_reason = N'Thanh toán VNPay thất bại'
                            WHERE id = ? AND status = 'pending'
                            """)) {
                        statement.setInt(1, payment.getOrderId());
                        statement.executeUpdate();
                    }
                    LoyaltyDAO.refundOrderPoints(connection, payment.getOrderId());
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException(exception);
            }
        }
    }

    private void completeInitialOrder(Connection connection, Payment payment) throws SQLException {
        boolean deposit = "deposit".equals(payment.getPaymentStage());
        if (!deposit) incrementSoldCount(connection, payment.getOrderId());
        String status = deposit ? "deposit_pending" : "completed";
        String sql = """
                UPDATE orders SET status = ?, completed_at = ?
                WHERE id = ? AND status = 'pending'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setTimestamp(2, deposit ? null : new Timestamp(System.currentTimeMillis()));
            statement.setInt(3, payment.getOrderId());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Đơn hàng không còn chờ thanh toán.");
            }
        }
    }

    private void completePickup(Connection connection, int orderId) throws SQLException {
        incrementSoldCount(connection, orderId);
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE orders SET status = 'picked_up', pickup_status = 'picked_up',
                    completed_at = SYSDATETIME()
                WHERE id = ? AND status = 'deposit_pending'
                """)) {
            statement.setInt(1, orderId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không thể xác nhận nhận hàng.");
            }
        }
    }

    private void incrementSoldCount(Connection connection, int orderId) throws SQLException {
        String sql = """
                UPDATE p SET sold_count = sold_count + items.quantity
                FROM products p
                INNER JOIN (
                    SELECT product_id, SUM(quantity) quantity
                    FROM order_items WHERE order_id = ? GROUP BY product_id
                ) items ON items.product_id = p.id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    private void restoreInventory(Connection connection, int orderId) throws SQLException {
        String sql = """
                UPDATE p SET stock = stock + items.quantity
                FROM products p
                INNER JOIN (
                    SELECT product_id, SUM(quantity) quantity
                    FROM order_items WHERE order_id = ? GROUP BY product_id
                ) items ON items.product_id = p.id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    private void lockAndValidateStock(Connection connection, List<CartItem> cart)
            throws SQLException {
        String sql = "SELECT stock FROM products WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
        for (CartItem item : cart) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, item.getProduct().getId());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || result.getInt("stock") < item.getQuantity()) {
                        throw new SQLException("Sản phẩm " + item.getProduct().getName()
                                + " không đủ hàng.");
                    }
                }
            }
        }
    }

    private void insertItems(Connection connection, int orderId, List<CartItem> cart)
            throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (CartItem item : cart) {
                statement.setInt(1, orderId);
                statement.setInt(2, item.getProduct().getId());
                statement.setInt(3, item.getQuantity());
                statement.setDouble(4, item.getDiscountedPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void reserveInventory(Connection connection, List<CartItem> cart) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (CartItem item : cart) {
                statement.setInt(1, item.getQuantity());
                statement.setInt(2, item.getProduct().getId());
                statement.setInt(3, item.getQuantity());
                statement.addBatch();
            }
            for (int result : statement.executeBatch()) {
                if (result == 0) throw new SQLException("Tồn kho đã thay đổi.");
            }
        }
    }

    private Payment findPaymentByOrderAndStage(Connection connection, int orderId, String stage)
            throws SQLException {
        String sql = paymentSelect() + " WHERE p.order_id = ? AND p.payment_stage = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, stage);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPayment(result) : null;
            }
        }
    }

    private Payment findPaymentById(Connection connection, long paymentId) throws SQLException {
        String sql = paymentSelect() + " WHERE p.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPayment(result) : null;
            }
        }
    }

    private Payment lockByReference(Connection connection, String reference) throws SQLException {
        String sql = paymentSelect().replace("FROM payments p",
                "FROM payments p WITH (UPDLOCK, ROWLOCK)") + " WHERE p.transaction_reference = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPayment(result) : null;
            }
        }
    }

    private String paymentSelect() {
        return """
                SELECT p.id, p.order_id, o.user_id, o.order_type,
                       o.total_amount AS order_total, p.payment_stage,
                       p.payment_method, p.amount, p.status, p.transaction_reference
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                """;
    }

    private Payment mapPayment(ResultSet result) throws SQLException {
        Payment payment = new Payment();
        payment.setId(result.getLong("id"));
        payment.setOrderId(result.getInt("order_id"));
        payment.setUserId(result.getInt("user_id"));
        payment.setOrderType(result.getString("order_type"));
        payment.setOrderTotal(result.getDouble("order_total"));
        payment.setPaymentStage(result.getString("payment_stage"));
        payment.setPaymentMethod(result.getString("payment_method"));
        payment.setAmount(result.getDouble("amount"));
        payment.setStatus(result.getString("status"));
        payment.setTransactionReference(result.getString("transaction_reference"));
        return payment;
    }

    private void addLoyaltyAfterCommit(int userId, double amount) {
        if (amount <= 0) return;
        int vndPerPoint = SystemSettingsService.getPositiveInt("loyalty_vnd_per_point", 1000);
        new LoyaltyDAO().addPoints(userId, (int) (amount / vndPerPoint), amount);
    }
}
