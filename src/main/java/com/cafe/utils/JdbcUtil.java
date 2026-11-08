package com.cafe.utils;

import com.cafe.config.DatabaseConfig;
import java.sql.*;

/**
 * Lớp tiện ích kết nối và thao tác với CSDL SQL Server
 * Sử dụng JDBC Driver cho Microsoft SQL Server
 */
public class JdbcUtil {

    // Thông tin kết nối CSDL
	private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    // Đối tượng kết nối (Singleton pattern)
    private static Connection connection = null;

    /**
     * Nạp driver JDBC khi lớp được tải
     */
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi nạp driver JDBC: " + e.getMessage());
        }
    }

    /**
     * Lấy kết nối đến CSDL
     * @return Connection object
     * @throws SQLException nếu kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (DatabaseConfig.PASSWORD.isBlank()) {
                throw new SQLException("CAFE_DB_PASSWORD chưa được cấu hình.");
            }
            connection = DriverManager.getConnection(
                    DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
        }
        return connection;
    }

    /**
     * Đóng kết nối
     * @param conn Connection cần đóng
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
    }

    /**
     * Đóng PreparedStatement
     * @param stmt PreparedStatement cần đóng
     */
    public static void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng statement: " + e.getMessage());
            }
        }
    }

    /**
     * Đóng ResultSet
     * @param rs ResultSet cần đóng
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng result set: " + e.getMessage());
            }
        }
    }

    /**
     * Đóng tất cả resource
     * @param conn Connection
     * @param stmt PreparedStatement
     * @param rs ResultSet
     */
    public static void closeAll(Connection conn, PreparedStatement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

    /**
     * Thực thi câu lệnh SELECT, trả về ResultSet
     * @param sql Câu lệnh SQL
     * @param params Tham số cho PreparedStatement
     * @return ResultSet (cần đóng sau khi sử dụng)
     * @throws SQLException nếu truy vấn thất bại
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt.executeQuery();
    }

    /**
     * Thực thi câu lệnh INSERT, UPDATE, DELETE
     * @param sql Câu lệnh SQL
     * @param params Tham số cho PreparedStatement
     * @return Số dòng bị ảnh hưởng
     * @throws SQLException nếu thực thi thất bại
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } finally {
            closeStatement(stmt);
            // Không đóng connection ở đây để tái sử dụng
        }
    }

    /**
     * Thực thi câu lệnh INSERT và trả về ID tự sinh
     * @param sql Câu lệnh SQL
     * @param params Tham số cho PreparedStatement
     * @return ID tự sinh của bản ghi mới, -1 nếu thất bại
     * @throws SQLException nếu thực thi thất bại
     */
    public static int executeInsertReturnId(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setParameters(stmt, params);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeAll(null, stmt, rs);
        }
    }

    /**
     * Gán tham số cho PreparedStatement
     * @param stmt PreparedStatement
     * @param params Danh sách tham số
     * @throws SQLException nếu gán tham số thất bại
     */
    private static void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * Kiểm tra kết nối có thành công không
     * @return true nếu kết nối thành công, false nếu thất bại
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("Kết nối CSDL thành công!");
            closeConnection(conn);
            return true;
        } catch (SQLException e) {
            System.err.println("Kết nối CSDL thất bại: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bắt đầu transaction
     * @throws SQLException nếu lỗi
     */
    public static void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }

    /**
     * Commit transaction
     * @throws SQLException nếu lỗi
     */
    public static void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.commit();
        conn.setAutoCommit(true);
    }

    /**
     * Rollback transaction
     * @throws SQLException nếu lỗi
     */
    public static void rollbackTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.rollback();
        conn.setAutoCommit(true);
    }
}
