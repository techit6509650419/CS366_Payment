package com.example.kafka;

import java.time.LocalDateTime;

public class Invoice {
    private String invoiceId;
    private String customerName;
    private double totalAmount;
    private String paymentId;
    private LocalDateTime issuedAt;
    
    // Constructors
    public Invoice() {}
    
    public Invoice(String invoiceId, String customerName, double totalAmount, String paymentId) {
        this.invoiceId = invoiceId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
        this.issuedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId='" + invoiceId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentId='" + paymentId + '\'' +
                ", issuedAt=" + issuedAt +
                '}';
    }
}