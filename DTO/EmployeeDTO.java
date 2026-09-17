package DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EmployeeDTO {

    private Long employeeId;
    private String employeeName;
    private String phoneNumber;
    private BigDecimal salary;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor rỗng
    public EmployeeDTO() {
    }

    // Constructor đầy đủ
    public EmployeeDTO(Long employeeId, String employeeName, String phoneNumber,
                       BigDecimal salary, String status,
                       Timestamp createdAt, Timestamp updatedAt) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.phoneNumber = phoneNumber;
        this.salary = salary;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter & Setter
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return employeeName + " -ID: "+ employeeId;
    }
}

