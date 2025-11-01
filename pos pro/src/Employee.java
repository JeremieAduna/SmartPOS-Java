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

public class Employee {
    private JPanel employeePanel;
    private JTextField searchField;
    private JTextField nameField;
    private JTextField tp_numberField;
    private JButton saveButton;
    private JButton searchButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable employeeTable;
    private JTextField quickSearchField;

    private Connection con;

    public Employee() {
        con = DB.myCon();
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Employee ID", "Employee Name", "TP Number"},
                0
        );
        employeeTable.setModel(model);
        loadEmployees();

        saveButton.addActionListener(e -> saveEmployee());
        searchButton.addActionListener(e -> searchEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());

        employeeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = employeeTable.getSelectedRow();

                if (row != -1) {
                    String id = employeeTable.getValueAt(row, 0).toString();
                    String name = employeeTable.getValueAt(row, 1).toString();
                    String tp_number = employeeTable.getValueAt(row, 2).toString();

                    searchField.setText(id);
                    nameField.setText(name);
                    tp_numberField.setText(tp_number);
                }
            }
        });

        quickSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public  void keyReleased(KeyEvent e) {
                String name = quickSearchField.getText();

                if (!name.isEmpty()) {
                    try {
                        String sql = "SELECT * FROM employees WHERE name LIKE ?";
                        PreparedStatement pst = con.prepareStatement(sql);
                        pst.setString(1, "%" + name + "%");
                        ResultSet rs = pst.executeQuery();

                        DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
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
                    loadEmployees();
                }
            }
        });


    }

    private void deleteEmployee() {
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(employeePanel, "Please search or enter an ID to delete.");
            return;
        }
        try {
            String sql = "DELETE FROM employees WHERE employee_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(employeePanel, "Employee deleted successfully!");
                loadEmployees();
                nameField.setText("");
                tp_numberField.setText("");
                searchField.setText("");
            }
            else {
                JOptionPane.showMessageDialog(employeePanel, "No employee found with ID: " + id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(employeePanel, "Error deleting employee: " + e.getMessage());
        }

    }

    private void updateEmployee() {
        String name = nameField.getText().trim();
        String tp_number = tp_numberField.getText().trim();
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(employeePanel, "Please search or enter an ID to update.");
            return;
        }

        try {
            String sql = "UPDATE employees SET name = ?, tp_number = ? WHERE employee_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, tp_number);
            pst.setString(3, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(employeePanel, "Employee updated successfully!");
                loadEmployees();

                nameField.setText("");
                tp_numberField.setText("");
                searchField.setText("");
            }
            else  {
                JOptionPane.showMessageDialog(employeePanel, "No employee found with ID: " + id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(employeePanel, "Error: " + e.getMessage());
        }

    }

    private void searchEmployee() {
        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(employeePanel, "Please enter an ID to search.");
            return;
        }

        try {
            String sql = "SELECT * FROM employees WHERE employee_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, search);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                tp_numberField.setText(rs.getString("tp_number"));
            }
            else {
                JOptionPane.showMessageDialog(employeePanel, "Employee ID not found.");
                nameField.setText("");
                tp_numberField.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(employeePanel, "Error : " + e.getMessage() );
        }

    }

    private void saveEmployee() {
        String name = nameField.getText();
        String tp_number = tp_numberField.getText();

        if (name.isEmpty() || tp_number.isEmpty()) {
            JOptionPane.showMessageDialog(employeePanel, "Please fill in all fields.");
            return;
        }

        try {
            String sql = "INSERT INTO employees (name, tp_number) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, tp_number);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(employeePanel, "Employee Saved Successfully!");
            loadEmployees();

            nameField.setText("");
            tp_numberField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(employeePanel, "Error: " +ex.getMessage());
        }


    }

    private void loadEmployees() {
        try {
            String sql = "SELECT * FROM employees";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("employee_id"),
                        rs.getString("name"),
                        rs.getString("tp_number")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(employeePanel, "Error loading employees: " + e.getMessage());
        }
    }

    public JPanel getEmployeePanelPanel() {
        return employeePanel;
    }
}
