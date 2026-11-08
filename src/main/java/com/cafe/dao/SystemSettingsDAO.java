package com.cafe.dao;

import com.cafe.model.SystemSetting;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SystemSettingsDAO {

    public List<SystemSetting> getAll() {
        List<SystemSetting> list = new ArrayList<>();
        String sql = "SELECT * FROM system_settings ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SystemSetting s = new SystemSetting();
                s.setId(rs.getInt("id"));
                s.setKey(rs.getString("setting_key"));
                s.setValue(rs.getString("setting_value"));
                s.setDescription(rs.getString("description"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public SystemSetting getByKey(String key) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SystemSetting s = new SystemSetting();
                s.setId(rs.getInt("id"));
                s.setKey(rs.getString("setting_key"));
                s.setValue(rs.getString("setting_value"));
                s.setDescription(rs.getString("description"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateSetting(String key, String value, int updatedByUserId) {
        String sql = """
                UPDATE system_settings
                SET setting_value = ?, updated_by_user_id = ?
                WHERE setting_key = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setInt(2, updatedByUserId);
            ps.setString(3, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateSettings(Map<String, String> settings, int updatedByUserId) {
        String sql = """
                MERGE dbo.system_settings WITH (HOLDLOCK) AS target
                USING (SELECT ? AS setting_key, ? AS setting_value) AS source
                   ON target.setting_key = source.setting_key
                WHEN MATCHED THEN
                    UPDATE SET setting_value = source.setting_value,
                               updated_by_user_id = ?
                WHEN NOT MATCHED THEN
                    INSERT (setting_key, setting_value, description, updated_by_user_id)
                    VALUES (source.setting_key, source.setting_value, N'Cấu hình hệ thống', ?);
                """;
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Map.Entry<String, String> entry : settings.entrySet()) {
                    statement.setString(1, entry.getKey());
                    statement.setString(2, entry.getValue());
                    statement.setInt(3, updatedByUserId);
                    statement.setInt(4, updatedByUserId);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            connection.commit();
            return true;
        } catch (SQLException exception) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) { }
            }
            exception.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) { }
            }
        }
    }
}
