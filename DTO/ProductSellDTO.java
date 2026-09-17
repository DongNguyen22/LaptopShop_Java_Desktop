package DTO;

import java.math.BigInteger;

public class ProductSellDTO {
    private Long product_id;
    private int stock;
    private String product_name;
    private double unit_price;
    private double sell_price;
    public ProductSellDTO(Long product_id, int stock, String product_name, double unit_price, double sell_price) {
        this.product_id = product_id;
        this.stock = stock;
        this.product_name = product_name;
        this.unit_price = unit_price;
        this.sell_price = sell_price;
    }
    public ProductSellDTO() {
    }
    public Long getProduct_id() {
        return product_id;
    }
    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    public String getProduct_name() {
        return product_name;
    }
    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }
    public double getUnit_price() {
        return unit_price;
    }
    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }
    public double getSell_price() {
        return sell_price;
    }
    public void setSell_price(double sell_price) {
        this.sell_price = sell_price;
    }
}
