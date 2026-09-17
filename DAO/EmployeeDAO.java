package DAO;

import DTO.EmployeeDTO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

import Connector.JDBCUTIL;

public class EmployeeDAO {

    private Connection conn;

    public EmployeeDAO() {
        try {
            this.conn = JDBCUTIL.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<EmployeeDTO> getAll() {
        ArrayList<EmployeeDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDTO emp = mapResultSet(rs);
                list.add(emp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<EmployeeDTO> getExcept() {
        ArrayList<EmployeeDTO> list = new ArrayList<>();

        String sql = """
                        SELECT
                    e.employee_id,
                    e.employee_name,
                    e.phone_number
                FROM employees e
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM accounts a
                    WHERE a.employee_id = e.employee_id
                )
                AND e.status = 'ACTIVE';
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO();
                emp.setEmployeeId(rs.getLong("employee_id"));
                emp.setEmployeeName(rs.getString("employee_name"));
                emp.setPhoneNumber(rs.getString("phone_number"));

                list.add(emp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public EmployeeDTO getById(Long id) {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(EmployeeDTO emp) {
        String sql = """
                    INSERT INTO employees(employee_name, phone_number, salary, status)
                    VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeName());
            ps.setString(2, emp.getPhoneNumber());
            ps.setBigDecimal(3, emp.getSalary());
            ps.setString(4, emp.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(EmployeeDTO emp) {
        String sql = """
                    UPDATE employees
                    SET employee_name = ?,
                        phone_number = ?,
                        salary = ?,
                        status = ?
                    WHERE employee_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeName());
            ps.setString(2, emp.getPhoneNumber());
            ps.setBigDecimal(3, emp.getSalary());
            ps.setString(4, emp.getStatus());
            ps.setLong(5, emp.getEmployeeId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "UPDATE employees SET status = 'inactive' WHERE employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<EmployeeDTO> search(String keyword) {
        ArrayList<EmployeeDTO> list = new ArrayList<>();
        String sql = """
                    SELECT * FROM employees
                    WHERE employee_name LIKE ?
                       OR phone_number LIKE ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<EmployeeDTO> getAllEmployeeForCombo() {
        ArrayList<EmployeeDTO> list = new ArrayList<>();
        String SQL_SELECT = "SELECT employee_id, employee_name FROM employees WHERE status = 'ACTIVE'";
        try (
                Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_SELECT);
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                long id = rs.getLong("employee_id");
                String name = rs.getString("employee_name");
                EmployeeDTO e = new EmployeeDTO();
                e.setEmployeeId(id);
                e.setEmployeeName(name);
                list.add(new EmployeeDTO(id, name, null, null, null, null, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ================= MAPPER ================= */
    private EmployeeDTO mapResultSet(ResultSet rs) throws SQLException {
        EmployeeDTO emp = new EmployeeDTO();

        emp.setEmployeeId(rs.getLong("employee_id"));
        emp.setEmployeeName(rs.getString("employee_name"));
        emp.setPhoneNumber(rs.getString("phone_number"));
        emp.setSalary(rs.getBigDecimal("salary"));
        emp.setStatus(rs.getString("status"));
        emp.setCreatedAt(rs.getTimestamp("created_at"));
        emp.setUpdatedAt(rs.getTimestamp("updated_at"));

        return emp;
    }

    public String getEmployeeNameById(int id) {
        String sql = "SELECT employee_name FROM employees WHERE employee_id = ?";

        try (Connection con = JDBCUTIL.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("employee_name");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
