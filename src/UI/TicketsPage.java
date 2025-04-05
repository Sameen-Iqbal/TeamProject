package UI;

import db.dbConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Tickets
 * Handles the Ticket Sales
 * - Alimattan
 */
public class TicketsPage extends JPanel {
    private JTabbedPane tabbedPane;
    private JPanel mainHallPanel;
    private JPanel smallHallPanel;
    private JComboBox<String> showComboBox;
    private JSpinner ticketCountSpinner;
    private JButton purchaseButton;
    private Connection connection;
    private Map<String, JLabel> seatButtons;
    private Map<String, String> seatStatuses;
    private int selectedShowId;
    private int selectedVenueId;

    private static final Color AVAILABLE_COLOR = new Color(200, 230, 201); // Light green
    private static final Color OCCUPIED_COLOR = new Color(239, 83, 80);    // Modern red
    private static final Color SELECTED_COLOR = new Color(33, 150, 243);   // Blue for selected seats
    private static final Color DISCOUNTED_COLOR = new Color(255, 165, 0);  // Orange for discounted
    private static final Color RESTRICTED_COLOR = new Color(66, 66, 66);   // Dark gray for restricted view
    private static final Color COMPANION_COLOR = new Color(171, 71, 188);  // Purple for companion seats
    private static final Color DISABILITY_COLOR = new Color(255, 202, 40); // Yellow for disability seats
    private static final Color DARK_GREEN = new Color(0, 100, 0);         // Dark green for button

    public TicketsPage() {
        try {
            connection = dbConnection.getConnection();
            seatButtons = new HashMap<>();
            seatStatuses = new HashMap<>();

            setupUI();
            loadShows();
        } catch (SQLException e) {
            e.printStackTrace();
            JPanel errorPanel = new JPanel();
            errorPanel.add(new JLabel("Database error: " + e.getMessage()));
            add(errorPanel);
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);
        showComboBox = new JComboBox<>();
        showComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        showComboBox.addActionListener(e -> updateSeatDisplay());
        leftPanel.add(new JLabel("Select Show: ") {{
            setFont(new Font("Arial", Font.BOLD, 14));
            setForeground(new Color(33, 33, 33));
        }});
        leftPanel.add(showComboBox);

        // Add "Add New Show" button for managers
        JButton addShowButton = new JButton("Add New Show");
        addShowButton.setFont(new Font("Arial", Font.BOLD, 14));
        addShowButton.setBackground(new Color(63, 81, 181)); // Blue color
        addShowButton.setForeground(Color.WHITE);
        addShowButton.setFocusPainted(false);
        addShowButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        addShowButton.setPreferredSize(new Dimension(150, 35));
        addShowButton.setOpaque(true);
        addShowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addShowButton.addActionListener(e -> showAddShowDialog());
        leftPanel.add(addShowButton);

        // Add "Manage Tickets" button for managers
        JButton manageTicketsButton = new JButton("Manage Tickets");
        manageTicketsButton.setFont(new Font("Arial", Font.BOLD, 14));
        manageTicketsButton.setBackground(new Color(156, 39, 176)); // Purple color
        manageTicketsButton.setForeground(Color.WHITE);
        manageTicketsButton.setFocusPainted(false);
        manageTicketsButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        manageTicketsButton.setPreferredSize(new Dimension(150, 35));
        manageTicketsButton.setOpaque(true);
        manageTicketsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        manageTicketsButton.addActionListener(e -> showManageTicketsDialog());
        leftPanel.add(manageTicketsButton);
        
        purchaseButton = new JButton("Purchase Tickets");
        purchaseButton.setFont(new Font("Arial", Font.BOLD, 14));
        purchaseButton.setBackground(DARK_GREEN);
        purchaseButton.setForeground(Color.WHITE);
        purchaseButton.setFocusPainted(false);
        purchaseButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        purchaseButton.setPreferredSize(new Dimension(200, 35));
        purchaseButton.setOpaque(true);
        purchaseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        purchaseButton.addActionListener(e -> processTicketPurchase());
        leftPanel.add(purchaseButton);

        topPanel.add(leftPanel, BorderLayout.WEST);

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tabPanel.setBackground(Color.WHITE);
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        mainHallPanel = createMainHallPanel();
        smallHallPanel = createSmallHallPanel();
        tabbedPane.addTab("Main Hall", new JScrollPane(mainHallPanel) {{
            setBorder(BorderFactory.createEmptyBorder());
        }});
        tabbedPane.addTab("Small Hall", new JScrollPane(smallHallPanel) {{
            setBorder(BorderFactory.createEmptyBorder());
        }});
        tabPanel.add(tabbedPane);
        topPanel.add(tabPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel legendPanel = createLegendPanel();
        legendPanel.setPreferredSize(new Dimension(0, 50));
        add(legendPanel, BorderLayout.SOUTH);
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        addLegendItem(legendPanel, "Available", AVAILABLE_COLOR);
        addLegendItem(legendPanel, "Occupied", OCCUPIED_COLOR);
        addLegendItem(legendPanel, "Selected", SELECTED_COLOR);
        addLegendItem(legendPanel, "Discounted", DISCOUNTED_COLOR);

        return legendPanel;
    }

    private void addLegendItem(JPanel panel, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setBackground(Color.WHITE);
        JLabel colorBox = new JLabel();
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(new Color(33, 33, 33)));

        item.add(colorBox);
        item.add(new JLabel(text) {{
            setFont(new Font("Arial", Font.PLAIN, 12));
            setForeground(new Color(33, 33, 33));
        }});
        panel.add(item);
    }

