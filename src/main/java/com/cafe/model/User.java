package com.cafe.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullname;
    private String email;
    private String role;

    // constructors, getters, setters
    public User() {}
    public User(int id, String username, String passwordHash, String fullname, String email, String role) {
        this.id = id; this.username = username; this.passwordHash = passwordHash;
        this.fullname = fullname; this.email = email; this.role = role;
    }
    // Generate all getters/setters (IDEs tự tạo)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}