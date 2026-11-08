package com.cafe.model;

import java.sql.Date;

public class PromotionItem {
    private int id;
    private int productId;
    private int discountPercent;
    private Date startDate;
    private Date endDate;
    private String status;

    // Constructors
    public PromotionItem() {}
    public PromotionItem(int id, int productId, int discountPercent, Date startDate, Date endDate, String status) {
        this.id = id;
        this.productId = productId;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}