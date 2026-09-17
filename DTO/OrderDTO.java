package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {

    private long order_id;
    private long customer_id;
    private Integer promotion_id; // có thể null
    private BigDecimal total;
    private String payment_method;
    private long created_user;
    private LocalDateTime created_at;
    public OrderDTO(long order_id, long customer_id, Integer promotion_id, BigDecimal total, String payment_method,
            long created_user, LocalDateTime created_at) {
        this.order_id = order_id;
        this.customer_id = customer_id;
        this.promotion_id = promotion_id;
        this.total = total;
        this.payment_method = payment_method;
        this.created_user = created_user;
        this.created_at = created_at;
    }
    public OrderDTO() {
    }
    public long getOrder_id() {
        return order_id;
    }
    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }
    public long getCustomer_id() {
        return customer_id;
    }
    public void setCustomer_id(long customer_id) {
        this.customer_id = customer_id;
    }
    public Integer getPromotion_id() {
        return promotion_id;
    }
    public void setPromotion_id(Integer promotion_id) {
        this.promotion_id = promotion_id;
    }
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    public String getPayment_method() {
        return payment_method;
    }
    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }
    public long getCreated_user() {
        return created_user;
    }
    public void setCreated_user(long created_user) {
        this.created_user = created_user;
    }
    public LocalDateTime getCreated_at() {
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
