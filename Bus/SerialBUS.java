package Bus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import DAO.SerialDAO;
import DTO.SerialDTO;

public class SerialBUS {

    private SerialDAO serialDAO;

    public SerialBUS() {
        serialDAO = new SerialDAO();
    }

    /*
     * =========================
     * Lấy toàn bộ serial chưa bán (status = 0)
     * key : product_id
     * value : danh sách serial
     * =========================
     */
    public ArrayList<SerialDTO> getAllSerialByProduct() {
        try {
            return serialDAO.getAllSerialByProduct();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /*
     * =========================
     * Thêm serial mới
     * =========================
     */
    public boolean insertSerial(SerialDTO item) {
        try {
            return serialDAO.insert(item);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================
     * Cập nhật serial khi bán
     * =========================
     */
    public boolean sellSerial(Connection conn,String serial) {
        try {
            return serialDAO.updateReceiptAndStatus(conn,serial);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * =========================
     * Kiểm tra serial còn sử dụng được không
     * =========================
     */
    public boolean isSerialAvailable(String serial) {
        return serialDAO.isSerialAvailable(serial);
    }
}
