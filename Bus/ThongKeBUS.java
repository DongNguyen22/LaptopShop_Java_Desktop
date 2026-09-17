package Bus;

import DAO.ThongKeDAO;
import DTO.*;

import java.util.ArrayList;

public class ThongKeBUS {
    private ThongKeDAO dao = new ThongKeDAO();

    // ==================== Lấy danh sách cơ bản ====================
    public ArrayList<EmployeeDTO> getEmployees() { return dao.getEmployees(); }
    public ArrayList<sanPham> getProducts() { return dao.getProducts(); }

    // ==================== Không phân trang ====================
    public ArrayList<OrderDTO> getOrdersByEmployee(long empId, String start, String end) {
        return dao.getOrdersByEmployee(empId, start, end);
    }
    public ArrayList<phieuNhap> getReceiptsByEmployee(long empId, String start, String end) {
        return dao.getReceiptsByEmployee(empId, start, end);
    }
    public ArrayList<OrderItemDTO> getOrdersByProduct(long productId, String start, String end) {
        return dao.getOrdersByProduct(productId, start, end);
    }
    public ArrayList<chiTietPhieuNhap> getReceiptsByProduct(long productId, String start, String end) {
        return dao.getReceiptsByProduct(productId, start, end);
    }
    public ArrayList<OrderDTO> getAllOrders(String start, String end) {
        return dao.getAllOrders(start, end);
    }
    public ArrayList<phieuNhap> getAllReceipts(String start, String end) {
        return dao.getAllReceipts(start, end);
    }

    // ==================== Tổng hợp ====================
    public ArrayList<ThongKePhuongThucThanhToanDTO> getPaymentData(String start, String end) {
        return dao.getPaymentData(start, end);
    }
    public double[] getSummaryByEmployee(String type, long id, String start, String end) {
        return dao.getSummaryByEmployee(type, id, start, end);
    }
    public double[] getSummaryByProduct(String type, long id, String start, String end) {
        return dao.getSummaryByProduct(type, id, start, end);
    }

    // ==================== ĐẾM (cho phân trang) ====================
    public int countOrdersByEmployee(long empId, String start, String end) {
        return dao.countOrdersByEmployee(empId, start, end);
    }
    public int countReceiptsByEmployee(long empId, String start, String end) {
        return dao.countReceiptsByEmployee(empId, start, end);
    }
    public int countOrdersByProduct(long productId, String start, String end) {
        return dao.countOrdersByProduct(productId, start, end);
    }
    public int countReceiptsByProduct(long productId, String start, String end) {
        return dao.countReceiptsByProduct(productId, start, end);
    }
    public int countAllOrders(String start, String end) {
        return dao.countAllOrders(start, end);
    }
    public int countAllReceipts(String start, String end) {
        return dao.countAllReceipts(start, end);
    }

    // ==================== PHÂN TRANG ====================
    public ArrayList<OrderDTO> getOrdersByEmployeePaginated(long empId, String start, String end, int offset, int limit) {
        return dao.getOrdersByEmployeePaginated(empId, start, end, offset, limit);
    }
    public ArrayList<phieuNhap> getReceiptsByEmployeePaginated(long empId, String start, String end, int offset, int limit) {
        return dao.getReceiptsByEmployeePaginated(empId, start, end, offset, limit);
    }
    public ArrayList<OrderItemDTO> getOrdersByProductPaginated(long productId, String start, String end, int offset, int limit) {
        return dao.getOrdersByProductPaginated(productId, start, end, offset, limit);
    }
    public ArrayList<chiTietPhieuNhap> getReceiptsByProductPaginated(long productId, String start, String end, int offset, int limit) {
        return dao.getReceiptsByProductPaginated(productId, start, end, offset, limit);
    }
    public ArrayList<OrderDTO> getAllOrdersPaginated(String start, String end, int offset, int limit) {
        return dao.getAllOrdersPaginated(start, end, offset, limit);
    }
    public ArrayList<phieuNhap> getAllReceiptsPaginated(String start, String end, int offset, int limit) {
        return dao.getAllReceiptsPaginated(start, end, offset, limit);
    }
}