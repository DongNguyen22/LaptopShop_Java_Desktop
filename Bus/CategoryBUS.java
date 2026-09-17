package Bus;

import DAO.CategoryDAO;
import DTO.CategoryDTO;

import java.util.ArrayList;

public class CategoryBUS {
    private CategoryDAO categoryDAO;

    public CategoryBUS() {
        categoryDAO = new CategoryDAO();
    }

    public ArrayList<CategoryDTO> getAll() {
        return categoryDAO.getAll();
    }

    public CategoryDTO getByID(int category_id) {
        return categoryDAO.getByID(category_id);
    }

    public ArrayList<CategoryDTO> search(String keyword) {
        return categoryDAO.search(keyword.trim());
    }

    public boolean add(CategoryDTO category) {
        ArrayList<CategoryDTO> all = categoryDAO.getAll();
        for (CategoryDTO c : all) {
            if (c.getName().equalsIgnoreCase(category.getName())) {
                return false; // Trùng tên
            }
        }
        return categoryDAO.insert(category);
    }

    public boolean update(CategoryDTO category) {
        CategoryDTO old = categoryDAO.getByID(category.getCategory_id());
        if (old == null)
            return false;
        // Nếu tên mới giống tên cũ thì không cần kiểm tra
        if (!old.getName().equalsIgnoreCase(category.getName())) {
            ArrayList<CategoryDTO> all = categoryDAO.getAll();
            for (CategoryDTO c : all) {
                if (c.getCategory_id() != category.getCategory_id() &&
                        c.getName().equalsIgnoreCase(category.getName())) {
                    return false; // Trùng với category khác
                }
            }
        }
        return categoryDAO.update(category);
    }

    public boolean delete(int category_id) {
        return categoryDAO.delete(category_id);
    }

    public boolean isCategoryInUse(int category_id) {
        return categoryDAO.isCategoryInUse(category_id);
    }

    public ArrayList<CategoryDTO> searchByCategoryAndKeyword(int categoryId, String keyword) {
        return categoryDAO.searchByCategoryAndKeyword(categoryId, keyword);
    }
}
