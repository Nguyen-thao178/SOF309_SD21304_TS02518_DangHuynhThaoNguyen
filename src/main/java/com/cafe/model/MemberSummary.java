package com.cafe.model;

public class MemberSummary {
    private int userId;
    private String membershipCode;
    private String fullname;
    private String username;
    private String email;
    private String phone;
    private String joinedAt;
    private int points;
    private double totalSpent;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMembershipCode() { return membershipCode; }
    public void setMembershipCode(String membershipCode) { this.membershipCode = membershipCode; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
}
