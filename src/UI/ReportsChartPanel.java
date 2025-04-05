package UI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class ReportsChartPanel extends JPanel {
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JButton generateButton;
    private JButton exportChartButton;
    private DefaultTableModel tableModel;
    private JPanel chartContainer;
    private JFreeChart currentChart;

    public ReportsChartPanel(DefaultTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBackground(new Color(0, 153, 0));

        monthCombo = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });

        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026});
        generateButton = new JButton("Generate Chart");
        exportChartButton = new JButton("Export Chart as PNG");

        controls.add(new JLabel("Month:"));
        controls.add(monthCombo);
        controls.add(new JLabel("Year:"));
        controls.add(yearCombo);
        controls.add(generateButton);
        controls.add(exportChartButton);

        add(controls, BorderLayout.NORTH);

        chartContainer = new JPanel(new BorderLayout());
        add(chartContainer, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateChart());
        exportChartButton.addActionListener(e -> exportChartAsPNG());
    }

    private void generateChart() {
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = (int) yearCombo.getSelectedItem();
        HashMap<String, Double> revenueMap = new HashMap<>();
        DateTimeFormatter tableFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String dateStr = tableModel.getValueAt(i, 1).toString();
                LocalDate reportDate = LocalDate.parse(dateStr, tableFormat);

                if (reportDate.getMonthValue() == selectedMonth && reportDate.getYear() == selectedYear) {
                    String reportType = tableModel.getValueAt(i, 2).toString();
                    double revenue = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

                    revenueMap.put(reportType, revenueMap.getOrDefault(reportType, 0.0) + revenue);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String type : revenueMap.keySet()) {
            dataset.addValue(revenueMap.get(type), "Revenue", type);
        }

        currentChart = ChartFactory.createBarChart(
                "Revenue by Report Type - " + monthCombo.getSelectedItem() + " " + selectedYear,
                "Report Type", "Revenue (£)", dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                false, true, false
        );

        currentChart.setBackgroundPaint(Color.WHITE);
        currentChart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(0, 153, 0));

        chartContainer.removeAll();
        chartContainer.add(new ChartPanel(currentChart), BorderLayout.CENTER);
        chartContainer.validate();
    }

    private void exportChartAsPNG() {
        if (currentChart == null) {
            JOptionPane.showMessageDialog(this, "Please generate the chart first.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Chart As PNG");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            try {
                ChartUtilities.saveChartAsPNG(fileToSave, currentChart, 800, 600);
                JOptionPane.showMessageDialog(this, "Chart successfully exported to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export chart: " + ex.getMessage());
            }
        }
    }
}
