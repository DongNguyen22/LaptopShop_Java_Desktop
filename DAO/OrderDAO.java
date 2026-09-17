package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Connector.JDBCUTIL;
import DTO.NewOrderDTO;
import DTO.OrderDTO;
import DTO.OrderDetailDTO;
import DTO.OrderItemDTO;
import DTO.OrderDetailDTO;

public class OrderDAO {

    private String UPDATE_ORDER_SQL = "UPDATE orders SET customer_id = ?, promotion_id = ?, total = ?, payment_method = ?, created_user = ? WHERE order_id = ?";

    private String INSERT_ORDER_ITEM_SQL = "INSERT INTO order_items (order_id, product_id,receipt_id,original_price, unit_price, product_serial) "
            +
            "VALUES (?, ?, ?,?, ?, ?)";

    private String INSERT_CASH_LEDGER_SQL = "INSERT INTO cash_ledger (type, reference_id, amount, description) "
            +
            "VALUES ('SALE', ?, ?, ?)";
    private String UPDATE_STOCK_PRODUCT = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
    private String FIND_ALL_DETAIL = """
                            SELECT
                o.order_id,
                o.total,
                p.product_id,
                p.name AS product_name,
                oi.original_price,
                oi.unit_price,
                o.created_at,
                e.employee_id,
                e.employee_name,
                c.customer_id,
                c.customer_name,
                o.promotion_id,
                pr.name AS promotion_name,
                ri.warranty_time,
                oi.product_serial AS serial,
                o.payment_method
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.order_id
            JOIN products p ON p.product_id = oi.product_id
            LEFT JOIN customer c ON c.customer_id = o.customer_id
            LEFT JOIN promotions pr ON pr.promotion_id = o.promotion_id
            LEFT JOIN employees e ON e.employee_id = o.created_user
            LEFT JOIN receipt_items ri ON ri.receipt_item_id = oi.receipt_id

            WHERE o.order_id = ?;
                            """;
    private String UPDATE_SERIAL_STATUS = "UPDATE item_serials SET is_used = 1 WHERE serial = ?";
    private String update_stock = """
                UPDATE products
            SET stock = stock - ?
            WHERE product_id = ? AND stock >= ?
            """;
    private SerialDAO serialDAO = new SerialDAO();

