package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashLedgerDTO {

    private Long id;
    private String type;
    private Long referenceId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public CashLedgerDTO() {}

    public CashLedgerDTO(Long id, String type, Long referenceId,
                         BigDecimal amount, String description,
                         LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.referenceId = referenceId;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}