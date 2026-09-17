package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import Connector.JDBCUTIL;
import DTO.nhaCungCapDTO;

public class nhaCungCapDAO {
    private Connection conn;

    public nhaCungCapDAO() {
        try {
            this.conn = JDBCUTIL.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<nhaCungCapDTO> getAll() {
        ArrayList<nhaCungCapDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                nhaCungCapDTO ncc = new nhaCungCapDTO(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_code"),
                        rs.getString("name"),
                        rs.getString("phone_number"),
                        rs.getString("address"));
                list.add(ncc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<nhaCungCapDTO> search(String keyword) {
        ArrayList<nhaCungCapDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE name LIKE ? OR phone_number LIKE ? OR address LIKE ? OR supplier_code LIKE ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchValue = "%" + keyword + "%";
            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);
            stmt.setString(3, searchValue);
            stmt.setString(4, searchValue);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                nhaCungCapDTO ncc = new nhaCungCapDTO(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_code"),
                        rs.getString("name"),
                        rs.getString("phone_number"),
                        rs.getString("address"));
                list.add(ncc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean insert(nhaCungCapDTO ncc) {
        String sql = "INSERT INTO suppliers (supplier_code, name, phone_number, address) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ncc.getCode());
            stmt.setString(2, ncc.getName());
            stmt.setString(3, ncc.getPhone_number());
            stmt.setString(4, ncc.getAddress());

            int rowAffected = stmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(nhaCungCapDTO ncc) {
        String sql = "UPDATE suppliers SET supplier_code = ?, name = ?, phone_number = ?, address = ? WHERE supplier_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ncc.getCode());
            stmt.setString(2, ncc.getName());
            stmt.setString(3, ncc.getPhone_number());
            stmt.setString(4, ncc.getAddress());
            stmt.setInt(5, ncc.getSupplier_id());

            int rowAffected = stmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int supplier_id) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplier_id);
            int rowAffected = stmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSupplierInUse(int supplier_id) {
        String sql = "SELECT COUNT(*) FROM receipts WHERE supplier_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplier_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            // Kiểm tra thêm bảng supplier_products nếu cần
            sql = "SELECT COUNT(*) FROM supplier_products WHERE supplier_id = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                stmt2.setInt(1, supplier_id);
                rs = stmt2.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSupplierCodeDuplicate(String code) {
    String sql = "SELECT COUNT(*) FROM suppliers WHERE supplier_code = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, code);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
    }

    public boolean isPhoneNumberExist(String phone) {
    String sql = "SELECT COUNT(*) FROM suppliers WHERE phone_number = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, phone);
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

