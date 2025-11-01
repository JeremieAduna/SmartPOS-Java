SmartPOS Java
SmartPOS is a desktop-based Point of Sale (POS) system built with Java Swing.
It is designed for small to medium-sized businesses that need a simple and efficient solution for managing sales, products, suppliers, employees, and customers without the need for an external database.

Features
• Customer Management – Add, edit, and view customer information
• Supplier Management – Track suppliers and supply records
• Employee Records – Manage employee profiles
• Product Management – Maintain product lists and pricing
• Sales and Invoicing – Process transactions and generate invoices
• CSV File Storage – Data stored locally for portability
• Simple GUI – Built with Java Swing for intuitive user interaction

Project Structure
pos-pro
│
├── csv files
│ └── invoices.csv (stores invoice data)
│
├── src
│ ├── Customer.java (handles customer logic)
│ ├── Employee.java (handles employee logic)
│ ├── Supplier.java (handles supplier logic)
│ ├── Product.java (manages products and prices)
│ ├── Sales.java (sales transactions)
│ ├── Invoice.java (invoice generation)
│ ├── Home.java (main dashboard window)
│ ├── Main.java (entry point of the program)
│ └── pos/pro/img (UI images and icons)
│
└── .gitignore

Requirements
• Java JDK 8 or later
• IntelliJ IDEA / NetBeans / Eclipse (any Java IDE)

How to Run
Clone this repository from GitHub.

Open the project in your Java IDE.

Run Main.java to start the POS system.

Screenshots
(optional – add screenshots from your img folder)

Home | Sales | Invoice

|
|
License
This project is open-source and available under the MIT License.
