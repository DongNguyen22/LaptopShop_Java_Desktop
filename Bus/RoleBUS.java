package Bus;

import java.util.ArrayList;
import java.util.List;

import DAO.RoleDAO;
import DTO.AccountDTO;
import DTO.RoleDTO;

public class RoleBUS {

    private RoleDAO roleDAO;

    public RoleBUS() {
        roleDAO = new RoleDAO();
    }

    // Lấy danh sách role
    public ArrayList<RoleDTO> getAllRoles() {
        return roleDAO.getAllRoles();
    }

    // Thêm role
    public int addRole(String role) {

        if (roleDAO.existsByName(role)) {
            return -1;
        }

        return roleDAO.insert(role);
    }

    // Sửa role
    public boolean updateRole(RoleDTO role) {
        if (role.getRoleId() <= 0) {
            throw new IllegalArgumentException("Role không hợp lệ");
        }

        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên quyền không được để trống");
        }

        return roleDAO.updateRole(role);
    }

    // Xóa role
    public boolean deleteRole(int roleId) {
        if (roleDAO.isRoleUsed(roleId)) {
            return false;
        }
        return roleDAO.deleteRole(roleId);
    }

    public RoleDTO getById(int roleId) {
        return roleDAO.getById(roleId);
    }
    public ArrayList<RoleDTO> getAllRoleForCombo(){
        return roleDAO.getAllRoleForCombo();
    }
}
