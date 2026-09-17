package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import  java.util.List;

public class chiTietPhieuNhap {
    private long phieuNhapId;
    private long sanPhamId;
    private String tenSP;
    private int soLuong;
    private List<String> serials;
    private BigDecimal giaNhap;
    private String serial;
    private LocalDate ngaySX;
    private int thoiGianBaoHanh;
    private LocalDate ngayHH;
    private LocalDateTime ngayTao;
    public long getPhieuNhapId() {
        return phieuNhapId;
    }
    public void setPhieuNhapId(long phieuNhapId) {
        this.phieuNhapId = phieuNhapId;
    }
    public long getSanPhamId() {
        return sanPhamId;
    }
    public void setSanPhamId(long sanPhamId) {
        this.sanPhamId = sanPhamId;
    }
    public int getSoLuong() {
        return soLuong;
    }
    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
    public BigDecimal getGiaNhap() {
        return giaNhap;
    }
    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }
    public LocalDate getNgaySX() {
        return ngaySX;
    }
    public void setNgaySX(LocalDate ngaySX) {
        this.ngaySX = ngaySX;
    }
    public int getThoiGianBaoHanh() {
        return thoiGianBaoHanh;
    }
    public void setThoiGianBaoHanh(int thoiGianBaoHanh) {
        this.thoiGianBaoHanh = thoiGianBaoHanh;
    }
    public String getTenSP() {
        return tenSP;
    }
    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }
    public LocalDate getNgayHH() {
        return ngayHH;
    }
    public void setNgayHH(LocalDate ngayHH) {
        this.ngayHH = ngayHH;
    }   
    public String getSerial() {
        return serial;
    }
    public void setSerial(String serial) {
        this.serial = serial;
    } 
    public List<String> getSerials() {
        return serials;
    }
    public void setSerials(List<String> serials) {
        this.serials = serials;
    }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
