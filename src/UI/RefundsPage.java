package UI;

import db.dbConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RefundsPage extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton, refundButton;
    private JComboBox<String> refundMethodBox;
    private JLabel selectedInfo;

    public RefundsPage() {
        setLayout(new BorderLayout());

        // === Top search bar ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");

        topPanel.add(new JLabel("Search Booking ID:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // === Table setup with IsRefunded ===
        String[] columns = {"Booking_ID", "Patron_ID", "TotalCost", "Booking_Date", "IsCancelled", "IsRefunded"};

        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === Bottom refund controls ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refundMethodBox = new JComboBox<>(new String[]{"Select Method", "Card", "Cash", "Voucher"});
        refundButton = new JButton("Process Refund");
        refundButton.setEnabled(false);

        selectedInfo = new JLabel("Select a booking to refund.");

        bottomPanel.add(new JLabel("Refund Method:"));
        bottomPanel.add(refundMethodBox);
        bottomPanel.add(refundButton);
        bottomPanel.add(selectedInfo);
        add(bottomPanel, BorderLayout.SOUTH);

        // === Table row selection logic ===
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int bookingId = (int) table.getValueAt(row, 0);
                selectedInfo.setText("Selected Booking ID: " + bookingId);
                refundButton.setEnabled(true);
            }
        });

        // === Refund button logic ===
        refundButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && refundMethodBox.getSelectedIndex() > 0) {
                int bookingId = (int) table.getValueAt(row, 0);

                boolean bookingUpdated = dbConnection.markBookingAsRefunded(bookingId);
                boolean paymentUpdated = dbConnection.markPaymentAsRefunded(bookingId);
                boolean ticketUpdated = dbConnection.markTicketsAsUnsold(bookingId);

                if (bookingUpdated && paymentUpdated && ticketUpdated) {
                    tableModel.setValueAt("Yes", row, 4); // IsCancelled
                    tableModel.setValueAt("Yes", row, 5); // IsRefunded
                    JOptionPane.showMessageDialog(this, "Refund processed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Refund failed. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a refund method.");
            }
        });

        // === Search filter ===
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        searchButton.addActionListener(e -> {
            String text = searchField.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter(text, 0)); // filter by Booking_ID
            }
        });

        loadBookingData(); // Load bookings from DB
    }

    private void loadBookingData() {
        tableModel.setRowCount(0); // Clear old data
        ResultSet rs = dbConnection.getAllBookings();

        try {
            while (rs != null && rs.next()) {
                Object[] row = {
                        rs.getInt("Booking_ID"),
                        rs.getInt("Patron_ID"),
                        rs.getDouble("TotalCost"),
                        rs.getTimestamp("Booking_Date"),
                        rs.getInt("IsCancelled") == 1 ? "Yes" : "No",
                        rs.getInt("IsRefunded") == 1 ? "Yes" : "No"
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading booking data.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Refunds Interface");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 400);
            frame.add(new RefundsPage());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
