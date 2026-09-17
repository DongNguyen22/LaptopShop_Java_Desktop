package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;

import Connector.JDBCUTIL;
import DTO.sanPham;

public class SanPhamDAO {
    public java.util.List<sanPham> getAll() {
        java.util.List<sanPham> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM products p JOIN categories c ON p.category_id = c.category_id JOIN vw_product_sell_price s ON p.product_id = s.product_id";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql);
                var rs = ps.executeQuery()) {

            while (rs.next()) {
                sanPham sp = new sanPham();
                sp.setMaSP(rs.getString("p.product_id"));
                sp.setCode(rs.getString("p.product_code"));
                sp.setTenSP(rs.getString("p.name"));
                sp.setTenDanhmuc(rs.getString("c.name"));
                sp.setLoiNhuan(rs.getDouble("p.profit"));
                sp.setSoLuong(rs.getInt("p.stock"));
                sp.setDonGia(rs.getDouble("p.sell_price"));
                sp.setActive(rs.getBoolean("p.is_active"));
                sp.setDanhMucId(rs.getInt("p.category_id"));
                sp.setDiscountPercent(rs.getDouble("s.discount_percent"));
                sp.setSellPrice(rs.getDouble("s.sell_price"));
                sp.setNgayTao(
                        rs.getTimestamp("p.created_at") != null ? rs.getTimestamp("p.created_at").toLocalDateTime()
                                : null);
                sp.setNgayCapNhat(
                        rs.getTimestamp("p.updated_at") != null ? rs.getTimestamp("p.updated_at").toLocalDateTime()
                                : null);
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.List<sanPham> getActiveProducts() {
        java.util.List<sanPham> list = new java.util.ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.sell_price as suggested_price, s.discount_percent " +
                "FROM products p " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN vw_product_sell_price s ON p.product_id = s.product_id " +
                "WHERE p.is_active = 1";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql);
                var rs = ps.executeQuery()) {

            while (rs.next()) {
                sanPham sp = new sanPham();
                sp.setMaSP(rs.getString("product_id")); // bỏ "p."
                sp.setCode(rs.getString("product_code"));
                sp.setTenSP(rs.getString("name")); // bỏ "p."
                sp.setTenDanhmuc(rs.getString("category_name")); // giữ nguyên (alias)
                sp.setLoiNhuan(rs.getDouble("profit")); // bỏ "p."
                sp.setSoLuong(rs.getInt("stock")); // bỏ "p."
                sp.setGiaNhapTb(rs.getDouble("avg_unit_price")); // bỏ "p."
                sp.setDonGia(rs.getDouble("sell_price")); // bỏ "p."
                sp.setActive(rs.getBoolean("is_active")); // bỏ "p."
                sp.setDanhMucId(rs.getInt("category_id")); // bỏ "p."
                sp.setDiscountPercent(rs.getDouble("discount_percent")); // từ view
                sp.setSellPrice(rs.getDouble("suggested_price")); // từ view
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getProductImages(String productId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT image_url FROM product_images WHERE product_id = ?";
        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, Long.parseLong(productId));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("image_url"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addProductImage(String productId, String imageUrl) {
        String sql = "INSERT INTO product_images (product_id, image_url) VALUES (?, ?)";
        try (Connection c = JDBCUTIL.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, Long.parseLong(productId));
            ps.setString(2, imageUrl);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Xóa ảnh theo ID
    public void deleteProductImage(String productId, String imageUrl) {
        String sql = "DELETE FROM product_images WHERE product_id = ? AND image_url = ?";
        try (Connection conn = JDBCUTIL.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, Long.parseLong(productId));
            ps.setString(2, imageUrl);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm helper lấy MIME type
    private String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        return "application/octet-stream"; // mặc định
    }

    public sanPham getById(String maSP) {
        String sql = "SELECT p.*, c.name as category_name, s.sell_price as suggested_price, s.discount_percent FROM products p JOIN categories c ON p.category_id = c.category_id JOIN vw_product_sell_price s ON p.product_id = s.product_id WHERE p.product_id = ?";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, maSP);
            var rs = ps.executeQuery();

            if (rs.next()) {
                sanPham sp = new sanPham();
                sp.setMaSP(rs.getString("product_id"));
                sp.setCode(rs.getString("product_code"));
                sp.setTenSP(rs.getString("name"));
                sp.setTenDanhmuc(rs.getString("category_name"));
                sp.setDanhMucId(rs.getInt("category_id"));
                sp.setLoiNhuan(rs.getDouble("profit"));
                sp.setSoLuong(rs.getInt("stock"));
                sp.setGiaNhapTb(rs.getDouble("p.avg_unit_price"));
                sp.setDonGia(rs.getDouble("p.sell_price"));
                sp.setActive(rs.getBoolean("is_active"));
                sp.setDiscountPercent(rs.getDouble("discount_percent"));
                sp.setSellPrice(rs.getDouble("suggested_price"));
                sp.setNgayTao(
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                sp.setNgayCapNhat(
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                return sp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(sanPham sp) {
        String sql = "INSERT INTO products(product_code, name, category_id, profit) VALUES (?,?,?,?)";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, sp.getCode());
            ps.setString(2, sp.getTenSP());
            ps.setInt(3, sp.getDanhMucId());
            ps.setDouble(4, sp.getLoiNhuan());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(sanPham sp) {
        String sql = "UPDATE products SET product_code = ?, name = ?, category_id = ?, profit = ?, sell_price = ? WHERE product_id = ?";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, sp.getCode());
            ps.setString(2, sp.getTenSP());
            ps.setInt(3, sp.getDanhMucId());
            ps.setDouble(4, sp.getLoiNhuan());
            ps.setDouble(5, sp.getDonGia());
            ps.setString(6, sp.getMaSP());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean delete(String maSP) {
        String sqlCheck = "SELECT COUNT(*) as cnt FROM receipt_items WHERE product_id = ?";
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sqlCheck)) {

            // Check if product has receipts
            ps.setString(1, maSP);
            var rs = ps.executeQuery();
            if (rs.next() && rs.getInt("cnt") > 0) {
                return false; // Cannot delete - has receipts
            }

            // Delete product
            try (var ps2 = c.prepareStatement(sql)) {
                ps2.setString(1, maSP);
                ps2.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateActive(String maSP, boolean isActive) {
        String sql = "UPDATE products SET is_active = ? WHERE product_id = ?";

        try (var c = JDBCUTIL.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setString(2, maSP);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCategoryExistsByCode(String categoryCode) {
        String sql = "SELECT COUNT(*) as cnt FROM categories WHERE category_code = ?";
        try (var c = JDBCUTIL.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, categoryCode);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getCategoryIdByCode(String categoryCode) {
        String sql = "SELECT category_id FROM categories WHERE category_code = ?";
        try (var c = JDBCUTIL.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, categoryCode);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}