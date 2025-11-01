import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class Product {
    private JPanel productPanel;
    private JTextField searchField;
    private JTextField nameField;
    private JTextField barcodeField;
    private JButton saveButton;
    private JButton searchButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable productTable;
    private JTextField priceField;
    private JTextField quantityField;
    private JTextField supplier_idField;
    private JTextField quickSearchField;

    private Connection con;

    public Product() {

        con = DB.myCon();
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Product ID", "Product Name", "Bar Code", "Price", "Qty", "SID"},
                0
        );
        productTable.setModel(model);
        loadProducts();

        saveButton.addActionListener(e -> saveProduct());
        searchButton.addActionListener(e -> searchProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = productTable.getSelectedRow();

                if (row != -1) {
                    String id = productTable.getValueAt(row, 0).toString();
                    String name = productTable.getValueAt(row, 1).toString();
                    String barcode = productTable.getValueAt(row, 2).toString();
                    String price = productTable.getValueAt(row, 3).toString();
                    String qty = productTable.getValueAt(row, 4).toString();
                    String supplier_id = productTable.getValueAt(row, 5).toString();

                    searchField.setText(id);
                    nameField.setText(name);
                    barcodeField.setText(barcode);
                    priceField.setText(price);
                    quantityField.setText(qty);
                    supplier_idField.setText(supplier_id);
                }
            }
        });

        quickSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public  void keyReleased(KeyEvent e) {
                String name = quickSearchField.getText();

                if (!name.isEmpty()) {
                    try {
                        String sql = "SELECT * FROM products WHERE name LIKE ?";
                        PreparedStatement pst = con.prepareStatement(sql);
                        pst.setString(1, "%" + name + "%");
                        ResultSet rs = pst.executeQuery();

                        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
                        model.setRowCount(0);

                        while (rs.next()) {
                            Vector v = new Vector();

                            v.add(rs.getString(1));
                            v.add(rs.getString(2));
                            v.add(rs.getString(3));
                            v.add(rs.getString(4));
                            v.add(rs.getString(5));
                            v.add(rs.getString(6));

                            model.addRow(v);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    loadProducts();
                }
            }
        });


    }

    private void deleteProduct() {
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(productPanel, "Please search or enter an ID to delete.");
            return;
        }
        try {
            String sql = "DELETE FROM products WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, id);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(productPanel, "Product deleted successfully!");
                loadProducts();
                nameField.setText("");
                barcodeField.setText("");
                priceField.setText("");
                quantityField.setText("");
                supplier_idField.setText("");
                searchField.setText("");
            }
            else {
                JOptionPane.showMessageDialog(productPanel, "No product found with ID: " + id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(productPanel, "Error deleting employee: " + e.getMessage());
        }

    }

    private void updateProduct() {
        String name = nameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String price = priceField.getText().trim();
        String quantity = quantityField.getText().trim();
        String supplier_id = supplier_idField.getText().trim();
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(productPanel, "Please search or enter an ID to update.");
            return;
        }

        try {
            String sql = "UPDATE products SET name = ?, bar_code = ?, price = ?, quantity = ?, supplier_id = ?  WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, barcode);
            pst.setBigDecimal(3, new BigDecimal(price));
            pst.setInt(4, Integer.parseInt(quantity));
            pst.setInt(5, Integer.parseInt(supplier_id));
            pst.setInt(6, Integer.parseInt(id));
            int rows = pst.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(productPanel, "Product updated successfully!");
                loadProducts();

                nameField.setText("");
                barcodeField.setText("");
                priceField.setText("");
                quantityField.setText("");
                supplier_idField.setText("");
                searchField.setText("");
            }
            else  {
                JOptionPane.showMessageDialog(productPanel, "No product found with ID: " + id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(productPanel, "Error: " + e.getMessage());
        }

    }

    private void searchProduct() {
        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            JOptionPane.showMessageDialog(productPanel, "Please enter an ID to search.");
            return;
        }

        try {
            String sql = "SELECT * FROM products WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, search);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                barcodeField.setText(rs.getString("bar_code"));
                priceField.setText(rs.getString("price"));
                quantityField.setText(rs.getString("quantity"));
                supplier_idField.setText(rs.getString("supplier_id"));
            }
            else {
                JOptionPane.showMessageDialog(productPanel, "Product ID not found.");
                nameField.setText("");
                barcodeField.setText("");
                priceField.setText("");
                quantityField.setText("");
                supplier_idField.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(productPanel, "Error : " + e.getMessage() );
        }
    }

    private void saveProduct() {
        String name = nameField.getText();
        String barcode = barcodeField.getText();
        String price = priceField.getText();
        String quantity = quantityField.getText();
        String supplier_id = supplier_idField.getText();

        if (name.isEmpty() || barcode.isEmpty() || price.isEmpty() || quantity.isEmpty() || supplier_id.isEmpty()) {
            JOptionPane.showMessageDialog(productPanel, "Please fill in all fields.");
            return;
        }

        try {
            String sql = "INSERT INTO products (name, bar_code, price, quantity, supplier_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, barcode);
            pst.setBigDecimal(3, new BigDecimal(price));
            pst.setInt(4, Integer.parseInt(quantity));
            pst.setInt(5, Integer.parseInt(supplier_id));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(productPanel, "Product saved successfully.");
            loadProducts();

            nameField.setText("");
            barcodeField.setText("");
            priceField.setText("");
            quantityField.setText("");
            supplier_idField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(productPanel, "Error: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            String sql = "SELECT * FROM products";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) productTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("bar_code"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getInt("supplier_id")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(productPanel, "Error loading products: " + e.getMessage());
        }

    }

    public JPanel getProductPanel() {
        return productPanel;
    }
}
