package DTO;

public class promotionItemsDTO {
    private int promotion_id, discount_percent;
    private long product_id;
    private Long gifted_product_id;
    private String product_name, gifted_product_name;

    public promotionItemsDTO() {

    }

    public promotionItemsDTO(int promotion_id, int discount_percent, long product_id, Long gifted_product_id) {
        this.promotion_id = promotion_id;
        this.discount_percent = discount_percent;
        this.product_id = product_id;
        this.gifted_product_id = gifted_product_id;
    }

    public int getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(int promotion_id) {
        this.promotion_id = promotion_id;
    }

    public int getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(int discount_percent) {
        this.discount_percent = discount_percent;
    }

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    public Long getGifted_product_id() {
        return gifted_product_id;
    }

    public void setGifted_product_id(Long gifted_product_id) {
        this.gifted_product_id = gifted_product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getGifted_product_name() {
        return gifted_product_name;
    }

    public void setGifted_product_name(String gifted_product_name) {
        this.gifted_product_name = gifted_product_name;
    }
}
