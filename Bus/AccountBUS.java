package Bus;

import java.util.ArrayList;
import java.util.Map;

import DAO.AccountDAO;
import DTO.AccountDTO;

public class AccountBUS {

    private AccountDAO accountDAO = new AccountDAO();

    public AccountDTO login(String username, String password) {

        AccountDTO acc = accountDAO.findActiveAccountByUsername(username);
        return acc; 
    }
    public Map<String, String> getUserInfo(String username) {
        return accountDAO.getEmployeeAndRoleName(username);
    }
    public ArrayList<AccountDTO> getAll(){
        return accountDAO.getAll();
    }
    public long insert(AccountDTO a){
        return  accountDAO.insert(a);
    }
    public AccountDTO getById(long id){
        return accountDAO.selectById(id);
    }
    public boolean update(AccountDTO acc){
        return accountDAO.update(acc);
    }
    public boolean delete(Long id){
        return accountDAO.delete(id);
    }
    public AccountDTO findByUserName(String username){
        return accountDAO.findByUsername(username);
    }
    public AccountDTO findByUserName_2(String username){
        return accountDAO.findByUsername_2(username);
    }
}

