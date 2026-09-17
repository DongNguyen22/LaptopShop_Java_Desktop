package valiateData;

import java.util.Map;
import java.util.Set;

import DAO.PermissionDAO;
import DTO.AccountDTO;
import DTO.UserSession;

public class PermissionManager {
    private Map<String, Set<String>> permissionMap;
    private PermissionDAO permissionDAO = new PermissionDAO();
    private AccountDTO account = UserSession.getInstance().getAccount();
    public PermissionManager() {
        loadByRole(account.getRole_id());
    };

    public void loadByRole(int roleId) {
        permissionMap = permissionDAO.getAllPermission(roleId);
    }

    public boolean hasModule(String module_name) {
        return permissionMap.containsKey(module_name);
    }

    public boolean hasPermission(String module, String action) {
        return permissionMap.containsKey(module)
                && permissionMap.get(module).contains(action);
    }
}
