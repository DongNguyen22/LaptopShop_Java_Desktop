package DTO;

public class RolePermissionsDTO {

    private int moduleId;
    private String moduleName;

    private int viewId;
    private boolean view;

    private int addId;
    private boolean add;

    private int editId;
    private boolean edit;

    private int deleteId;
    private boolean delete;

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public int getAddId() {
        return addId;
    }

    public void setAddId(int addId) {
        this.addId = addId;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

    public int getEditId() {
        return editId;
    }

    public void setEditId(int editId) {
        this.editId = editId;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public int getDeleteId() {
        return deleteId;
    }

    public void setDeleteId(int deleteId) {
        this.deleteId = deleteId;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
