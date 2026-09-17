package Bus;

import DTO.nhaCungCapDTO;
import DAO.nhaCungCapDAO;
import java.util.ArrayList;

public class nhaCungCapBUS {
    private final nhaCungCapDAO nccDAO = new nhaCungCapDAO();

    public ArrayList<nhaCungCapDTO> getAllNhaCungCap() {
        return nccDAO.getAll();
    }

    public ArrayList<nhaCungCapDTO> searchNhaCungCap(String keyword) {
        return nccDAO.search(keyword);
    }

    public boolean addNhaCungCap(nhaCungCapDTO ncc) {
        String phone = ncc.getPhone_number();
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("0\\d{9}")) {
                return false; 
            }
        }
        return nccDAO.insert(ncc);
    }

    public boolean updateNhaCungCap(nhaCungCapDTO ncc) {
        return nccDAO.update(ncc);
    }

    public boolean deleteNhaCungCap(int supplier_id) {
        return nccDAO.delete(supplier_id);
    }

    public boolean isSupplierInUse(int supplier_id) {
        return nccDAO.isSupplierInUse(supplier_id);
    }

    public boolean isSupplierCodeDuplicate(String code) {
    return nccDAO.isSupplierCodeDuplicate(code);
    }
    
    public boolean isPhoneNumberExist(String phone) {
    return nccDAO.isPhoneNumberExist(phone);
    }
}