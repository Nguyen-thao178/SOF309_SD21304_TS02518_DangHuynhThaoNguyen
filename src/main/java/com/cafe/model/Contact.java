package com.cafe.model;

public class Contact {
    private int id;
    private String name;
    private String position;
    private String phone;
    private String email;
    private String address;
    private String notes;

    // Constructors
    public Contact() {}
    public Contact(int id, String name, String position, String phone, String email, String address, String notes) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.notes = notes;
    }

    // Getters & Setters (bắt buộc phải có)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}