package DTO;

import java.time.LocalDateTime;

public class CategoryDTO {
    private int category_id;
    private String name;
    private String code;
    private LocalDateTime created_at, updated_at;

    public CategoryDTO() {

    }

    public CategoryDTO(int category_id, String name) {
        this.category_id = category_id;
        this.name = name;
    }

    public CategoryDTO(int category_id, String name, String code) {
        this.category_id = category_id;
        this.name = name;
        this.code = code;
    }

    public CategoryDTO(int category_id, String name, String code, LocalDateTime created_at, LocalDateTime updated_at) {
        this.category_id = category_id;
        this.name = name;
        this.code = code;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
