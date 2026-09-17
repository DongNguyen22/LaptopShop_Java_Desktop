package DAO;

import Connector.JDBCUTIL;
import DTO.CashLedgerDTO;
import DTO.phieuNhap;

import java.math.BigDecimal;
import java.sql.*;

import javax.swing.JOptionPane;

public class PhieuNhapDAO {

    public java.util.List<phieuNhap> getAll() {
        String sql = "SELECT * FROM receipts JOIN suppliers ON receipts.supplier_id = suppliers.supplier_id LEFT JOIN accounts ON receipts.created_user = accounts.account_id LEFT JOIN employees ON accounts.employee_id = employees.employee_id ORDER BY receipts.receipt_id";
        java.util.List<phieuNhap> list = new java.util.ArrayList<>();

        try (Connection c = JDBCUTIL.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                phieuNhap pn = new phieuNhap();
                pn.setId(rs.getLong("receipts.receipt_id"));
                pn.setNhaCungCap(rs.getString("suppliers.name"));
                pn.setTongTien(rs.getBigDecimal("receipts.total"));
                pn.setNgayTao(rs.getTimestamp("receipts.created_at").toLocalDateTime());
                pn.setNguoiTao(rs.getLong("receipts.created_user"));
                pn.setNguoiCapNhat(rs.getLong("receipts.updated_user"));
                pn.setStatus(rs.getInt("receipts.status"));
                list.add(pn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

   /**
    * Lấy thông tin phiếu nhập theo ID (dùng cho chế độ Edit)
    */
    public phieuNhap getById(Long receiptId) {
        String sql = """
            SELECT pn.receipt_id, pn.supplier_id, s.name as nha_cung_cap, 
                pn.total as tong_tien, pn.created_at as ngay_tao, 
                pn.created_user as nguoi_tao, pn.status as trang_thai
            FROM receipts pn
            LEFT JOIN suppliers s ON pn.supplier_id = s.supplier_id
            WHERE pn.receipt_id = ?
        """;

        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, receiptId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                phieuNhap pn = new phieuNhap();
                pn.setId(rs.getLong("receipt_id"));
                pn.setNhaCungCapId(rs.getInt("supplier_id"));
                pn.setNhaCungCap(rs.getString("nha_cung_cap"));
                pn.setTongTien(rs.getBigDecimal("tong_tien"));
                pn.setNgayTao(rs.getTimestamp("ngay_tao") != null 
                    ? rs.getTimestamp("ngay_tao").toLocalDateTime() : null);
                pn.setNguoiTao(rs.getLong("nguoi_tao"));
                pn.setStatus(rs.getInt("trang_thai"));  
                return pn;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public long insert(phieuNhap pn) {
        String sql = "INSERT INTO receipts(supplier_id, total, created_user) VALUES (?,?,?)";

        try (Connection c = JDBCUTIL.getConnection()) {

            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, pn.getNhaCungCapId());
                ps.setBigDecimal(2, pn.getTongTien());
                ps.setLong(3, pn.getNguoiTao());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    long receiptId = rs.getLong(1);

                    c.commit();

                    return receiptId;
                }

            } catch (Exception e) {
                c.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int updateStatus(long receiptId, long updatedUser, int status) {
        String sql = "UPDATE receipts SET status = ?, updated_user = ?, updated_at = CURRENT_TIMESTAMP WHERE receipt_id = ?";

        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setLong(2, updatedUser);
            ps.setLong(3, receiptId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0 && status == 1) {
                BigDecimal total = getTotalById(receiptId);
                CashLedgerDTO cto = new CashLedgerDTO(
                    null,
                    "PURCHASE",
                    receiptId,
                    BigDecimal.ZERO.subtract(total != null ? total : BigDecimal.ZERO),
                    "Thanh toán phiếu nhập #" + receiptId,
                    null
                );

                CashLedgerDAO cd = new CashLedgerDAO();
                cd.insert(cto);
            }

            return rowsAffected;  

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private BigDecimal getTotalById(long receiptId) {
        String sql = "SELECT total FROM receipts WHERE receipt_id = ?";
        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public long update(phieuNhap pn) {
        String sql = "UPDATE receipts SET supplier_id = ?, total = ?, updated_user = ? WHERE receipt_id = ?";

        try (Connection c = JDBCUTIL.getConnection()) {

            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, pn.getNhaCungCapId());
                ps.setBigDecimal(2, pn.getTongTien());
                ps.setLong(3, pn.getNguoiCapNhat());
                ps.setLong(4, pn.getId());
                ps.executeUpdate();

            } catch (Exception e) {
                c.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void deleteByReceiptId(long receiptId) {
        String sql1 = "DELETE FROM receipt_items WHERE receipt_id = ?";
        String sql2 = "DELETE FROM receipts WHERE receipt_id = ?";

        try (Connection c = JDBCUTIL.getConnection();
             PreparedStatement ps1 = c.prepareStatement(sql1);
             PreparedStatement ps2 = c.prepareStatement(sql2)) {

            ps1.setLong(1, receiptId);
            ps1.executeUpdate();
            
            ps2.setLong(1, receiptId);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTotal(long receiptId){

        String sql = """
            UPDATE receipts r
            SET total = (
                SELECT SUM(quantity * unit_price)
                FROM receipt_items
                WHERE receipt_id = ?
            )
            WHERE receipt_id = ?
        """;

        try(Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)){

            ps.setLong(1, receiptId);
            ps.setLong(2, receiptId);

            ps.executeUpdate();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateTongTien(long receiptId, BigDecimal tongTien) {

        String sql = "UPDATE receipts SET total = ? WHERE receipt_id = ?";

        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBigDecimal(1, tongTien);
            ps.setLong(2, receiptId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
