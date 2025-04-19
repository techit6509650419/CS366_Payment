package com.example.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // ส่งคำขอชำระเงิน
    public void sendPaymentRequest(Payment payment) {
        logger.info("Sending payment request to kafka: {}", payment.getPaymentId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send("payment-requests", payment.getPaymentId(), payment);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Payment request sent successfully: {}", payment.getPaymentId());
            } else {
                logger.error("Failed to send payment request: {}", payment.getPaymentId(), ex);
            }
        });
    }
    
    // แจ้งผลการชำระเงินเมื่อล้มเหลว
    public void notifyPaymentFailed(Payment payment) {
        logger.info("Sending payment failed notification: {}", payment.getPaymentId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send("payment-failed-notifications", payment.getPaymentId(), payment);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Payment failed notification sent successfully: {}", payment.getPaymentId());
            } else {
                logger.error("Failed to send payment failed notification: {}", payment.getPaymentId(), ex);
            }
        });
    }
    
    // ส่งข้อมูลใบเสร็จ
    public void sendInvoice(Invoice invoice) {
        logger.info("Sending invoice to kafka: {}", invoice.getInvoiceId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send("invoice-topic", invoice.getInvoiceId(), invoice);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Invoice sent successfully: {}", invoice.getInvoiceId());
            } else {
                logger.error("Failed to send invoice: {}", invoice.getInvoiceId(), ex);
            }
        });
    }
}