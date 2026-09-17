package DTO;

public class SerialDTO {
    private long receiptId;
    private long productId;
    private String serial;
    private boolean status;
    public SerialDTO(long receiptId, long productId, String serial, boolean status) {
        this.receiptId = receiptId;
        this.productId = productId;
        this.serial = serial;
        this.status = status;
    }
    public SerialDTO() {
    }
    public long getReceiptId() {
        return receiptId;
    }
    public void setReceiptId(long receiptId) {
        this.receiptId = receiptId;
    }
    public long getProductId() {
        return productId;
    }
    public void setProductId(long productId) {
        this.productId = productId;
    }
    public String getSerial() {
        return serial;
    }
    public void setSerial(String serial) {
        this.serial = serial;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return serial;
    }
    
}
