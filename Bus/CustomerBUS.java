package Bus;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTextField;

import DAO.CustomerDAO;
import DTO.CustomerDTO;
import valiateData.inputValidator;
import valiateData.validationResult;

public class CustomerBUS {
    private CustomerDAO customerDAO = new CustomerDAO();
    public CustomerBUS() {
    }
    public CustomerDTO getKHinfor(String phone){
        if(phone == null || phone.trim().isEmpty()){
            return null;
        }
        return customerDAO.getOldCustomer(phone);
    }
    public validationResult validateSDT(String phone){
        
        boolean isValid = inputValidator.isValidPhone(phone);
        if (isValid) {
            return validationResult.success();
        }
        return validationResult.error("Định dạng số điện thoại không đúng");
    }
    public boolean insert(CustomerDTO o){
        return customerDAO.insert(o);
    }
    public long createCustomerID(){
        return customerDAO.createEmptyOrder();
    }
    public boolean delete(long customerID){
        return customerDAO.deleteCustomer(customerID);
    }
    public ArrayList<CustomerDTO> getAll(){
        return customerDAO.getAll();
    }
    
}

