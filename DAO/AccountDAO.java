package DAO;

import DTO.AccountDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Connector.JDBCUTIL;

public class AccountDAO {
    public AccountDAO() {

    };

    public AccountDTO findActiveAccountByUsername(String username) {

        String sql = """
                    SELECT employee_id, username, password,role_id
                    FROM accounts
                    WHERE username = ? AND status = '1'
                """;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setRole_id(rs.getInt("role_id"));
                accountDTO.setEmployeeId(rs.getLong("employee_id"));
                accountDTO.setUsername(rs.getString("username"));
                accountDTO.setPassword(rs.getString("password"));
                return accountDTO;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getEmployeeAndRoleName(String username) {

        Map<String, String> result = new HashMap<>();
        String sql = """
                    SELECT
                        e.employee_name,
                        r.name AS role_name
                    FROM accounts a
                    JOIN employees e ON a.employee_id = e.employee_id
                    JOIN roles r ON a.role_id = r.role_id
                    WHERE a.username = ?
                      AND a.status = '1'
                      AND e.status = 'ACTIVE'
                """;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result.put(rs.getString("employee_name"), rs.getString("role_name"));
                JDBCUTIL.closeConnection(conn);
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<AccountDTO> getAll() {
        ArrayList<AccountDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts where status = 1";

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAccount(rs));
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    public long insert(AccountDTO acc) {

        String sql = """
                    INSERT INTO accounts
                    (employee_id, username, password, role_id, status)
                    VALUES (?, ?, ?, ?, '1')
                """;

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, acc.getEmployeeId());
            ps.setString(2, acc.getUsername());
            ps.setString(3, acc.getPassword());
            ps.setInt(4, acc.getRole_id());

            int affected = ps.executeUpdate();
            if (affected == 0)
                return -1;

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                acc.setId(id);
                return id;
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public AccountDTO selectById(long accountId) {

        String sql = "SELECT * FROM accounts WHERE account_id = ? and status =1";

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private AccountDTO mapResultSetToAccount(ResultSet rs) throws SQLException {

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new AccountDTO(
                rs.getLong("account_id"),
                rs.getLong("employee_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("role_id"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null,
                rs.getString("status"));
    }

    public boolean update(AccountDTO acc) {

        StringBuilder sql = new StringBuilder("""
                    UPDATE accounts
                    SET employee_id = ?,
                        role_id = ?,
                        updated_at = CURRENT_TIMESTAMP
                """);

        if (acc.getPassword() != null) {
            sql.append(", password = ?");
        }

        sql.append("""
                    WHERE account_id = ?
                      AND status = '1'
                """);

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, acc.getEmployeeId());
            ps.setInt(idx++, acc.getRole_id());

            if (acc.getPassword() != null) {
                ps.setString(idx++, acc.getPassword());
            }

            ps.setLong(idx, acc.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(long accountId) {

        String sql = """
                    UPDATE accounts
                    SET
                        status = '0',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE account_id = ?
                """;

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountId);
            return ps.executeUpdate() > 0;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public AccountDTO findByUsername(String username) {

        String sql = """
                    SELECT *
                    FROM accounts
                    WHERE username = ?
                      AND status = '1'
                    LIMIT 1
                """;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AccountDTO findByUsername_2(String username) {

        String sql = """
                    SELECT *
                    FROM accounts
                    WHERE username = ?
                    LIMIT 1
                """;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
