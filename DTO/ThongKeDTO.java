package DTO;

import java.math.BigDecimal;

public class ThongKeDTO {
    // Thông tin chung
    private String maPhieu;
    private String ngayTao;
    private BigDecimal tongTien;
    
    // Thông tin bổ sung theo tiêu chí
    private String tenDoiTuong; // Tên nhân viên hoặc Tên sản phẩm
    private int soLuong;       // Dùng cho sản phẩm
    private BigDecimal donGia; // Dùng cho sản phẩm
    private String phuongThuc;  // Dùng cho hóa đơn

    public ThongKeDTO() {}

    // Getter và Setter
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public String getTenDoiTuong() { return tenDoiTuong; }
    public void setTenDoiTuong(String tenDoiTuong) { this.tenDoiTuong = tenDoiTuong; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }
}