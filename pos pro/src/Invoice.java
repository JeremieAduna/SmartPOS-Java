import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class Invoice {
    private JPanel invoicePanel;
    private JTextField invoiceField;
    private JComboBox<String> statusComboBox;
    private JTable salesTable;
    private JLabel invoiceLabel;
    private JLabel cnameLabel;
    private JTextField cnameField;
    private JLabel statusLabel;
    private JLabel totalBalanceLabel;
    private JButton exportButton;


    private Connection con;

    public Invoice() {
        con = DB.myCon();

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Sales no.","Invoice ID", "Customer ID", "Customer Name", "Total Qty", "Total Bill", "Status", "Balance"},
                0
        );
        salesTable.setModel(model);

        statusComboBox.addItem("All");
        statusComboBox.addItem("Paid");
        statusComboBox.addItem("Partial");
        statusComboBox.addItem("Unpaid");

        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("invoices.csv"));
            int option = fileChooser.showSaveDialog(invoicePanel);
            if (option == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                exportToCSV(path);
            }
        });

        // Search by invoice number
        invoiceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String invoice_number = invoiceField.getText().trim();

                if (!invoice_number.isEmpty()) {
                    filterBy("invoice_id", invoice_number);
                } else {
                    loadInvoiceData();
                }
            }
        });

        // Search by customer name
        cnameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String name = cnameField.getText().trim();

                if (!name.isEmpty()) {
                    filterBy("customer_name", name);
                } else {
                    loadInvoiceData();
                }
            }
        });

        // Filter by status using combo box
        statusComboBox.addActionListener(e -> {
            String selectedStatus = (String) statusComboBox.getSelectedItem();
            if (selectedStatus != null && !selectedStatus.equals("All")) {
                filterBy("status", selectedStatus);
            } else {
                loadInvoiceData();
            }
        });


        loadInvoiceData();
    }

    private void exportToCSV(String filePath) {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();

        try (FileWriter csvWriter = new FileWriter(filePath)) {
            // Write header row
            for (int i = 0; i < model.getColumnCount(); i++) {
                csvWriter.append(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) csvWriter.append(",");
            }
            csvWriter.append("\n");

            // Write data rows
            double totalBalance = 0.0;
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    String text = value == null ? "" : value.toString();
                    csvWriter.append(text);
                    if (col < model.getColumnCount() - 1) csvWriter.append(",");
                }
                csvWriter.append("\n");

                // accumulate balance (last column index 7)
                Object balanceVal = model.getValueAt(row, 7);
                if (balanceVal != null) {
                    try {
                        totalBalance += Double.parseDouble(balanceVal.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Add a summary row
            csvWriter.append("\n,,,,,,,TOTAL BALANCE: ").append(String.format("%.2f", totalBalance)).append("\n");

            JOptionPane.showMessageDialog(null, "CSV file saved: " + filePath);

            // 🔥 Open automatically
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.open(new java.io.File(filePath));

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving CSV: " + e.getMessage());
        }
    }

    private void filterBy(String column, String value) {
        String sql;

        // Status uses exact match, others use LIKE
        if (column.equals("status")) {
            sql = "SELECT * FROM sales WHERE status = ?";
        } else {
            sql = "SELECT * FROM sales WHERE " + column + " LIKE ?";
            value = "%" + value + "%";
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, value);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel tableModel = (DefaultTableModel) salesTable.getModel();
            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                        rs.getString("sale_num"),
                        rs.getString("invoice_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("total_quantity"),
                        rs.getString("total_bill"),
                        rs.getString("status"),
                        rs.getString("balance")
                };
                tableModel.addRow(row);
            }

            updateTotalBalance();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while filtering: " + ex.getMessage());
        }
    }

    private void loadInvoiceData() {
        String sql = "SELECT * FROM sales";

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
            model.setRowCount(0); // clear existing rows

            while (rs.next()) {
                Object[] row = {
                        rs.getString("sale_num"),
                        rs.getString("invoice_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("total_quantity"),
                        rs.getString("total_bill"),
                        rs.getString("status"),
                        rs.getString("balance")
                };
                model.addRow(row);
            }

            updateTotalBalance();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading invoices: " + e.getMessage());
        }
    }

    private void updateTotalBalance() {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        double total = 0.0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 7); // "Balance" column
            if (val != null) {
                try {
                    total += Double.parseDouble(val.toString());
                } catch (NumberFormatException ignored) {}
            }
        }

        totalBalanceLabel.setText("TOTAL BALANCE : " + String.format("%.2f", total));
    }

    public JPanel getInvoicePanel() {
        return invoicePanel;
    }
}
