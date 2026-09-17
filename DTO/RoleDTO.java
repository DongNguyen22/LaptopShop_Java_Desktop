package DTO;

import java.sql.Timestamp;

public class RoleDTO {

    private int roleId;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor rỗng
    public RoleDTO() {
    }

    // Constructor đầy đủ
    public RoleDTO(int roleId, String name, Timestamp createdAt, Timestamp updatedAt) {
        this.roleId = roleId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter & Setter
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return name + " -ID: "+roleId;
    }
}
