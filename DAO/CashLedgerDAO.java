package DAO;

import Connector.JDBCUTIL;
import DTO.CashLedgerDTO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CashLedgerDAO {

    public ArrayList<CashLedgerDTO> getTodayTransactions() {

        ArrayList<CashLedgerDTO> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM cash_ledger
            ORDER BY created_at ASC
        """;

        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                CashLedgerDTO dto = new CashLedgerDTO();

                dto.setId(rs.getLong("id"));
                dto.setType(rs.getString("type"));
                dto.setReferenceId(rs.getLong("reference_id"));
                dto.setAmount(rs.getBigDecimal("amount"));
                dto.setDescription(rs.getString("description"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    dto.setCreatedAt(ts.toLocalDateTime());
                }

                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public double getTotalByType(String type) {

        String sql = """
            SELECT SUM(amount)
            FROM cash_ledger
            WHERE type = ?
            AND DATE(created_at) = CURDATE()
        """;

        try (Connection conn = JDBCUTIL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    public boolean insert(CashLedgerDTO dto) {

    String sql = """
        INSERT INTO cash_ledger
        (type, reference_id, amount, description, created_at)
        VALUES (?, ?, ?, ?, NOW())
    """;

    try (Connection conn = JDBCUTIL.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, dto.getType().toString());

        if(dto.getReferenceId() != null){
            ps.setLong(2, dto.getReferenceId());
        }else{
            ps.setNull(2, java.sql.Types.BIGINT);
        }

        ps.setBigDecimal(3, dto.getAmount());
        ps.setString(4, dto.getDescription());

        return ps.executeUpdate() > 0;

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}

}