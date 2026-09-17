package Bus;

import DAO.SanPhamDAO;
import DTO.sanPham;
import java.util.List;

public class SanPhamBUS {
    private SanPhamDAO sanPhamDAO = new SanPhamDAO();

    public List<sanPham> getAllProducts() {
        return sanPhamDAO.getAll(); 
    }

    public sanPham getProductById(String maSP) {
        return sanPhamDAO.getById(maSP);
    }
}