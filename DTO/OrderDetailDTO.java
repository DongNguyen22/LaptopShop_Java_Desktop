package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDetailDTO {
    private long orderId;
    private long productCode;
    private long customerCode;
    private String productName;
    private BigDecimal total;
    private BigDecimal originalPrice;
    private BigDecimal unitPrice;
    private java.time.LocalDateTime createdAt;
    private long employeeId;
    private String employeeName;
    private String customerName;
    private Integer promotionId;
    private String promotionName;
    private Integer warrantyTime;
    private String serial;
    private String paymentMethod;
    public OrderDetailDTO(long orderId, long productCode, long customerCode, String productName, BigDecimal total,
            BigDecimal originalPrice, BigDecimal unitPrice, LocalDateTime createdAt, long employeeId,
            String employeeName, String customerName, Integer promotionId, String promotionName, Integer warrantyTime,
            String serial, String paymentMethod) {
        this.orderId = orderId;
        this.productCode = productCode;
        this.customerCode = customerCode;
        this.productName = productName;
        this.total = total;
        this.originalPrice = originalPrice;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.customerName = customerName;
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.warrantyTime = warrantyTime;
        this.serial = serial;
        this.paymentMethod = paymentMethod;
    }
    public OrderDetailDTO() {
    }
    public long getOrderId() {
        return orderId;
    }
    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
    public long getProductCode() {
        return productCode;
    }
    public void setProductCode(long productCode) {
        this.productCode = productCode;
    }
    public long getCustomerCode() {
        return customerCode;
    }
    public void setCustomerCode(long customerCode) {
        this.customerCode = customerCode;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }
    public String getEmployeeName() {
        return employeeName;
    }
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Integer getPromotionId() {
        return promotionId;
    }
    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }
    public String getPromotionName() {
        return promotionName;
    }
    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }
    public Integer getWarrantyTime() {
        return warrantyTime;
    }
    public void setWarrantyTime(Integer warrantyTime) {
        this.warrantyTime = warrantyTime;
    }
    public String getSerial() {
        return serial;
    }
    public void setSerial(String serial) {
        this.serial = serial;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    

}