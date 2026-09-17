package DTO;

public class nhaCungCapDTO {
    private int supplier_id;
    private String name, phone_number, address, code;

    public nhaCungCapDTO() {

    }

    public nhaCungCapDTO(String code, String name, String phone_number, String address) {
        this.code = code;
        this.name = name;
        this.phone_number = phone_number;
        this.address = address;
    }

    public nhaCungCapDTO(int supplier_id, String code, String name, String phone_number, String address) {
        this.supplier_id = supplier_id;
        this.code = code;
        this.name = name;
        this.phone_number = phone_number;
        this.address = address;
    }

    public int getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(int supplier_id) {
        this.supplier_id = supplier_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
