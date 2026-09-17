package DAO;

import DTO.promotionsDTO;
import DTO.promotionItemsDTO;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import Connector.JDBCUTIL;

public class promotionsDAO {
    private Connection conn;

    public promotionsDAO() {
        try {
            this.conn = JDBCUTIL.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<promotionsDTO> getAll() {
        ArrayList<promotionsDTO> list = new ArrayList<>();
        String sql = """
                        SELECT *
                FROM promotions
                WHERE status = 1
                  AND promotion_type <> 'PERCENT'
                  AND end_at > now()
                ORDER BY created_at DESC
                        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                promotionsDTO km = new promotionsDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("name"),
                        rs.getString("promotion_type"),
                        rs.getDouble("min_paid"),
                        rs.getInt("discount_on_bill"),
                        rs.getInt("available_time"),
                        rs.getBoolean("is_auto"),
                        rs.getBoolean("status"),
                        rs.getTimestamp("start_at").toLocalDateTime(),
                        rs.getTimestamp("end_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
                list.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<promotionsDTO> getAll_2() {
        ArrayList<promotionsDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM promotions  ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                promotionsDTO km = new promotionsDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("name"),
                        rs.getString("promotion_type"),
                        rs.getDouble("min_paid"),
                        rs.getInt("discount_on_bill"),
                        rs.getInt("available_time"),
                        rs.getBoolean("is_auto"),
                        rs.getBoolean("status"),
                        rs.getTimestamp("start_at").toLocalDateTime(),
                        rs.getTimestamp("end_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
                list.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<promotionsDTO> search(String keyword) {
        ArrayList<promotionsDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM promotions WHERE name LIKE ?  ORDER BY start_at DESC ";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                promotionsDTO km = new promotionsDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("name"),
                        rs.getString("promotion_type"),
                        rs.getDouble("min_paid"),
                        rs.getInt("discount_on_bill"),
                        rs.getInt("available_time"),
                        rs.getBoolean("is_auto"),
                        rs.getBoolean("status"),
                        rs.getTimestamp("start_at").toLocalDateTime(),
                        rs.getTimestamp("end_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
                list.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<promotionsDTO> filterByDate(LocalDateTime from, LocalDateTime to) {
        ArrayList<promotionsDTO> list = new ArrayList<>();
        // Lọc các khuyến mãi có thời gian giao với khoảng [from, to]
        String sql = "SELECT * FROM promotions WHERE start_at <= ? AND end_at >= ? ORDER BY start_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(to)); // start_at <= to
            stmt.setTimestamp(2, Timestamp.valueOf(from)); // end_at >= from
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                promotionsDTO km = new promotionsDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("name"),
                        rs.getString("promotion_type"),
                        rs.getDouble("min_paid"),
                        rs.getInt("discount_on_bill"),
                        rs.getInt("available_time"),
                        rs.getBoolean("is_auto"),
                        rs.getBoolean("status"),
                        rs.getTimestamp("start_at").toLocalDateTime(),
                        rs.getTimestamp("end_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
                list.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(promotionsDTO km) {
        String sql = "INSERT INTO promotions (name, promotion_type, min_paid, discount_on_bill, "
                + "available_time, is_auto, start_at, end_at, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, km.getName());
            stmt.setString(2, km.getPromotion_type());
            stmt.setDouble(3, km.getMin_paid());
            stmt.setInt(4, km.getDiscount_on_bill());
            stmt.setObject(5, km.getAvailable_time());
            stmt.setBoolean(6, km.isIs_auto());
            stmt.setTimestamp(7, Timestamp.valueOf(km.getStart_at()));
            stmt.setTimestamp(8, Timestamp.valueOf(km.getEnd_at()));
            stmt.setBoolean(9, km.isStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        km.setPromotion_id(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(promotionsDTO km) {
        String sql = "UPDATE promotions SET name=?, promotion_type=?, min_paid=?, discount_on_bill=?, "
                + "available_time=?, is_auto=?, start_at=?, end_at=?, status=? WHERE promotion_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, km.getName());
            stmt.setString(2, km.getPromotion_type());
            stmt.setDouble(3, km.getMin_paid());
            stmt.setInt(4, km.getDiscount_on_bill());
            stmt.setObject(5, km.getAvailable_time());
            stmt.setBoolean(6, km.isIs_auto());
            stmt.setTimestamp(7, Timestamp.valueOf(km.getStart_at()));
            stmt.setTimestamp(8, Timestamp.valueOf(km.getEnd_at()));
            stmt.setBoolean(9, km.isStatus());
            stmt.setInt(10, km.getPromotion_id());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int promotion_id) {
        // Xóa các promotion_items trước
        deletePromotionItems(promotion_id);

        String sql = "DELETE FROM promotions WHERE promotion_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============ Methods for promotion_items ============

    public ArrayList<promotionItemsDTO> getPromotionItems(int promotion_id) {
        ArrayList<promotionItemsDTO> list = new ArrayList<>();
        String sql = "SELECT pi.*, p1.name as product_name, p2.name as gifted_product_name "
                + "FROM promotion_items pi "
                + "LEFT JOIN products p1 ON pi.product_id = p1.product_id "
                + "LEFT JOIN products p2 ON pi.gifted_product_id = p2.product_id "
                + "WHERE pi.promotion_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Long giftedId = rs.getObject("gifted_product_id", Long.class);
                promotionItemsDTO item = new promotionItemsDTO(
                        rs.getInt("promotion_id"),
                        rs.getInt("discount_percent"),
                        rs.getLong("product_id"),
                        giftedId);
                item.setProduct_name(rs.getString("product_name"));
                item.setGifted_product_name(rs.getString("gifted_product_name"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertPromotionItem(promotionItemsDTO item) {
        String sql = "INSERT INTO promotion_items (promotion_id, product_id, gifted_product_id, discount_percent) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getPromotion_id());
            stmt.setLong(2, item.getProduct_id());
            if (item.getGifted_product_id() != null) {
                stmt.setLong(3, item.getGifted_product_id());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setInt(4, item.getDiscount_percent());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePromotionItem(promotionItemsDTO item) {
        String sql = "UPDATE promotion_items SET gifted_product_id=?, discount_percent=? "
                + "WHERE promotion_id=? AND product_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (item.getGifted_product_id() != null) {
                stmt.setLong(1, item.getGifted_product_id());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setInt(2, item.getDiscount_percent());
            stmt.setInt(3, item.getPromotion_id());
            stmt.setLong(4, item.getProduct_id());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePromotionItems(int promotion_id) {
        String sql = "DELETE FROM promotion_items WHERE promotion_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePromotionItem(int promotion_id, long product_id) {
        String sql = "DELETE FROM promotion_items WHERE promotion_id=? AND product_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            stmt.setLong(2, product_id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============ Helper methods ============

    public ArrayList<promotionsDTO> getActivePromotions() {
        ArrayList<promotionsDTO> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        String sql = "SELECT * FROM promotions WHERE status = 1 AND start_at <= ? AND end_at >= ? ORDER BY start_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(now));
            stmt.setTimestamp(2, Timestamp.valueOf(now));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                promotionsDTO km = new promotionsDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("name"),
                        rs.getString("promotion_type"),
                        rs.getDouble("min_paid"),
                        rs.getInt("discount_on_bill"),
                        rs.getInt("available_time"),
                        rs.getBoolean("is_auto"),
                        rs.getBoolean("status"),
                        rs.getTimestamp("start_at").toLocalDateTime(),
                        rs.getTimestamp("end_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
                list.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countPromotionItems(int promotion_id) {
        String sql = "SELECT COUNT(*) as count FROM promotion_items WHERE promotion_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public promotionsDTO getById(int promotion_id) {
        String sql = "SELECT * FROM promotions WHERE promotion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new promotionsDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("name"),
                        rs.getString("promotion_type"),
                        rs.getDouble("min_paid"),
                        rs.getInt("discount_on_bill"),
                        rs.getInt("available_time"),
                        rs.getBoolean("is_auto"),
                        rs.getBoolean("status"),
                        rs.getTimestamp("start_at").toLocalDateTime(),
                        rs.getTimestamp("end_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isProductInActivePromotion(long productId, int excludePromotionId) {
        String sql = "SELECT COUNT(*) FROM promotion_items pi "
                + "JOIN promotions p ON pi.promotion_id = p.promotion_id "
                + "WHERE pi.product_id = ? "
                + "AND p.status = 1 "
                + "AND NOW() BETWEEN p.start_at AND p.end_at "
                + "AND pi.promotion_id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.setInt(2, excludePromotionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
