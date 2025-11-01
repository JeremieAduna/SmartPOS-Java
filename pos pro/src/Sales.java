import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.Vector;

public class Sales {
    private JPanel panel1;
    private JPanel salesPanel;
    private JTable salesTable;
    private JComboBox customerComboBox;
    private JComboBox productComboBox;
    private JTextField qtyField;
    private JTextField paymentField;
    private JButton payPrintButton;
    private JButton addToCartButton;
    private JButton removeButton;
    private JButton removeAllButton;
    private JLabel invoiceNumberLabel;
    private JLabel unitPriceLabel;
    private JLabel totalLabel;
    private JLabel barcodeLabel;
    private JLabel totalAmountLabel;
    private JLabel balanceLabel;
    private JLabel totalqtyLabel;

    private Connection con;

    public Sales () {

        con = DB.myCon();
        DefaultTableModel model = new DefaultTableModel(
                new Object[] {"INVOICE #", "NAME", "BARCODE", "QTY", "UNIT PRICE", "TOTAL"},
                0
        );
        salesTable.setModel(model);
        loadCustomerData();
        loadProductData();
        loadInvoiceNumber();

        productComboBox.addActionListener(e -> {
            loadProductPrice();
            calculateTotal();
        });

        qtyField.addKeyListener( new KeyAdapter() {
            @Override
            public void  keyReleased(KeyEvent e) {
                calculateTotal();
            }
        });

        addToCartButton.addActionListener(e -> addToCart());
        removeButton.addActionListener(e -> removeOrder());
        removeAllButton.addActionListener(e -> removeAllOrder());

        totalAmount();

        paymentField.addKeyListener( new KeyAdapter() {
            @Override
            public void  keyReleased(KeyEvent e) {
                calculateBalance();
            }
        });

        payPrintButton.addActionListener(e -> payAndPrintReceipt());
    }

