package DTO;

public class supplierProductsDTO {
    private int supplier_product_id;
    private int supplier_id;
    private int product_id;

    // Constructors
    public supplierProductsDTO() {}

    public supplierProductsDTO(int supplier_product_id, int supplier_id, int product_id) {
        this.supplier_product_id = supplier_product_id;
        this.supplier_id = supplier_id;
        this.product_id = product_id;
    }

    // Getters & Setters
    public int getSupplier_product_id() { return supplier_product_id; }
    public void setSupplier_product_id(int supplier_product_id) { this.supplier_product_id = supplier_product_id; }

    public int getSupplier_id() { return supplier_id; }
    public void setSupplier_id(int supplier_id) { this.supplier_id = supplier_id; }

    public int getProduct_id() { return product_id; }
    public void setProduct_id(int product_id) { this.product_id = product_id; }
}