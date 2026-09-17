package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class sanPham {
    private String maSP, tenSP, code;
    private int soLuong;
    private double donGia;
    private double loiNhuan;
    private boolean isActive;
    private int danhMucId;
    private String tenDanhmuc;
    private long donViId;
    private double giaNhapTb;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private double discountPercent;
    private double sellPrice;
    private List<String> imageUrls = new ArrayList<>(); 

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> urls) { this.imageUrls = urls; }
    public void addImageUrl(String url) {
        if (imageUrls.size() < 5) imageUrls.add(url);
    }

    public sanPham(String maSP, String code, String tenSP, int soLuong, double donGia, double loiNhuan, boolean isActive) {
        this.maSP = maSP;
        this.code = code;
        this.tenSP = tenSP;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.isActive = isActive;
        this.loiNhuan = loiNhuan;
    }

    public sanPham() {
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public double getLoiNhuan() {
        return loiNhuan;
    }

    public void setLoiNhuan(double loiNhuan) {
        this.loiNhuan = loiNhuan;
    }

    public int getDanhMucId() {
        return danhMucId;
    }

    public void setDanhMucId(int danhMucId) {
        this.danhMucId = danhMucId;
    }

    public long getDonViId() {
        return donViId;
    }

    public void setDonViId(long donViId) {
        this.donViId = donViId;
    }

    public double getGiaNhapTb() {
        return giaNhapTb;
    }

    public void setGiaNhapTb(double giaNhapTb) {
        this.giaNhapTb = giaNhapTb;
    }

    public String getTenDanhmuc() {
        return tenDanhmuc;
    }

    public void setTenDanhmuc(String tenDanhmuc) {
        this.tenDanhmuc = tenDanhmuc;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.sellPrice = sellPrice;
    }
    @Override
    public String toString() {
        return maSP + " - " + tenSP;
    }
}