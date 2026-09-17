package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Connector.JDBCUTIL;

public class PermissionDAO {

    public PermissionDAO() {
    }

    public Map<String, Set<String>> getAllPermission(int role_id) {
        Map<String, Set<String>> list = new HashMap<>();
        String sql = """
                         SELECT
                    m.code  AS module_name,
                    a.name  AS action_name,
                    r.status
                FROM rbac r
                JOIN module_actions ma ON r.module_action_id = ma.id
                JOIN modules m ON ma.module_id = m.id
                JOIN actions a ON ma.action_id = a.id
                WHERE r.role_id = ?
                  AND r.status = 1
                  order by module_name asc;
                  """;
        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, role_id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String module = rs.getString("module_name").trim();
                    String action = rs.getString("action_name").trim();
                    list
                            .computeIfAbsent(module, k -> new HashSet<>())
                            .add(action);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
