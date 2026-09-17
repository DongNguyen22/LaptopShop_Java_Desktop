package Bus;

import DAO.supplierProductsDAO;
import DTO.supplierProductsDTO;
import java.util.ArrayList;

public class supplierProductsBUS {
    private supplierProductsDAO spDAO = new supplierProductsDAO();

    public ArrayList<supplierProductsDTO> getProductsBySupplierId(int supplierId) {
        return spDAO.getBySupplierId(supplierId);
    }

    public boolean addProductToSupplier(int supplierId, int productId) {
        // Kiểm tra xem đã tồn tại chưa
        ArrayList<supplierProductsDTO> current = spDAO.getBySupplierId(supplierId);
        for (supplierProductsDTO sp : current) {
            if (sp.getProduct_id() == productId) {
                return false; // đã tồn tại
            }
        }
        return spDAO.insert(supplierId, productId);
    }

    public boolean deleteProductFromSupplier(int supplierId, int productId) {
        return spDAO.delete(supplierId, productId);
    }
}