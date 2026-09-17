package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NewOrderDTO {
    private Long orderId;
    private BigDecimal total;
    private String customerName;
    private String employeeName;
    private String promotionName;
    private LocalDateTime createdAt;
    private String paymentMethod;
    private Integer promotionID;
    private Long customer_id,employee_id;

    public NewOrderDTO(Long orderId, BigDecimal total, String customerName, String employeeName, String promotionName,
            LocalDateTime createdAt, String paymentMethod, Integer promotionID, Long customer_id, Long employee_id) {
        this.orderId = orderId;
        this.total = total;
        this.customerName = customerName;
        this.employeeName = employeeName;
        this.promotionName = promotionName;
        this.createdAt = createdAt;
        this.paymentMethod = paymentMethod;
        this.promotionID = promotionID;
        this.customer_id = customer_id;
        this.employee_id = employee_id;
    }

    // ===== Constructor rỗng =====
    public NewOrderDTO() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getPromotionID() {
        return promotionID;
    }

    public void setPromotionID(Integer promotionID) {
        this.promotionID = promotionID;
    }

    public Long getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Long customer_id) {
        this.customer_id = customer_id;
    }

    public Long getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(Long employee_id) {
        this.employee_id = employee_id;
    }
}
