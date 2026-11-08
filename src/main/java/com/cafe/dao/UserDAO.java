package com.cafe.dao;

import com.cafe.model.User;
import com.cafe.utils.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // === CREATE ===
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, fullname, email, role) VALUES (?, ?, ?, ?, ?)";
        return JdbcUtil.executeInsertReturnId(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.getFullname(),
                user.getEmail(),
                user.getRole()
        );
    }

    // === READ ===
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, id);
            return rs.next() ? mapResultSetToUser(rs) : null;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, username);
            return rs.next() ? mapResultSetToUser(rs) : null;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, email);
            return rs.next() ? mapResultSetToUser(rs) : null;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql);
            while (rs.next()) list.add(mapResultSetToUser(rs));
            return list;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    // === UPDATE ===
    public int update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, fullname = ?, email = ?, role = ? WHERE id = ?";
        return JdbcUtil.executeUpdate(sql,
                user.getUsername(),
                user.getFullname(),
                user.getEmail(),
                user.getRole(),
                user.getId()
        );
    }

    public boolean updatePassword(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        return JdbcUtil.executeUpdate(sql, newPasswordHash, userId) > 0;
    }

    public String getPasswordHash(int userId) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE id = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, userId);
            return rs.next() ? rs.getString("password_hash") : null;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    public boolean changePassword(int userId, String oldPasswordHash, String newPasswordHash) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = JdbcUtil.getConnection();
            String checkSql = "SELECT id FROM users WHERE id = ? AND password_hash = ?";
            stmt = conn.prepareStatement(checkSql);
            stmt.setInt(1, userId);
            stmt.setString(2, oldPasswordHash);
            rs = stmt.executeQuery();
            if (!rs.next()) return false;

            String updateSql = "UPDATE users SET password_hash = ? WHERE id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } finally {
            JdbcUtil.closeAll(conn, stmt, rs);
        }
    }

    // === DELETE ===
    public int deleteById(int id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(checkSql, id);
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Không thể xóa người dùng đã có đơn hàng!");
            }
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
        String sql = "DELETE FROM users WHERE id = ?";
        return JdbcUtil.executeUpdate(sql, id);
    }

    // === UTILITY ===
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, username);
            return rs.next() && rs.getInt(1) > 0;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql, email);
            return rs.next() && rs.getInt(1) > 0;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        ResultSet rs = null;
        try {
            rs = JdbcUtil.executeQuery(sql);
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            JdbcUtil.closeResultSet(rs);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullname(rs.getString("fullname"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        return user;
    }
}