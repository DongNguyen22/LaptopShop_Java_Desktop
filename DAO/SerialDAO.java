package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Connector.JDBCUTIL;
import DTO.SerialDTO;
import DTO.SerialSanPhamDTO;

public class SerialDAO {
    private Connection conn;

    public SerialDAO() {
        try {
            conn = JDBCUTIL.getConnection();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public List<SerialSanPhamDTO> getSerial(Long receiptItemId) {
        String sql = "SELECT * FROM item_serials WHERE receipt_item_id = ?";
        List<SerialSanPhamDTO> list = new java.util.ArrayList<>();

        try (Connection c = JDBCUTIL.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, receiptItemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SerialSanPhamDTO sp = new SerialSanPhamDTO();
                sp.setIdCTPhieuNhap(rs.getLong("receipt_item_id"));
                sp.setSerial(rs.getString("serial"));
                sp.setStatus(rs.getInt("is_used"));
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm serial
    public boolean insert(SerialSanPhamDTO sp) {
        String sql = """
            INSERT INTO item_serials
            (receipt_item_id, serial)
            VALUES (?, ?)
        """;

        try {var conn = JDBCUTIL.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, sp.getIdCTPhieuNhap());
            ps.setString(2, sp.getSerial());
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Serial bị trùng!");
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Lấy tất cả serial của từng sản phẩm (status = 0) */
    public ArrayList<SerialDTO> getAllSerialByProduct() throws SQLException {
        
        ArrayList<SerialDTO> map = new ArrayList<>();

        String sql = "SELECT \n" +
                        "    ri.receipt_id,\n" +
                        "    ri.product_id,\n" +
                        "    ri.receipt_item_id,\n" +
                        "    s.serial,\n" +
                        "    s.is_used\n" +
                        "FROM receipt_items ri\n" + 
                        "JOIN item_serials s \n" + 
                        "    ON ri.receipt_item_id = s.receipt_item_id where is_used=0";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long productId = rs.getLong("product_id");
                String serial = rs.getString("serial");
                long receipt_id = rs.getLong("receipt_id");
                boolean is_active = rs.getBoolean("is_used");
                SerialDTO serialDTO = new SerialDTO(receipt_id, productId, serial, is_active);
                map.add(serialDTO);
            }
        }
        return map;
    }

    // Thêm vào class SerialDAO
    public String getOrderIdBySerial(String serial) {
        String sql = """
            SELECT oi.order_id 
            FROM order_items oi 
            WHERE oi.product_serial = ?
            LIMIT 1
        """;
        try (var c = JDBCUTIL.getConnection();
            var ps = c.prepareStatement(sql)) {
            ps.setString(1, serial);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("order_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Chưa bán
    }

    /* Thêm serial */
    public boolean insert(SerialDTO item) throws SQLException {
        String sql = "INSERT INTO item_serials values(receipt_id, product_id, serial, is_used) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, item.getReceiptId());
            ps.setLong(2, item.getProductId());
            ps.setString(3, item.getSerial());
            ps.setBoolean(4, item.isStatus());

            return ps.executeUpdate() > 0;
        }
    }

    /* Cập nhật status khi bán */
    public boolean updateReceiptAndStatus(Connection conn, String serial) throws SQLException {
        String sql = "UPDATE item_serials SET is_used = 1 WHERE serial = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            return ps.executeUpdate() > 0;
        }
    }

    // check serial
    public boolean isSerialAvailable(String serial) {
        String sql = "SELECT is_used FROM item_serials WHERE serial = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serial);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("is_used") == 0;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy ID nhà cung cấp từ code
     */
    public Long getSupplierIdByCode(String code) {
        String sql = "SELECT supplier_id FROM suppliers WHERE supplier_code = ?";
        try (var c = JDBCUTIL.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("supplier_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy ID sản phẩm từ code
     */
    public Long getProductIdByCode(String code) {
        String sql = "SELECT product_id FROM products WHERE product_code = ?";
        try (var c = JDBCUTIL.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("product_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra sản phẩm có thuộc nhà cung cấp không
     */
    public boolean isProductForSupplier(Long productId, Long supplierId) {
        String sql = "SELECT COUNT(*) as cnt FROM supplier_products WHERE product_id = ? AND supplier_id = ?";
        try (var c = JDBCUTIL.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, supplierId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm các serial numbers cho receipt item
     */
    public void addSerialNumbers(Long receiptId, Long productId, List<String> serials) {
        // Lấy receipt_item_id từ receipt_items table
        String sqlGetItemId = "SELECT receipt_item_id FROM receipt_items WHERE receipt_id = ? AND product_id = ? LIMIT 1";
        try (var c = JDBCUTIL.getConnection();
             var ps = c.prepareStatement(sqlGetItemId)) {
            ps.setLong(1, receiptId);
            ps.setLong(2, productId);
            var rs = ps.executeQuery();
            
            if (rs.next()) {
                Long receiptItemId = rs.getLong("receipt_item_id");
                
                // Thêm từng serial vào item_serials table
                String sqlInsertSerial = "INSERT INTO item_serials (receipt_item_id, serial) VALUES (?, ?)";
                try (var psSerial = c.prepareStatement(sqlInsertSerial)) {
                    for (String serial : serials) {
                        psSerial.setLong(1, receiptItemId);
                        psSerial.setString(2, serial);
                        psSerial.addBatch();
                    }
                    psSerial.executeBatch();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSerialExists(String serial) {

        String sql = "SELECT COUNT(*) FROM item_serials WHERE serial = ?";

        try (var c = JDBCUTIL.getConnection();
            var ps = c.prepareStatement(sql)) {

            ps.setString(1, serial);

            var rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
