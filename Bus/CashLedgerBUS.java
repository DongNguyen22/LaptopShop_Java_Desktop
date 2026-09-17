package Bus;

import DAO.CashLedgerDAO;
import DTO.CashLedgerDTO;

import java.util.ArrayList;
import java.util.List;

public class CashLedgerBUS {

    private CashLedgerDAO dao = new CashLedgerDAO();

    public ArrayList<CashLedgerDTO> getTodayTransactions() {
        return dao.getTodayTransactions();
    }

    public double getTotalSale() {
        return dao.getTotalByType("SALE");
    }

    public double getTotalCapitalIn() {
        return dao.getTotalByType("CAPITAL_IN");
    }

    public double getTotalCapitalOut() {
        return dao.getTotalByType("CAPITAL_OUT");
    }

    public double getTotalPurchase() {
        return dao.getTotalByType("PURCHASE");
    }
}