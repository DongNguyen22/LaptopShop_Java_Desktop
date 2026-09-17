package Bus;

import java.util.ArrayList;

import DAO.RolePermissionsDAO;
import DTO.RolePermissionsDTO;

public class RolePermissionsBUS {
    private RolePermissionsDAO rolePermissionsDAO;
    public RolePermissionsBUS() {
        rolePermissionsDAO = new RolePermissionsDAO();
    }
    public ArrayList<RolePermissionsDTO> getAll(int role_id){
        return rolePermissionsDAO.getAll(role_id);
    }
    public boolean update(int role_id,int module_action_id,boolean status){
        return rolePermissionsDAO.updateStatus(role_id, module_action_id, status);
    }

}
