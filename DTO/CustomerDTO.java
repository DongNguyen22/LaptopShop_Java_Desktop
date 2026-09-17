package DTO;

import java.time.LocalDateTime;

public class CustomerDTO {

    private Long customerId;        // bigint
    private String customerName;    // varchar(45)
    private String phoneNumber;     // varchar(45)
    private LocalDateTime createdAt; // timestamp
    private LocalDateTime updatedAt; // timestamp

    // Constructor rỗng
    public CustomerDTO() {
    }

    // Constructor đầy đủ
    public CustomerDTO(Long customerId, String customerName, String phoneNumber,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter & Setter
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
}
