package DAO;

import DTO.WarrantyTempDTO;
import DTO.warrantyCardDTO;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Connector.JDBCUTIL;

public class warrantyCardDAO {

    /**
     * Lấy tất cả phiếu bảo hành
     */
    public List<warrantyCardDTO> getAll() {
        List<warrantyCardDTO> list = new ArrayList<>();
        String sql = "SELECT wc.*, p.name as product_name, c.customer_name, c.phone_number, e.employee_name " +
                "FROM warranty_card wc " +
                "LEFT JOIN products p ON wc.product_id = p.product_id " +
                "LEFT JOIN orders o ON wc.order_id = o.order_id " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "LEFT JOIN employees e ON wc.created_user = e.employee_id " +
                "ORDER BY wc.created_at";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                warrantyCardDTO dto = mapResultSetToDTO(rs);
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getWarrantyMonthsFromReceipt(long orderId) {
        String sql = "SELECT ri.warranty_time FROM receipt_items ri " +
                "LEFT JOIN order_items oi ON ri.receipt_id = oi.receipt_id " +
                "WHERE oi.order_id = ? LIMIT 1";
        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("warranty_time");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // hoặc giá trị mặc định
    }

    /**
     * Lấy phiếu bảo hành theo ID
     */
    public warrantyCardDTO getById(long warrantyId) {
        String sql = "SELECT wc.*, p.name as product_name, c.customer_name, c.phone_number, e.employee_name " +
                "FROM warranty_card wc " +
                "LEFT JOIN products p ON wc.product_id = p.product_id " +
                "LEFT JOIN orders o ON wc.order_id = o.order_id " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "LEFT JOIN employees e ON wc.created_user = e.employee_id " +
                "WHERE wc.warranty_id = ?";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, warrantyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy các phiếu bảo hành của một đơn hàng
     */
    public List<warrantyCardDTO> getByOrderId(long orderId) {
        List<warrantyCardDTO> list = new ArrayList<>();
        String sql = "SELECT wc.*, p.name as product_name, c.customer_name, c.phone_number, e.employee_name " +
                "FROM warranty_card wc " +
                "LEFT JOIN products p ON wc.product_id = p.product_id " +
                "LEFT JOIN orders o ON wc.order_id = o.order_id " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "LEFT JOIN employees e ON wc.created_user = e.employee_id " +
                "WHERE wc.order_id = ? ORDER BY wc.created_at DESC";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy các đơn hàng của một khách hàng theo SĐT
     */
    public List<Long> getOrdersByCustomerPhone(String phone) {
        List<Long> list = new ArrayList<>();
        String sql = "SELECT DISTINCT o.order_id FROM orders o " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "WHERE c.phone_number LIKE ? " +
                "ORDER BY o.created_at DESC";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + phone + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("order_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy các đơn hàng của khách hàng theo tên
     */
    public List<Long> getOrdersByCustomerName(String name) {
        List<Long> list = new ArrayList<>();
        String sql = "SELECT DISTINCT o.order_id FROM orders o " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "WHERE c.customer_name LIKE ? " +
                "ORDER BY o.created_at DESC";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("order_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy thông tin chi tiết các sản phẩm trong một đơn hàng
     */
    public List<warrantyCardDTO> getOrderItemsByOrderId(long orderId) {
        List<warrantyCardDTO> list = new ArrayList<>();

        String sql = """
                SELECT
                    oi.order_id,
                    oi.product_id,
                    p.name AS product_name,
                    oi.product_serial,
                    oi.unit_price,
                    rp.warranty_time,
                    c.customer_name,
                    c.phone_number,
                    o.created_at
                FROM order_items oi
                JOIN products p
                    ON oi.product_id = p.product_id
                JOIN orders o
                    ON oi.order_id = o.order_id
                JOIN customer c
                    ON o.customer_id = c.customer_id
                LEFT JOIN receipts recv
                    ON oi.receipt_id = recv.receipt_id
                LEFT JOIN receipt_items rp
                    ON recv.receipt_id = rp.receipt_id
                    AND oi.product_id = rp.product_id
                WHERE oi.order_id = ?
                """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    warrantyCardDTO dto = new warrantyCardDTO();

                    dto.setOrderId(rs.getLong("order_id"));
                    dto.setProductId(rs.getLong("product_id"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setProductSerial(rs.getString("product_serial"));

                    int warranty = rs.getInt("warranty_time");
                    dto.setWarrantyMonths(rs.wasNull() ? 0 : warranty);

                    dto.setCustomerName(rs.getString("customer_name"));
                    dto.setCustomerPhone(rs.getString("phone_number"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        dto.setCreatedAt(ts.toLocalDateTime());
                    }

                    list.add(dto);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Thêm mới phiếu bảo hành
     */
    public boolean insert(warrantyCardDTO dto) {
        String sql = "INSERT INTO warranty_card (order_id, product_id, product_serial, return_date, details, status, created_user) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, dto.getOrderId());
            ps.setLong(2, dto.getProductId());
            ps.setString(3, dto.getProductSerial()); // ← mới
            ps.setDate(4, Date.valueOf(dto.getReturnDate()));
            ps.setString(5, dto.getDetails());
            ps.setInt(6, dto.getStatusCode());
            ps.setLong(7, dto.getCreatedUser());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        dto.setWarrantyId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật phiếu bảo hành
     */
    public boolean update(warrantyCardDTO dto) {
        String sql = "UPDATE warranty_card SET product_id = ?, product_serial = ?, return_date = ?, details = ?, status = ? "
                +
                "WHERE warranty_id = ?";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, dto.getProductId());
            ps.setString(2, dto.getProductSerial()); // ← mới
            ps.setDate(3, Date.valueOf(dto.getReturnDate()));
            ps.setString(4, dto.getDetails());
            ps.setInt(5, dto.getStatusCode());
            ps.setLong(6, dto.getWarrantyId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa phiếu bảo hành
     */
    public boolean delete(long warrantyId) {
        String sql = "DELETE FROM warranty_card WHERE warranty_id = ?";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, warrantyId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Map ResultSet thành DTO
     */
    private warrantyCardDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        warrantyCardDTO dto = new warrantyCardDTO();
        dto.setWarrantyId(rs.getLong("warranty_id"));
        dto.setOrderId(rs.getLong("order_id"));
        dto.setProductId(rs.getLong("product_id"));
        dto.setProductSerial(rs.getString("product_serial")); // ← mới
        dto.setProductName(rs.getString("product_name"));

        Timestamp receiveTime = rs.getTimestamp("receive_time");
        if (receiveTime != null) {
            dto.setReceiveTime(receiveTime.toLocalDateTime());
        }

        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            dto.setReturnDate(returnDate.toLocalDate());
        }

        dto.setDetails(rs.getString("details"));
        dto.setStatusCode(rs.getInt("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dto.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            dto.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        dto.setCreatedUser(rs.getLong("created_user"));
        dto.setCreatedUserName(rs.getString("employee_name"));
        dto.setCustomerName(rs.getString("customer_name"));
        dto.setCustomerPhone(rs.getString("phone_number"));

        return dto;
    }

    /**
     * Lấy danh sách tất cả các đơn hàng với thông tin khách hàng
     * Trả về Map<Long, String> với key là order_id, value là "Tên - SDT - Mã đơn
     * #order_id"
     */
    public java.util.LinkedHashMap<Long, String> getAllOrdersWithCustomerInfo() {
        java.util.LinkedHashMap<Long, String> orders = new java.util.LinkedHashMap<>();
        String sql = "SELECT DISTINCT o.order_id, c.customer_name, c.phone_number, o.created_at " +
                "FROM orders o " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "ORDER BY o.created_at DESC";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("order_id");
                    String customerName = rs.getString("customer_name");
                    String phone = rs.getString("phone_number");
                    String display = String.format("%s - %s - #%d", customerName, phone, orderId);
                    orders.put(orderId, display);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Get all orders from database with customer information
     * Format: "#OrderID - CustomerName - Phone (yyyy-MM-dd)"
     */
    public java.util.Map<Long, String> getAllOrdersFromDatabase() {
        java.util.LinkedHashMap<Long, String> orders = new java.util.LinkedHashMap<>();
        String sql = "SELECT o.order_id, c.customer_name, c.phone_number, o.created_at " +
                "FROM orders o " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "ORDER BY o.created_at DESC";

        try (Connection c = JDBCUTIL.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("order_id");
                    String customerName = rs.getString("customer_name");
                    String phone = rs.getString("phone_number");
                    java.sql.Date createdAt = rs.getDate("created_at");
                    String display = String.format("#%d - %s - %s (%s)", orderId, customerName, phone, createdAt);
                    orders.put(orderId, display);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Get orders for a specific customer
     */
    public java.util.Map<Long, String> getOrdersByCustomerInfo(String phone) {
        java.util.LinkedHashMap<Long, String> orders = new java.util.LinkedHashMap<>();

        String sql = "SELECT o.order_id, o.created_at " +
                "FROM orders o " +
                "JOIN customer c ON o.customer_id = c.customer_id " +
                "WHERE c.phone_number = ? " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("order_id");
                    java.sql.Date createdAt = rs.getDate("created_at");

                    String display = String.format("#%d (%s)", orderId, createdAt);
                    orders.put(orderId, display);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public java.util.Map<Long, String> getAllCustomersFromDatabase() {
        java.util.Map<Long, String> customers = new java.util.LinkedHashMap<>();
        String sql = "SELECT customer_id, customer_name, phone_number FROM customer ORDER BY customer_name";

        try (Connection conn = JDBCUTIL.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                long id = rs.getLong("customer_id");
                String name = rs.getString("customer_name");
                String phone = rs.getString("phone_number");
                String display = name + " - " + phone;
                customers.put(id, display);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customers;
    }

    public java.util.Map<Long, String> getAllOrdersWithAllDetails() {
        java.util.Map<Long, String> orders = new java.util.LinkedHashMap<>();
        String sql = "SELECT o.order_id, c.customer_name, c.phone_number, o.created_at " +
                "FROM orders o " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "ORDER BY o.order_id DESC";

        try (Connection conn = JDBCUTIL.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                long orderId = rs.getLong("order_id");
                String customerName = rs.getString("customer_name");
                String phone = rs.getString("phone_number");
                java.sql.Date createdAt = rs.getDate("created_at");
                String display = String.format("#%d - %s - %s (%s)", orderId, customerName, phone, createdAt);
                orders.put(orderId, display);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public WarrantyTempDTO getOld(String serial) {
        WarrantyTempDTO temp = new WarrantyTempDTO();
        String sql = """
                                SELECT
                    c.phone_number AS phone_number,
                    c.customer_name AS customer_name,
                    o.order_id AS order_id,
                    o.created_at as created_at
                FROM order_items od
                JOIN orders o ON od.order_id = o.order_id
                JOIN customer c ON o.customer_id = c.customer_id
                WHERE od.product_serial = ?;
                                """;
        try (Connection con = JDBCUTIL.getConnection()) {
            PreparedStatement psm = con.prepareStatement(sql);
            psm.setString(1, serial);
            try (ResultSet rs = psm.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("order_id");
                    String customerName = rs.getString("customer_name");
                    String phone = rs.getString("phone_number");
                    java.sql.Date createdAt = rs.getDate("created_at");
                    String display = String.format("#%d (%s)", orderId, createdAt);
                    temp = new WarrantyTempDTO(customerName, phone, orderId, createdAt, display);
                    return temp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
