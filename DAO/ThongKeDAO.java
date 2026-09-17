package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import Connector.JDBCUTIL;
import DTO.EmployeeDTO;
import DTO.OrderDTO;
import DTO.OrderItemDTO;
import DTO.ThongKePhuongThucThanhToanDTO;
import DTO.chiTietPhieuNhap;
import DTO.phieuNhap;
import DTO.sanPham;

public class ThongKeDAO {

    // ==================== Lấy danh sách nhân viên / sản phẩm ====================
    public ArrayList<EmployeeDTO> getEmployees() {
        ArrayList<EmployeeDTO> list = new ArrayList<>();
        String sql = "SELECT employee_id, employee_name FROM employees WHERE status = 'ACTIVE'";
        try (Connection conn = JDBCUTIL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                EmployeeDTO dto = new EmployeeDTO();
                dto.setEmployeeId(rs.getLong("employee_id"));
                dto.setEmployeeName(rs.getString("employee_name"));
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<sanPham> getProducts() {
        ArrayList<sanPham> list = new ArrayList<>();
        String sql = "SELECT product_id, name FROM products WHERE is_active = 1";
        try (Connection conn = JDBCUTIL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sanPham sp = new sanPham();
                sp.setMaSP(String.valueOf(rs.getLong("product_id")));
                sp.setTenSP(rs.getString("name"));
                list.add(sp);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================== ĐẾM BẢN GHI ====================
    public int countOrdersByEmployee(long empId, String start, String end) {
        String sql = "SELECT COUNT(*) FROM orders o " +
                     "JOIN accounts a ON o.created_user = a.account_id " +
                     "WHERE a.employee_id = ? AND o.created_at BETWEEN ? AND ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int countReceiptsByEmployee(long empId, String start, String end) {
        String sql = "SELECT COUNT(*) FROM receipts WHERE created_user = ? AND created_at BETWEEN ? AND ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Đếm số dòng order_items (mỗi serial là 1 dòng) cho sản phẩm
    public int countOrdersByProduct(long productId, String start, String end) {
        String sql = "SELECT COUNT(*) FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.order_id " +
                     "WHERE oi.product_id = ? AND o.created_at BETWEEN ? AND ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Đếm số serial nhập (mỗi serial là 1 dòng trong item_serials)
    public int countReceiptsByProduct(long productId, String start, String end) {
        String sql = "SELECT COUNT(*) FROM receipt_items ri " +
                     "JOIN receipts r ON ri.receipt_id = r.receipt_id " +
                     "JOIN item_serials ise ON ise.receipt_item_id = ri.receipt_item_id " +
                     "WHERE ri.product_id = ? AND r.created_at BETWEEN ? AND ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int countAllOrders(String start, String end) {
        String sql = "SELECT COUNT(*) FROM orders WHERE created_at BETWEEN ? AND ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int countAllReceipts(String start, String end) {
        String sql = "SELECT COUNT(*) FROM receipts WHERE created_at BETWEEN ? AND ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ==================== LẤY DỮ LIỆU (không phân trang) ====================
    public ArrayList<OrderDTO> getOrdersByEmployee(long empId, String start, String end) {
        ArrayList<OrderDTO> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.total, o.payment_method, o.created_at " +
                     "FROM orders o JOIN accounts a ON o.created_user = a.account_id " +
                     "WHERE a.employee_id = ? AND o.created_at BETWEEN ? AND ? " +
                     "ORDER BY o.created_at DESC";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDTO dto = new OrderDTO();
                dto.setOrder_id(rs.getLong("order_id"));
                dto.setTotal(rs.getBigDecimal("total"));
                dto.setPayment_method(rs.getString("payment_method"));
                dto.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<phieuNhap> getReceiptsByEmployee(long empId, String start, String end) {
        ArrayList<phieuNhap> list = new ArrayList<>();
        String sql = "SELECT receipt_id, total, created_at FROM receipts " +
                     "WHERE created_user = ? AND created_at BETWEEN ? AND ? " +
                     "ORDER BY created_at DESC";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                phieuNhap dto = new phieuNhap();
                dto.setId(rs.getLong("receipt_id"));
                dto.setTongTien(rs.getBigDecimal("total"));
                dto.setNgayTao(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy chi tiết order_items cho sản phẩm (mỗi serial là 1 dòng)
    public ArrayList<OrderItemDTO> getOrdersByProduct(long productId, String start, String end) {
        ArrayList<OrderItemDTO> list = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, oi.unit_price, oi.product_serial, " +
                     "o.payment_method, o.created_at " +
                     "FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.order_id " +
                     "WHERE oi.product_id = ? AND o.created_at BETWEEN ? AND ? " +
                     "ORDER BY o.created_at DESC";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItemDTO dto = new OrderItemDTO();
                dto.setOrderId(rs.getLong("order_id"));
                dto.setProductId(rs.getLong("product_id"));
                dto.setUnitPrice(rs.getBigDecimal("unit_price"));
                dto.setProductSerial(rs.getString("product_serial"));
                dto.setPaymentMethod(rs.getString("payment_method"));
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy chi tiết receipt_items cho sản phẩm (mỗi serial là 1 dòng)
    public ArrayList<chiTietPhieuNhap> getReceiptsByProduct(long productId, String start, String end) {
        ArrayList<chiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT ri.receipt_id, ri.product_id, ri.unit_price, ise.serial, " +
                     "r.created_at " +
                     "FROM receipt_items ri " +
                     "JOIN receipts r ON ri.receipt_id = r.receipt_id " +
                     "JOIN item_serials ise ON ise.receipt_item_id = ri.receipt_item_id " +
                     "WHERE ri.product_id = ? AND r.created_at BETWEEN ? AND ? " +
                     "ORDER BY r.created_at DESC";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chiTietPhieuNhap dto = new chiTietPhieuNhap();
                dto.setPhieuNhapId(rs.getLong("receipt_id"));
                dto.setSanPhamId(rs.getLong("product_id"));
                dto.setGiaNhap(rs.getBigDecimal("unit_price"));
                dto.setSerial(rs.getString("serial"));
                dto.setNgayTao(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<OrderDTO> getAllOrders(String start, String end) {
        ArrayList<OrderDTO> list = new ArrayList<>();
        String sql = "SELECT order_id, total, payment_method, created_at FROM orders " +
                     "WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDTO dto = new OrderDTO();
                dto.setOrder_id(rs.getLong("order_id"));
                dto.setTotal(rs.getBigDecimal("total"));
                dto.setPayment_method(rs.getString("payment_method"));
                dto.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<phieuNhap> getAllReceipts(String start, String end) {
        ArrayList<phieuNhap> list = new ArrayList<>();
        String sql = "SELECT receipt_id, total, created_at FROM receipts " +
                     "WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                phieuNhap dto = new phieuNhap();
                dto.setId(rs.getLong("receipt_id"));
                dto.setTongTien(rs.getBigDecimal("total"));
                dto.setNgayTao(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================== LẤY DỮ LIỆU CÓ PHÂN TRANG ====================
    public ArrayList<OrderDTO> getOrdersByEmployeePaginated(long empId, String start, String end, int offset, int limit) {
        ArrayList<OrderDTO> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.total, o.payment_method, o.created_at " +
                     "FROM orders o JOIN accounts a ON o.created_user = a.account_id " +
                     "WHERE a.employee_id = ? AND o.created_at BETWEEN ? AND ? " +
                     "ORDER BY o.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empId);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.setInt(4, limit);
            ps.setInt(5, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDTO dto = new OrderDTO();
                dto.setOrder_id(rs.getLong("order_id"));
                dto.setTotal(rs.getBigDecimal("total"));
                dto.setPayment_method(rs.getString("payment_method"));
                dto.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<phieuNhap> getReceiptsByEmployeePaginated(long empId, String start, String end, int offset, int limit) {
        ArrayList<phieuNhap> list = new ArrayList<>();
        String sql = "SELECT receipt_id, total, created_at FROM receipts " +
                     "WHERE created_user = ? AND created_at BETWEEN ? AND ? " +
                     "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empId);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.setInt(4, limit);
            ps.setInt(5, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                phieuNhap dto = new phieuNhap();
                dto.setId(rs.getLong("receipt_id"));
                dto.setTongTien(rs.getBigDecimal("total"));
                dto.setNgayTao(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<OrderItemDTO> getOrdersByProductPaginated(long productId, String start, String end, int offset, int limit) {
        ArrayList<OrderItemDTO> list = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, oi.unit_price, oi.product_serial, " +
                     "o.payment_method, o.created_at " +
                     "FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.order_id " +
                     "WHERE oi.product_id = ? AND o.created_at BETWEEN ? AND ? " +
                     "ORDER BY o.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.setInt(4, limit);
            ps.setInt(5, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItemDTO dto = new OrderItemDTO();
                dto.setOrderId(rs.getLong("order_id"));
                dto.setProductId(rs.getLong("product_id"));
                dto.setUnitPrice(rs.getBigDecimal("unit_price"));
                dto.setProductSerial(rs.getString("product_serial"));
                dto.setPaymentMethod(rs.getString("payment_method"));
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<chiTietPhieuNhap> getReceiptsByProductPaginated(long productId, String start, String end, int offset, int limit) {
        ArrayList<chiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT ri.receipt_id, ri.product_id, ri.unit_price, ise.serial, " +
                     "r.created_at " +
                     "FROM receipt_items ri " +
                     "JOIN receipts r ON ri.receipt_id = r.receipt_id " +
                     "JOIN item_serials ise ON ise.receipt_item_id = ri.receipt_item_id " +
                     "WHERE ri.product_id = ? AND r.created_at BETWEEN ? AND ? " +
                     "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.setInt(4, limit);
            ps.setInt(5, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chiTietPhieuNhap dto = new chiTietPhieuNhap();
                dto.setPhieuNhapId(rs.getLong("receipt_id"));
                dto.setSanPhamId(rs.getLong("product_id"));
                dto.setGiaNhap(rs.getBigDecimal("unit_price"));
                dto.setSerial(rs.getString("serial"));
                dto.setNgayTao(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<OrderDTO> getAllOrdersPaginated(String start, String end, int offset, int limit) {
        ArrayList<OrderDTO> list = new ArrayList<>();
        String sql = "SELECT order_id, total, payment_method, created_at FROM orders " +
                     "WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDTO dto = new OrderDTO();
                dto.setOrder_id(rs.getLong("order_id"));
                dto.setTotal(rs.getBigDecimal("total"));
                dto.setPayment_method(rs.getString("payment_method"));
                dto.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<phieuNhap> getAllReceiptsPaginated(String start, String end, int offset, int limit) {
        ArrayList<phieuNhap> list = new ArrayList<>();
        String sql = "SELECT receipt_id, total, created_at FROM receipts " +
                     "WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                phieuNhap dto = new phieuNhap();
                dto.setId(rs.getLong("receipt_id"));
                dto.setTongTien(rs.getBigDecimal("total"));
                dto.setNgayTao(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ==================== TỔNG HỢP ====================
    public ArrayList<ThongKePhuongThucThanhToanDTO> getPaymentData(String start, String end) {
        ArrayList<ThongKePhuongThucThanhToanDTO> list = new ArrayList<>();
        String sql = "SELECT payment_method, SUM(total) as total " +
                     "FROM orders WHERE created_at BETWEEN ? AND ? GROUP BY payment_method";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ThongKePhuongThucThanhToanDTO(
                    rs.getString("payment_method"),
                    rs.getDouble("total")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public double[] getSummaryByEmployee(String type, long id, String start, String end) {
        double[] result = new double[2];
        String sql;
        if ("orders".equals(type)) {
            sql = "SELECT SUM(o.total) as total, COUNT(*) as count " +
                  "FROM orders o JOIN accounts a ON o.created_user = a.account_id " +
                  "WHERE a.employee_id = ? AND o.created_at BETWEEN ? AND ?";
        } else {
            sql = "SELECT SUM(total) as total, COUNT(*) as count " +
                  "FROM receipts WHERE created_user = ? AND created_at BETWEEN ? AND ?";
        }
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setString(2, start);
            ps.setString(3, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result[0] = rs.getDouble("total");
                result[1] = rs.getInt("count");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Tổng hợp cho sản phẩm: tổng tiền là sum(unit_price) của các serial, số lượng là số dòng
    public double[] getSummaryByProduct(String type, long id, String start, String end) {
        double[] result = new double[2];
        if ("orders".equals(type)) {
            String sql = "SELECT COUNT(*) as count, SUM(oi.unit_price) as total " +
                         "FROM order_items oi " +
                         "JOIN orders o ON oi.order_id = o.order_id " +
                         "WHERE oi.product_id = ? AND o.created_at BETWEEN ? AND ?";
            try (Connection conn = JDBCUTIL.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, start);
                ps.setString(3, end);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    result[0] = rs.getInt("count");
                    result[1] = rs.getDouble("total");
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            // Phiếu nhập: tổng tiền = sum(unit_price) của từng serial (join item_serials)
            String sql = "SELECT COUNT(*) as count, SUM(ri.unit_price) as total " +
                         "FROM receipt_items ri " +
                         "JOIN receipts r ON ri.receipt_id = r.receipt_id " +
                         "JOIN item_serials ise ON ise.receipt_item_id = ri.receipt_item_id " +
                         "WHERE ri.product_id = ? AND r.created_at BETWEEN ? AND ?";
            try (Connection conn = JDBCUTIL.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, start);
                ps.setString(3, end);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    result[0] = rs.getInt("count");
                    result[1] = rs.getDouble("total");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return result;
    }
}