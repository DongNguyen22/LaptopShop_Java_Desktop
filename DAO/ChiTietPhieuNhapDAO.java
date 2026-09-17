package DAO;

import Connector.JDBCUTIL;
import DTO.chiTietPhieuNhap;
import DTO.phieuNhap;

import java.sql.*;
import java.time.LocalDate;

public class ChiTietPhieuNhapDAO {
    public java.util.List<chiTietPhieuNhap> getAll(phieuNhap pn) {
        String sql = "SELECT ri.*, p.name FROM receipt_items ri JOIN products p ON ri.product_id = p.product_id WHERE ri.receipt_id = ?";
        java.util.List<chiTietPhieuNhap> list = new java.util.ArrayList<>();

        try (Connection c = JDBCUTIL.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, pn.getId());
            var rs = ps.executeQuery();

            while (rs.next()) {
                chiTietPhieuNhap ct = new chiTietPhieuNhap();
                ct.setPhieuNhapId(rs.getLong("ri.receipt_id"));
                ct.setSanPhamId(rs.getLong("ri.product_id"));
                ct.setTenSP(rs.getString("p.name"));
                ct.setSerial(rs.getString("ri.receipt_id"));
                ct.setSoLuong(rs.getInt("ri.quantity"));
                ct.setGiaNhap(rs.getBigDecimal("ri.unit_price"));
                ct.setThoiGianBaoHanh(rs.getInt("ri.warranty_time"));
                list.add(ct);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Kiểm tra xem phiếu nhập có serial nào đã bán chưa (không cho sửa/xóa nếu có)
     */
    public boolean hasAnySerialUsedInOrder(Long receiptId) {
        String sql = """
            SELECT COUNT(*) 
            FROM item_serials s
            JOIN receipt_items ri ON s.receipt_item_id = ri.receipt_item_id
            JOIN order_items oi ON oi.product_serial = s.serial
            WHERE ri.receipt_id = ?
        """;

        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa an toàn chi tiết + serial (chỉ xóa serial chưa bán)
     */
    public void deleteAllByReceiptIdSafe(Long receiptId) {
        // Xóa serial chưa bán
        String sqlDeleteUnusedSerial = """
            DELETE s FROM item_serials s
            JOIN receipt_items ri ON s.receipt_item_id = ri.receipt_item_id
            WHERE ri.receipt_id = ? 
            AND NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.product_serial = s.serial)
        """;

        String sqlDeleteReceiptItems = "DELETE FROM receipt_items WHERE receipt_id = ?";

        try (Connection c = JDBCUTIL.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(sqlDeleteUnusedSerial);
                PreparedStatement ps2 = c.prepareStatement(sqlDeleteReceiptItems)) {

                ps1.setLong(1, receiptId);
                ps1.executeUpdate();

                ps2.setLong(1, receiptId);
                ps2.executeUpdate();

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw new RuntimeException("Không thể xóa phiếu vì có serial đã bán!", ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa tất cả chi tiết phiếu nhập và serial liên quan (dùng trước khi cập nhật lại khi sửa phiếu)
     */
    public void deleteAllByReceiptId(Long receiptId) {
        String sqlSerial = """
            DELETE s FROM item_serials s 
            JOIN receipt_items ri ON s.receipt_item_id = ri.receipt_item_id 
            WHERE ri.receipt_id = ?
        """;

        String sqlDetail = "DELETE FROM receipt_items WHERE receipt_id = ?";

        try (Connection c = JDBCUTIL.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement psSerial = c.prepareStatement(sqlSerial);
                PreparedStatement psDetail = c.prepareStatement(sqlDetail)) {

                psSerial.setLong(1, receiptId);
                psSerial.executeUpdate();

                psDetail.setLong(1, receiptId);
                psDetail.executeUpdate();

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long insert(chiTietPhieuNhap ct) {
        long id = -1;

        String sql = """
            INSERT INTO receipt_items
            (receipt_id, product_id,
            quantity, unit_price, warranty_time)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps =
                c.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, ct.getPhieuNhapId());
            ps.setLong(2, ct.getSanPhamId());
            ps.setInt(3, ct.getSoLuong());
            ps.setBigDecimal(4, ct.getGiaNhap());
            ps.setInt(5, ct.getThoiGianBaoHanh());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) id = rs.getLong(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }

    public void calculateAveragePrice(long productId) {

        String sql = """
            CALL sp_recalculate_avg_price(?);
        """;

        try {Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, productId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Long getIDCTPhieuNhap(long idPhieuNhap, long idSanPham) {

        String sql = """
            SELECT receipt_item_id
            FROM receipt_items
            WHERE receipt_id = ?
            AND product_id   = ?
        """;

        try {Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, idPhieuNhap);
            ps.setLong(2, idSanPham);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("receipt_item_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1L;
    }

    public void updateSoLuongBySerial(Long idCTPhieuNhap) {

        String sql = """
            UPDATE receipt_items
            SET quantity = (
                SELECT COUNT(*)
                FROM item_serials
                WHERE receipt_item_id = ?
            )
            WHERE receipt_item_id = ?
        """;

        try {Connection conn = JDBCUTIL.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, idCTPhieuNhap);
            ps.setLong(2, idCTPhieuNhap);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
