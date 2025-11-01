import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class Supplier {
    private JPanel supplierPanel;
    private JTextField searchField;
    private JTextField nameField;
    private JTextField tp_numberField;
    private JButton saveButton;
    private JButton searchButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable supplierTable;
    private JTextField quickSearchField;

    private Connection con;

    public Supplier() {
        con = DB.myCon();

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Supplier ID", "Supplier Name", "TP Number"},
                0
        );
        supplierTable.setModel(model);
        loadSuppliers();

        saveButton.addActionListener(e -> saveSupplier());
        searchButton.addActionListener(e -> searchSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        supplierTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = supplierTable.getSelectedRow();

                if (row != -1) {
                    String id = supplierTable.getValueAt(row, 0).toString();
                    String name = supplierTable.getValueAt(row, 1).toString();
                    String tp_number = supplierTable.getValueAt(row, 2).toString();

                    searchField.setText(id);
                    nameField.setText(name);
                    tp_numberField.setText(tp_number);
                }
            }
        });

        quickSearchField.addKeyListener(new  KeyAdapter() {
            @Override
            public  void keyReleased(KeyEvent e) {
                String name = quickSearchField.getText();

                if (!name.isEmpty()) {
                    try {
                        String sql = "SELECT * FROM suppliers WHERE name LIKE ?";
                        PreparedStatement pst = con.prepareStatement(sql);
                        pst.setString(1, "%" + name + "%");
                        ResultSet rs = pst.executeQuery();

                        DefaultTableModel model = (DefaultTableModel) supplierTable.getModel();
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
                    loadSuppliers();
                }
            }
        });

    }

    private void deleteSupplier() {

        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(supplierPanel, "Please search or enter an ID to delete.");
            return;
        }
        try {
            String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(supplierPanel, "Supplier deleted successfully!");
                loadSuppliers();
                nameField.setText("");
                tp_numberField.setText("");
                searchField.setText("");
            }
            else {
                JOptionPane.showMessageDialog(supplierPanel, "No supplier found with ID: " + id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(supplierPanel, "Error deleting supplier: " + e.getMessage());
        }
    }

    private void updateSupplier() {
        String name = nameField.getText().trim();
        String tp_number = tp_numberField.getText().trim();
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(supplierPanel, "Please search or enter an ID to update.");
            return;
        }

        try {
            String sql = "UPDATE suppliers SET name = ?, tp_number = ? WHERE supplier_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, tp_number);
            pst.setString(3, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(supplierPanel, "Supplier updated successfully!");
                loadSuppliers();

                nameField.setText("");
                tp_numberField.setText("");
                searchField.setText("");
            }
            else  {
                JOptionPane.showMessageDialog(supplierPanel, "No supplier found with ID: " + id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(supplierPanel, "Error: " + e.getMessage());
        }

    }

    private void searchSupplier() {
        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(supplierPanel, "Please enter an ID to search.");
            return;
        }

        try {
            String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, search);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                tp_numberField.setText(rs.getString("tp_number"));
            }
            else {
                JOptionPane.showMessageDialog(supplierPanel, "Supplier ID not found.");
                nameField.setText("");
                tp_numberField.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(supplierPanel, "Error : " + e.getMessage() );
        }

    }

    private void saveSupplier() {
        String name = nameField.getText();
        String tp_number = tp_numberField.getText();

        if (name.isEmpty() || tp_number.isEmpty()) {
            JOptionPane.showMessageDialog(supplierPanel, "Please fill in all fields.");
            return;
        }

        try {
            String sql = "INSERT INTO suppliers (name, tp_number) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, tp_number);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(supplierPanel, "Supplier Saved Successfully!");
            loadSuppliers();

            nameField.setText("");
            tp_numberField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(supplierPanel, "Error: " +ex.getMessage());
        }

    }

    private void loadSuppliers() {
        try {
            String sql = "SELECT * FROM suppliers";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model= (DefaultTableModel) supplierTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("tp_number")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(supplierPanel, "Error loading suppliers:" + e.getMessage());
        }

    }

    public JPanel getSupplierPanel() {
        return supplierPanel;
    }
}