    private void loadShows() {
        showComboBox.removeAllItems();
        List<Map<String, Object>> shows = dbConnection.getAllShows();
        for (Map<String, Object> show : shows) {
            String showInfo = show.get("id") + " - " + show.get("title");
            showComboBox.addItem(showInfo);
        }
    }

    private void updateSeatDisplay() {
        String selectedShow = (String) showComboBox.getSelectedItem();
        if (selectedShow == null) return;
        
        try {
            int showId = Integer.parseInt(selectedShow.split(" - ")[0]);
            selectedShowId = showId;
            
            // Get venue ID for the selected show
            String venueQuery = "SELECT Venue_ID FROM Shows WHERE Show_ID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(venueQuery)) {
                pstmt.setInt(1, showId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    selectedVenueId = rs.getInt("Venue_ID");
                }
            }
            
            // Clear existing seat statuses
            seatStatuses.clear();
            
            // Get seat availability for the show
            String availabilityQuery = "SELECT s.Seat_Code, sa.Status, s.IsWheelchairAccessible, s.IsRestrictedView " +
                                     "FROM Seat s " +
                                     "LEFT JOIN Seat_Availability sa ON s.Seat_ID = sa.Seat_ID AND sa.Show_ID = ? " +
                                     "WHERE s.Venue_ID = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(availabilityQuery)) {
                pstmt.setInt(1, showId);
                pstmt.setInt(2, selectedVenueId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String seatCode = rs.getString("Seat_Code");
                    String status = rs.getString("Status");
                    boolean isWheelchairAccessible = rs.getBoolean("IsWheelchairAccessible");
                    boolean isRestrictedView = rs.getBoolean("IsRestrictedView");
                    
                    // Determine seat status
                    if (status == null) {
                        status = "Available";
                    }
                    
                    // Store seat status
                    seatStatuses.put(seatCode, status);
                    
                    // Update seat button appearance
                    JLabel seatButton = seatButtons.get(seatCode);
                    if (seatButton != null) {
                        updateSeatButtonAppearance(seatButton, status, isWheelchairAccessible, isRestrictedView);
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating seat display: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSeatButtonAppearance(JLabel button, String status, boolean isWheelchairAccessible, 
                                          boolean isRestrictedView) {
        Color color;
        String tooltip = "";
        
        switch (status) {
            case "Available":
                if (isWheelchairAccessible) {
                    color = DISABILITY_COLOR;
                    tooltip = "Wheelchair Accessible";
                } else if (isRestrictedView) {
                    color = RESTRICTED_COLOR;
                    tooltip = "Restricted View";
                } else {
                    color = AVAILABLE_COLOR;
                }
                break;
            case "Selected":
                color = SELECTED_COLOR;
                break;
            case "Sold":
                color = OCCUPIED_COLOR;
                break;
            default:
                color = AVAILABLE_COLOR;
        }
        
        button.setBackground(color);
        button.setToolTipText(tooltip);
    }
    
    private void processTicketPurchase() {
        // Get selected seats
        List<String> selectedSeats = new ArrayList<>();
        for (Map.Entry<String, JLabel> entry : seatButtons.entrySet()) {
            if (entry.getValue().getBackground().equals(SELECTED_COLOR)) {
                selectedSeats.add(entry.getKey());
            }
        }
        
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat",
                                        "No Seats Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get ticket count
        int ticketCount = (int) ticketCountSpinner.getValue();
        if (selectedSeats.size() != ticketCount) {
            JOptionPane.showMessageDialog(this, 
                "Number of selected seats (" + selectedSeats.size() + 
                ") does not match ticket count (" + ticketCount + ")",
                "Selection Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get show details
        String selectedShow = (String) showComboBox.getSelectedItem();
        if (selectedShow == null) {
            JOptionPane.showMessageDialog(this, "Please select a show",
                                        "No Show Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int showId = Integer.parseInt(selectedShow.split(" - ")[0]);
            
            // Get seat IDs for selected seats
            List<Integer> seatIds = new ArrayList<>();
            String seatQuery = "SELECT Seat_ID FROM Seat WHERE Seat_Code = ? AND Venue_ID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(seatQuery)) {
                for (String seatCode : selectedSeats) {
                    pstmt.setString(1, seatCode);
                    pstmt.setInt(2, selectedVenueId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        seatIds.add(rs.getInt("Seat_ID"));
                    }
                }
            }
            
            // Get show price
            double totalPrice = 0;
            String priceQuery = "SELECT Base_Price FROM Shows WHERE Show_ID = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
                pstmt.setInt(1, showId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    double basePrice = rs.getDouble("Base_Price");
                    totalPrice = basePrice * ticketCount;
                }
            }
            
            // Show confirmation dialog
            int patronId = showPatronSelectionDialog();
            if (patronId == -1) return;
            
            // Process the sale
            if (dbConnection.processSale(showId, patronId, seatIds, totalPrice)) {
                JOptionPane.showMessageDialog(this, 
                    "Tickets purchased successfully!\n" +
                    "Total Amount: £" + String.format("%.2f", totalPrice),
                    "Purchase Complete", JOptionPane.INFORMATION_MESSAGE);
                
                // Update seat display
                updateSeatDisplay();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to process ticket purchase. Please try again.",
                    "Purchase Failed", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error processing purchase: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int showPatronSelectionDialog() {
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "Select Patron", true);
        dialog.setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {"ID", "Name", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Load patron data
        try {
            String query = "SELECT Patron_ID, First_Name, Last_Name, Email, Phone FROM Patron";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    model.addRow(new Object[] {
                        rs.getInt("Patron_ID"),
                        rs.getString("First_Name") + " " + rs.getString("Last_Name"),
                        rs.getString("Email"),
                        rs.getString("Phone")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, 
                "Error loading patron data: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Add buttons
        JPanel buttonPanel = new JPanel();
        JButton selectButton = new JButton("Select");
        JButton cancelButton = new JButton("Cancel");
        
        final int[] selectedId = {-1};
        
        selectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                selectedId[0] = (int) table.getValueAt(selectedRow, 0);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a patron",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        return selectedId[0];
    }

    private JPanel createMainHallPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel balconyLabel = new JLabel("BALCONY", SwingConstants.CENTER);
        balconyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balconyLabel.setForeground(new Color(33, 33, 33));
        balconyLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        contentPanel.add(balconyLabel);

        JPanel balconyPanel = new JPanel(new GridLayout(3, 1, 1, 1));
        balconyPanel.setBackground(Color.WHITE);

        balconyPanel.add(createRow("CC", 1, 8));
        balconyPanel.add(createRow("BB", 6, 23));
        balconyPanel.add(createRow("AA", 21, 33));

        contentPanel.add(balconyPanel);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel stallsLabel = new JLabel("STALLS", SwingConstants.CENTER);
        stallsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        stallsLabel.setForeground(new Color(33, 33, 33));
        stallsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        contentPanel.add(stallsLabel);

        JPanel stallsWithBalconies = new JPanel(new BorderLayout());
        stallsWithBalconies.setBackground(Color.WHITE);

        JPanel leftBalcony = createSideBalcony("BB", 1, 5, "AA", 1, 20);
        leftBalcony.setPreferredSize(new Dimension(80, 0));
        stallsWithBalconies.add(leftBalcony, BorderLayout.WEST);

        JPanel rightBalcony = createSideBalcony("AA", 34, 53, "BB", 24, 28);
        rightBalcony.setPreferredSize(new Dimension(80, 0));
        stallsWithBalconies.add(rightBalcony, BorderLayout.EAST);

        JPanel stallsPanel = new JPanel(new GridLayout(16, 1, 1, 1));
        stallsPanel.setBackground(Color.WHITE);

        stallsPanel.add(createRow("Q", 1, 10));
        stallsPanel.add(createRow("P", 1, 11));
        stallsPanel.add(createSplitRow("O", 1, 16, 17, 20, 0, 0));
        stallsPanel.add(createSplitRow("N", 4, 14, 1, 3, 17, 19));
        stallsPanel.add(createSplitRow("M", 3, 12, 1, 2, 15, 16));

        char[] rows = {'L', 'K', 'J', 'H', 'G', 'F', 'E', 'D', 'C', 'B', 'A'};
        for (char row : rows) {
            int endSeat = row == 'A' ? 19 : 19;
            stallsPanel.add(createSplitRow(row + "", 4, 16, 1, 3, 17, endSeat));
        }

        stallsWithBalconies.add(stallsPanel, BorderLayout.CENTER);
        contentPanel.add(stallsWithBalconies);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel stagePanel = new JPanel(new BorderLayout());
        stagePanel.setBackground(Color.WHITE);

        JPanel stageContainer = new JPanel(new BorderLayout());
        stageContainer.setBackground(Color.WHITE);
        stageContainer.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        JLabel stageLabel = new JLabel("STAGE", SwingConstants.CENTER);
        stageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        stageLabel.setForeground(Color.BLACK);
        stageLabel.setBackground(Color.LIGHT_GRAY);
        stageLabel.setOpaque(true);
        stageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        stageContainer.add(Box.createRigidArea(new Dimension(5, 0)), BorderLayout.WEST);
        stageContainer.add(stageLabel, BorderLayout.CENTER);
        stageContainer.add(Box.createRigidArea(new Dimension(5, 0)), BorderLayout.EAST);

        stagePanel.add(stageContainer, BorderLayout.CENTER);
        mainPanel.add(stagePanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createSmallHallPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel soundDeskPanel = new JPanel(new BorderLayout());
        JLabel soundDeskLabel = new JLabel("SOUND DESK", SwingConstants.CENTER);
        soundDeskLabel.setFont(new Font("Arial", Font.BOLD, 14));
        soundDeskLabel.setForeground(Color.WHITE);
        soundDeskLabel.setBackground(new Color(50, 50, 50));
        soundDeskLabel.setOpaque(true);
        soundDeskLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JPanel soundDeskContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        soundDeskContainer.setBackground(Color.WHITE);
        soundDeskContainer.add(soundDeskLabel);
        soundDeskPanel.add(soundDeskContainer, BorderLayout.CENTER);
        soundDeskPanel.setBackground(Color.WHITE);
        mainPanel.add(soundDeskPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        leftPanel.setBackground(Color.WHITE);

        JPanel entrancePanel = new JPanel();
        entrancePanel.setLayout(new BoxLayout(entrancePanel, BoxLayout.Y_AXIS));
        entrancePanel.setBackground(Color.WHITE);
        JLabel arrowLabel = new JLabel("↑");
        arrowLabel.setFont(new Font("Arial", Font.BOLD, 18));
        arrowLabel.setForeground(new Color(33, 150, 243));
        arrowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel entranceLabel = new JLabel("ENTRANCE");
        entranceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        entranceLabel.setForeground(new Color(33, 33, 33));
        entranceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        entrancePanel.add(arrowLabel);
        entrancePanel.add(entranceLabel);
        leftPanel.add(entrancePanel);
        leftPanel.add(Box.createVerticalGlue());

        JPanel aisleTextPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.setColor(new Color(33, 150, 243));
                g2d.rotate(Math.PI / 2, getWidth() / 2, getHeight() / 2);
                g2d.drawString("AISLE", getWidth() / 2 - 25, getHeight() / 2 + 5);
                g2d.dispose();
            }
        };
        aisleTextPanel.setPreferredSize(new Dimension(30, 150));
        aisleTextPanel.setBackground(Color.WHITE);
        leftPanel.add(aisleTextPanel);
        leftPanel.add(Box.createVerticalGlue());

        centerPanel.add(leftPanel, BorderLayout.WEST);

        JPanel stallsPanel = new JPanel(new GridLayout(13, 1, 5, 5));
        stallsPanel.setBackground(Color.WHITE);
        stallsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel rowN = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        rowN.setBackground(Color.WHITE);
        rowN.add(Box.createHorizontalStrut(20));
        JLabel rowNLabel = new JLabel("N");
        rowNLabel.setFont(new Font("Arial", Font.BOLD, 12));
        rowNLabel.setForeground(new Color(33, 33, 33));
        rowN.add(rowNLabel);
        for (int i = 1; i <= 4; i++) {
            String seatCode = "N" + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(28, 22));
            seat.setFont(new Font("Arial", Font.BOLD, 10));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            rowN.add(seat);
        }
        stallsPanel.add(rowN);

        JPanel rowM = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        rowM.setBackground(Color.WHITE);
        rowM.add(Box.createHorizontalStrut(45));
        JLabel rowMLabel = new JLabel("M");
        rowMLabel.setFont(new Font("Arial", Font.BOLD, 12));
        rowMLabel.setForeground(new Color(33, 33, 33));
        rowM.add(rowMLabel);
        for (int i = 1; i <= 4; i++) {
            String seatCode = "M" + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(28, 22));
            seat.setFont(new Font("Arial", Font.BOLD, 10));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            rowM.add(seat);
        }
        stallsPanel.add(rowM);

        char[] rowsToInclude = {'L', 'K', 'J', 'H', 'G', 'F', 'E', 'D', 'C', 'B', 'A'};
        for (char rowLetter : rowsToInclude) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            row.setBackground(Color.WHITE);
            row.add(Box.createHorizontalStrut(45));
            JLabel rowLabel = new JLabel("" + rowLetter);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 12));
            rowLabel.setForeground(new Color(33, 33, 33));
            row.add(rowLabel);
            for (int i = 1; i <= 7; i++) {
                String seatCode = rowLetter + String.valueOf(i);
                JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
                seat.setPreferredSize(new Dimension(28, 22));
                seat.setFont(new Font("Arial", Font.BOLD, 10));
                seat.setOpaque(true);
                seat.setBackground(AVAILABLE_COLOR);
                seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
                seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
                seat.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSeatSelection(seatCode, seat);
                    }
                });
                seatButtons.put(seatCode, seat);
                row.add(seat);
            }
            stallsPanel.add(row);
        }

        centerPanel.add(stallsPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel stagePanel = new JPanel(new BorderLayout());
        stagePanel.setBackground(Color.WHITE);

        JPanel stageContainer = new JPanel(new BorderLayout());
        stageContainer.setBackground(Color.WHITE);
        stageContainer.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        JLabel stageLabel = new JLabel("STAGE", SwingConstants.CENTER);
        stageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        stageLabel.setForeground(Color.BLACK);
        stageLabel.setBackground(Color.LIGHT_GRAY);
        stageLabel.setOpaque(true);
        stageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        stageContainer.add(Box.createRigidArea(new Dimension(5, 0)), BorderLayout.WEST);
        stageContainer.add(stageLabel, BorderLayout.CENTER);
        stageContainer.add(Box.createRigidArea(new Dimension(5, 0)), BorderLayout.EAST);

        stagePanel.add(stageContainer, BorderLayout.CENTER);
        mainPanel.add(stagePanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createRow(String row, int start, int end) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        rowPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel(row);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(33, 33, 33));
        rowPanel.add(label);
        for (int i = start; i <= end; i++) {
            String seatCode = row + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(25, 18));
            seat.setFont(new Font("Arial", Font.BOLD, 9));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            rowPanel.add(seat);
        }
        return rowPanel;
    }

    private void handleSeatSelection(String seatCode, JLabel seatLabel) {
        if (seatLabel.getBackground() == AVAILABLE_COLOR) {
            seatLabel.setBackground(SELECTED_COLOR);
            seatStatuses.put(seatCode, "SELECTED");
        } else if (seatLabel.getBackground() == SELECTED_COLOR) {
            seatLabel.setBackground(AVAILABLE_COLOR);
            seatStatuses.remove(seatCode);
        }
    }

    private JPanel createSplitRow(String row, int upperStart, int upperEnd, int lowerStart1, int lowerEnd1, int lowerStart2, int lowerEnd2) {
        JPanel rowPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        rowPanel.setBackground(Color.WHITE);

        JPanel upperRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        upperRow.setBackground(Color.WHITE);
        JLabel label = new JLabel(row);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(33, 33, 33));
        upperRow.add(label);
        for (int i = upperStart; i <= upperEnd; i++) {
            String seatCode = row + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(25, 18));
            seat.setFont(new Font("Arial", Font.BOLD, 9));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            upperRow.add(seat);
        }
        rowPanel.add(upperRow);

