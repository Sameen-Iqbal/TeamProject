import UI.PatronPage;
import UI.ReportsPage;
import UI.SeatingPage;
import UI.TicketsPage;
import UI.RefundsPage;
import java.sql.SQLException;

import db.dbConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * This is the Dashboard file. Run this file.
 * Subject to change for better implementation and focus of data.
 */
public class Dashboard {

    private static final Color PRIMARY_COLOR = new Color(30, 71, 19);
    private static final Color SECONDARY_COLOR = new Color(82, 109, 165);
    private static final Color BACKGROUND_COLOR = new Color(245, 246, 250);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setUndecorated(true);

            // Make the frame resizable even though it's undecorated
            frame.setMinimumSize(new Dimension(1000, 700));

            // Create the resizable border
            JPanel resizablePanel = new JPanel(new BorderLayout());
            resizablePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            // Create and add components
            JPanel mainPanel = new JPanel(new BorderLayout(0, 0));

            // Title bar
            TitleBar titleBar = createCustomTitleBar(frame);
            mainPanel.add(titleBar, BorderLayout.NORTH);

            // Main content area with sidebar and dashboard
            JPanel contentPanel = new JPanel(new BorderLayout(0, 0));

            // Use CardLayout for switching pages
            CardLayout cardLayout = new CardLayout();
            JPanel cardPanel = new JPanel(cardLayout);

            //dashboard = home
            cardPanel.add(createDashboardContent(), "Home");
            cardPanel.add(createReportsPage(), "Reports");
            cardPanel.add(createTicketsPage(), "Tickets");
            cardPanel.add(createPatronPage(), "Patron");
            cardPanel.add(createSeatingPage(), "Seating");
            cardPanel.add(createRefundsPage(), "Refunds");
            cardPanel.add(createSettingsPage(), "Settings");

            // Sidebar with navigation
            NavigationBar sidebar = new NavigationBar(cardLayout, cardPanel);
            contentPanel.add(sidebar, BorderLayout.WEST);

            contentPanel.add(cardPanel, BorderLayout.CENTER);

            mainPanel.add(contentPanel, BorderLayout.CENTER);

            // Add drag functionality to the title bar
            DragListener dragListener = new DragListener();
            titleBar.addMouseListener(dragListener);
            titleBar.addMouseMotionListener(dragListener);

            // Add the main panel to the resizable panel
            resizablePanel.add(mainPanel, BorderLayout.CENTER);

            // Add the resizable border and functionality
            ResizeListener resizeListener = new ResizeListener(frame);
            resizablePanel.addMouseListener(resizeListener);
            resizablePanel.addMouseMotionListener(resizeListener);

