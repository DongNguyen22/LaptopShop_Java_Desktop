package DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class promotionsDTO {
    private int promotion_id;
    private String name, promotion_type;
    private double min_paid;
    private int discount_on_bill;
    private Integer available_time;
    private boolean is_auto, status;
    private LocalDateTime start_at, end_at, created_at, updated_at;
    private ArrayList<promotionItemsDTO> list;

    public promotionsDTO() {

    }

    public promotionsDTO(int promotion_id, String name, String promotion_type, double min_paid, int discount_on_bill,
            Integer available_time, boolean is_auto, boolean status, LocalDateTime start_at, LocalDateTime end_at,
            LocalDateTime created_at, LocalDateTime updated_at) {
        this.promotion_id = promotion_id;
        this.name = name;
        this.promotion_type = promotion_type;
        this.min_paid = min_paid;
        this.discount_on_bill = discount_on_bill;
        this.available_time = available_time;
        this.is_auto = is_auto;
        this.status = status;
        this.start_at = start_at;
        this.end_at = end_at;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(int promotion_id) {
        this.promotion_id = promotion_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPromotion_type() {
        return promotion_type;
    }

    public void setPromotion_type(String promotion_type) {
        this.promotion_type = promotion_type;
    }

    public double getMin_paid() {
        return min_paid;
    }

    public void setMin_paid(double min_paid) {
        this.min_paid = min_paid;
    }

    public int getDiscount_on_bill() {
        return discount_on_bill;
    }

    public void setDiscount_on_bill(int discount_on_bill) {
        this.discount_on_bill = discount_on_bill;
    }

    public Integer getAvailable_time() {
        return available_time;
    }

    public void setAvailable_time(Integer available_time) {
        this.available_time = available_time;
    }

    public boolean isIs_auto() {
        return is_auto;
    }

    public void setIs_auto(boolean is_auto) {
        this.is_auto = is_auto;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public LocalDateTime getStart_at() {
        return start_at;
    }

    public void setStart_at(LocalDateTime start_at) {
        this.start_at = start_at;
    }

    public LocalDateTime getEnd_at() {
        return end_at;
    }

    public void setEnd_at(LocalDateTime end_at) {
        this.end_at = end_at;
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

    public ArrayList<promotionItemsDTO> getList() {
        return list;
    }

    public void setList(ArrayList<promotionItemsDTO> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return name;
    }
}