        JPanel lowerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        lowerRow.setBackground(Color.WHITE);
        lowerRow.add(Box.createHorizontalStrut(row.equals("O") ? 465 : row.equals("N") ? 20 : row.equals("M") ? 50 : 20));
        for (int i = lowerStart1; i <= lowerEnd1; i++) {
            String seatCode = row + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(25, 18));
            seat.setFont(new Font("Arial", Font.BOLD, 9));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            lowerRow.add(seat);
        }
        lowerRow.add(Box.createHorizontalStrut(row.equals("O") ? 25 : row.equals("N") ? 300 : row.equals("M") ? 268 : 355));
        for (int i = lowerStart2; i <= lowerEnd2; i++) {
            String seatCode = row + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(25, 18));
            seat.setFont(new Font("Arial", Font.BOLD, 9));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            lowerRow.add(seat);
        }
        lowerRow.add(Box.createHorizontalStrut(row.equals("M") ? 66 : 25));
        rowPanel.add(lowerRow);

        return rowPanel;
    }

    private JPanel createSideBalcony(String row1, int start1, int end1, String row2, int start2, int end2) {
        JPanel sideBalcony = new JPanel(new GridLayout(1, 2, 5, 5));
        sideBalcony.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        JLabel label1 = new JLabel(row1, SwingConstants.CENTER);
        label1.setFont(new Font("Arial", Font.BOLD, 12));
        label1.setForeground(new Color(33, 33, 33));
        leftPanel.add(label1);
        for (int i = end1; i >= start1; i--) {
            String seatCode = row1 + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(40, 30));
            seat.setFont(new Font("Arial", Font.BOLD, 12));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            leftPanel.add(seat);
            leftPanel.add(Box.createVerticalStrut(2));
        }
        sideBalcony.add(leftPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        JLabel label2 = new JLabel(row2, SwingConstants.CENTER);
        label2.setFont(new Font("Arial", Font.BOLD, 12));
        label2.setForeground(new Color(33, 33, 33));
        rightPanel.add(label2);
        for (int i = end2; i >= start2; i--) {
            String seatCode = row2 + i;
            JLabel seat = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            seat.setPreferredSize(new Dimension(40, 30));
            seat.setFont(new Font("Arial", Font.BOLD, 12));
            seat.setOpaque(true);
            seat.setBackground(AVAILABLE_COLOR);
            seat.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            seat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            seat.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSeatSelection(seatCode, seat);
                }
            });
            seatButtons.put(seatCode, seat);
            rightPanel.add(seat);
            rightPanel.add(Box.createVerticalStrut(2));
        }
        sideBalcony.add(rightPanel);

        return sideBalcony;
    }

    // Method to display the "Add New Show" dialog
    private void showAddShowDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Show");
        dialog.setModal(true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Title label
        JLabel titleLabel = new JLabel("Create New Show");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Show title field
        JPanel showTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showTitlePanel.setBackground(Color.WHITE);
        showTitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel showTitleLabel = new JLabel("Show Title:");
        showTitleLabel.setPreferredSize(new Dimension(120, 25));
        showTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField showTitleField = new JTextField(20);
        showTitleField.setFont(new Font("Arial", Font.PLAIN, 14));
        showTitlePanel.add(showTitleLabel);
        showTitlePanel.add(showTitleField);
        contentPanel.add(showTitlePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Show date field
        JPanel showDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showDatePanel.setBackground(Color.WHITE);
        showDatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel showDateLabel = new JLabel("Date (YYYY-MM-DD):");
        showDateLabel.setPreferredSize(new Dimension(120, 25));
        showDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField showDateField = new JTextField(20);
        showDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        // Set default to today's date
        showDateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        showDatePanel.add(showDateLabel);
        showDatePanel.add(showDateField);
        contentPanel.add(showDatePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Venue dropdown
        JPanel venuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        venuePanel.setBackground(Color.WHITE);
        venuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel venueLabel = new JLabel("Venue:");
        venueLabel.setPreferredSize(new Dimension(120, 25));
        venueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> venueComboBox = new JComboBox<>();
        venueComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Get venues from database
        try {
            java.util.List<java.util.Map<String, Object>> venues = dbConnection.getVenues();
            for (java.util.Map<String, Object> venue : venues) {
                venueComboBox.addItem(venue.get("id") + " - " + venue.get("name"));
            }
        } catch (Exception e) {
            // Add some defaults if database fails
            venueComboBox.addItem("1 - Main Music Hall");
            venueComboBox.addItem("2 - Small Music Hall");
        }
        
        venuePanel.add(venueLabel);
        venuePanel.add(venueComboBox);
        contentPanel.add(venuePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Hall type dropdown
        JPanel hallTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hallTypePanel.setBackground(Color.WHITE);
        hallTypePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hallTypeLabel = new JLabel("Hall Type:");
        hallTypeLabel.setPreferredSize(new Dimension(120, 25));
        hallTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> hallTypeComboBox = new JComboBox<>();
        hallTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        hallTypeComboBox.addItem("Main Hall");
        hallTypeComboBox.addItem("Small Hall");
        hallTypePanel.add(hallTypeLabel);
        hallTypePanel.add(hallTypeComboBox);
        contentPanel.add(hallTypePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Base price field
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel priceLabel = new JLabel("Ticket Price (£):");
        priceLabel.setPreferredSize(new Dimension(120, 25));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField priceField = new JTextField(20);
        priceField.setFont(new Font("Arial", Font.PLAIN, 14));
        priceField.setText("99.99");
        pricePanel.add(priceLabel);
        pricePanel.add(priceField);
        contentPanel.add(pricePanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton createButton = new JButton("Create Show");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setBackground(DARK_GREEN);
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> {
            try {
                // Validate inputs
                String title = showTitleField.getText().trim();
                String date = showDateField.getText().trim();
                String venueStr = (String) venueComboBox.getSelectedItem();
                int venueId = Integer.parseInt(venueStr.split(" - ")[0]);
                String hallType = (String) hallTypeComboBox.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a show title", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Add show to database
                int showId = dbConnection.addNewShow(title, date, venueId, hallType, price);
                
                if (showId > 0) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Show created successfully!\n" +
                        "Show ID: " + showId + "\n" +
                        "Title: " + title + "\n" +
                        "Date: " + date,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh the shows dropdown
                    showComboBox.removeAllItems();
                    loadShows();
                    
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create show. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid price", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(createButton);
        
        contentPanel.add(buttonsPanel);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Method to display the "Manage Tickets" dialog
    private void showManageTicketsDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Ticket Management");
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(245, 245, 245));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Show filter
        JLabel showLabel = new JLabel("Show:");
        showLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(showLabel);
        
        JComboBox<String> showFilterComboBox = new JComboBox<>();
        showFilterComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        showFilterComboBox.setPreferredSize(new Dimension(200, 30));
        showFilterComboBox.addItem("All Shows");
        
        // Populate with the same shows as the main dropdown
        for (int i = 0; i < showComboBox.getItemCount(); i++) {
            showFilterComboBox.addItem(showComboBox.getItemAt(i));
        }
        
        searchPanel.add(showFilterComboBox);
        
        // Date filter
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(dateLabel);
        
        JTextField dateField = new JTextField(10);
        dateField.setFont(new Font("Arial", Font.PLAIN, 14));
        // Default to today's date
        dateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        searchPanel.add(dateField);
        
        // Ticket ID search
        JLabel ticketIdLabel = new JLabel("Ticket ID:");
        ticketIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(ticketIdLabel);
        
        JTextField ticketIdField = new JTextField(8);
        ticketIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(ticketIdField);
        
        // Search button
        JButton searchButton = new JButton("Search Tickets");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(63, 81, 181));
        searchButton.setForeground(Color.WHITE);
        searchPanel.add(searchButton);
        
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Results table in the center
        String[] columnNames = {"Ticket ID", "Show", "Date", "Seat", "Customer", "Type", "Price"};
        Object[][] data = {
            // Sample data - in a real app, this would be populated from the database
            {1001, "Hamilton", "2023-04-15", "A12", "John Smith", "Regular", "£99.99"},
            {1002, "Hamilton", "2023-04-15", "A13", "Jane Doe", "NHS Discount", "£79.99"},
            {1003, "The Lion King", "2023-04-16", "B05", "Robert Johnson", "Military Discount", "£75.50"},
            {1004, "The Lion King", "2023-04-16", "B06", "Sarah Williams", "Regular", "£89.99"},
            {1005, "Wicked", "2023-04-20", "C08", "Michael Brown", "Disability Discount", "£71.99"}
        };
        
        JTable ticketsTable = new JTable(data, columnNames);
        ticketsTable.setRowHeight(25);
        ticketsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        ticketsTable.getTableHeader().setBackground(new Color(63, 81, 181));
        ticketsTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Action panel at the bottom
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        
        JButton editButton = new JButton("Edit Selected Ticket");
        editButton.setFont(new Font("Arial", Font.BOLD, 14));
        editButton.setBackground(new Color(255, 152, 0));
        editButton.setForeground(Color.BLACK);
        editButton.setEnabled(false); // Disabled until a ticket is selected
        
        // Enable the edit button when a row is selected
        ticketsTable.getSelectionModel().addListSelectionListener(e -> {
            editButton.setEnabled(ticketsTable.getSelectedRow() != -1);
        });
        
        // Handle edit button click
        editButton.addActionListener(e -> {
            int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow != -1) {
                int ticketId = (int) ticketsTable.getValueAt(selectedRow, 0);
                String show = (String) ticketsTable.getValueAt(selectedRow, 1);
                String date = (String) ticketsTable.getValueAt(selectedRow, 2);
                String seat = (String) ticketsTable.getValueAt(selectedRow, 3);
                String customer = (String) ticketsTable.getValueAt(selectedRow, 4);
                String type = (String) ticketsTable.getValueAt(selectedRow, 5);
                String price = (String) ticketsTable.getValueAt(selectedRow, 6);
                
                showEditTicketDialog(dialog, ticketId, show, date, seat, customer, type, price, ticketsTable);
            }
        });
        
        actionPanel.add(editButton);
        
        JButton exportButton = new JButton("Export Results");
        exportButton.setFont(new Font("Arial", Font.BOLD, 14));
        exportButton.setBackground(new Color(0, 121, 107));
        exportButton.setForeground(Color.BLACK);
        exportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, 
                "This would export the ticket results to CSV or Excel.\n" +
                "Implementation depends on the specific reporting requirements.",
                "Export Functionality", JOptionPane.INFORMATION_MESSAGE);
        });
        actionPanel.add(exportButton);
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> dialog.dispose());
        actionPanel.add(closeButton);
        
        contentPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Search functionality
        searchButton.addActionListener(e -> {
            String showFilter = (String) showFilterComboBox.getSelectedItem();
            String dateFilter = dateField.getText().trim();
            String ticketIdStr = ticketIdField.getText().trim();
            
            JOptionPane.showMessageDialog(dialog, 
                "This would search the database for tickets matching:\n" +
                "Show: " + showFilter + "\n" +
                "Date: " + dateFilter + "\n" +
                "Ticket ID: " + (ticketIdStr.isEmpty() ? "Any" : ticketIdStr) + "\n\n" +
                "The results table would be populated with the matching tickets.",
                "Search Functionality", JOptionPane.INFORMATION_MESSAGE);
        });
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    // Dialog for editing ticket details
    private void showEditTicketDialog(JDialog parentDialog, int ticketId, String show, String date, String seat, 
                                     String customer, String type, String price, JTable ticketsTable) {
        JDialog dialog = new JDialog(parentDialog, "Edit Ticket #" + ticketId, true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Edit Ticket #" + ticketId);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Show info - read only
        JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showPanel.setBackground(Color.WHITE);
        showPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel showLabel = new JLabel("Show:");
        showLabel.setPreferredSize(new Dimension(100, 25));
        showLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel showValueLabel = new JLabel(show);
        showValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        showPanel.add(showLabel);
        showPanel.add(showValueLabel);
        contentPanel.add(showPanel);
        
        // Date info - read only
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(Color.WHITE);
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setPreferredSize(new Dimension(100, 25));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel dateValueLabel = new JLabel(date);
        dateValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        datePanel.add(dateLabel);
        datePanel.add(dateValueLabel);
        contentPanel.add(datePanel);
        
        // Seat info - read only
        JPanel seatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel seatLabel = new JLabel("Seat:");
        seatLabel.setPreferredSize(new Dimension(100, 25));
        seatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel seatValueLabel = new JLabel(seat);
        seatValueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        seatPanel.add(seatLabel);
        seatPanel.add(seatValueLabel);
        contentPanel.add(seatPanel);
        
        // Customer info - editable
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerPanel.setBackground(Color.WHITE);
        customerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel customerLabel = new JLabel("Customer:");
        customerLabel.setPreferredSize(new Dimension(100, 25));
        customerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField customerField = new JTextField(customer, 20);
        customerField.setFont(new Font("Arial", Font.PLAIN, 14));
        customerPanel.add(customerLabel);
        customerPanel.add(customerField);
        contentPanel.add(customerPanel);
        
        // Ticket type - dropdown
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBackground(Color.WHITE);
        typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel typeLabel = new JLabel("Ticket Type:");
        typeLabel.setPreferredSize(new Dimension(100, 25));
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        String[] ticketTypes = {
            "Regular", "NHS Discount", "Military Discount", 
            "Disability Discount", "Student Discount", "Senior Discount"
        };
        JComboBox<String> typeComboBox = new JComboBox<>(ticketTypes);
        typeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        typeComboBox.setSelectedItem(type);
        
        typePanel.add(typeLabel);
        typePanel.add(typeComboBox);
        contentPanel.add(typePanel);
        
        // Price - editable
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel priceLabel = new JLabel("Price (£):");
        priceLabel.setPreferredSize(new Dimension(100, 25));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField priceField = new JTextField(price.replace("£", ""), 20);
        priceField.setFont(new Font("Arial", Font.PLAIN, 14));
        pricePanel.add(priceLabel);
        pricePanel.add(priceField);
        contentPanel.add(pricePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Discount section
        JPanel discountPanel = new JPanel();
        discountPanel.setLayout(new BoxLayout(discountPanel, BoxLayout.Y_AXIS));
        discountPanel.setBackground(Color.WHITE);
        discountPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        discountPanel.setBorder(BorderFactory.createTitledBorder("Apply Discount"));
        
        JPanel discountTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        discountTypePanel.setBackground(Color.WHITE);
        JLabel discountTypeLabel = new JLabel("Discount Type:");
        discountTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        String[] discountTypes = {
            "None", "NHS (20%)", "Military (20%)", "Disability (20%)", 
            "Student (15%)", "Senior (15%)", "Group (10%)", "Custom"
        };
        JComboBox<String> discountComboBox = new JComboBox<>(discountTypes);
        discountComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        
        discountTypePanel.add(discountTypeLabel);
        discountTypePanel.add(discountComboBox);
        discountPanel.add(discountTypePanel);
        
        JPanel customDiscountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customDiscountPanel.setBackground(Color.WHITE);
        JLabel customDiscountLabel = new JLabel("Custom % (if selected):");
        customDiscountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField customDiscountField = new JTextField("10", 5);
        customDiscountField.setFont(new Font("Arial", Font.PLAIN, 14));
        customDiscountPanel.add(customDiscountLabel);
        customDiscountPanel.add(customDiscountField);
        discountPanel.add(customDiscountPanel);
        
        JPanel applyDiscountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        applyDiscountPanel.setBackground(Color.WHITE);
        
        JButton applyDiscountButton = new JButton("Apply Discount");
        applyDiscountButton.setFont(new Font("Arial", Font.BOLD, 14));
        applyDiscountButton.setBackground(new Color(76, 175, 80));
        applyDiscountButton.setForeground(Color.WHITE);
        
        applyDiscountButton.addActionListener(e -> {
            try {
                // Get the current price
                double currentPrice = Double.parseDouble(priceField.getText().trim());
                
                // Get the selected discount type
                String selectedDiscount = (String) discountComboBox.getSelectedItem();
                double discountPercentage = 0;
                
                if ("Custom".equals(selectedDiscount)) {
                    // Use the custom percentage
                    discountPercentage = Double.parseDouble(customDiscountField.getText().trim());
                } else if (!"None".equals(selectedDiscount)) {
                    // Extract the percentage from the discount type
                    String percentStr = selectedDiscount.substring(
                        selectedDiscount.indexOf("(") + 1, 
                        selectedDiscount.indexOf("%")
                    );
                    discountPercentage = Double.parseDouble(percentStr);
                }
                
                // Calculate the discounted price
                double newPrice = currentPrice * (1 - (discountPercentage / 100));
                priceField.setText(String.format("%.2f", newPrice));
                
                // Update the ticket type if appropriate
                if (!"None".equals(selectedDiscount) && !"Custom".equals(selectedDiscount)) {
                    String discountName = selectedDiscount.substring(0, selectedDiscount.indexOf(" ("));
                    typeComboBox.setSelectedItem(discountName + " Discount");
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numbers for price and discount percentage", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        applyDiscountPanel.add(applyDiscountButton);
        discountPanel.add(applyDiscountPanel);
        
        contentPanel.add(discountPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(Color.WHITE);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setBackground(DARK_GREEN);
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            try {
                // In a real app, this would update the database
                JOptionPane.showMessageDialog(dialog, 
                    "Changes to ticket #" + ticketId + " would be saved to the database.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Update the table with the new values
                int selectedRow = ticketsTable.getSelectedRow();
                ticketsTable.setValueAt(customerField.getText(), selectedRow, 4);
                ticketsTable.setValueAt(typeComboBox.getSelectedItem(), selectedRow, 5);
                ticketsTable.setValueAt("£" + priceField.getText(), selectedRow, 6);
                
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error saving changes: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(saveButton);
        
        contentPanel.add(buttonsPanel);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
}
