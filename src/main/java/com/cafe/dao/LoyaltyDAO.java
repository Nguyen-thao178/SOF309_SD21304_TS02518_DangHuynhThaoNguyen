package com.cafe.dao;

import com.cafe.model.LoyaltyPoint;
import com.cafe.model.MemberProfile;
import com.cafe.model.MemberSummary;
import com.cafe.model.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LoyaltyDAO {
    public LoyaltyPoint getByUserId(int userId) {
        String sql = "SELECT * FROM loyalty_points WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapPoints(result) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể đọc điểm thưởng.", exception);
        }
    }

    /** Only real customers earn loyalty points; counter and admin accounts never do. */
    public boolean addPoints(int userId, int points, double spent) {
        String sql = """
                UPDATE lp SET points = points + ?, total_spent = total_spent + ?,
                    updated_at = SYSDATETIME()
                FROM loyalty_points lp
                INNER JOIN users u ON u.id = lp.user_id
                WHERE lp.user_id = ? AND LOWER(u.role) IN ('customer', 'member')
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(0, points));
            statement.setDouble(2, Math.max(0, spent));
            statement.setInt(3, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể cộng điểm thưởng.", exception);
        }
    }

    public boolean createEmpty(int userId) {
        String sql = """
                IF NOT EXISTS (SELECT 1 FROM loyalty_points WHERE user_id = ?)
                    INSERT INTO loyalty_points (user_id, points, total_spent) VALUES (?, 0, 0)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tạo tài khoản điểm thưởng.", exception);
        }
    }

    public MemberProfile getMemberProfile(int userId) {
        String sql = "SELECT * FROM member_profiles WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                MemberProfile profile = new MemberProfile();
                profile.setUserId(result.getInt("user_id"));
                profile.setMembershipCode(result.getString("membership_code"));
                profile.setPhone(result.getString("phone"));
                Date birthDate = result.getDate("birth_date");
                profile.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
                profile.setAddress(result.getString("address"));
                profile.setJoinedAt(result.getString("joined_at"));
                return profile;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể đọc hồ sơ thành viên.", exception);
        }
    }

    public MemberProfile registerMember(User user, String fullname, String email, String phone,
                                        Date birthDate, String address) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String lockSql = "SELECT role FROM users WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
                String role;
                try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
                    statement.setInt(1, user.getId());
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new SQLException("Không tìm thấy tài khoản.");
                        role = result.getString(1);
                    }
                }
                if (!"customer".equalsIgnoreCase(role) && !"member".equalsIgnoreCase(role)) {
                    throw new SQLException("Chỉ tài khoản khách hàng được đăng ký thành viên.");
                }

                String code = "CDR" + String.format("%07d", user.getId());
                try (PreparedStatement statement = connection.prepareStatement("""
                        IF NOT EXISTS (SELECT 1 FROM member_profiles WHERE user_id = ?)
                            INSERT INTO member_profiles
                                (user_id, membership_code, phone, birth_date, address)
                            VALUES (?, ?, ?, ?, ?)
                        """)) {
                    statement.setInt(1, user.getId());
                    statement.setInt(2, user.getId());
                    statement.setString(3, code);
                    statement.setString(4, phone);
                    statement.setDate(5, birthDate);
                    statement.setString(6, address);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE users SET fullname = ?, email = ?, role = 'member' WHERE id = ?
                        """)) {
                    statement.setString(1, fullname);
                    statement.setString(2, email);
                    statement.setInt(3, user.getId());
                    statement.executeUpdate();
                }
                connection.commit();
                user.setFullname(fullname);
                user.setEmail(email);
                user.setRole("member");
                return getMemberProfile(user.getId());
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException(exception);
            }
        }
    }

    public List<MemberSummary> getAllMembers() {
        List<MemberSummary> members = new ArrayList<>();
        String sql = """
                SELECT u.id, u.username, u.fullname, u.email, mp.membership_code,
                       mp.phone, mp.joined_at, COALESCE(lp.points, 0) points,
                       COALESCE(lp.total_spent, 0) total_spent
                FROM users u
                INNER JOIN member_profiles mp ON mp.user_id = u.id
                LEFT JOIN loyalty_points lp ON lp.user_id = u.id
                WHERE LOWER(u.role) = 'member' AND mp.status = 'active'
                ORDER BY mp.joined_at DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                MemberSummary member = new MemberSummary();
                member.setUserId(result.getInt("id"));
                member.setUsername(result.getString("username"));
                member.setFullname(result.getString("fullname"));
                member.setEmail(result.getString("email"));
                member.setMembershipCode(result.getString("membership_code"));
                member.setPhone(result.getString("phone"));
                member.setJoinedAt(result.getString("joined_at"));
                member.setPoints(result.getInt("points"));
                member.setTotalSpent(result.getDouble("total_spent"));
                members.add(member);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải danh sách thành viên.", exception);
        }
        return members;
    }

    public static void redeemPoints(Connection connection, int userId, int points) throws SQLException {
        if (points <= 0) return;
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE lp SET points = points - ?, updated_at = SYSDATETIME()
                FROM loyalty_points lp
                INNER JOIN users u ON u.id = lp.user_id
                WHERE lp.user_id = ? AND lp.points >= ? AND LOWER(u.role) = 'member'
                """)) {
            statement.setInt(1, points);
            statement.setInt(2, userId);
            statement.setInt(3, points);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Điểm thưởng không đủ hoặc tài khoản chưa là thành viên.");
            }
        }
    }

    public static void refundOrderPoints(Connection connection, int orderId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE lp SET points = points + o.loyalty_points_used,
                    updated_at = SYSDATETIME()
                FROM loyalty_points lp
                INNER JOIN orders o ON o.user_id = lp.user_id
                WHERE o.id = ? AND o.loyalty_points_used > 0
                  AND o.loyalty_points_refunded = 0;
                UPDATE orders SET loyalty_points_refunded = 1
                WHERE id = ? AND loyalty_points_used > 0 AND loyalty_points_refunded = 0;
                """)) {
            statement.setInt(1, orderId);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    private LoyaltyPoint mapPoints(ResultSet result) throws SQLException {
        LoyaltyPoint points = new LoyaltyPoint();
        points.setId(result.getInt("id"));
        points.setUserId(result.getInt("user_id"));
        points.setPoints(result.getInt("points"));
        points.setTotalSpent(result.getDouble("total_spent"));
        points.setUpdatedAt(result.getString("updated_at"));
        return points;
    }
}
