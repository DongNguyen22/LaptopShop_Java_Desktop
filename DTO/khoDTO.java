package DTO;

public class khoDTO {
    private long phieuNhapId;
    private long productId;
    private int maSP;
    private String tenSP;
    private int soLuong;
    private double giaNhap;
    private int tonKho;
    private String serial;
    private int thoiGianBaoHanh;
    private boolean trangThai;
    
    public long getProductId() {
        return productId;
    }
    
    public void setProductId(long productId) {
        this.productId = productId;
    }
    
    public int getMaSP() {
        return maSP;
    }
    public void setMaSP(int maSP) {
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
    public double getGiaNhap() {
        return giaNhap;
    }
    public void setGiaNhap(double giaNhap) {
        this.giaNhap = giaNhap;
    }
    public int getTonKho() {
        return tonKho;
    }
    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }
    public int getThoiGianBaoHanh() {
        return thoiGianBaoHanh;
    }
    public void setThoiGianBaoHanh(int thoiGianBaoHanh) {
        this.thoiGianBaoHanh = thoiGianBaoHanh;
    }
    public boolean isTrangThai() {
        return trangThai;
    }
    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
    public long getPhieuNhapId() {
        return phieuNhapId;
    }
    public void setPhieuNhapId(long phieuNhapId) {
        this.phieuNhapId = phieuNhapId;
    }
    public String getSerial() {
        return serial;
    }
    public void setSerial(String serial) {
        this.serial = serial;
    }
}
