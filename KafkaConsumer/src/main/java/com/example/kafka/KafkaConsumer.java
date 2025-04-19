package com.example.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    
    // ข้อมูลสำหรับการตรวจสอบ (ในระบบจริงควรอยู่ในฐานข้อมูล)
    private static final Map<String, Double> METHOD_LIMITS = new HashMap<>();
    private static final Map<String, String> CUSTOMER_STATUS = new HashMap<>();
    
    static {
        // กำหนดวงเงินสูงสุดสำหรับแต่ละวิธีการชำระเงิน
        METHOD_LIMITS.put("CREDIT_CARD", 100000.0);
        METHOD_LIMITS.put("DEBIT_CARD", 50000.0);
        METHOD_LIMITS.put("BANK_TRANSFER", 1000000.0);
        METHOD_LIMITS.put("E_WALLET", 20000.0);
        
        // กำหนดสถานะของลูกค้าบางคน
        CUSTOMER_STATUS.put("BLOCK001", "BLACKLISTED");
        CUSTOMER_STATUS.put("BLOCK002", "BLACKLISTED");
        CUSTOMER_STATUS.put("CUST111", "REQUIRES_VERIFICATION");
        CUSTOMER_STATUS.put("CUST222", "REQUIRES_VERIFICATION");
    }
    
    @Autowired
    private KafkaTemplate<String, Payment> paymentKafkaTemplate;
    
    @Autowired
    private KafkaTemplate<String, Invoice> invoiceKafkaTemplate;
    
    // รับข้อความทั่วไป
    @KafkaListener(topics = "test-topic", groupId = "my-group")
    public void consume(String message) {
        logger.info("Received message: {}", message);
        System.out.println("message = " + message);
    }
    
    // 1. รับคำขอชำระเงิน
    @KafkaListener(topics = "payment-requests", groupId = "payment-group", containerFactory = "paymentKafkaListenerContainerFactory")
    public void processPaymentRequest(Payment payment) {
        logger.info("Processing payment request: {}", payment.getPaymentId());
        System.out.println("Processing payment request: " + payment);
        
        try {
            // ดำเนินการชำระเงิน (จำลอง)
            Thread.sleep(1000); // จำลองการประมวลผล
            
            // อัปเดตสถานะ
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            
            // ส่งไปตรวจสอบการชำระเงิน
            paymentKafkaTemplate.send("payment-verification", payment.getPaymentId(), payment);
            logger.info("Payment sent for verification: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Error processing payment request: {}", payment.getPaymentId(), e);
        }
    }
    
    // 2. ตรวจสอบการชำระเงิน
    @KafkaListener(topics = "payment-verification", groupId = "payment-group", containerFactory = "paymentKafkaListenerContainerFactory")
    public void verifyPayment(Payment payment) {
        logger.info("Verifying payment: {}", payment.getPaymentId());
        System.out.println("Verifying payment: " + payment);
        
        try {
            // จำลองการตรวจสอบ
            Thread.sleep(1000);
            
            // ตรวจสอบโดยใช้ logic ที่มีความหมายมากขึ้น
            PaymentVerificationResult result = validatePayment(payment);
            
            if (result.isSuccess()) {
                // การชำระเงินสำเร็จ
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                logger.info("Payment verification successful: {}", payment.getPaymentId());
                
                // สร้างใบเสร็จ
                String invoiceId = "INV-" + UUID.randomUUID().toString().substring(0, 8);
                Invoice invoice = new Invoice(
                    invoiceId,
                    "Customer for " + payment.getCustomerId(),
                    payment.getAmount(),
                    payment.getPaymentId()
                );
                
                // ส่งไปออกใบเสร็จ
                invoiceKafkaTemplate.send("invoice-topic", invoice.getInvoiceId(), invoice);
                logger.info("Invoice generated and sent: {}", invoice.getInvoiceId());
            } else {
                // การชำระเงินล้มเหลว
                payment.setStatus(Payment.PaymentStatus.FAILED);
                logger.info("Payment verification failed: {}, reason: {}", 
                    payment.getPaymentId(), result.getFailureReason());
                
                // แจ้งกลับไปยังระบบ (หรือผู้ใช้)
                paymentKafkaTemplate.send("payment-failed", payment.getPaymentId(), payment);
            }
        } catch (Exception e) {
            logger.error("Error verifying payment: {}", payment.getPaymentId(), e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentKafkaTemplate.send("payment-failed", payment.getPaymentId(), payment);
        }
    }
    
    // 3. รับใบเสร็จและดำเนินการต่อ
    @KafkaListener(topics = "invoice-topic", groupId = "invoice-group", containerFactory = "invoiceKafkaListenerContainerFactory")
    public void processInvoice(Invoice invoice) {
        logger.info("Processing invoice: {}", invoice.getInvoiceId());
        System.out.println("Received Invoice: " + invoice);
        
        try {
            // จำลองการจัดส่งใบเสร็จให้ลูกค้า
            Thread.sleep(500);
            
            // ในระบบจริงอาจมีการส่งอีเมล หรือ SMS หรือ บันทึกลงฐานข้อมูล
            logger.info("Invoice sent to customer: {}", invoice.getCustomerName());
            System.out.println("Invoice " + invoice.getInvoiceId() + " sent to " + invoice.getCustomerName());
        } catch (Exception e) {
            logger.error("Error processing invoice: {}", invoice.getInvoiceId(), e);
        }
    }
    
    // 4. รับการแจ้งเตือนการชำระเงินล้มเหลว
    @KafkaListener(topics = "payment-failed", groupId = "payment-group", containerFactory = "paymentKafkaListenerContainerFactory")
    public void handlePaymentFailure(Payment payment) {
        logger.info("Handling payment failure: {}", payment.getPaymentId());
        System.out.println("Payment Failed: " + payment);
        
        try {
            // จำลองการแจ้งเตือนไปยังลูกค้า
            Thread.sleep(500);
            
            // ในระบบจริงอาจมีการส่งอีเมล หรือ SMS แจ้งเตือน
            logger.info("Payment failure notification sent for payment: {}", payment.getPaymentId());
            System.out.println("Payment failure notification sent for: " + payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Error handling payment failure: {}", payment.getPaymentId(), e);
        }
    }
    
    /**
     * ตรวจสอบความถูกต้องของการชำระเงินด้วยเงื่อนไขที่มีความหมาย
     */
    private PaymentVerificationResult validatePayment(Payment payment) {
        // 1. ตรวจสอบจำนวนเงิน
        if (payment.getAmount() <= 0) {
            return new PaymentVerificationResult(false, "Payment amount must be greater than zero");
        }
        
        // 2. ตรวจสอบความถูกต้องของวิธีการชำระเงิน
        if (payment.getMethod() == null || payment.getMethod().trim().isEmpty()) {
            return new PaymentVerificationResult(false, "Payment method is required");
        }
        
        // 3. ตรวจสอบว่าวิธีการชำระเงินได้รับการสนับสนุนหรือไม่
        if (!METHOD_LIMITS.containsKey(payment.getMethod())) {
            return new PaymentVerificationResult(false, "Payment method not supported: " + payment.getMethod());
        }
        
        // 4. ตรวจสอบวงเงินตามวิธีการชำระเงิน
        double limit = METHOD_LIMITS.get(payment.getMethod());
        if (payment.getAmount() > limit) {
            return new PaymentVerificationResult(false, 
                String.format("Payment amount %s exceeds limit %s for method %s", 
                    payment.getAmount(), limit, payment.getMethod()));
        }
        
        // 5. ตรวจสอบลูกค้าที่มีประวัติไม่ดี (blacklist)
        String customerStatus = CUSTOMER_STATUS.get(payment.getCustomerId());
        if ("BLACKLISTED".equals(customerStatus)) {
            return new PaymentVerificationResult(false, "Customer is blacklisted: " + payment.getCustomerId());
        }
        
        // 6. ตรวจสอบลูกค้าที่ต้องการการยืนยันเพิ่มเติม
        if ("REQUIRES_VERIFICATION".equals(customerStatus)) {
            // ในระบบจริงอาจมีขั้นตอนยืนยันเพิ่มเติม เช่น OTP
            logger.info("Customer requires additional verification: {}", payment.getCustomerId());
            // สมมติว่าได้รับการยืนยันเรียบร้อยแล้ว
        }
        
        // 7. ตรวจสอบธุรกรรมที่น่าสงสัย
        if (isSuspiciousTransaction(payment)) {
            return new PaymentVerificationResult(false, "Suspicious transaction detected");
        }
        
        // 8. ตรวจสอบความสอดคล้องของข้อมูล
        // เช่น จำนวนเงินที่ต้องอยู่ในช่วงที่เหมาะสมสำหรับแต่ละวิธีการชำระเงิน
        if ("CREDIT_CARD".equals(payment.getMethod()) && payment.getAmount() < 10) {
            return new PaymentVerificationResult(false, "Credit card minimum amount is 10");
        }
        
        // ผ่านการตรวจสอบทั้งหมด
        logger.info("Payment passed all verification checks: {}", payment.getPaymentId());
        return new PaymentVerificationResult(true, null);
    }
    
    /**
     * ตรวจสอบธุรกรรมที่น่าสงสัย
     */
    private boolean isSuspiciousTransaction(Payment payment) {
        // กรณีที่ 1: ชำระเงินจำนวนมากในช่วงเวลากลางคืน
        LocalDateTime now = LocalDateTime.now();
        boolean isNightTime = now.getHour() >= 23 || now.getHour() <= 4;
        
        if (isNightTime && payment.getAmount() > 50000) {
            logger.warn("Suspicious: Large payment during night hours: {}", payment.getPaymentId());
            return true;
        }
        
        // กรณีที่ 2: วิธีการชำระเงินและจำนวนเงินไม่สอดคล้องกัน
        if ("E_WALLET".equals(payment.getMethod()) && payment.getAmount() > 10000) {
            logger.warn("Suspicious: Unusually large e-wallet payment: {}", payment.getPaymentId());
            return true;
        }
        
        // กรณีที่ 3: รหัสลูกค้าสงสัย (ในระบบจริงอาจมีการตรวจสอบรูปแบบที่ซับซ้อนกว่านี้)
        if (payment.getCustomerId() != null && 
            (payment.getCustomerId().contains("SUSP") || payment.getCustomerId().contains("FRAUD"))) {
            logger.warn("Suspicious: Customer ID has suspicious pattern: {}", payment.getCustomerId());
            return true;
        }
        
        return false;
    }
    
    /**
     * คลาสภายในสำหรับเก็บผลลัพธ์การตรวจสอบการชำระเงิน
     */
    private static class PaymentVerificationResult {
        private final boolean success;
        private final String failureReason;
        
        public PaymentVerificationResult(boolean success, String failureReason) {
            this.success = success;
            this.failureReason = failureReason;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getFailureReason() {
            return failureReason;
        }
    }
}