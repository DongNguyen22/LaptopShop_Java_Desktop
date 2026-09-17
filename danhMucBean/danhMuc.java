package danhMucBean;

import javax.swing.JPanel;

public class danhMuc {
    private int kind;
    private JPanel jPanel;
    public danhMuc(int kind, JPanel jPanel) {
        this.kind = kind;
        this.jPanel = jPanel;
    }
    public danhMuc() {
    }
    public int getKind() {
        return kind;
    }
    public void setKind(int kind) {
        this.kind = kind;
    }
    public JPanel getjPanel() {
        return jPanel;
    }
    public void setjPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }
}
