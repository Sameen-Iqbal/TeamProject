package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import db.dbConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class ReportsPage extends JPanel {
    private JTextField searchField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> reportTypeCombo;
    private JButton searchButton;
    private JButton exportButton;

    private TableRowSorter<DefaultTableModel> sorter;
    private JTable table;
    private DefaultTableModel tableModel;

    public ReportsPage() {
        setLayout(new BorderLayout());

        // === Filter Panel ===
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        fromDateField = new JTextField(10);
        toDateField = new JTextField(10);
        reportTypeCombo = new JComboBox<>(new String[] {
                "All", "Matinee", "Friday Night", "Rehearsal", "Weekend Special"
        });

        fromDateField.setToolTipText("dd/mm/yyyy");
        toDateField.setToolTipText("dd/mm/yyyy");

        filterPanel.add(new JLabel("From Date:"));
        filterPanel.add(fromDateField);
        filterPanel.add(new JLabel("To Date:"));
        filterPanel.add(toDateField);
        filterPanel.add(new JLabel("Report Type:"));
        filterPanel.add(reportTypeCombo);

        add(filterPanel, BorderLayout.NORTH);

        // === Search Panel ===
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        exportButton = new JButton("Export as CSV");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(exportButton);

        add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);

        // === Table Setup ===
        String[] columns = {
                "Report_ID", "Report_Date", "Report_Type", "Show_ID",
                "Total_Tickets_Sold", "Total_Revenue", "Seats_Available",
                "Wheelchair_Seats_Sold", "Restricted_View_Seats_Sold",
                "Personnel", "Total_Discounts_Applied", "Total_Discounted_Amount",
                "Total_Group_Tickets_Sold", "Total_Group_Revenue",
                "Total_Friends_Tickets_Sold", "Total_Friends_Revenue"
        };

        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // === Search Logic with Filters ===
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            String selectedType = reportTypeCombo.getSelectedItem().toString();
            String from = fromDateField.getText().trim();
            String to = toDateField.getText().trim();

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    StringBuilder rowText = new StringBuilder();
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        rowText.append(String.valueOf(entry.getValue(i)).toLowerCase()).append(" ");
                    }

                    boolean matchesSearch = searchText.isEmpty() || rowText.toString().contains(searchText);
                    boolean matchesType = selectedType.equals("All") || entry.getStringValue(2).equalsIgnoreCase(selectedType);
                    boolean matchesDate = true;

                    try {
                        DateTimeFormatter userFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        DateTimeFormatter tableFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        LocalDate fromDate = from.isEmpty() ? null : LocalDate.parse(from, userFormat);
                        LocalDate toDate = to.isEmpty() ? null : LocalDate.parse(to, userFormat);

                        String tableDateStr = entry.getStringValue(1);
                        LocalDate tableDate = LocalDate.parse(tableDateStr, tableFormat);

                        if (fromDate != null && tableDate.isBefore(fromDate)) matchesDate = false;
                        if (toDate != null && tableDate.isAfter(toDate)) matchesDate = false;
                    } catch (Exception ex) {
                        matchesDate = false;
                    }

                    return matchesSearch && matchesType && matchesDate;
                }
            });
        });

        exportButton.addActionListener(e -> exportToCSV());

        loadReportData();

        // === Chart Panel ===
        add(new ReportsChartPanel(tableModel), BorderLayout.SOUTH);
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report As");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter fw = new FileWriter(fileToSave)) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    fw.write(tableModel.getColumnName(i));
                    if (i != tableModel.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        fw.write(String.valueOf(tableModel.getValueAt(row, col)));
                        if (col != tableModel.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage());
            }
        }
    }

    private void loadReportData() {
        ResultSet rs = dbConnection.getAllBoxOfficeReports();

        try {
            if (rs != null) {
                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("Report_ID"),
                            rs.getDate("Report_Date"),
                            rs.getString("Report_Type"),
                            rs.getInt("Show_ID"),
                            rs.getInt("Total_Tickets_Sold"),
                            rs.getDouble("Total_Revenue"),
                            rs.getInt("Seats_Available"),
                            rs.getInt("Wheelchair_Seats_Sold"),
                            rs.getInt("Restricted_View_Seats_Sold"),
                            rs.getString("Personnel"),
                            rs.getInt("Total_Discounts_Applied"),
                            rs.getDouble("Total_Discounted_Amount"),
                            rs.getInt("Total_Group_Tickets_Sold"),
                            rs.getDouble("Total_Group_Revenue"),
                            rs.getInt("Total_Friends_Tickets_Sold"),
                            rs.getDouble("Total_Friends_Revenue")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load reports: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Box Office Reports");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new ReportsPage());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
