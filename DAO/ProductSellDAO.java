package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import Connector.JDBCUTIL;
import DTO.ProductSellDTO;

public class ProductSellDAO {

    private String GET_ALL_SQL = """
                            SELECT
                p.product_id,
                p.name,
                p.stock,

                v.product_unit_price AS unit_price,
                v.sell_price AS sell_price

            FROM products p

            LEFT JOIN vw_product_sell_price v
                ON p.product_id = v.product_id

            WHERE
                (
                    p.is_active = 1
                    OR EXISTS (
                        SELECT 1
                        FROM receipt_items ri
                        WHERE ri.product_id = p.product_id
                          AND ri.is_active = 1
                    )
                )
                AND p.stock > 0;
                        """;

    public ProductSellDAO() {
    }

    public ArrayList<ProductSellDTO> getAllAvailableProducts() {
        ArrayList<ProductSellDTO> list = new ArrayList<>();

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(GET_ALL_SQL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductSellDTO dto = new ProductSellDTO();
                dto.setProduct_id(rs.getLong("product_id"));
                dto.setProduct_name(rs.getString("name"));
                dto.setStock(rs.getInt("stock"));
                dto.setSell_price(rs.getDouble("sell_price"));
                dto.setUnit_price(rs.getDouble("unit_price"));
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public int getStock(Long product_id) {
        int stock = 0;
        try {
            String sql = "select stock from products";
            Connection conn = JDBCUTIL.getConnection();
            PreparedStatement psm = conn.prepareStatement(sql);
            ResultSet rs = psm.executeQuery();
            while (rs.next()) {
                stock = rs.getInt("stock");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stock;
    }

    public boolean decreaseStockSafe(Long productId) {
        String sql = "UPDATE products " +
                "SET stock = stock - ? " +
                "WHERE product_id = ? AND stock >= ?";

        try (
                Connection conn = JDBCUTIL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 1);
            ps.setLong(2, productId);
            ps.setInt(3, 1);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
