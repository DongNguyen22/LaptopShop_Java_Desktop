package DTO;

public class ThongKePhuongThucThanhToanDTO {
    private String phuongThuc;
    private double tongTien;

    public ThongKePhuongThucThanhToanDTO() {}

    public ThongKePhuongThucThanhToanDTO(String phuongThuc, double tongTien) {
        this.phuongThuc = phuongThuc;
        this.tongTien = tongTien;
    }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}