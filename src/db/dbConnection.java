package db;

import java.sql.*;
import java.util.Properties;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
//This class is

/**
 * Establishes Connection with the Database
 *
 */

public class dbConnection {

    private static final String URL = "jdbc:mysql://sst-stuproj00.city.ac.uk:3306/in2033t10";
    private static final String USERNAME = "in2033t10_a";
    private static final String PASSWORD = "lonmF2uLJSc";



    public static Connection getConnection() throws SQLException {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Properties connectionProps = new Properties();
            connectionProps.put("user", USERNAME);
            connectionProps.put("password", PASSWORD);

            con = DriverManager.getConnection(URL, connectionProps);
            System.out.println("Connection Successful...");
            return con;
        }
        catch(SQLException sql) {
            System.out.println("Database Connection has Failed...");
            sql.printStackTrace();
            throw sql;
        }
        catch (ClassNotFoundException sql){
            System.out.println("Driver is not found!");
            sql.printStackTrace();
            throw new SQLException("JDBC Driver not found", sql);
        }
    }

    public static boolean LoginUser(String Username, String Password) {
        String query = "SELECT * FROM Users WHERE Username = ? AND Password = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, Username);
            preparedStatement.setString(2, Password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Provides sample ticket sales data for demonstration when real data is not available
     * @return Map with show names as keys and ticket counts as values
     */
    public static Map<String, Integer> getSampleTicketSalesData() {
        Map<String, Integer> sampleData = new HashMap<>();
        sampleData.put("Hamilton", 358);
        sampleData.put("The Lion King", 245);
        sampleData.put("Wicked", 189);
        sampleData.put("The Phantom of the Opera", 320);
        sampleData.put("Les Misérables", 275);
        return sampleData;
    }
    
    /**
     * Gets ticket sales data for reports by show, with fallback to sample data
     * @return Map with show names as keys and ticket counts as values
     */
    public static Map<String, Integer> getTicketSalesByShow() {
        Map<String, Integer> salesData = new HashMap<>();
        String query = "SELECT s.Show_Title, COUNT(sa.Seat_ID) AS TicketsSold " +
                      "FROM Shows s " +
                      "LEFT JOIN Seat_Availability sa ON s.Show_ID = sa.Show_ID " +
                      "WHERE sa.Status = 'Sold' " +
                      "GROUP BY s.Show_Title";
                     
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                String showTitle = resultSet.getString("Show_Title");
                int ticketCount = resultSet.getInt("TicketsSold");
                salesData.put(showTitle, ticketCount);
            }
            
            // If no data from database, use sample data
            if (!hasData) {
                return getSampleTicketSalesData();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // On error, return sample data
            return getSampleTicketSalesData();
        }
        
        return salesData;
    }
    
    /**
     * Provides sample ticket sales data by date for demonstration
     * @return Map with dates as keys and ticket counts as values
     */
    public static Map<String, Integer> getSampleTicketSalesByDate() {
        Map<String, Integer> sampleData = new HashMap<>();
        // Generate sales data for the last 30 days
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        
        // Current date as end point
        cal.add(Calendar.DAY_OF_MONTH, -30);
        
        // Generate 30 days of data
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String date = sdf.format(cal.getTime());
            
            // Generate a random number between 5 and 50 for ticket sales
            int ticketsSold = random.nextInt(46) + 5;
            sampleData.put(date, ticketsSold);
        }
        
        return sampleData;
    }
    
    /**
     * Gets ticket sales data by date range, with fallback to sample data
     * @param startDate the start date in format 'YYYY-MM-DD'
     * @param endDate the end date in format 'YYYY-MM-DD'
     * @return Map with dates as keys and ticket counts as values
     */
    public static Map<String, Integer> getTicketSalesByDateRange(String startDate, String endDate) {
        Map<String, Integer> salesData = new HashMap<>();
        String query = "SELECT DATE(s.Show_Date) as SaleDate, COUNT(sa.Seat_ID) AS TicketsSold " +
                      "FROM Shows s " +
                      "LEFT JOIN Seat_Availability sa ON s.Show_ID = sa.Show_ID " +
                      "WHERE sa.Status = 'Sold' AND s.Show_Date BETWEEN ? AND ? " +
                      "GROUP BY DATE(s.Show_Date) " +
                      "ORDER BY SaleDate";
                     
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                String date = resultSet.getString("SaleDate");
                int ticketCount = resultSet.getInt("TicketsSold");
                salesData.put(date, ticketCount);
            }
            
            // If no data from database, use sample data
            if (!hasData) {
                return getSampleTicketSalesByDate();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // On error, return sample data
            return getSampleTicketSalesByDate();
        }
        
        return salesData;
    }
    
    /**
     * Provides sample revenue data for demonstration
     * @return Map with show names as keys and revenue as values
     */
    public static Map<String, Double> getSampleRevenueData() {
        Map<String, Double> sampleData = new HashMap<>();
        sampleData.put("Hamilton", 35800.50);
        sampleData.put("The Lion King", 24500.75);
        sampleData.put("Wicked", 18920.25);
        sampleData.put("The Phantom of the Opera", 32080.00);
        sampleData.put("Les Misérables", 27540.50);
        return sampleData;
    }
    
    /**
     * Gets revenue data by show, with fallback to sample data
     * @return Map with show names as keys and revenue as values
     */
    public static Map<String, Double> getRevenueByShow() {
        Map<String, Double> revenueData = new HashMap<>();
        String query = "SELECT s.Show_Title, SUM(t.Price) AS Revenue " +
                      "FROM Shows s " +
                      "LEFT JOIN Tickets t ON s.Show_ID = t.Show_ID " +
                      "GROUP BY s.Show_Title";
                     
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                String showTitle = resultSet.getString("Show_Title");
                double revenue = resultSet.getDouble("Revenue");
                revenueData.put(showTitle, revenue);
            }
            
            // If no data from database, use sample data
            if (!hasData) {
                return getSampleRevenueData();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // On error, return sample data
            return getSampleRevenueData();
        }
        
        return revenueData;
    }
    
    /**
     * Provides sample hall occupancy data for demonstration
     * @return List of maps containing show title, capacity, and occupied seats
     */
    public static List<Map<String, Object>> getSampleOccupancyData() {
        List<Map<String, Object>> sampleData = new ArrayList<>();
        
        // Sample show 1
        Map<String, Object> show1 = new HashMap<>();
        show1.put("showTitle", "Hamilton");
        show1.put("capacity", 500);
        show1.put("occupied", 358);
        sampleData.add(show1);
        
        // Sample show 2
        Map<String, Object> show2 = new HashMap<>();
        show2.put("showTitle", "The Lion King");
        show2.put("capacity", 500);
        show2.put("occupied", 245);
        sampleData.add(show2);
        
        // Sample show 3
        Map<String, Object> show3 = new HashMap<>();
        show3.put("showTitle", "Wicked");
        show3.put("capacity", 300);
        show3.put("occupied", 189);
        sampleData.add(show3);
        
        // Sample show 4
        Map<String, Object> show4 = new HashMap<>();
        show4.put("showTitle", "The Phantom of the Opera");
        show4.put("capacity", 400);
        show4.put("occupied", 320);
        sampleData.add(show4);
        
        // Sample show 5
        Map<String, Object> show5 = new HashMap<>();
        show5.put("showTitle", "Les Misérables");
        show5.put("capacity", 450);
        show5.put("occupied", 275);
        sampleData.add(show5);
        
        return sampleData;
    }

    /**
     * Gets hall occupancy data for each show, with fallback to sample data
     * @return List of maps containing show title, capacity, and occupied seats
     */
    public static List<Map<String, Object>> getHallOccupancyData() {
        List<Map<String, Object>> occupancyData = new ArrayList<>();
        String query = "SELECT s.Show_ID, s.Show_Title, " +
                      "(SELECT COUNT(*) FROM Seat WHERE Venue_ID = s.Venue_ID) AS TotalCapacity, " +
                      "COUNT(sa.Seat_ID) AS OccupiedSeats " +
                      "FROM Shows s " +
                      "LEFT JOIN Seat_Availability sa ON s.Show_ID = sa.Show_ID AND sa.Status = 'Sold' " +
                      "GROUP BY s.Show_ID, s.Show_Title";
                     
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                Map<String, Object> showData = new HashMap<>();
                showData.put("showTitle", resultSet.getString("Show_Title"));
                showData.put("capacity", resultSet.getInt("TotalCapacity"));
                showData.put("occupied", resultSet.getInt("OccupiedSeats"));
                occupancyData.add(showData);
            }
            
            // If no data from database, use sample data
            if (!hasData) {
                return getSampleOccupancyData();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // On error, return sample data
            return getSampleOccupancyData();
        }
        
        return occupancyData;
    }
}





