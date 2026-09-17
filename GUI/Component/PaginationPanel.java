package GUI.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PaginationPanel extends JPanel {
    private JButton btnFirst;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnLast;
    private JLabel lblPageInfo;
    private JLabel lblRecordInfo;
    private JTextField txtPage;
    private JComboBox<Integer> cboPageSize;

    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;
    private int totalRecords = 0;

    private ActionListener pageChangeListener;

    public PaginationPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        setBackground(new Color(245, 245, 245));

        btnFirst = createButton("<<", e -> goToPage(1));
        add(btnFirst);

        btnPrev = createButton("<", e -> goToPage(currentPage - 1));
        add(btnPrev);

        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPageInfo.setForeground(new Color(60, 60, 60));
        add(lblPageInfo);

        lblRecordInfo = new JLabel("Đang hiển thị 0 - 0 trên tổng 0");
        lblRecordInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRecordInfo.setForeground(new Color(100, 100, 100));

        txtPage = new JTextField("1", 4);
        txtPage.setHorizontalAlignment(JTextField.CENTER);
        txtPage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPage.addActionListener(e -> {
            try {
                int page = Integer.parseInt(txtPage.getText().trim());
                if (page >= 1 && page <= totalPages) {
                    goToPage(page);
                } else {
                    txtPage.setText(String.valueOf(currentPage));
                }
            } catch (NumberFormatException ex) {
                txtPage.setText(String.valueOf(currentPage));
            }
        });
        add(txtPage);

        btnNext = createButton(">", e -> goToPage(currentPage + 1));
        add(btnNext);

        btnLast = createButton(">>", e -> goToPage(totalPages));
        add(btnLast);

        JLabel lblSize = new JLabel("Hiển thị:");
        lblSize.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(lblSize);

        cboPageSize = new JComboBox<>(new Integer[]{10, 20, 30, 50, 100});
        cboPageSize.setSelectedItem(pageSize);
        cboPageSize.addActionListener(e -> {
            pageSize = (Integer) cboPageSize.getSelectedItem();
            goToPage(1);
        });
        add(cboPageSize);
        add(lblRecordInfo);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(45, 32));
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        return btn;
    }

    /**
     * @param current
     * @param total
     * @param size
     */
    public void updatePagination(int current, int total, int size) {
        this.currentPage = current;
        this.totalRecords = total;
        this.pageSize = size;

        this.totalPages = (int) Math.ceil((double) total / size);
        if (totalPages == 0) totalPages = 1;

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        txtPage.setText(String.valueOf(currentPage));

        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);

        int start = (currentPage - 1) * pageSize + 1;
        int end = Math.min(currentPage * pageSize, totalRecords);

        if (totalRecords == 0) {
            start = 0;
            end = 0;
        }

        lblRecordInfo.setText("Đang hiển thị " + start + " - " + end + " trên tổng " + totalRecords);
    }

    private void goToPage(int page) {
        if (page < 1 || page > totalPages) return;

        currentPage = page;
        updatePagination(currentPage, totalRecords, pageSize);

        if (pageChangeListener != null) {
            pageChangeListener.actionPerformed(null);
        }
    }

    public void setPageChangeListener(ActionListener listener) {
        this.pageChangeListener = listener;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }
}