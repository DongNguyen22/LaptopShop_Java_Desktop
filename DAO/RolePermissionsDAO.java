package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import Connector.JDBCUTIL;
import DTO.RolePermissionsDTO;

public class RolePermissionsDAO {

    public RolePermissionsDAO() {
    }

    public ArrayList<RolePermissionsDTO> getAll(int role_id) {
        ArrayList<RolePermissionsDTO> list = new ArrayList<>();

        String sql = """
                        SELECT
                    m.id   AS module_id,
                    m.name AS module_name,

                    MAX(CASE WHEN a.name = 'VIEW'   THEN ma.id END) AS view_id,
                    MAX(CASE WHEN a.name = 'VIEW'   THEN r.status END) AS view_status,

                    MAX(CASE WHEN a.name = 'ADD'    THEN ma.id END) AS add_id,
                    MAX(CASE WHEN a.name = 'ADD'    THEN r.status END) AS add_status,

                    MAX(CASE WHEN a.name = 'UPDATE'   THEN ma.id END) AS edit_id,
                    MAX(CASE WHEN a.name = 'UPDATE'   THEN r.status END) AS edit_status,

                    MAX(CASE WHEN a.name = 'DELETE' THEN ma.id END) AS delete_id,
                    MAX(CASE WHEN a.name = 'DELETE' THEN r.status END) AS delete_status

                FROM rbac r
                JOIN module_actions ma ON r.module_action_id = ma.id
                JOIN modules m ON ma.module_id = m.id
                JOIN actions a ON ma.action_id = a.id
                WHERE r.role_id = ?
                GROUP BY m.id, m.name
                ORDER BY m.id;

                    """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, role_id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RolePermissionsDTO dto = new RolePermissionsDTO();

                    dto.setModuleId(rs.getInt("module_id"));
                    dto.setModuleName(rs.getString("module_name"));

                    dto.setViewId(rs.getInt("view_id"));
                    dto.setView(rs.getBoolean("view_status"));

                    dto.setAddId(rs.getInt("add_id"));
                    dto.setAdd(rs.getBoolean("add_status"));

                    dto.setEditId(rs.getInt("edit_id"));
                    dto.setEdit(rs.getBoolean("edit_status"));

                    dto.setDeleteId(rs.getInt("delete_id"));
                    dto.setDelete(rs.getBoolean("delete_status"));

                    list.add(dto);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateStatus(int roleId, int moduleActionId, boolean status) {

        String sql = """
                    UPDATE rbac
                    SET status = ?, updated_at = NOW()
                    WHERE role_id = ? AND module_action_id = ?
                """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, status);
            ps.setInt(2, roleId);
            ps.setInt(3, moduleActionId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