    private void incrementInvoiceNumber() {
        String updateSQL = "UPDATE extra SET val = val + 1 WHERE exid = 1";
        try (PreparedStatement pst = con.prepareStatement(updateSQL)) {
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // reload updated value back into the label
        loadInvoiceNumber();
    }

    private void loadInvoiceNumber() {
        String sql = "SELECT val FROM extra LIMIT 1";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                int invoiceNo = rs.getInt("val");
                invoiceNumberLabel.setText(String.valueOf(invoiceNo));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void payAndPrintReceipt() {

        String insertCartSQL = "INSERT INTO cart (invoice_id, product_name, bar_code, quantity, unit_price, total_price) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement pst = con.prepareStatement(insertCartSQL)) {
            DefaultTableModel dt = (DefaultTableModel) salesTable.getModel();
            int rc = dt.getRowCount();

            for (int i = 0; i < rc; i++) {
                pst.setString(1, dt.getValueAt(i, 0).toString()); // inid
                pst.setString(2, dt.getValueAt(i, 1).toString()); // product_name
                pst.setString(3, dt.getValueAt(i, 2).toString()); // barcode
                pst.setString(4, dt.getValueAt(i, 3).toString()); // quantity
                pst.setString(5, dt.getValueAt(i, 4).toString()); // unit_price
                pst.setString(6, dt.getValueAt(i, 5).toString()); // total_price

                pst.addBatch();
            }

            pst.executeBatch(); // executes all inserts at once
            JOptionPane.showMessageDialog(null, "Data saved!");
        } catch (HeadlessException | SQLException e) {
            System.out.println(e);
        }

        try {
            // sales DB
            String invoice_id = invoiceNumberLabel.getText();
            CustomerOption selectedCustomer = (CustomerOption) customerComboBox.getSelectedItem();
            int customer_id = selectedCustomer.id;
            String customer_name = selectedCustomer.name;
            String total_qty = totalqtyLabel.getText();
            String total_bill = totalAmountLabel.getText();
            String balance =  balanceLabel.getText();

            // paid status
            double total = Double.parseDouble(totalAmountLabel.getText());
            double paid = Double.parseDouble(paymentField.getText());
            String status;

            if (paid == 0.0)
                status = "Unpaid";
            else if (total > paid)
                status = "Partial";
            else
                status = "Paid";

            String insertSalesSQL = "INSERT INTO sales (invoice_id, customer_id, customer_name, total_quantity, total_bill, status, balance) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(insertSalesSQL);
            pst.setString(1, invoice_id);
            pst.setInt(2, customer_id);
            pst.setString(3, customer_name);
            pst.setString(4, total_qty);
            pst.setString(5, total_bill);
            pst.setString(6, status);
            pst.setString(7, balance);

            pst.executeUpdate();
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
        incrementInvoiceNumber();
    }

    private void calculateBalance() {

        Double payment = Double.valueOf(paymentField.getText());
        Double total = Double.valueOf(totalAmountLabel.getText());
        Double due;

        due = payment - total;
        balanceLabel.setText(String.valueOf(due));
    }

    private void totalAmount() {

        int numOfRow =  salesTable.getRowCount();
        double totalAmount = 0;

        for (int i = 0; i < numOfRow; i++) {
            double value = Double.valueOf(salesTable.getValueAt(i, 5).toString());
            totalAmount += value;
        }
        totalAmountLabel.setText(String.format("%.2f", totalAmount));

        int totalQty = 0;

        for (int i = 0; i < numOfRow; i++) {
            int qty = Integer.valueOf(salesTable.getValueAt(i, 3).toString());
            totalQty += qty;
        }
        totalqtyLabel.setText(String.valueOf(totalQty));
    }

    private void removeAllOrder() {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        model.setRowCount(0);

        totalAmount();
        calculateBalance();
    }

    private void removeOrder() {

        try {
            DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
            int row = salesTable.getSelectedRow();

            model.removeRow(row);
            totalAmount();
            calculateBalance();
        } catch (Exception e) {
        }
    }

    private void addToCart() {

        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();

        Vector v = new Vector<>();

        v.add(invoiceNumberLabel.getText());
        v.add(productComboBox.getSelectedItem().toString());
        v.add(barcodeLabel.getText());
        v.add(qtyField.getText());
        v.add(unitPriceLabel.getText());
        v.add(totalLabel.getText());

        model.addRow(v);
        totalAmount();
        calculateBalance();
    }

    private void calculateTotal() {
        String qty = qtyField.getText();
        String priceText = unitPriceLabel.getText();

        if(!qty.isEmpty() && priceText != null && !priceText.equals("N/A")){
            try {
                Double price = Double.valueOf(priceText);
                Double quantity = Double.valueOf(qty);
                Double total = price * quantity;

                totalLabel.setText(String.format("%.2f", total));
            } catch (NumberFormatException e) {
                totalLabel.setText("N/A");
            }
        }
        else {
            totalLabel.setText("N/A");
        }
    }


    private void loadProductPrice() {
        String selectedProduct = productComboBox.getSelectedItem().toString();

        if (selectedProduct == null || selectedProduct.isEmpty()) {
            unitPriceLabel.setText("N/A");
            return;
        }

        try {
            String sql = "SELECT bar_code, price FROM products WHERE name = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, selectedProduct);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
               unitPriceLabel.setText(rs.getBigDecimal("price").toString());
               barcodeLabel.setText(rs.getString("bar_code"));
            }
            else  {
                unitPriceLabel.setText("N/A");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading product price: " + e.getMessage());
        }
    }


    private void loadProductData() {
        try {
            String sql = "SELECT * FROM products";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            Vector<String> v = new Vector<>();

            while (rs.next()) {
                v.add(rs.getString(("name")));

                DefaultComboBoxModel com = new DefaultComboBoxModel<>(v);
                productComboBox.setModel(com);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage() );
        }
    }

    private void loadCustomerData() {
        try {
            String sql = "SELECT id, name FROM customers";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultComboBoxModel<CustomerOption> model = new DefaultComboBoxModel<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                model.addElement(new CustomerOption(id, name));
            }
            customerComboBox.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    public JPanel getSalesPanel() {
        return salesPanel;
    }
}
