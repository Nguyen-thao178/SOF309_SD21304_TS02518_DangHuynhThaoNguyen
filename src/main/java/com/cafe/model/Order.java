// Order.java
package com.cafe.model;
import java.util.Date;
public class Order {
    private int id;
    private int userId;
    private Date orderDate;
    private double totalAmount;
    private String status;
    private String orderType;
    private String paymentMethod;
    private double depositAmount;
    private Date pickupDate;
    private String pickupStatus;
    private String customerName;
    private String customerUsername;
    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double depositAmount) { this.depositAmount = depositAmount; }
    public Date getPickupDate() { return pickupDate; }
    public void setPickupDate(Date pickupDate) { this.pickupDate = pickupDate; }
    public String getPickupStatus() { return pickupStatus; }
    public void setPickupStatus(String pickupStatus) { this.pickupStatus = pickupStatus; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }

    public String getTransactionTag() {
        if ("direct".equals(orderType)) {
            return "Thanh toán trực tiếp";
        }
        if ("no_show".equals(status) || "no_show".equals(pickupStatus)) {
            return "Đã cọc nhưng không nhận hàng";
        }
        if ("picked_up".equals(status) || "picked_up".equals(pickupStatus)) {
            return "Đã cọc và đã nhận hàng";
        }
        return "Đã cọc - Chờ nhận hàng";
    }
}
