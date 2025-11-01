import javax.swing.*;
import java.awt.*;

public class Home {
    private JButton customerButton;
    private JButton supplierButton;
    private JButton employeeButton;
    private JButton productButton;
    private JButton salesButton;
    private JButton invoiceButton;
    private JPanel menuPanel;
    private JPanel homePanel;
    private JPanel loadPanel;
    private JLabel logoLabel;
    private JFrame frame;

    public Home() {
        frame = new JFrame("POS System");
        frame.setContentPane(homePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon("src/pos/pro/img/japzonelogo.png");
        Image scaled = icon.getImage().getScaledInstance(750, 200, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);
        logoLabel.setIcon(scaledIcon);

        setButtonIcons();

        loadPanel.setLayout(new BorderLayout());
        initActions();
        showCustomerPanel();
    }

    private void setButtonIcons() {
        setButtonIcon(customerButton, "src/pos/pro/img/customer.png");
        setButtonIcon(supplierButton, "src/pos/pro/img/supplier.png");
        setButtonIcon(employeeButton, "src/pos/pro/img/employee.png");
        setButtonIcon(productButton, "src/pos/pro/img/product.png");
        setButtonIcon(salesButton, "src/pos/pro/img/sales1.png");
        setButtonIcon(invoiceButton, "src/pos/pro/img/invoice.png");
    }

    private void setButtonIcon(JButton button, String path) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaled));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(8);
    }

    private void initActions() {
        customerButton.addActionListener(e -> showCustomerPanel());
        supplierButton.addActionListener(e -> showSupplierPanel());
        employeeButton.addActionListener(e -> showEmployeePanel());
        productButton.addActionListener(e -> showProductPanel());
        salesButton.addActionListener(e -> showSales() );
        invoiceButton.addActionListener(e -> showInvoicePanel());

    }

    private void showInvoicePanel() {
        Invoice invoice = new Invoice();
        loadPanel.removeAll();
        loadPanel.add(invoice.getInvoicePanel());
        loadPanel.revalidate();
        loadPanel.repaint();
    }

    private void showSales() {
        Sales sales = new Sales();
        loadPanel.removeAll();
        loadPanel.add(sales.getSalesPanel());
        loadPanel.revalidate();
        loadPanel.repaint();
    }

    private void showCustomerPanel() {
        Customer customer = new Customer();
        loadPanel.removeAll();
        loadPanel.add(customer.getCustomerPanel(), BorderLayout.CENTER);
        loadPanel.revalidate();
        loadPanel.repaint();
    }
    private void showSupplierPanel() {
        Supplier supplier = new Supplier();
        loadPanel.removeAll();
        loadPanel.add(supplier.getSupplierPanel(), BorderLayout.CENTER);
        loadPanel.revalidate();
        loadPanel.repaint();
    }
    private void showEmployeePanel() {
        Employee employee = new Employee();
        loadPanel.removeAll();
        loadPanel.add(employee.getEmployeePanelPanel(), BorderLayout.CENTER);
        loadPanel.revalidate();
        loadPanel.repaint();
    }

    private void showProductPanel() {
        Product product = new Product();
        loadPanel.removeAll();
        loadPanel.add(product.getProductPanel(), BorderLayout.CENTER);
        loadPanel.revalidate();
        loadPanel.repaint();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
