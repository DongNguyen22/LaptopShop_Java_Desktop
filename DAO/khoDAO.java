package DAO;

import DTO.khoDTO;
import java.sql.*;
import Connector.JDBCUTIL;

public class khoDAO {
    public java.util.List<khoDTO> getAll() {
        java.util.List<khoDTO> list = new java.util.ArrayList<>();

        String sql = "SELECT " +
                "    v.receipt_id, " +
                "    v.product_id, " +
                "    p.name, " +
                "    r.quantity, " +
                "    r.unit_price, " +
                "    v.stock, " +
                "    r.warranty_time, " +
                "    r.is_active " +
                "FROM vw_receipt_items v " +
                "JOIN receipt_items r " +
                "    ON v.receipt_id = r.receipt_id " +
                "    AND v.product_id = r.product_id " +
                "JOIN products p " +
                "    ON v.product_id = p.product_id";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql);
                var rs = ps.executeQuery()) {

            while (rs.next()) {
                khoDTO kho = new khoDTO();

                kho.setPhieuNhapId(rs.getLong("receipt_id"));
                kho.setProductId(rs.getLong("product_id"));
                kho.setTenSP(rs.getString("name"));

                // Số lượng nhập lấy trực tiếp từ receipt_items
                kho.setSoLuong(rs.getInt("quantity"));

                // Giá nhập
                kho.setGiaNhap(rs.getDouble("unit_price"));

                // Số lượng tồn kho lấy từ VIEW
                kho.setTonKho(rs.getInt("stock"));

                // Thời gian bảo hành
                kho.setThoiGianBaoHanh(rs.getInt("warranty_time"));

                // Trạng thái
                kho.setTrangThai(rs.getBoolean("is_active"));

                list.add(kho);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateIsActive(long receiptId, long productId, boolean isActive) {
        String sql = "UPDATE receipt_items SET is_active = ? WHERE receipt_id = ? AND product_id = ?";
        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setLong(2, receiptId);
            ps.setLong(3, productId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
