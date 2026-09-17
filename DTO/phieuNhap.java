package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class phieuNhap {
    private long id;
    private int nhaCungCapId;
    private String nhaCungCap;
    private int status;
    private String trangThai;
    private BigDecimal tongTien;
    private LocalDateTime ngayTao;
    private long nguoiTao;
    private long nguoiCapNhat;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public BigDecimal getTongTien() {
        return tongTien;
    }
    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }
    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
    public long getNguoiTao() {
        return nguoiTao;
    }
    public void setNguoiTao(long nguoiTao) {
        this.nguoiTao = nguoiTao;
    }
    public int getNhaCungCapId() {
        return nhaCungCapId;
    }
    public void setNhaCungCapId(int nhaCungCapId) {
        this.nhaCungCapId = nhaCungCapId;
    }
    public String getNhaCungCap() {
        return nhaCungCap;
    }   
    public void setNhaCungCap(String nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public String getTrangThai() {
        return status == 0 ? "Chờ xác nhận" : "Đã xác nhận";
    }
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    public long getNguoiCapNhat() {
        return nguoiCapNhat;
    }
    public void setNguoiCapNhat(long nguoiCapNhat) {
        this.nguoiCapNhat = nguoiCapNhat;
    }
}

