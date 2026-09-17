package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Connector.JDBCUTIL;
import DTO.RoleDTO;

public class RoleDAO {

    // Lấy tất cả role
    public ArrayList<RoleDTO> getAllRoles() {
        ArrayList<RoleDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_id";

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RoleDTO role = new RoleDTO();
                role.setRoleId(rs.getInt("role_id"));
                role.setName(rs.getString("name"));
                role.setCreatedAt(rs.getTimestamp("created_at"));
                role.setUpdatedAt(rs.getTimestamp("updated_at"));

                list.add(role);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm role
    public int insert(String roleName) {
        String insertRoleSQL = """
                    INSERT INTO roles(name, created_at)
                    VALUES (?, NOW())
                """;

        String insertRBACSQL = """
                    INSERT INTO rbac(role_id, module_action_id, status)
                    SELECT ?, id, 0
                    FROM module_actions
                """;

        try (Connection con = JDBCUTIL.getConnection()) {
            con.setAutoCommit(false);

            // 1. insert role
            PreparedStatement psRole = con.prepareStatement(
                    insertRoleSQL, Statement.RETURN_GENERATED_KEYS);
            psRole.setString(1, roleName);
            psRole.executeUpdate();

            ResultSet rs = psRole.getGeneratedKeys();
            if (!rs.next()) {
                con.rollback();
                return -1;
            }

            int roleId = rs.getInt(1);

            // 2. insert rbac
            PreparedStatement psRBAC = con.prepareStatement(insertRBACSQL);
            psRBAC.setInt(1, roleId);
            psRBAC.executeUpdate();

            con.commit();
            return roleId;

        } catch (SQLIntegrityConstraintViolationException e) {
            // role trùng
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Cập nhật role
    public boolean updateRole(RoleDTO role) {
        String sql = """
                    UPDATE roles
                    SET name = ?, updated_at = NOW()
                    WHERE role_id = ?
                """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.getName());
            ps.setInt(2, role.getRoleId());

            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra trùng tên
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM roles WHERE name = ?";
        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public RoleDTO getById(int roleId) {
        String sql = """
                    SELECT role_id, name, created_at, updated_at
                    FROM roles
                    WHERE role_id = ?
                """;

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RoleDTO role = new RoleDTO();
                role.setRoleId(rs.getInt("role_id"));
                role.setName(rs.getString("name"));
                role.setCreatedAt(rs.getTimestamp("created_at"));
                role.setUpdatedAt(rs.getTimestamp("updated_at"));
                return role;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isRoleUsed(int roleId) {
        String sql = "SELECT 1 FROM accounts WHERE role_id = ? LIMIT 1";

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean deleteRole(int roleId) {
        String deleteRBAC = "DELETE FROM rbac WHERE role_id = ?";
        String deleteRole = "DELETE FROM roles WHERE role_id = ?";

        try (Connection con = JDBCUTIL.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps1 = con.prepareStatement(deleteRBAC);
                    PreparedStatement ps2 = con.prepareStatement(deleteRole)) {

                ps1.setInt(1, roleId);
                ps1.executeUpdate();

                ps2.setInt(1, roleId);
                int affected = ps2.executeUpdate();

                con.commit();
                return affected > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public ArrayList<RoleDTO> getAllRoleForCombo(){
        ArrayList<RoleDTO> list = new ArrayList<>();
        String sql = "SELECT role_id,name FROM roles ORDER BY role_id";

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RoleDTO role = new RoleDTO();
                role.setRoleId(rs.getInt("role_id"));
                role.setName(rs.getString("name"));
                role.setCreatedAt(null);
                role.setUpdatedAt(null);
                list.add(role);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

}
