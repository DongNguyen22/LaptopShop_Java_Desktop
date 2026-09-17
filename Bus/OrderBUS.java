package Bus;

import java.math.BigDecimal;
import java.util.ArrayList;

import DAO.OrderDAO;
import DTO.NewOrderDTO;
import DTO.OrderDTO;
import DTO.OrderDetailDTO;
import DTO.OrderItemDTO;
import DTO.OrderDetailDTO;

public class OrderBUS {

    private OrderDAO orderDAO;

    public OrderBUS() {
        orderDAO = new OrderDAO();
    }
    public boolean insert(OrderDTO order,ArrayList<OrderItemDTO> listCTHD) {
        if (order == null) return false;
        return orderDAO.insert(order,listCTHD);
    }

    public ArrayList<OrderDTO> getAllOrders() {
        return orderDAO.findAll();
    }
    public NewOrderDTO findOrder(Long orderID){
        return orderDAO.findOrder(orderID);
    }
    public ArrayList<OrderDetailDTO> getOrderDetailByOrderId(Long orderId){
        return orderDAO.getByOrderId(orderId);
    }
    public BigDecimal getOrderTotalByOrderId(Long orderId){
        return orderDAO.getOrderTotalByOrderId(orderId);
    }
    public Long getOrderID(){
        return orderDAO.createEmptyOrder();
    }
    public boolean deleteOrder(Long orderID){
        return orderDAO.deleteOrder(orderID);
    }
    public ArrayList<NewOrderDTO> getAllNew(){
        return orderDAO.getAllNew();
    }
    public boolean setAutoIncrement(Long value){
        return orderDAO.setAutoIncrement(value);
    }
}
