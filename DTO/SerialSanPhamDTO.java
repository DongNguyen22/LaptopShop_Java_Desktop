package DTO;

public class SerialSanPhamDTO {

    private int idSerial;
    private Long idCTPhieuNhap;
    private String serial;
    private String trangThai;
    private String orderId;
    private int status;

    public SerialSanPhamDTO() {}

    public SerialSanPhamDTO(Long idCTPhieuNhap, String serial, int status) {
        this.idCTPhieuNhap = idCTPhieuNhap;
        this.serial = serial;
        this.status = status;
    }

    public int getIdSerial() {
        return idSerial;
    }

    public void setIdSerial(int idSerial) {
        this.idSerial = idSerial;
    }

    public Long getIdCTPhieuNhap() {
        return idCTPhieuNhap;
    }

    public void setIdCTPhieuNhap(Long idCTPhieuNhap) {
        this.idCTPhieuNhap = idCTPhieuNhap;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTrangThai() {
        return status == 0 ? "Còn hàng" : "Đã bán";
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