    public Long createEmptyOrder() {

        String sql = "insert into orders(total) values(0);";

        Long orderId = null;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getLong(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orderId;
    }

    public boolean deleteOrder(Long orderId) {
        String sqlOrder = " DELETE FROM orders WHERE order_id = ?";

        try (
                Connection conn = JDBCUTIL.getConnection()) {

            conn.setAutoCommit(false);

            try (
                    PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {
                psOrder.setLong(1, orderId);
                psOrder.executeUpdate();

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Lưu hóa đơn + chi tiết + cash ledger (TRANSACTION)
     */
    public boolean insert(OrderDTO order, ArrayList<OrderItemDTO> listCTHD) {
        Connection conn = null;

        try {
            conn = JDBCUTIL.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(UPDATE_ORDER_SQL)) {
                ps.setLong(6, order.getOrder_id());
                ps.setLong(1, order.getCustomer_id());
                if (order.getPromotion_id() != null) {
                    ps.setInt(2, order.getPromotion_id());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setBigDecimal(3, order.getTotal());
                ps.setString(4, order.getPayment_method());
                ps.setLong(5, order.getCreated_user());
                ps.executeUpdate();
            }

            try (PreparedStatement psItem = conn.prepareStatement(INSERT_ORDER_ITEM_SQL)) {

                for (OrderItemDTO item : listCTHD) {
                    psItem.setLong(1, item.getOrderId());
                    psItem.setLong(2, item.getProductId());
                    psItem.setLong(3, item.getReceiptId());
                    psItem.setBigDecimal(4, item.getOriginal_price());
                    psItem.setBigDecimal(5, item.getUnitPrice());
                    psItem.setString(6, item.getProductSerial());
                    psItem.addBatch();
                }

                psItem.executeBatch();
                try (PreparedStatement psSerial = conn.prepareStatement(UPDATE_SERIAL_STATUS)) {

                    for (OrderItemDTO item : listCTHD) {
                        psSerial.setString(1, item.getProductSerial());
                        psSerial.addBatch();
                    }

                    psSerial.executeBatch();
                }
            }
            Map<Long, Integer> productCount = new HashMap<>();

            for (OrderItemDTO item : listCTHD) {
                productCount.put(
                        item.getProductId(),
                        productCount.getOrDefault(item.getProductId(), 0) + 1);
            }
            try (PreparedStatement psStock = conn.prepareStatement(update_stock)) {

                for (Map.Entry<Long, Integer> entry : productCount.entrySet()) {
                    Long productId = entry.getKey();
                    Integer quantity = entry.getValue();

                    psStock.setInt(1, quantity);
                    psStock.setLong(2, productId);
                    psStock.setInt(3, quantity);

                    int affected = psStock.executeUpdate();

                    if (affected == 0) {
                        throw new RuntimeException("Sản phẩm ID " + productId + " không đủ hàng!");
                    }
                }
            }

            try (PreparedStatement psCash = conn.prepareStatement(INSERT_CASH_LEDGER_SQL)) {

                psCash.setLong(1, order.getOrder_id());
                psCash.setBigDecimal(2, order.getTotal());
                psCash.setString(3, "Bán hàng - Order #" + order.getOrder_id());
                psCash.executeUpdate();
            }
            conn.commit(); // COMMIT
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null)
                    conn.rollback(); // ROLLBACK
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<OrderDTO> findAll() {
        ArrayList<OrderDTO> list = new ArrayList<>();

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM orders ORDER BY created_at DESC");
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                OrderDTO o = new OrderDTO();
                o.setOrder_id(rs.getLong("order_id"));
                o.setCustomer_id(rs.getLong("customer_id"));
                o.setPromotion_id((Integer) rs.getObject("promotion_id"));
                o.setTotal(rs.getBigDecimal("total"));
                o.setPayment_method(rs.getString("payment_method"));
                o.setCreated_user(rs.getLong("created_user"));
                o.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(o);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public NewOrderDTO findOrder(Long orderID) {
        NewOrderDTO o = null;

        String sql = """
                    SELECT
                        o.order_id,
                        o.total,
                        c.customer_id as customer_id,
                        c.customer_name AS customer_name,
                        e.employee_id as employee_id,
                        e.employee_name AS employee_name,
                        p.promotion_id as promotion_id,
                        COALESCE(p.name, 'Không có') AS promotion_name,
                        o.created_at,
                        o.payment_method
                    FROM orders o
                    JOIN customer c ON o.customer_id = c.customer_id
                    JOIN employees e ON o.created_user = e.employee_id
                    LEFT JOIN promotions p ON o.promotion_id = p.promotion_id
                    WHERE o.order_id = ?
                """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // set tham số
            ps.setLong(1, orderID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    o = new NewOrderDTO();
                    o.setOrderId(rs.getLong("order_id"));
                    o.setTotal(rs.getBigDecimal("total"));
                    o.setCustomerName(rs.getString("customer_name"));
                    o.setEmployeeName(rs.getString("employee_name"));
                    o.setPromotionName(rs.getString("promotion_name")); // đã COALESCE
                    o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    o.setPaymentMethod(rs.getString("payment_method"));
                    o.setCustomer_id(rs.getLong("customer_id"));
                    o.setEmployee_id(rs.getLong("employee_id"));
                    int promoId = rs.getInt("promotion_id");
                    if (rs.wasNull()) {
                        o.setPromotionID(null);
                    } else {
                        o.setPromotionID(promoId);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }

    public ArrayList<OrderDetailDTO> getByOrderId(long orderId) {
        ArrayList<OrderDetailDTO> list = new ArrayList<>();

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(FIND_ALL_DETAIL)) {

            ps.setLong(1, orderId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderDetailDTO dto = new OrderDetailDTO();
                dto.setOrderId(rs.getLong("order_id"));
                dto.setTotal(rs.getBigDecimal("total"));
                dto.setProductCode(rs.getLong("product_id"));
                dto.setCustomerCode(rs.getLong("customer_id"));
                dto.setProductName(rs.getString("product_name"));
                dto.setOriginalPrice(rs.getBigDecimal("original_price"));
                dto.setUnitPrice(rs.getBigDecimal("unit_price"));
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                dto.setEmployeeId(rs.getLong("employee_id"));
                dto.setEmployeeName(rs.getString("employee_name"));
                dto.setCustomerName(rs.getString("customer_name"));
                dto.setPromotionId(rs.getObject("promotion_id") != null ? rs.getInt("promotion_id") : null);
                dto.setPromotionName(rs.getString("promotion_name"));
                dto.setWarrantyTime(rs.getObject("warranty_time") != null ? rs.getInt("warranty_time") : null);
                dto.setSerial(rs.getString("serial"));
                dto.setPaymentMethod(rs.getString("payment_method"));

                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public BigDecimal getOrderTotalByOrderId(Long orderId) {

        BigDecimal total = BigDecimal.ZERO;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT total FROM orders WHERE order_id = ?")) {

            ps.setLong(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getBigDecimal("total");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return total;
    }

    public ArrayList<OrderItemDTO> getAllOrderItem(Long order_id) {
        ArrayList<OrderItemDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, order_id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderItemDTO item = new OrderItemDTO(
                        rs.getLong("order_id"),
                        rs.getLong("product_id"),
                        rs.getBigDecimal("original_price"),
                        rs.getBigDecimal("unit_price"),
                        rs.getLong("receipt_id"),
                        rs.getString("product_serial"));
                list.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<NewOrderDTO> getAllNew() {
        ArrayList<NewOrderDTO> list = new ArrayList<>();

        String sql = """
                select
                                o.order_id,
                            o.total,
                            c.customer_id as customer_id,
                            c.customer_name AS customer_name,
                            e.employee_id as employee_id,
                            e.employee_name AS employee_name,
                            p.promotion_id as promotion_id,
                            COALESCE(p.name, 'Không có') AS promotion_name,
                            o.created_at,
                            o.payment_method
                        FROM orders o
                        JOIN customer c ON o.customer_id = c.customer_id
                        JOIN employees e ON o.created_user = e.employee_id
                        LEFT JOIN promotions p ON o.promotion_id = p.promotion_id
                            """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NewOrderDTO o = new NewOrderDTO();

                o = new NewOrderDTO();
                o.setOrderId(rs.getLong("order_id"));
                o.setTotal(rs.getBigDecimal("total"));
                o.setCustomerName(rs.getString("customer_name"));
                o.setEmployeeName(rs.getString("employee_name"));
                o.setPromotionName(rs.getString("promotion_name")); // đã COALESCE
                o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setCustomer_id(rs.getLong("customer_id"));
                o.setEmployee_id(rs.getLong("employee_id"));
                int promoId = rs.getInt("promotion_id");
                if (rs.wasNull()) {
                    o.setPromotionID(null);
                } else {
                    o.setPromotionID(promoId);
                }

                list.add(o);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean setAutoIncrement(Long value) {
    String sql = "ALTER TABLE orders AUTO_INCREMENT = ?";

    try (Connection conn = JDBCUTIL.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, value);
        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    } catch (ClassNotFoundException e1) {
        e1.printStackTrace();
    }
    return false;
}
}
