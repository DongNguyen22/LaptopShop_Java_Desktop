package DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class warrantyCardDTO {
    private long warrantyId;
    private long orderId;
    private long productId;
    private String productName;
    private String productSerial;
    private int warrantyMonths; // Hạn bảo hành của sản phẩm (tháng)
    private LocalDateTime receiveTime;
    private LocalDate returnDate;
    private String details;
    private int statusCode; // 1: Đã tiếp nhận, 2: Từ chối BH, 3: Đang bảo hành, 4: Hoàn thành
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long createdUser;
    private String createdUserName;
    private long customerId;
    private String customerName;
    private String customerPhone;

    // Constructor không tham số
    public warrantyCardDTO() {
    }

    // Constructor đầy đủ
    public warrantyCardDTO(long warrantyId, long orderId, long productId, String productName, 
                          String productSerial, LocalDateTime receiveTime, LocalDate returnDate, 
                          String details, int statusCode, LocalDateTime createdAt, LocalDateTime updatedAt, 
                          long createdUser, String createdUserName) {
        this.warrantyId = warrantyId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.productSerial = productSerial;
        this.receiveTime = receiveTime;
        this.returnDate = returnDate;
        this.details = details;
        this.statusCode = statusCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdUser = createdUser;
        this.createdUserName = createdUserName;
    }

    // Getters and Setters
    public long getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(long warrantyId) {
        this.warrantyId = warrantyId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSerial() {
        return productSerial;
    }

    public void setProductSerial(String productSerial) {
        this.productSerial = productSerial;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusName() {
        switch(statusCode) {
            case 1: return "Đã tiếp nhận";
            case 2: return "Từ chối BH";
            case 3: return "Đang bảo hành";
            case 4: return "Hoàn thành";
            default: return "Không xác định";
        }
    }

    public static int getStatusCodeByName(String statusName) {
        switch(statusName) {
            case "Đã tiếp nhận": return 1;
            case "Từ chối BH": return 2;
            case "Đang bảo hành": return 3;
            case "Hoàn thành": return 4;
            default: return 1;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(long createdUser) {
        this.createdUser = createdUser;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
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

    @Override
    public String toString() {
        return "warrantyCardDTO{" +
                "warrantyId=" + warrantyId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", statusCode=" + statusCode +
                ", status='" + getStatusName() + '\'' +
                '}';
    }
}
