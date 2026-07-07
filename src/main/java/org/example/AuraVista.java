import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class ComboItem {
    private int id;
    private String name;

    public ComboItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return name;
    }
}

class DatabaseConnection {
    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://localhost:3307/eventmanagement";
                String username = "root";
                String password = "data123";

                conn = DriverManager.getConnection(url, username, password);
                System.out.println("Database connected.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }
}

class Booking {
    Connection conn;
    public JPanel bPanel = new JPanel(new BorderLayout());
    private JLabel hText = new JLabel("Book Event");
    private JPanel AddPanel = new JPanel(new GridBagLayout());

    JTextField bDate;
    JTextField customerName;
    JTextField customerCNIC;
    JTextField customerPhone;
    JTextField bPrice;

    private String[] methods = { "Credit Card", "Cash", "Online Payment" };

    JLabel date, event, venue, customer, cnic, phone, price, method;
    JComboBox<ComboItem> evTypes;
    JComboBox<ComboItem> evVenues;
    JComboBox<String> pMethods;

    private JButton addEvent;

    public Booking() {

        // Header styling
        hText.setFont(new Font("Montserrat", Font.BOLD, 20));
        hText.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Main form panel padding
        AddPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Input fields
        date = new JLabel("Event Date:");
        date.setFont(new Font("Montserrat", Font.BOLD, 14));
        bDate = new JTextField(20);
        bDate.setFont(new Font("Montserrat", Font.PLAIN, 14));

        event = new JLabel("Event Types:");
        event.setFont(new Font("Montserrat", Font.BOLD, 14));
        evTypes = new JComboBox<>();
        loadComboBoxData("eventtypes", "EventTypeId", "EventTypeName", evTypes);
        evTypes.setFont(new Font("Montserrat", Font.PLAIN, 14));

        venue = new JLabel("Venue:");
        venue.setFont(new Font("Montserrat", Font.BOLD, 14));
        evVenues = new JComboBox<>();
        loadComboBoxData("venues", "VenueID", "VenueName", evVenues);
        evVenues.setFont(new Font("Montserrat", Font.PLAIN, 14));

        cnic = new JLabel("CNIC (xxxxx-xxxxxxx-x):");
        cnic.setFont(new Font("Montserrat", Font.BOLD, 14));
        customerCNIC = new JTextField(15);
        customerCNIC.setFont(new Font("Montserrat", Font.PLAIN, 14));

        customer = new JLabel("Customer Name:");
        customer.setFont(new Font("Montserrat", Font.BOLD, 14));
        customerName = new JTextField(30);
        customerName.setFont(new Font("Montserrat", Font.PLAIN, 14));

        phone = new JLabel("Customer Phone:");
        phone.setFont(new Font("Montserrat", Font.BOLD, 14));
        customerPhone = new JTextField();
        customerPhone.setFont(new Font("Montserrat", Font.PLAIN, 14));

        price = new JLabel("Booking Price:");
        price.setFont(new Font("Montserrat", Font.BOLD, 14));
        bPrice = new JTextField();
        bPrice.setFont(new Font("Montserrat", Font.PLAIN, 14));

        method = new JLabel("Payment Method:");
        method.setFont(new Font("Montserrat", Font.BOLD, 14));
        pMethods = new JComboBox<>();
        pMethods.addItem("Cash");
        pMethods.addItem("Credit Card");
        pMethods.addItem("Bank Transfer");
        pMethods.setFont(new Font("Montserrat", Font.PLAIN, 14));

        // Add all components to AddPanel with GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addRow(date, bDate, gbc, y++);
        addRow(event, evTypes, gbc, y++);
        addRow(venue, evVenues, gbc, y++);
        addRow(cnic, customerCNIC, gbc, y++);
        addRow(customer, customerName, gbc, y++);
        addRow(phone, customerPhone, gbc, y++);
        addRow(price, bPrice, gbc, y++);
        addRow(method, pMethods, gbc, y++);

        addEvent = new JButton("Book Event");
        addEvent.setBackground(Color.BLACK);
        addEvent.setForeground(Color.WHITE);
        addEvent.setFont(new Font("Arial", Font.BOLD, 16));
        addEvent.setAlignmentX(Component.CENTER_ALIGNMENT);
        addEvent.setPreferredSize(new Dimension(150, 40));
        addEvent.setMaximumSize(new Dimension(150, 40));
        addEvent.setMinimumSize(new Dimension(150, 40));
        addEvent.setBorderPainted(false);
        addEvent.setFocusPainted(false);

        addEvent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertBooking();
            }
        });
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(Box.createVerticalStrut(100));
        buttonWrapper.add(addEvent);

        bPanel.add(hText, BorderLayout.NORTH);
        bPanel.add(AddPanel, BorderLayout.CENTER);
        bPanel.add(buttonWrapper, BorderLayout.SOUTH);
    }

    private void addRow(JLabel label, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.3;
        AddPanel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        AddPanel.add(field, gbc);

    }

    private void loadComboBoxData(String tableName, String idCol, String nameCol, JComboBox<ComboItem> comboBox) {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT " + idCol + ", " + nameCol + " FROM " + tableName;

        try (PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comboBox.addItem(new ComboItem(rs.getInt(idCol), rs.getString(nameCol)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertBooking() {
        // Field validation
        if (bDate.getText().trim().isEmpty() ||
                customerName.getText().trim().isEmpty() ||
                customerCNIC.getText().trim().isEmpty() ||
                customerPhone.getText().trim().isEmpty() ||
                bPrice.getText().trim().isEmpty() ||
                evTypes.getSelectedItem() == null ||
                evVenues.getSelectedItem() == null ||
                pMethods.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields.");
            return;
        }

        String date = bDate.getText().trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(null, "Invalid date format. Use YYYY-MM-DD (e.g., 2025-12-31)");
            return;
        }

        // CNIC format validation
        String cnic = customerCNIC.getText().trim();
        if (!cnic.matches("\\d{5}-\\d{7}-\\d{1}")) {
            JOptionPane.showMessageDialog(null, "Invalid CNIC format. Use xxxxx-xxxxxxx-x");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "Database connection failed");
            return;
        }

        try {
            conn.setAutoCommit(false); // Begin transaction

            // 3. Insert into customers
            String customerSQL = "INSERT INTO customers (CustomerName, CustomerCNIC, CustomerPhone) VALUES (?, ?, ?)";
            int customerId = -1;

            try (PreparedStatement custStmt = conn.prepareStatement(customerSQL, Statement.RETURN_GENERATED_KEYS)) {
                custStmt.setString(1, customerName.getText().trim());
                custStmt.setString(2, cnic);
                custStmt.setString(3, customerPhone.getText().trim());
                custStmt.executeUpdate();

                ResultSet rs = custStmt.getGeneratedKeys();
                if (rs.next()) {
                    customerId = rs.getInt(1);
                }
            }

            if (customerId == -1)
                throw new SQLException("Failed to get customer ID.");

            // 4. Insert into bookings
            int bookingId = -1;
            String bookingSQL = "INSERT INTO eventbooking (EventDate, EventTypeId, VenueId, CustomerId, EventBookedPrice) "
                    +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement bookStmt = conn.prepareStatement(bookingSQL, Statement.RETURN_GENERATED_KEYS)) {
                bookStmt.setString(1, bDate.getText().trim());

                ComboItem selectedEvent = (ComboItem) evTypes.getSelectedItem();
                ComboItem selectedVenue = (ComboItem) evVenues.getSelectedItem();

                bookStmt.setInt(2, selectedEvent.getId());
                bookStmt.setInt(3, selectedVenue.getId());
                bookStmt.setInt(4, customerId);
                bookStmt.setDouble(5, Double.parseDouble(bPrice.getText().trim()));

                bookStmt.executeUpdate();
                ResultSet rs = bookStmt.getGeneratedKeys();
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                }
            }
            if (bookingId == -1)
                throw new SQLException("Failed to insert booking.");

            // Step 3: Insert into payments
            String paymentSQL = "INSERT INTO payments (BookingId, Amount, PaymentDate, PaymentMethod) VALUES (?, ?, ?,?)";
            try (PreparedStatement payStmt = conn.prepareStatement(paymentSQL)) {
                payStmt.setInt(1, bookingId);
                payStmt.setDouble(2, Double.parseDouble(bPrice.getText().trim()));
                payStmt.setString(3, bDate.getText().trim());
                payStmt.setString(4, pMethods.getSelectedItem().toString());
                payStmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(null, "Booking and payment saved successfully!");

        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            try {
                conn.rollback(); // rollback if failed
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

}

class viewBooking {
    JPanel viewPanel = new JPanel(new BorderLayout());
    JLabel hText;
    JTable bookingTable;
    JScrollPane scrollPane;

    public viewBooking() {
        // Header label
        hText = new JLabel("Booked Events");
        hText.setFont(new Font("Montserrat", Font.BOLD, 22));
        hText.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Column names
        String[] columns = {
                "BookingID", "Date", "Event Type", "Venue", "CNIC",
                "Customer Name", "Phone", "Price", "Payment Method"
        };

        ArrayList<String[]> rows = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            String query = """
                        SELECT
                            eb.BookingID,
                            eb.EventDate,
                            et.EventTypeName AS event_type,
                            v.VenueName AS venue,
                            c.CustomerCNIC,
                            c.CustomerName AS customer_name,
                            c.CustomerPhone,
                            eb.EventBookedPrice,
                            p.PaymentMethod AS payment_method
                        FROM eventbooking eb
                        JOIN eventtypes et ON eb.eventTypeID = et.eventTypeID
                        JOIN venues v ON eb.venueID = v.venueID
                        JOIN customers c ON eb.customerID = c.customerID
                        JOIN payments p ON p.bookingID = eb.bookingID
                    """;

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                rows.add(new String[] {
                        rs.getString("bookingID"),
                        rs.getString("EventDate"),
                        rs.getString("event_type"),
                        rs.getString("venue"),
                        rs.getString("CustomerCNIC"),
                        rs.getString("customer_name"),
                        rs.getString("CustomerPhone"),
                        rs.getString("EventBookedPrice"),
                        rs.getString("payment_method")
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert list to 2D array
        String[][] data = new String[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i);
        }

        // Create table
        bookingTable = new JTable(data, columns);
        bookingTable.setFont(new Font("Montserrat", Font.PLAIN, 14));
        bookingTable.setRowHeight(28);
        bookingTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 14));
        bookingTable.getTableHeader().setForeground(Color.WHITE);
        bookingTable.getTableHeader().setBackground(Color.BLACK);

        // Wrap in scroll pane
        scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Add to main panel
        viewPanel.add(hText, BorderLayout.NORTH);
        viewPanel.add(scrollPane, BorderLayout.CENTER);
    }

}

class updateBooking {
    JPanel updPanel = new JPanel(new BorderLayout());
    JLabel hText;
    JPanel centerdltPanel = new JPanel(new GridBagLayout());
    JLabel bId;
    JTextField dltInp;
    JButton loadBtn;

    // Removed the instance Connection conn

    public updateBooking() {
        setupUI();
    }

    void setupUI() {
        // Header
        hText = new JLabel("Update Booked Event");
        hText.setFont(new Font("Montserrat", Font.BOLD, 24));
        hText.setHorizontalAlignment(SwingConstants.CENTER);
        hText.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));

        // Booking ID input
        bId = new JLabel("Enter Booking ID:");
        bId.setFont(new Font("Montserrat", Font.BOLD, 16));

        dltInp = new JTextField();
        dltInp.setFont(new Font("Montserrat", Font.PLAIN, 16));
        dltInp.setPreferredSize(new Dimension(250, 30));

        loadBtn = new JButton("Load Event");
        loadBtn.setBackground(new Color(33, 33, 33));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setFont(new Font("Montserrat", Font.BOLD, 16));
        loadBtn.setFocusPainted(false);
        loadBtn.setBorderPainted(false);
        loadBtn.setPreferredSize(new Dimension(160, 40));

        // Layout Booking ID row
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        centerdltPanel.add(bId, gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        centerdltPanel.add(dltInp, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        centerdltPanel.add(loadBtn, gbc);

        // Add to main panel
        updPanel.add(hText, BorderLayout.NORTH);
        updPanel.add(centerdltPanel, BorderLayout.CENTER);

        // Button listener
        loadBtn.addActionListener(e -> {
            String idText = dltInp.getText().trim();
            if (!idText.matches("\\d+")) {
                JOptionPane.showMessageDialog(null, "Please enter a valid numeric Booking ID.");
                return;
            }

            int bookingId = Integer.parseInt(idText);
            showUpdateDialog(bookingId);
        });
    }

    private void showUpdateDialog(int bookingId) {
        Connection conn = DatabaseConnection.getConnection();  // local connection

        String query = """
            SELECT eb.EventDate, eb.EventTypeId, eb.VenueId, eb.EventBookedPrice,
                   p.PaymentMethod
            FROM eventbooking eb
            JOIN payments p ON eb.BookingId = p.BookingId
            WHERE eb.BookingId = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Booking ID not found.");
                return;
            }

            // Populating data
            String eventDate = rs.getString("EventDate");
            int eventTypeId = rs.getInt("EventTypeId");
            int venueId = rs.getInt("VenueId");
            double price = rs.getDouble("EventBookedPrice");
            String paymentMethod = rs.getString("PaymentMethod");

            // Build dialog with form
            JDialog dialog = new JDialog((Frame) null, "Update Booking", true);
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.EAST;

            JLabel lblDate = new JLabel("Event Date (YYYY-MM-DD):");
            JTextField txtDate = new JTextField(eventDate, 15);

            JLabel lblEventType = new JLabel("Event Type:");
            JComboBox<ComboItem> cbEventType = new JComboBox<>();
            loadEventTypes(cbEventType, conn);
            selectComboBoxItem(cbEventType, eventTypeId);

            JLabel lblVenue = new JLabel("Venue:");
            JComboBox<ComboItem> cbVenue = new JComboBox<>();
            loadVenues(cbVenue, conn);
            selectComboBoxItem(cbVenue, venueId);

            JLabel lblPrice = new JLabel("Price:");
            JTextField txtPrice = new JTextField(String.valueOf(price), 15);

            JLabel lblPaymentMethod = new JLabel("Payment Method:");
            JComboBox<String> cbPaymentMethod = new JComboBox<>(new String[] {
                    "Cash", "Credit Card", "Bank Transfer", "Cheque"
            });
            cbPaymentMethod.setSelectedItem(paymentMethod);

            JButton saveBtn = new JButton("Save Changes");
            saveBtn.setBackground(new Color(33, 33, 33));
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFont(new Font("Montserrat", Font.BOLD, 16));
            saveBtn.setFocusPainted(false);

            // Layout fields
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(lblDate, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(txtDate, gbc);

            gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
            formPanel.add(lblEventType, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(cbEventType, gbc);

            gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
            formPanel.add(lblVenue, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(cbVenue, gbc);

            gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
            formPanel.add(lblPrice, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(txtPrice, gbc);

            gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
            formPanel.add(lblPaymentMethod, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(cbPaymentMethod, gbc);

            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(saveBtn, gbc);

            dialog.add(formPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            // Pass conn to save button logic
            saveBtn.addActionListener(e -> {
                System.out.println("Save button clicked");  // Check if button fires

                Connection conn1 = null;
                try {
                    conn1 = DatabaseConnection.getConnection();
                    if (conn1 == null) {
                        System.out.println("Connection is null!");
                        JOptionPane.showMessageDialog(dialog, "Database connection failed.");
                        return;
                    }
                    conn1.setAutoCommit(false);

                    String date = txtDate.getText().trim();
                    double newPrice = Double.parseDouble(txtPrice.getText().trim());

                    ComboItem selectedEventType = (ComboItem) cbEventType.getSelectedItem();
                    ComboItem selectedVenue = (ComboItem) cbVenue.getSelectedItem();
                    String selectedPayment = (String) cbPaymentMethod.getSelectedItem();

                    System.out.printf("Updating bookingId=%d with date=%s, price=%.2f, eventType=%d, venue=%d, payment=%s%n",
                            bookingId, date, newPrice, selectedEventType.getId(), selectedVenue.getId(), selectedPayment);

                    String updateBooking = """
            UPDATE eventbooking
            SET EventDate = ?, EventTypeId = ?, VenueId = ?, EventBookedPrice = ?
            WHERE BookingId = ?
            """;

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateBooking)) {
                        updateStmt.setString(1, date);
                        updateStmt.setInt(2, selectedEventType.getId());
                        updateStmt.setInt(3, selectedVenue.getId());
                        updateStmt.setDouble(4, newPrice);
                        updateStmt.setInt(5, bookingId);

                        int rows = updateStmt.executeUpdate();
                        System.out.println("eventbooking rows updated: " + rows);
                        if (rows == 0) throw new SQLException("No rows updated in eventbooking");
                    }

                    String updatePayment = """
            UPDATE payments
            SET Amount = ?, PaymentDate = ?, PaymentMethod = ?
            WHERE BookingId = ?
            """;

                    try (PreparedStatement updateStmt = conn.prepareStatement(updatePayment)) {
                        updateStmt.setDouble(1, newPrice);
                        updateStmt.setString(2, date);
                        updateStmt.setString(3, selectedPayment);
                        updateStmt.setInt(4, bookingId);

                        int rows = updateStmt.executeUpdate();
                        System.out.println("payments rows updated: " + rows);
                        if (rows == 0) throw new SQLException("No rows updated in payments");
                    }

                    conn1.commit();
                    JOptionPane.showMessageDialog(dialog, "Event updated successfully!");
                    dialog.dispose();

                } catch (Exception ex) {
                    System.err.println("Update error: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Update failed: " + ex.getMessage());
                    if (conn1 != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException se) {
                            se.printStackTrace();
                        }
                    }
                } finally {
                    if (conn != null) {
                        try {
                            conn1.setAutoCommit(true);
                            conn1.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading booking: " + e.getMessage());
        }
    }

    private void selectComboBoxItem(JComboBox<ComboItem> comboBox, int idToSelect) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            ComboItem item = comboBox.getItemAt(i);
            if (item.getId() == idToSelect) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadEventTypes(JComboBox<ComboItem> comboBox, Connection conn) {
        comboBox.removeAllItems();
        String sql = "  ";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboBox.addItem(new ComboItem(rs.getInt("EventTypeId"), rs.getString("EventTypeName")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to load event types: " + e.getMessage());
        }
    }

    private void loadVenues(JComboBox<ComboItem> comboBox, Connection conn) {
        comboBox.removeAllItems();
        String sql = "SELECT VenueId, VenueName FROM venues";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboBox.addItem(new ComboItem(rs.getInt("VenueId"), rs.getString("VenueName")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to load venues: " + e.getMessage());
        }
    }

    public JPanel getPanel() {
        return updPanel;
    }

    public static class ComboItem {
        private int id;
        private String name;

        public ComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return name;
        }
    }
}

class cancelBooking {
    JPanel cnclPanel = new JPanel(new BorderLayout());
    JLabel hText;
    JPanel centerdltPanel = new JPanel(new GridBagLayout());
    JLabel bId;
    JTextField dltInp;
    JButton delete;

    public cancelBooking() {
        delete();
    }

    void delete() {
        // Title/Header
        hText = new JLabel("Delete Booked Event");
        hText.setFont(new Font("Montserrat", Font.BOLD, 24));
        hText.setHorizontalAlignment(SwingConstants.CENTER);
        hText.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));

        // Booking ID Label & Field
        bId = new JLabel("Enter Booking ID:");
        bId.setFont(new Font("Montserrat", Font.BOLD, 16));

        dltInp = new JTextField(20);
        dltInp.setFont(new Font("Montserrat", Font.PLAIN, 16));
        dltInp.setPreferredSize(new Dimension(250, 30));

        // Delete Button Styling
        delete = new JButton("Delete Event");
        delete.setBackground(new Color(33, 33, 33));
        delete.setForeground(Color.WHITE);
        delete.setFont(new Font("Montserrat", Font.BOLD, 16));
        delete.setFocusPainted(false);
        delete.setBorderPainted(false);
        delete.setPreferredSize(new Dimension(160, 40));

        // Add components using GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        centerdltPanel.add(bId, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        centerdltPanel.add(dltInp, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerdltPanel.add(delete, gbc);

        // Add everything to main panel
        cnclPanel.add(hText, BorderLayout.NORTH);
        cnclPanel.add(centerdltPanel, BorderLayout.CENTER);

        // Add action listener to delete button
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookingIdText = dltInp.getText().trim();
                if (bookingIdText.isEmpty()) {
                    JOptionPane.showMessageDialog(cnclPanel, "Please enter a Booking ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int bookingId;
                try {
                    bookingId = Integer.parseInt(bookingIdText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(cnclPanel, "Booking ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(cnclPanel, "Are you sure you want to delete booking ID " + bookingId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                // Delete booking from database
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "DELETE FROM EventBooking WHERE BookingID = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setInt(1, bookingId);

                    int affectedRows = pst.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(cnclPanel, "Booking ID " + bookingId + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dltInp.setText("");
                    } else {
                        JOptionPane.showMessageDialog(cnclPanel, "Booking ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    pst.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(cnclPanel, "Database error occurred while deleting.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}


class Users {
    String name, email, password;

    Users(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}

class Home {
    JButton home, booking, view, update, delete; // Declare these FIRST
    JButton[] menuButtons;
    JButton logout;
    JPanel centerPanel;
    Booking bkEvent;
    viewBooking vwBooking;
    updateBooking updBooking;
    cancelBooking cnclBooking;

    public Home() {
        home = createMenuButton("Home");
        booking = createMenuButton("Booking");
        view = createMenuButton("View Events");
        update = createMenuButton("Update Event");
        delete = createMenuButton("Delete Event");

        menuButtons = new JButton[] { home, booking, view, update, delete }; // Initialize after declaration

        JFrame frame = new JFrame("AuraVista Event And Planners");
        frame.setSize(1400, 768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container cn = frame.getContentPane();
        cn.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        header.setPreferredSize(new Dimension(1366, 70));
        JLabel heading = new JLabel("AuraVista Event And Planners");
        heading.setFont(new Font("Montserrat", Font.BOLD, 18));
        logout = new JButton("Logout");
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setFont(new Font("Montserrat", Font.BOLD, 14));
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        header.setPreferredSize(new Dimension(1400, 70));
        header.add(heading, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        cn.add(header, BorderLayout.NORTH);

        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setPreferredSize(new Dimension(250, 628));
        sideMenu.setBackground(Color.BLACK);

        sideMenu.add(Box.createVerticalStrut(70));
        sideMenu.add(home);
        sideMenu.add(Box.createVerticalStrut(20));
        sideMenu.add(booking);
        sideMenu.add(Box.createVerticalStrut(20));
        sideMenu.add(view);
        sideMenu.add(Box.createVerticalStrut(20));
        sideMenu.add(update);
        sideMenu.add(Box.createVerticalStrut(20));
        sideMenu.add(delete);

        cn.add(sideMenu, BorderLayout.WEST);
        setActiveButton(home, menuButtons);

        // centerPanel
        centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        cn.add(centerPanel, BorderLayout.CENTER);
        showHomePanel();

        for (JButton btn : menuButtons) {
            btn.addActionListener(e -> {
                setActiveButton(btn, menuButtons);

                // Clear centerPanel
                centerPanel.removeAll();
                centerPanel.setLayout(new BorderLayout());

                if (btn == home) {
                    showHomePanel();
                } else if (btn == booking) {
                    bkEvent = new Booking();

                    centerPanel.add(bkEvent.bPanel, BorderLayout.CENTER);
                } else if (btn == view) {
                    vwBooking = new viewBooking();
                    centerPanel.add(vwBooking.viewPanel, BorderLayout.CENTER);
                } else if (btn == update) {
                    updBooking = new updateBooking();
                    centerPanel.add(updBooking.updPanel, BorderLayout.CENTER);
                } else if (btn == delete) {
                    cnclBooking = new cancelBooking();
                    centerPanel.add(cnclBooking.cnclPanel, BorderLayout.CENTER);
                }

                centerPanel.revalidate();
                centerPanel.repaint();
            });
        }

        JPanel footer = new JPanel(new GridBagLayout());
        JLabel copyRight = new JLabel("Copy@ Right AuraVista 2025");
        copyRight.setFont(new Font("Arial", Font.BOLD, 15));
        footer.setBackground(new Color(0xEEEEEE));
        footer.setPreferredSize(new Dimension(1366, 70));

        footer.add(copyRight);
        cn.add(footer, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Montserrat", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void setActiveButton(JButton active, JButton[] buttons) {
        for (JButton btn : buttons) {
            if (btn == active) {
                btn.setFont(underlineFont(btn.getFont()));
            } else {
                clearButtonStyles();
            }
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
        }
    }

    private Font underlineFont(Font font) {

        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        return font.deriveFont(attributes);
    }

    private void clearButtonStyles() {
        for (JButton btn : menuButtons) {
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
        }
    }

    private void showHomePanel() {
        centerPanel.removeAll();
        centerPanel.setLayout(new GridLayout(2, 2, 40, 40));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        String[] titles = { "Happy Clients", "Project", "Working Hours" };
        String[] details = { "103", "200", "704" };
        String[] iconPaths = {
                "icons/customer.png",
                "icons/closure.png",
                "icons/working.png",
        };

        for (int i = 0; i < titles.length; i++) {
            JPanel card = new JPanel();
            card.setPreferredSize(new Dimension(200, 100));
            card.setLayout(new BorderLayout());
            card.setBackground(new Color(0xF5F5F5));
            card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Top/Left/Bottom/Right padding
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // ===== Icon Label =====
            JLabel iconLabel = new JLabel();
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPaths[i]));
                Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                iconLabel.setText("No Icon");
            }
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // ===== Text and Number Panel =====
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // spacing between text and number
            textPanel.setOpaque(false); // transparent background

            JLabel textLabel = new JLabel(titles[i]);
            textLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel numLabel = new JLabel(String.valueOf(details[i])); // Convert int to String
            numLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
            numLabel.setHorizontalAlignment(SwingConstants.CENTER);

            textPanel.add(textLabel);
            textPanel.add(numLabel);

            // ===== Add to Card =====
            card.add(iconLabel, BorderLayout.NORTH);
            card.add(textPanel, BorderLayout.CENTER);

            centerPanel.add(card);
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }
}

class Login implements ActionListener {
    JFrame loginFrame;
    JPanel loginPanel;
    JTextField email;
    JPasswordField password;
    JButton loginBtn;
    Users users[];

    Home home;

    public Login() {
        users = new Users[2];
        users[0] = new Users("Admin", "admin", "admin");
        users[1] = new Users("Shaheer", "shaheer@gmail.com", "shaheer123!");
        loginFrame = new JFrame("AuraVista Event And Planners");

        loginFrame.setSize(1400, 770);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null); // Center the frame

        Container cn = loginFrame.getContentPane();
        cn.setLayout(new BorderLayout());

        // Top heading
        JLabel heading = new JLabel("AuraVista Event And Planners", SwingConstants.CENTER);
        heading.setFont(new Font("Montserrat", Font.BOLD, 24));
        heading.setBackground(Color.BLACK);
        heading.setForeground(Color.WHITE);
        heading.setOpaque(true);

        heading.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10)); // top, left, bottom, right
        cn.add(heading, BorderLayout.NORTH);

        // Login panel container to center everything
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        cn.add(centerPanel, BorderLayout.CENTER);

        // Login form panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(5, 1, 10, 10));
        loginPanel.setPreferredSize(new Dimension(400, 250));
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));
        loginPanel.setBackground(Color.BLACK);

        // Form fields
        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField();
        emailLabel.setForeground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password:");
        password = new JPasswordField();
        passwordLabel.setForeground(Color.WHITE);

        loginBtn = new JButton("Login");
        loginBtn.setBackground(Color.decode("#EEEEEE"));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginPanel.add(emailLabel);
        loginPanel.add(email);
        loginPanel.add(passwordLabel);
        loginPanel.add(password);
        loginPanel.add(loginBtn);

        loginBtn.addActionListener(this);
        // Center the loginPanel in the centerPanel
        centerPanel.add(loginPanel);

        loginFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            String eml;
            eml = email.getText();
            char[] passwordChars = password.getPassword();
            String pass = new String(passwordChars);

            for (int i = 0; i < users.length; i++) {
                if ((eml.equals(users[i].email)) && (pass.equals(users[i].password))) {
                    home = new Home();
                    loginFrame.setVisible(false);
                }
            }
        }
    }
}

public class AuraVista {
    public static void main(String[] args) {
        Login login = new Login();
    }

}
