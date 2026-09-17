package Bus;

import java.util.ArrayList;

import DAO.ProductSellDAO;
import DTO.ProductSellDTO;

public class ProductSellBUS {

    private ProductSellDAO productSellDAO;

    public ProductSellBUS() {
        productSellDAO = new ProductSellDAO();
    }

    public ArrayList<ProductSellDTO> getAllAvailableProducts() {
        return productSellDAO.getAllAvailableProducts();
    }
    public int getStock(Long product_id){
        return productSellDAO.getStock(product_id);
    }
    public boolean decreaseStock(Long productID){
        return productSellDAO.decreaseStockSafe(productID);
    }
}

