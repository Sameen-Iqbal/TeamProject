package UI;

import db.dbConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * ReportsPage displays ticket sales data using charts
 */
public class ReportsPage extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(245, 246, 250);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color PRIMARY_COLOR = new Color(60, 90, 153);

    private JPanel chartsPanel;
    private JComboBox<String> reportTypeComboBox;
    private JPanel controlPanel;
    private JComboBox<String> dateRangeComboBox;
    private JPanel datePickerPanel;
    private JTextField startDateField;
    private JTextField endDateField;

    public ReportsPage() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.setBackground(BACKGROUND_COLOR);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(chartsPanel, BorderLayout.CENTER);
        
        // Initial display
        updateCharts("Ticket Sales by Show");
    }
    
    private void createControlPanel() {
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(CARD_COLOR);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBackground(CARD_COLOR);
        
        JLabel reportTypeLabel = new JLabel("Report Type:");
        reportTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectionPanel.add(reportTypeLabel);
        
        String[] reportTypes = {
            "Ticket Sales by Show", 
            "Ticket Sales by Date", 
            "Revenue by Show",
            "Hall Occupancy"
        };
        
        reportTypeComboBox = new JComboBox<>(reportTypes);
        reportTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        reportTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedType = (String) reportTypeComboBox.getSelectedItem();
                updateDatePickerVisibility(selectedType);
                updateCharts(selectedType);
            }
        });
        selectionPanel.add(reportTypeComboBox);
        
        // Date range selector
        JLabel dateRangeLabel = new JLabel("    Date Range:");
        dateRangeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectionPanel.add(dateRangeLabel);
        
        String[] dateRanges = {
            "Last 7 Days", 
            "Last 30 Days", 
            "Last 90 Days",
            "Custom Range"
        };
        
        dateRangeComboBox = new JComboBox<>(dateRanges);
        dateRangeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dateRangeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedRange = (String) dateRangeComboBox.getSelectedItem();
                if ("Custom Range".equals(selectedRange)) {
                    datePickerPanel.setVisible(true);
                } else {
                    datePickerPanel.setVisible(false);
                    updateCharts((String) reportTypeComboBox.getSelectedItem());
                }
            }
        });
        selectionPanel.add(dateRangeComboBox);
        
        // Date picker for custom range
        datePickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePickerPanel.setBackground(CARD_COLOR);
        
        JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
        startDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        datePickerPanel.add(startDateLabel);
        
        startDateField = new JTextField(10);
        startDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        datePickerPanel.add(startDateField);
        
        JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
        endDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        datePickerPanel.add(endDateLabel);
        
        endDateField = new JTextField(10);
        endDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        datePickerPanel.add(endDateField);
        
        JButton applyButton = new JButton("Apply");
        applyButton.setFont(new Font("Arial", Font.BOLD, 14));
        applyButton.setBackground(PRIMARY_COLOR);
        applyButton.setForeground(Color.WHITE);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCharts((String) reportTypeComboBox.getSelectedItem());
            }
        });
        datePickerPanel.add(applyButton);
        
        datePickerPanel.setVisible(false);
        
        selectionPanel.add(datePickerPanel);
        controlPanel.add(selectionPanel, BorderLayout.CENTER);
        
        // Set default values
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        endDateField.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, -30);
        startDateField.setText(sdf.format(cal.getTime()));
    }
    
    private void updateDatePickerVisibility(String reportType) {
        boolean showDateSelector = reportType.equals("Ticket Sales by Date");
        dateRangeComboBox.setVisible(showDateSelector);
        if (!showDateSelector) {
            datePickerPanel.setVisible(false);
        } else if ("Custom Range".equals(dateRangeComboBox.getSelectedItem())) {
            datePickerPanel.setVisible(true);
        }
    }
    
    private void updateCharts(String reportType) {
        chartsPanel.removeAll();
        
        switch (reportType) {
            case "Ticket Sales by Show":
                displayTicketSalesByShow();
                break;
            case "Ticket Sales by Date":
                displayTicketSalesByDate();
                break;
            case "Revenue by Show":
                displayRevenueByShow();
                break;
            case "Hall Occupancy":
                displayHallOccupancy();
                break;
        }
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private void displayTicketSalesByShow() {
        Map<String, Integer> data = dbConnection.getTicketSalesByShow();
        
        // Create pie chart
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            pieDataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart pieChart = ChartFactory.createPieChart(
            "Ticket Sales Distribution by Show",
            pieDataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        pieChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        pieChartPanel.setBackground(CARD_COLOR);
        
        // Create bar chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            barDataset.addValue(entry.getValue(), "Tickets Sold", entry.getKey());
        }
        
        JFreeChart barChart = ChartFactory.createBarChart(
            "Number of Tickets Sold by Show",
            "Show",
            "Tickets Sold",
            barDataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        barChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        barChartPanel.setBackground(CARD_COLOR);
        
        chartsPanel.add(pieChartPanel);
        chartsPanel.add(barChartPanel);
        
        // If no data, display a message
        if (data.isEmpty()) {
            displayNoDataMessage();
        }
    }
    
    private void displayTicketSalesByDate() {
        String startDate, endDate;
        String selectedRange = (String) dateRangeComboBox.getSelectedItem();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        endDate = sdf.format(cal.getTime());
        
        switch (selectedRange) {
            case "Last 7 Days":
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startDate = sdf.format(cal.getTime());
                break;
            case "Last 30 Days":
                cal.add(Calendar.DAY_OF_MONTH, -30);
                startDate = sdf.format(cal.getTime());
                break;
            case "Last 90 Days":
                cal.add(Calendar.DAY_OF_MONTH, -90);
                startDate = sdf.format(cal.getTime());
                break;
            case "Custom Range":
                startDate = startDateField.getText();
                endDate = endDateField.getText();
                break;
            default:
                cal.add(Calendar.DAY_OF_MONTH, -30);
                startDate = sdf.format(cal.getTime());
        }
        
        Map<String, Integer> data = dbConnection.getTicketSalesByDateRange(startDate, endDate);
        
        // Create line chart
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            lineDataset.addValue(entry.getValue(), "Tickets Sold", entry.getKey());
        }
        
        JFreeChart lineChart = ChartFactory.createLineChart(
            "Ticket Sales Trend (" + startDate + " to " + endDate + ")",
            "Date",
            "Tickets Sold",
            lineDataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel lineChartPanel = new ChartPanel(lineChart);
        lineChartPanel.setPreferredSize(new Dimension(400, 300));
        lineChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        lineChartPanel.setBackground(CARD_COLOR);
        
        // Create bar chart for the same data
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            barDataset.addValue(entry.getValue(), "Tickets Sold", entry.getKey());
        }
        
        JFreeChart barChart = ChartFactory.createBarChart(
            "Daily Ticket Sales (" + startDate + " to " + endDate + ")",
            "Date",
            "Tickets Sold",
            barDataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        barChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        barChartPanel.setBackground(CARD_COLOR);
        
        chartsPanel.add(lineChartPanel);
        chartsPanel.add(barChartPanel);
        
        // If no data, display a message
        if (data.isEmpty()) {
            displayNoDataMessage();
        }
    }
    
    private void displayRevenueByShow() {
        Map<String, Double> data = dbConnection.getRevenueByShow();
        
        // Create pie chart
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            pieDataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart pieChart = ChartFactory.createPieChart(
            "Revenue Distribution by Show",
            pieDataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        pieChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        pieChartPanel.setBackground(CARD_COLOR);
        
        // Create bar chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            barDataset.addValue(entry.getValue(), "Revenue (£)", entry.getKey());
        }
        
        JFreeChart barChart = ChartFactory.createBarChart(
            "Revenue by Show",
            "Show",
            "Revenue (£)",
            barDataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        barChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        barChartPanel.setBackground(CARD_COLOR);
        
        chartsPanel.add(pieChartPanel);
        chartsPanel.add(barChartPanel);
        
        // If no data, display a message
        if (data.isEmpty()) {
            displayNoDataMessage();
        }
    }
    
    private void displayHallOccupancy() {
        List<Map<String, Object>> data = dbConnection.getHallOccupancyData();
        
        // Create stacked bar chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        
        for (Map<String, Object> showData : data) {
            String show = (String) showData.get("showTitle");
            int capacity = (int) showData.get("capacity");
            int occupied = (int) showData.get("occupied");
            int available = capacity - occupied;
            
            barDataset.addValue(occupied, "Occupied", show);
            barDataset.addValue(available, "Available", show);
        }
        
        JFreeChart barChart = ChartFactory.createStackedBarChart(
            "Hall Occupancy by Show",
            "Show",
            "Number of Seats",
            barDataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        barChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        barChartPanel.setBackground(CARD_COLOR);
        
        // Create pie chart for overall occupancy
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        int totalOccupied = 0;
        int totalCapacity = 0;
        
        for (Map<String, Object> showData : data) {
            totalCapacity += (int) showData.get("capacity");
            totalOccupied += (int) showData.get("occupied");
        }
        
        int totalAvailable = totalCapacity - totalOccupied;
        pieDataset.setValue("Occupied", totalOccupied);
        pieDataset.setValue("Available", totalAvailable);
        
        JFreeChart pieChart = ChartFactory.createPieChart(
            "Overall Hall Occupancy",
            pieDataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
        
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        pieChartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        pieChartPanel.setBackground(CARD_COLOR);
        
        chartsPanel.add(barChartPanel);
        chartsPanel.add(pieChartPanel);
        
        // If no data, display a message
        if (data.isEmpty()) {
            displayNoDataMessage();
        }
    }
    
    private void displayNoDataMessage() {
        chartsPanel.removeAll();
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(CARD_COLOR);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        
        JLabel messageLabel = new JLabel("No data available for this report type", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(TEXT_COLOR);
        
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        chartsPanel.add(messagePanel);
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
}
