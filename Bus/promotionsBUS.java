package Bus;

import DTO.promotionsDTO;
import DTO.promotionItemsDTO;
import DAO.promotionsDAO;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class promotionsBUS {
    private final promotionsDAO kmDAO = new promotionsDAO();

    public ArrayList<promotionsDTO> getAllPromotions() {
        return kmDAO.getAll();
    }
    public ArrayList<promotionsDTO> getAll_2() {
        return kmDAO.getAll_2();
    }

    public ArrayList<promotionsDTO> searchPromotions(String keyword) {
        return kmDAO.search(keyword);
    }

    public ArrayList<promotionsDTO> filterByDate(LocalDateTime from, LocalDateTime to) {
        return kmDAO.filterByDate(from, to);
    }

    public boolean addPromotion(promotionsDTO km) {
        return kmDAO.insert(km);
    }

    public boolean updatePromotion(promotionsDTO km) {
        return kmDAO.update(km);
    }

    public boolean deletePromotion(int promotion_id) {
        return kmDAO.delete(promotion_id);
    }

    // ============ Methods for promotion_items ============

    public ArrayList<promotionItemsDTO> getPromotionItems(int promotion_id) {
        return kmDAO.getPromotionItems(promotion_id);
    }

    public boolean addPromotionItem(promotionItemsDTO item) {
        return kmDAO.insertPromotionItem(item);
    }

    public boolean updatePromotionItem(promotionItemsDTO item) {
        return kmDAO.updatePromotionItem(item);
    }

    public boolean deletePromotionItem(int promotion_id, long product_id) {
        return kmDAO.deletePromotionItem(promotion_id, product_id);
    }

    // ============ Helper methods ============

    public ArrayList<promotionsDTO> getActivePromotions() {
        return kmDAO.getActivePromotions();
    }

    public int countPromotionItems(int promotion_id) {
        return kmDAO.countPromotionItems(promotion_id);
    }

    public String getPromotionStatus(LocalDateTime start, LocalDateTime end, boolean status) {
        LocalDateTime now = LocalDateTime.now();

        if (!status) {
            return "Đã tắt";
        }
        if (now.isBefore(start)) {
            return "Chưa bắt đầu";
        }
        if (now.isAfter(end)) {
            return "Đã kết thúc";
        }
        return "Đang chạy";
    }

    public promotionsDTO getPromotionById(int promotion_id) {
        return kmDAO.getById(promotion_id);
    }

    public boolean isProductInActivePromotion(long productId, int excludePromotionId) {
        return kmDAO.isProductInActivePromotion(productId, excludePromotionId);
    }
}