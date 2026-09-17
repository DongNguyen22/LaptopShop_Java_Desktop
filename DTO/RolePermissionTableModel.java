package DTO;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class RolePermissionTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Module", "View", "Add", "UPDATE", "Delete"
    };

    private List<RolePermissionsDTO> data;

    public RolePermissionTableModel(List<RolePermissionsDTO> data) {
        this.data = data;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Boolean.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RolePermissionsDTO dto = data.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> dto.getModuleName();
            case 1 -> dto.isView();
            case 2 -> dto.isAdd();
            case 3 -> dto.isEdit();
            case 4 -> dto.isDelete();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        RolePermissionsDTO dto = data.get(rowIndex);
        boolean checked = (Boolean) value;

        switch (columnIndex) {
            case 1 -> dto.setView(checked);
            case 2 -> dto.setAdd(checked);
            case 3 -> dto.setEdit(checked);
            case 4 -> dto.setDelete(checked);
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public List<RolePermissionsDTO> getData() {
        return data;
    }
}
