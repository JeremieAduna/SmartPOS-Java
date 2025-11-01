import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;

public class Customer {
    private JPanel customerPanel;
    private JTextField searchField;
    private JTextField nameField;
    private JTextField tp_numberField;
    private JButton saveButton;
    private JButton searchButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable customerTable;
    private JTextField quickSearchField;

    private Connection con;

    public Customer() {
        con = DB.myCon();

        // Set up the table model
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Customer Name", "TP Number"},
                0
        );
        customerTable.setModel(model);
        loadCustomers();

        // Wire buttons
        saveButton.addActionListener(e -> saveCustomer());
        searchButton.addActionListener(e -> searchCustomer());
        updateButton.addActionListener(e -> updateCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = customerTable.getSelectedRow();

                if (row != -1) {
                    String id = customerTable.getValueAt(row,0).toString();
                    String name = customerTable.getValueAt(row,1).toString();
                    String tp_number = customerTable.getValueAt(row,2).toString();

                    searchField.setText(id);
                    nameField.setText(name);
                    tp_numberField.setText(tp_number);
                }
            }
        });
        quickSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String name = quickSearchField.getText();

                if (!name.isEmpty()) {

                    try {
                        String sql = "SELECT * FROM customers WHERE name LIKE ?";
                        PreparedStatement pst = con.prepareStatement(sql);
                        pst.setString(1, "%" + name + "%");
                        ResultSet rs = pst.executeQuery();

                        DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
                        model.setRowCount(0);

                        while (rs.next()) {
                            Vector v = new Vector();

                            v.add(rs.getString(1));
                            v.add(rs.getString(2));
                            v.add(rs.getString(3));

                            model.addRow(v);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    loadCustomers();
                }
            }
        });
    }

    private void deleteCustomer() {
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(customerPanel, "Please search or enter an ID to delete.");
            return;
        }

        try {
            String sql = "DELETE FROM customers WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(customerPanel, "Customer deleted successfully!");
                loadCustomers();
                nameField.setText("");
                tp_numberField.setText("");
                searchField.setText("");
            } else {
                JOptionPane.showMessageDialog(customerPanel, "No customer found with ID: " + id);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(customerPanel, "Error deleting customer: " + e.getMessage());
        }

    }

    private void updateCustomer() {
        String name = nameField.getText().trim();
        String tp_number = tp_numberField.getText().trim();
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(customerPanel, "Please search or enter an ID to update.");
            return;
        }

        try {
            String sql = "UPDATE customers SET name = ?, tp_number = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, tp_number);
            pst.setString(3, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(customerPanel, "Customer updated successfully!");
                loadCustomers(); // refresh table
            } else {
                JOptionPane.showMessageDialog(customerPanel, "No customer found with ID: " + id);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(customerPanel, "Error: " + e.getMessage());
        }
    }

    private void searchCustomer() {
        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(customerPanel, "Please enter an ID to search.");
            return;
        }

        try {
            String sql = "SELECT * FROM customers WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, search);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                tp_numberField.setText(rs.getString("tp_number"));
            } else {
                JOptionPane.showMessageDialog(customerPanel, "No customer found with ID: " + search);
                nameField.setText("");
                tp_numberField.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(customerPanel, "Error: " + e.getMessage());
        }
    }

    private void saveCustomer() {
        String name = nameField.getText().trim();
        String tp = tp_numberField.getText().trim();

        if (name.isEmpty() || tp.isEmpty()) {
            JOptionPane.showMessageDialog(customerPanel, "Please fill in all fields.");
            return;
        }

        try {
            String sql = "INSERT INTO customers (name, tp_number) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, tp);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(customerPanel, "Customer Saved Successfully!");
            loadCustomers(); // refresh table

            nameField.setText("");
            tp_numberField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(customerPanel, "Error: " + ex.getMessage());
        }
    }

    public JPanel getCustomerPanel() {
        return customerPanel;
    }


    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing DB connection: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        try {
            String sql = "SELECT * FROM customers";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("tp_number")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(customerPanel, "Error loading customers: " + e.getMessage());
        }
    }
}
