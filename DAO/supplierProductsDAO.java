package DAO;

import Connector.JDBCUTIL;
import DTO.supplierProductsDTO;
import java.sql.*;
import java.util.ArrayList;

public class supplierProductsDAO {

    public ArrayList<supplierProductsDTO> getBySupplierId(int supplierId) {
        ArrayList<supplierProductsDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM supplier_products WHERE supplier_id = ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                supplierProductsDTO sp = new supplierProductsDTO();
                sp.setSupplier_product_id(rs.getInt("supplier_product_id"));
                sp.setSupplier_id(rs.getInt("supplier_id"));
                sp.setProduct_id(rs.getInt("product_id"));
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(int supplierId, int productId) {
        String sql = "INSERT INTO supplier_products (supplier_id, product_id) VALUES (?, ?)";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int supplierId, int productId) {
        String sql = "DELETE FROM supplier_products WHERE supplier_id = ? AND product_id = ?";
        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}