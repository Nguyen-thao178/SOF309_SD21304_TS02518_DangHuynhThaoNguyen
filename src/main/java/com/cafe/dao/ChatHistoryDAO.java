package com.cafe.dao;

import com.cafe.model.ChatHistoryEntry;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryDAO {

    public void save(int userId, String question, String answer, String provider)
            throws SQLException {
        String sql = """
                INSERT INTO chat_history (user_id, question, answer, provider)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, question);
            statement.setString(3, answer);
            statement.setString(4, provider);
            statement.executeUpdate();
        }
    }

    public List<ChatHistoryEntry> findByDate(LocalDate date) {
        List<ChatHistoryEntry> history = new ArrayList<>();
        String sql = """
                SELECT ch.id, ch.user_id, u.username, u.fullname,
                       ch.question, ch.answer, ch.provider, ch.created_at
                FROM chat_history ch
                LEFT JOIN users u ON u.id = ch.user_id
                WHERE ch.created_at >= ?
                  AND ch.created_at < DATEADD(DAY, 1, ?)
                ORDER BY ch.created_at DESC, ch.id DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Date sqlDate = Date.valueOf(date);
            statement.setDate(1, sqlDate);
            statement.setDate(2, sqlDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ChatHistoryEntry entry = new ChatHistoryEntry();
                    entry.setId(resultSet.getLong("id"));
                    int userId = resultSet.getInt("user_id");
                    entry.setUserId(resultSet.wasNull() ? null : userId);
                    entry.setUsername(resultSet.getString("username"));
                    entry.setFullname(resultSet.getString("fullname"));
                    entry.setQuestion(resultSet.getString("question"));
                    entry.setAnswer(resultSet.getString("answer"));
                    entry.setProvider(resultSet.getString("provider"));
                    entry.setCreatedAt(resultSet.getTimestamp("created_at"));
                    history.add(entry);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return history;
    }
}
