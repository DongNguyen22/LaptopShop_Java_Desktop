package DAO;

import DTO.CustomerDTO;
import Connector.JDBCUTIL;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomerDAO {

    public CustomerDAO() {
    }

    // GENERATE ID
    public Long createEmptyOrder() {

        String sql = "insert into customer(customer_name) values('a');";

        Long customer_id = null;

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                customer_id = rs.getLong(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return customer_id;
    }

    public boolean deleteCustomer(long customerId) {

        String sql = "DELETE FROM customer WHERE customer_id = ?";
        String resetAI = "ALTER TABLE customer AUTO_INCREMENT = ? ";

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            boolean deleted = ps.executeUpdate() > 0;

            if (deleted) {
                try (PreparedStatement ps2 = conn.prepareStatement(resetAI)) {
                    ps2.setLong(1, customerId);
                    ps2.executeUpdate();
                }
            }

            return deleted;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // update new khách hàng
    public boolean insert(CustomerDTO customer) {
        String sql = """
                UPDATE customer
                SET customer_name = ?,
                    phone_number = ?
                WHERE customer_id = ?
                """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhoneNumber());
            ps.setLong(3, customer.getCustomerId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Lấy mã khách hàng theo số điện thoại
    public CustomerDTO getOldCustomer(String phoneNumber) {
        String sql = "SELECT * FROM customer WHERE phone_number = ?";
        CustomerDTO result = new CustomerDTO();
        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phoneNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result = new CustomerDTO(rs.getLong("customer_id"), rs.getString("customer_name"), phoneNumber, null, null);
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<CustomerDTO> getAll() {

        ArrayList<CustomerDTO> list = new ArrayList<>();

        String sql = """
                    SELECT
                        customer_id,
                        customer_name,
                        phone_number,
                        created_at,
                        updated_at
                    FROM customer
                    ORDER BY customer_id DESC
                """;

        try (Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                CustomerDTO c = new CustomerDTO();

                c.setCustomerId(rs.getLong("customer_id"));
                c.setCustomerName(rs.getString("customer_name"));
                c.setPhoneNumber(rs.getString("phone_number"));
                c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
