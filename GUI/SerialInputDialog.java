package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Connector.JDBCUTIL;

public class SerialInputDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private boolean confirmed = false;

    public SerialInputDialog(
            JFrame parent,
            long productId,
            int quantity,
            List<String> oldSerials
    ) {
        super(parent, "Nhập serial", true);
        setSize(400, Math.min(400, 60 + quantity * 25));
        setLocationRelativeTo(parent);

        model = new DefaultTableModel(new Object[]{"Serial"}, 0);
        table = new JTable(model);
        table.setRowHeight(25);

        // tạo đúng số dòng
        for (int i = 0; i < quantity; i++) {
            model.addRow(new Object[]{""});
        }

        // nếu có serial cũ → fill lại
        if (oldSerials != null) {
            for (int i = 0; i < oldSerials.size() && i < quantity; i++) {
                model.setValueAt(oldSerials.get(i), i, 0);
            }
        } else {
            loadSuggestSerial(productId);
        }

        JScrollPane scroll = new JScrollPane(table);

        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Hủy");

        btnOK.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnOK);
        bottom.add(btnCancel);

        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadSuggestSerial(long productId) {
        String sql = """
            SELECT serial 
            FROM item_serials
            WHERE product_id = ? AND is_used = 0
            LIMIT ?
        """;

        try (Connection c = JDBCUTIL.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ps.setInt(2, model.getRowCount());

            ResultSet rs = ps.executeQuery();
            int row = 0;
            while (rs.next() && row < model.getRowCount()) {
                model.setValueAt(rs.getString(1), row++, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<String> getSerials() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            list.add(model.getValueAt(i, 0).toString().trim());
        }
        return list;
    }
}
