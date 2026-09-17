package DAO;

import DTO.CategoryDTO;

import java.sql.*;
import java.util.ArrayList;

import Connector.JDBCUTIL;

public class CategoryDAO {
    private Connection conn;

    public CategoryDAO() {
        try {
            this.conn = JDBCUTIL.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<CategoryDTO> getAll() {
        ArrayList<CategoryDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                CategoryDTO category = new CategoryDTO(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("category_code"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                list.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public CategoryDTO getByID(int category_id) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, category_id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new CategoryDTO(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("category_code"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<CategoryDTO> search(String keyword) {
        ArrayList<CategoryDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE name LIKE ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchValue = "%" + keyword + "%";
            stmt.setString(1, searchValue);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CategoryDTO category = new CategoryDTO(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("category_code"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                list.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean insert(CategoryDTO category) {
        String sql = "INSERT INTO categories (name, category_code) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getCode());

            int rowAffected = stmt.executeUpdate();
            if (rowAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    category.setCategory_id(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(CategoryDTO category) {
        String sql = "UPDATE categories SET name = ?, category_code = ? WHERE category_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getCode());
            stmt.setInt(3, category.getCategory_id());

            int rowAffected = stmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int category_id) {
        if (isCategoryInUse(category_id)) {
            return false;
        }

        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, category_id);
            int rowAffected = stmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isCategoryInUse(int category_id) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, category_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String[] getCategoryNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("Tất cả"); // Thêm mặc định
        String sql = "SELECT name FROM categories";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names.toArray(new String[0]);
    }

    public ArrayList<CategoryDTO> getCategoriesForComboBox() {
        return getAll();
    }

    public ArrayList<CategoryDTO> searchByCategoryAndKeyword(int categoryId, String keyword) {
        ArrayList<CategoryDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE category_id = ? AND name LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CategoryDTO category = new CategoryDTO(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("category_code"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                list.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}