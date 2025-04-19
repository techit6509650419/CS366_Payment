package com.example.kafka;

import java.time.LocalDateTime;

public class Payment {
    private String paymentId;
    private String customerId;
    private double amount;
    private String method;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
    
    public Payment() {}

    public Payment(String paymentId, String customerId, double amount, String method) {
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", method='" + method + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}