            frame.add(resizablePanel);
            frame.setVisible(true);
        });
    }

    private static TitleBar createCustomTitleBar(JFrame frame) {
        TitleBar titleBar = new TitleBar();
        titleBar.setBackground(PRIMARY_COLOR);
        titleBar.setPreferredSize(new Dimension(0, 40));

        // Add custom title label
        JLabel titleLabel = new JLabel("Lancaster Music Hall OS");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Add window control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);

        // Minimize button
        JButton minimizeButton = new JButton("−");
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        minimizeButton.setForeground(Color.WHITE);
        minimizeButton.setBackground(PRIMARY_COLOR);
        minimizeButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        minimizeButton.setFocusPainted(false);
        minimizeButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));

        // Maximize/restore button
        JButton maximizeButton = new JButton("□");
        maximizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        maximizeButton.setForeground(Color.WHITE);
        maximizeButton.setBackground(PRIMARY_COLOR);
        maximizeButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        maximizeButton.setFocusPainted(false);
        maximizeButton.addActionListener(e -> {
            if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                frame.setExtendedState(JFrame.NORMAL);
                maximizeButton.setText("□");
            } else {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                maximizeButton.setText("❐");
            }
        });

        // Close button
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(PRIMARY_COLOR);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> System.exit(0));

        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);

        titleBar.add(controlPanel, BorderLayout.EAST);

        return titleBar;
    }

    private static JPanel createDashboardContent() {
        JPanel dashboardContent = new JPanel(new BorderLayout());
        dashboardContent.setBackground(BACKGROUND_COLOR);
        dashboardContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create a panel for the top section
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel welcomeLabel = new JLabel("Welcome back, Muhammad!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 23));
        welcomeLabel.setForeground(TEXT_COLOR);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
        String formattedDate = now.format(formatter);

        JLabel dateLabel = new JLabel(formattedDate);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(120, 120, 120));

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(welcomeLabel);
        headerTextPanel.add(dateLabel);

        headerPanel.add(headerTextPanel, BorderLayout.WEST);

        // Add the header to the top section
        topSection.add(headerPanel, BorderLayout.NORTH);

        // Create metric cards
        JPanel cardPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardPanel.setOpaque(false);

        // Example data for future uses when integrated with the database - such as to calculate the revenue etc
        cardPanel.add(createMetricCard("$15,678", "Total Revenue", "📈 +12% from last month", PRIMARY_COLOR));
        cardPanel.add(createMetricCard("247", "New Patron", "👥 +8% from last month", SECONDARY_COLOR));
        cardPanel.add(createMetricCard("18", "Ticket Sales", "📦 -5% from last month", new Color(255, 153, 0)));

        topSection.add(cardPanel, BorderLayout.CENTER);


        dashboardContent.add(topSection, BorderLayout.NORTH);

        // Create a panel for the table
        JPanel tablePanel = new JPanel();
        tablePanel.setBackground(CARD_COLOR);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tablePanel.setLayout(new BorderLayout());

        JLabel tableTitle = new JLabel("Recent Patron");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_COLOR);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        tablePanel.add(tableTitle, BorderLayout.NORTH);

        // Table
        JTable table = new JTable();
        table.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Name", "Email", "Personal", "Joined"}
        ));

        JScrollPane tableScrollPane = new JScrollPane(table);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add some data to the table - not from the database at all - can be deleted.
        ((javax.swing.table.DefaultTableModel) table.getModel()).addRow(new Object[]{"Mike Bhand", "mikebhand@gmail.com", "Patron", "25 Apr, 2021"});
        ((javax.swing.table.DefaultTableModel) table.getModel()).addRow(new Object[]{"Andrew Strauss", "andrewstrauss@gmail.com", "Patron", "25 Apr, 2021"});
        ((javax.swing.table.DefaultTableModel) table.getModel()).addRow(new Object[]{"Ross Kopelman", "rosskopelman@gmail.com", "FriendOfLancaster", "25 Apr, 2024"});
        ((javax.swing.table.DefaultTableModel) table.getModel()).addRow(new Object[]{"Mike Hussy", "mikehussy@gmail.com", "Admin", "Patron", "2024"});
        ((javax.swing.table.DefaultTableModel) table.getModel()).addRow(new Object[]{"Kevin Pietersen", "kevinpietersen@gmail.com", "Patron", "25 Apr, 2024"});

        dashboardContent.add(tablePanel, BorderLayout.CENTER);

        return dashboardContent;
    }

    private static JPanel createMetricCard(String value, String title, String subtitle, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(4, 4, getWidth(), getHeight(), 15, 15);


                g2.setColor(CARD_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setOpaque(false);

        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(120, 120, 120));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_COLOR);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(accentColor);

        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);
        contentPanel.add(subtitleLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        // Add a small colored indicator at the top
        JPanel indicator = new JPanel();
        indicator.setBackground(accentColor);
        indicator.setPreferredSize(new Dimension(0, 4));
        card.add(indicator, BorderLayout.NORTH);

        return card;
    }




    //creation of Pages - !!! DO NOT MODIFY or EDIT
    // only modify when you want to implement the database retrieval from the database by adding SQLException
    // look at the createPatronPage below and use this format to change your page with a try-catch exception.

    private static JPanel createReportsPage() {
        return new ReportsPage();
    }

    private static JPanel createTicketsPage() {
        return new TicketsPage();
    }



    private static JPanel createPatronPage() {
        try {
            return new PatronPage();
        } catch (SQLException e) {
            e.printStackTrace();
            JPanel errorPanel = new JPanel();
            errorPanel.add(new JLabel("Database error: " + e.getMessage()));
            return errorPanel;
        }
    }

    private static JPanel createSeatingPage() {
        try {
            return new SeatingPage();
        } catch (SQLException e) {
            e.printStackTrace();
            JPanel errorPanel = new JPanel();
            errorPanel.add(new JLabel("Database error: " + e.getMessage()));
            return errorPanel;
        }
    }




    private static JPanel createSettingsPage() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBackground(BACKGROUND_COLOR);
        settingsPanel.add(new JLabel("Settings Page"));
        return settingsPanel;
    }


    private static JPanel createRefundsPage() {
        try {
            return new RefundsPage(); // This is your full RefundsPage class!
        } catch (Exception e) {
            e.printStackTrace();
            JPanel errorPanel = new JPanel();
            errorPanel.add(new JLabel("Failed to load Refunds Page: " + e.getMessage()));
            return errorPanel;
        }
    }



    // Class to handle window dragging
    private static class DragListener extends MouseAdapter {
        private Point startPoint;

        @Override
        public void mousePressed(MouseEvent e) {
            startPoint = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point currentPoint = e.getLocationOnScreen();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());

            if (frame != null && frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                frame.setLocation(
                        currentPoint.x - startPoint.x,
                        currentPoint.y - startPoint.y
                );
            }
        }
    }

    // Class to handle window resizing
    private static class ResizeListener extends MouseAdapter {
        private static final int RESIZE_BORDER = 5;
        private JFrame frame;
        private int cursor;
        private Rectangle startBounds;
        private Point startPoint;

        public ResizeListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            startPoint = e.getPoint();
            startBounds = frame.getBounds();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int width = frame.getWidth();
            int height = frame.getHeight();

            // Determine cursor based on position
            if (y >= height - RESIZE_BORDER) {
                if (x >= width - RESIZE_BORDER) {
                    cursor = Cursor.SE_RESIZE_CURSOR;
                } else if (x <= RESIZE_BORDER) {
                    cursor = Cursor.SW_RESIZE_CURSOR;
                } else {
                    cursor = Cursor.S_RESIZE_CURSOR;
                }
            } else if (y <= RESIZE_BORDER) {
                if (x >= width - RESIZE_BORDER) {
                    cursor = Cursor.NE_RESIZE_CURSOR;
                } else if (x <= RESIZE_BORDER) {
                    cursor = Cursor.NW_RESIZE_CURSOR;
                } else {
                    cursor = Cursor.N_RESIZE_CURSOR;
                }
            } else if (x >= width - RESIZE_BORDER) {
                cursor = Cursor.E_RESIZE_CURSOR;
            } else if (x <= RESIZE_BORDER) {
                cursor = Cursor.W_RESIZE_CURSOR;
            } else {
                cursor = Cursor.DEFAULT_CURSOR;
            }

            frame.setCursor(Cursor.getPredefinedCursor(cursor));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (cursor == Cursor.DEFAULT_CURSOR || frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                return;
            }

            int dx = e.getX() - startPoint.x;
            int dy = e.getY() - startPoint.y;

            int x = startBounds.x;
            int y = startBounds.y;
            int width = startBounds.width;
            int height = startBounds.height;

            // Resize based on cursor position
            switch (cursor) {
                case Cursor.NW_RESIZE_CURSOR:
                    x += dx;
                    y += dy;
                    width -= dx;
                    height -= dy;
                    break;
                case Cursor.N_RESIZE_CURSOR:
                    y += dy;
                    height -= dy;
                    break;
                case Cursor.NE_RESIZE_CURSOR:
                    y += dy;
                    width += dx;
                    height -= dy;
                    break;
                case Cursor.E_RESIZE_CURSOR:
                    width += dx;
                    break;
                case Cursor.SE_RESIZE_CURSOR:
                    width += dx;
                    height += dy;
                    break;
                case Cursor.S_RESIZE_CURSOR:
                    height += dy;
                    break;
                case Cursor.SW_RESIZE_CURSOR:
                    x += dx;
                    width -= dx;
                    height += dy;
                    break;
                case Cursor.W_RESIZE_CURSOR:
                    x += dx;
                    width -= dx;
                    break;
            }

            // Enforce minimum size
            Dimension minSize = frame.getMinimumSize();
            if (width < minSize.width) {
                if (x != startBounds.x) {
                    x = startBounds.x + startBounds.width - minSize.width;
                }
                width = minSize.width;
            }
            if (height < minSize.height) {
                if (y != startBounds.y) {
                    y = startBounds.y + startBounds.height - minSize.height;
                }
                height = minSize.height;
            }

            // Set the new bounds
            frame.setBounds(x, y, width, height);
            frame.revalidate();
        }
    }


    private static class TitleBar extends JPanel {
        public TitleBar() {
            setLayout(new BorderLayout());
        }
    }

}
