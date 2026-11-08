package com.cafe.dao;

import com.cafe.model.Contact;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {
    public List<Contact> getAllContacts() throws SQLException {
        List<Contact> list = new ArrayList<>();
        String sql = "SELECT * FROM contacts ORDER BY " +
                     "CASE position " +
                     "WHEN 'owner' THEN 1 " +
                     "WHEN 'manager' THEN 2 " +
                     "WHEN 'employee' THEN 3 " +
                     "ELSE 4 END, name";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) list.add(mapContact(rs));
        }
        return list;
    }

    public Contact findById(int id) throws SQLException {
        String sql = "SELECT id, name, position, phone, email, address, notes " +
                "FROM contacts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapContact(rs) : null;
            }
        }
    }

    public boolean insert(Contact contact) throws SQLException {
        String sql = "INSERT INTO contacts (name, position, phone, email, address, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            bind(statement, contact);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(Contact contact) throws SQLException {
        String sql = "UPDATE contacts SET name = ?, position = ?, phone = ?, " +
                "email = ?, address = ?, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            bind(statement, contact);
            statement.setInt(7, contact.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "DELETE FROM contacts WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, Contact contact) throws SQLException {
        statement.setString(1, contact.getName());
        statement.setString(2, contact.getPosition());
        statement.setString(3, contact.getPhone());
        statement.setString(4, contact.getEmail());
        statement.setString(5, contact.getAddress());
        statement.setString(6, contact.getNotes());
    }

    private Contact mapContact(ResultSet rs) throws SQLException {
        return new Contact(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("position"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("notes")
        );
    }
}
