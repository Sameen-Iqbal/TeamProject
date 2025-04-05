package UI;

import db.dbConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * TicketManagementPanel
 * Allows managers to create shows and manage ticket inventory
 */
public class TicketManagementPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 246, 250);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color PRIMARY_COLOR = new Color(60, 90, 153);
    
    private JTabbedPane tabbedPane;
    private JTable showsTable;
    private DefaultTableModel showsTableModel;
    private JTable ticketSalesTable;
    private DefaultTableModel ticketSalesTableModel;
    private JComboBox<String> venueComboBox;
    private JComboBox<String> hallTypeComboBox;
    
    private Map<String, Integer> venueIdMap = new HashMap<>();
    
    private JTable ticketsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> showFilterComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField ticketIdField;
    private JTextField patronNameField;
    
    public TicketManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Create tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Create tabs
        tabbedPane.addTab("Shows Overview", createShowsOverviewPanel());
        tabbedPane.addTab("Create New Show", createNewShowPanel());
        tabbedPane.addTab("Ticket Search", createTicketSearchPanel());
        tabbedPane.addTab("Ticket Sales Report", createTicketSalesPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Load data
        loadShows();
        loadTicketSales();
    }
    
    private JPanel createShowsOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create table model with column names
        showsTableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {
                "ID", "Show Title", "Date", "Venue", "Hall Type", "Price (£)", "Total Seats", "Sold", "Available"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        showsTable = new JTable(showsTableModel);
        showsTable.setRowHeight(25);
        showsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        showsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        showsTable.getTableHeader().setBackground(PRIMARY_COLOR);
        showsTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(showsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Header section
        JPanel headerPanel = createHeaderPanel("Shows Overview", "View and manage all current and upcoming shows");
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(CARD_COLOR);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton refreshButton = createButton("Refresh Shows", PRIMARY_COLOR);
        refreshButton.addActionListener(e -> loadShows());
        
        JButton printButton = createButton("Print Report", new Color(31, 122, 140));
        printButton.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Printing functionality will be implemented in future updates", 
            "Print Report", JOptionPane.INFORMATION_MESSAGE));
        
        actionPanel.add(refreshButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(printButton);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createNewShowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header section
        JPanel headerPanel = createHeaderPanel("Create New Show", "Add a new show to the system and make tickets available for sale");
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Show title
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Show Title:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(titleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JTextField titleField = new JTextField(30);
        titleField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(titleField, gbc);
        
        // Show date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel dateLabel = new JLabel("Show Date (YYYY-MM-DD):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        JTextField dateField = new JTextField(10);
        dateField.setFont(new Font("Arial", Font.PLAIN, 14));
        // Set default date to today
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(sdf.format(new Date()));
        formPanel.add(dateField, gbc);
        
        // Venue
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel venueLabel = new JLabel("Venue:");
        venueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(venueLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        venueComboBox = new JComboBox<>();
        venueComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(venueComboBox, gbc);
        
        // Hall type
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel hallTypeLabel = new JLabel("Hall Type:");
        hallTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(hallTypeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        hallTypeComboBox = new JComboBox<>(new String[]{"Main Hall", "Small Hall"});
        hallTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(hallTypeComboBox, gbc);
        
        // Base ticket price
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JLabel priceLabel = new JLabel("Base Ticket Price (£):");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(priceLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        JTextField priceField = new JTextField(10);
        priceField.setFont(new Font("Arial", Font.PLAIN, 14));
        priceField.setText("50.00");
        formPanel.add(priceField, gbc);
        
        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setBackground(CARD_COLOR);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton clearButton = createButton("Clear Form", new Color(150, 150, 150));
        clearButton.addActionListener(e -> {
            titleField.setText("");
            dateField.setText(sdf.format(new Date()));
            priceField.setText("50.00");
        });
        
        JButton createButton = createButton("Create Show", new Color(46, 125, 50));
        createButton.addActionListener(e -> {
            // Validate form
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a show title", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    JOptionPane.showMessageDialog(this, "Price must be greater than 0", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String selectedVenue = (String) venueComboBox.getSelectedItem();
            if (selectedVenue == null) {
                JOptionPane.showMessageDialog(this, "Please select a venue", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int venueId = venueIdMap.get(selectedVenue);
            String hallType = (String) hallTypeComboBox.getSelectedItem();
            
            // Add show to database
            int showId = dbConnection.addNewShow(
                titleField.getText().trim(),
                dateField.getText().trim(),
                venueId,
                hallType,
                price
            );
            
            if (showId > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Show created successfully with ID: " + showId, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear form
                titleField.setText("");
                dateField.setText(sdf.format(new Date()));
                priceField.setText("50.00");
                
                // Refresh shows table
                loadShows();
                
                // Switch to shows overview tab
                tabbedPane.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create show. Please check your inputs and try again.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        actionsPanel.add(clearButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionsPanel.add(createButton);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        formPanel.add(actionsPanel, gbc);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Load venues
        loadVenues();
        
        return panel;
    }
    
    private JPanel createTicketSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header section
        JPanel headerPanel = createHeaderPanel("Ticket Sales Report", "View detailed ticket sales information for all shows");
        
        // Create table model with column names
        ticketSalesTableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {
                "Show ID", "Show Title", "Date", "Total Sales", "Revenue (£)", "Occupancy Rate"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        ticketSalesTable = new JTable(ticketSalesTableModel);
        ticketSalesTable.setRowHeight(25);
        ticketSalesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketSalesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        ticketSalesTable.getTableHeader().setBackground(PRIMARY_COLOR);
        ticketSalesTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(ticketSalesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(CARD_COLOR);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton refreshButton = createButton("Refresh Data", PRIMARY_COLOR);
        refreshButton.addActionListener(e -> loadTicketSales());
        
        JButton exportButton = createButton("Export to Excel", new Color(46, 125, 50));
        exportButton.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Export functionality will be implemented in future updates", 
            "Export to Excel", JOptionPane.INFORMATION_MESSAGE));
        
        actionPanel.add(refreshButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(exportButton);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createHeaderPanel(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_COLOR);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_COLOR);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        headerPanel.add(textPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void loadVenues() {
        venueComboBox.removeAllItems();
        venueIdMap.clear();
        
        List<Map<String, Object>> venues = dbConnection.getVenues();
        for (Map<String, Object> venue : venues) {
            int id = (int) venue.get("id");
            String name = (String) venue.get("name");
            venueComboBox.addItem(name);
            venueIdMap.put(name, id);
        }
    }
    
    private void loadShows() {
        showFilterComboBox.removeAllItems();
        showFilterComboBox.addItem("All Shows");
        
        List<Map<String, Object>> shows = dbConnection.getAllShows();
        for (Map<String, Object> show : shows) {
            String showInfo = show.get("id") + " - " + show.get("title");
            showFilterComboBox.addItem(showInfo);
        }
    }
    
    private void loadTicketSales() {
        // Clear the table
        ticketSalesTableModel.setRowCount(0);
        
        // Load shows from database
        List<Map<String, Object>> shows = dbConnection.getAllShows();
        
        // Add shows to table with calculated fields
        for (Map<String, Object> show : shows) {
            int totalSeats = (int) show.get("totalSeats");
            int soldSeats = (int) show.get("soldSeats");
            double price = (double) show.get("price");
            double revenue = soldSeats * price;
            double occupancyRate = totalSeats > 0 ? ((double) soldSeats / totalSeats) * 100 : 0;
            
            ticketSalesTableModel.addRow(new Object[] {
                show.get("id"),
                show.get("title"),
                show.get("date"),
                soldSeats,
                String.format("%.2f", revenue),
                String.format("%.1f%%", occupancyRate)
            });
        }
    }
    
    // New ticket search panel for finding and managing individual tickets
    private JPanel createTicketSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header section
        JPanel headerPanel = createHeaderPanel("Ticket Search & Management", 
            "Find specific tickets, apply discounts, and manage ticket information");
        
        // Search panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(CARD_COLOR);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Show filter
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Show:"), gbc);
        
        gbc.gridx = 1;
        showFilterComboBox = new JComboBox<>();
        showFilterComboBox.setPreferredSize(new Dimension(200, 25));
        searchPanel.add(showFilterComboBox, gbc);
        
        // Date range
        gbc.gridx = 0;
        gbc.gridy = 1;
        searchPanel.add(new JLabel("Date Range:"), gbc);
        
        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.setBackground(CARD_COLOR);
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        datePanel.add(startDateField);
        datePanel.add(new JLabel("to"));
        datePanel.add(endDateField);
        searchPanel.add(datePanel, gbc);
        
        // Ticket ID
        gbc.gridx = 0;
        gbc.gridy = 2;
        searchPanel.add(new JLabel("Ticket ID:"), gbc);
        
        gbc.gridx = 1;
        ticketIdField = new JTextField(10);
        searchPanel.add(ticketIdField, gbc);
        
        // Patron name
        gbc.gridx = 0;
        gbc.gridy = 3;
        searchPanel.add(new JLabel("Patron Name:"), gbc);
        
        gbc.gridx = 1;
        patronNameField = new JTextField(20);
        searchPanel.add(patronNameField, gbc);
        
        // Search button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton searchButton = createButton("Search Tickets", PRIMARY_COLOR);
        searchButton.addActionListener(e -> searchTickets());
        searchPanel.add(searchButton, gbc);
        
        // Results table
        String[] columnNames = {
            "Ticket ID", "Show", "Date", "Time", "Seat", "Row", 
            "Price", "Discount", "Wheelchair", "Restricted View", "Status",
            "Patron", "Email", "Phone"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ticketsTable = new JTable(tableModel);
        ticketsTable.setRowHeight(25);
        ticketsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        ticketsTable.getTableHeader().setBackground(PRIMARY_COLOR);
        ticketsTable.getTableHeader().setForeground(Color.WHITE);
        
        // Add selection listener for ticket editing
        ticketsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ticketsTable.getSelectedRow() != -1) {
                int selectedRow = ticketsTable.getSelectedRow();
                showEditDialog(selectedRow);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        // Load initial ticket data
        loadTickets();
        
        return panel;
    }
    
    private void loadTickets() {
        tableModel.setRowCount(0);
        List<Map<String, Object>> tickets = dbConnection.getDetailedTickets();
        for (Map<String, Object> ticket : tickets) {
            addTicketToTable(ticket);
        }
    }
    
    private void searchTickets() {
        // Clear the table first
        tableModel.setRowCount(0);
        
        try {
            // Get search criteria
            Integer showId = null;
            String selectedShow = (String) showFilterComboBox.getSelectedItem();
            if (selectedShow != null && !selectedShow.equals("All Shows")) {
                try {
                    showId = Integer.parseInt(selectedShow.split(" - ")[0]);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid show selection", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Get date range
            String startDate = startDateField.getText().trim();
            String endDate = endDateField.getText().trim();
            if (startDate.isEmpty()) startDate = null;
            if (endDate.isEmpty()) endDate = null;
            
            // Get ticket ID
            Integer ticketId = null;
            String ticketIdStr = ticketIdField.getText().trim();
            if (!ticketIdStr.isEmpty()) {
                try {
                    ticketId = Integer.parseInt(ticketIdStr);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Ticket ID", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Get patron name
            String patronName = patronNameField.getText().trim();
            if (patronName.isEmpty()) patronName = null;
            
            // Debug: Print search criteria
            System.out.println("Searching with criteria:");
            System.out.println("Show ID: " + showId);
            System.out.println("Start Date: " + startDate);
            System.out.println("End Date: " + endDate);
            System.out.println("Ticket ID: " + ticketId);
            System.out.println("Patron Name: " + patronName);
            
            // Perform the search
            List<Map<String, Object>> tickets = dbConnection.searchTickets(showId, startDate, endDate, ticketId, patronName);
            
            // Debug: Print number of results
            System.out.println("Found " + tickets.size() + " tickets");
            
            // Display results
            if (tickets.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No tickets found matching the search criteria", 
                    "No Results", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Add tickets to table
            for (Map<String, Object> ticket : tickets) {
                try {
                    Object[] row = {
                        ticket.get("ticketId"),
                        ticket.get("showTitle"),
                        ticket.get("showDate"),
                        ticket.get("showTime"),
                        ticket.get("seatCode"),
                        ticket.get("rowNumber"),
                        String.format("£%.2f", ticket.get("price")),
                        String.format("£%.2f", ticket.get("discountAmount")),
                        (Boolean) ticket.get("isWheelchairAccessible") ? "Yes" : "No",
                        (Boolean) ticket.get("isRestrictedView") ? "Yes" : "No",
                        (Boolean) ticket.get("isSold") ? "Sold" : "Available",
                        ticket.get("firstName") + " " + ticket.get("lastName"),
                        ticket.get("email"),
                        ticket.get("phone")
                    };
                    tableModel.addRow(row);
                } catch (Exception e) {
                    System.err.println("Error adding ticket to table: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Show success message
            JOptionPane.showMessageDialog(this, 
                "Found " + tickets.size() + " matching tickets", 
                "Search Results", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "An error occurred while searching for tickets: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addTicketToTable(Map<String, Object> ticket) {
        try {
            Object[] row = {
                ticket.get("ticketId"),
                ticket.get("showTitle"),
                ticket.get("showDate"),
                ticket.get("showTime"),
                ticket.get("seatCode"),
                ticket.get("rowNumber"),
                String.format("£%.2f", ticket.get("price")),
                String.format("£%.2f", ticket.get("discountAmount")),
                (Boolean) ticket.get("isWheelchairAccessible") ? "Yes" : "No",
                (Boolean) ticket.get("isRestrictedView") ? "Yes" : "No",
                (Boolean) ticket.get("isSold") ? "Sold" : "Available",
                ticket.get("firstName") + " " + ticket.get("lastName"),
                ticket.get("email"),
                ticket.get("phone")
            };
            tableModel.addRow(row);
        } catch (Exception e) {
            System.err.println("Error adding ticket to table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showEditDialog(int selectedRow) {
        int ticketId = (int) tableModel.getValueAt(selectedRow, 0);
        double price = Double.parseDouble(((String) tableModel.getValueAt(selectedRow, 6)).substring(1));
        double discountAmount = Double.parseDouble(((String) tableModel.getValueAt(selectedRow, 7)).substring(1));
        boolean isWheelchairAccessible = tableModel.getValueAt(selectedRow, 8).equals("Yes");
        boolean isRestrictedView = tableModel.getValueAt(selectedRow, 9).equals("Yes");
        boolean isSold = tableModel.getValueAt(selectedRow, 10).equals("Sold");
        
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Ticket", true);
        editDialog.setLayout(new BorderLayout());
        editDialog.setSize(400, 300);
        editDialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Price field
        formPanel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField(String.format("%.2f", price));
        formPanel.add(priceField);
        
        // Discount field
        formPanel.add(new JLabel("Discount:"));
        JTextField discountField = new JTextField(String.format("%.2f", discountAmount));
        formPanel.add(discountField);
        
        // Wheelchair accessible checkbox
        formPanel.add(new JLabel("Wheelchair Accessible:"));
        JCheckBox wheelchairCheckBox = new JCheckBox("", isWheelchairAccessible);
        formPanel.add(wheelchairCheckBox);
        
        // Restricted view checkbox
        formPanel.add(new JLabel("Restricted View:"));
        JCheckBox restrictedCheckBox = new JCheckBox("", isRestrictedView);
        formPanel.add(restrictedCheckBox);
        
        // Sold status checkbox
        formPanel.add(new JLabel("Sold:"));
        JCheckBox soldCheckBox = new JCheckBox("", isSold);
        formPanel.add(soldCheckBox);
        
        editDialog.add(formPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                double newPrice = Double.parseDouble(priceField.getText());
                double newDiscount = Double.parseDouble(discountField.getText());
                boolean newWheelchair = wheelchairCheckBox.isSelected();
                boolean newRestricted = restrictedCheckBox.isSelected();
                boolean newSold = soldCheckBox.isSelected();
                
                if (dbConnection.updateTicket(ticketId, newPrice, newDiscount, 
                        newWheelchair, newRestricted, newSold)) {
                    JOptionPane.showMessageDialog(editDialog, "Ticket updated successfully", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    searchTickets(); // Refresh the table
                    editDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Failed to update ticket", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editDialog, "Please enter valid numbers for price and discount", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        editDialog.setVisible(true);
    }
} 