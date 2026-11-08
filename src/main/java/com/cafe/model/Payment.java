package com.cafe.model;

public class Payment {
    private long id;
    private int orderId;
    private int userId;
    private String orderType;
    private double orderTotal;
    private String paymentStage;
    private String paymentMethod;
    private double amount;
    private String status;
    private String transactionReference;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public double getOrderTotal() { return orderTotal; }
    public void setOrderTotal(double orderTotal) { this.orderTotal = orderTotal; }
    public String getPaymentStage() { return paymentStage; }
    public void setPaymentStage(String paymentStage) { this.paymentStage = paymentStage; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
