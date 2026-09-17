package DTO;

import java.time.LocalDateTime;

public class AccountDTO {
    private long id;
    private long employeeId;
    private String username;
    private String password;
    private int role_id;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private String status;
    public AccountDTO(long id, long employeeId, String username, String password, int role_id, LocalDateTime createdAt,
            LocalDateTime updateAt, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.role_id = role_id;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.status = status;
    }
    public AccountDTO() {
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getRole_id() {
        return role_id;
    }
    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdateAt() {
        return updateAt;
    }
    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
}
