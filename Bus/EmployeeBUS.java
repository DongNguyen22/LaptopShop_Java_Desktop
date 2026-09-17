package Bus;

import DAO.EmployeeDAO;
import DTO.EmployeeDTO;
import valiateData.inputValidator;
import valiateData.validationResult;

import java.math.BigDecimal;
import java.util.ArrayList;

public class EmployeeBUS {

    private EmployeeDAO employeeDAO;

    public EmployeeBUS() {
        employeeDAO = new EmployeeDAO();
    }

    public ArrayList<EmployeeDTO> getAllEmployees() {
        return employeeDAO.getAll();
    }

    public EmployeeDTO getEmployeeById(Long id) {
        return employeeDAO.getById(id);
    }

    public boolean addEmployee(EmployeeDTO emp) {
        return employeeDAO.insert(emp);
    }

    public boolean updateEmployee(EmployeeDTO emp) {
        return employeeDAO.update(emp);
    }

    public boolean deleteEmployee(Long id) {
        if (id == null) return false;
        return employeeDAO.delete(id);
    }

    public ArrayList<EmployeeDTO> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }
        return employeeDAO.search(keyword.trim());
    }
    public ArrayList<EmployeeDTO> getAllForCombo(){
        return employeeDAO.getAllEmployeeForCombo();
    }

    /* ================= VALIDATE ================= */
    public validationResult validate(EmployeeDTO emp) {
        if (emp == null) return validationResult.error("Thông tin nhân viên không được trống");

        if (emp.getEmployeeName() == null || emp.getEmployeeName().trim().isEmpty())
            return validationResult.error("name");

        if (!inputValidator.isValidPhone(emp.getPhoneNumber().toString()))

            return validationResult.error("phone");

        if (emp.getSalary() == null || emp.getSalary().compareTo(BigDecimal.ZERO) < 0)
            return validationResult.error("salary");

        return validationResult.success();
    }
    public ArrayList<EmployeeDTO> getExcept(){
        return employeeDAO.getExcept();
    }
}
