package DTO;

import java.util.Date;

public class WarrantyTempDTO {
    private String customerName,customerPhone;
    private Long orderID;
    private Date createdAt;
    private String display;
    public WarrantyTempDTO(String customerName, String customerPhone, Long orderID, Date createdAt, String display) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.orderID = orderID;
        this.createdAt = createdAt;
        this.display = display;
    }
    public WarrantyTempDTO() {
    }
    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public String getCustomerPhone() {
        return customerPhone;
    }
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    public Long getOrderID() {
        return orderID;
    }
    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }
}
