package com.example.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private KafkaProducerService producerService;
    
    // รับคำขอชำระเงิน
    @PostMapping
    public ResponseEntity<Map<String, Object>> initiatePayment(@RequestBody Map<String, Object> request) {
        // รับข้อมูลจากคำขอ
        String customerId = (String) request.get("customerId");
        double amount = Double.parseDouble(request.get("amount").toString());
        String method = (String) request.get("method");
        
        // สร้างรายการชำระเงินใหม่
        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, customerId, amount, method);
        
        // ส่งคำขอชำระเงินไปยัง Kafka
        producerService.sendPaymentRequest(payment);
        
        // ส่งข้อความตอบกลับ
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment request initiated");
        response.put("paymentId", paymentId);
        
        return ResponseEntity.ok(response);
    }
    
    // ดูสถานะการชำระเงิน (จำลอง)
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        // ในระบบจริงจะมีการเชื่อมต่อกับฐานข้อมูลเพื่อดูสถานะที่บันทึกไว้
        // แต่ที่นี่เราจำลองการตอบกลับ
        
        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", paymentId);
        response.put("status", "Processing"); // สถานะจำลอง
        response.put("message", "Payment is being processed");
        
        return ResponseEntity.ok(response);
    }
    
    // รับคำขอสร้างใบเสร็จโดยตรง (สำหรับทดสอบ)
    @PostMapping("/generate-invoice")
    public ResponseEntity<Map<String, Object>> generateInvoice(@RequestBody Map<String, Object> request) {
        String paymentId = (String) request.get("paymentId");
        String customerName = (String) request.get("customerName");
        double amount = Double.parseDouble(request.get("amount").toString());
        
        String invoiceId = "INV-" + UUID.randomUUID().toString().substring(0, 8);
        Invoice invoice = new Invoice(invoiceId, customerName, amount, paymentId);
        
        producerService.sendInvoice(invoice);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Invoice generation initiated");
        response.put("invoiceId", invoiceId);
        
        return ResponseEntity.ok(response);
    }
}