package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderItemDTO {

    private Long orderId; // order_id BIGINT
    private Long productId; // product_id BIGINT
    private BigDecimal original_price; // unit_price DECIMAL(15,2)
    private BigDecimal unitPrice; // unit_price DECIMAL(15,2)
    private Long receiptId; // receipt_id BIGINT
    private String productSerial; // product_serial VARCHAR(45)
    private String paymentMethod;
    private LocalDateTime createdAt;
    public OrderItemDTO(Long orderId, Long productId, BigDecimal original_price, BigDecimal unitPrice, Long receiptId,
            String productSerial) {
        this.orderId = orderId;
        this.productId = productId;
        this.original_price = original_price;
        this.unitPrice = unitPrice;
        this.receiptId = receiptId;
        this.productSerial = productSerial;
    }
    public OrderItemDTO() {
    }
    public Long getOrderId() {
        return orderId;
    }
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public BigDecimal getOriginal_price() {
        return original_price;
    }
    public void setOriginal_price(BigDecimal original_price) {
        this.original_price = original_price;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    public Long getReceiptId() {
        return receiptId;
    }
    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }
    public String getProductSerial() {
        return productSerial;
    }
    public void setProductSerial(String productSerial) {
        this.productSerial = productSerial;
    }
    public String getPaymentMethod() { return paymentMethod; }
public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}