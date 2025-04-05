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
import java.lang.StringBuilder;
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

    // Get all bookings for refund UI
    public static ResultSet getAllBookings() {
        String query = "SELECT b.Booking_ID, b.Patron_ID, b.TotalCost, b.Booking_Date, " +
                "b.IsCancelled, p.IsRefunded " +
                "FROM Booking b " +
                "LEFT JOIN Payment p ON b.Booking_ID = p.Booking_ID";

        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Mark a booking as refunded (IsCancelled = 1)
    public static boolean markBookingAsRefunded(int bookingId) {
        String query = "UPDATE Booking SET IsCancelled = 1 WHERE Booking_ID = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mark a payment as refunded (IsRefunded = 1)
    public static boolean markPaymentAsRefunded(int bookingId) {
        String query = "UPDATE Payment SET IsRefunded = 1 WHERE Booking_ID = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mark related tickets as unsold (IsSold = 0)
    public static boolean markTicketsAsUnsold(int bookingId) {
        String query = "UPDATE Ticket SET IsSold = 0 WHERE Booking_ID = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
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

    /**
     * Adds a new show to the database
     * @param showTitle The title of the show
     * @param showDate Date of the show (yyyy-MM-dd)
     * @param venueId Venue ID for the show
     * @param hallType Type of hall (e.g., "Main Hall" or "Small Hall")
     * @param ticketPrice Base ticket price
     * @return The ID of the newly created show, or -1 if creation failed
     */
    public static int addNewShow(String showTitle, String showDate, int venueId, String hallType, double ticketPrice) {
        String query = "INSERT INTO Shows (Show_Title, Show_Date, Venue_ID, Hall_Type, Base_Price) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            preparedStatement.setString(1, showTitle);
            preparedStatement.setString(2, showDate);
            preparedStatement.setInt(3, venueId);
            preparedStatement.setString(4, hallType);
            preparedStatement.setDouble(5, ticketPrice);
            
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Gets all available venues
     * @return List of venue maps containing ID and name
     */
    public static List<Map<String, Object>> getVenues() {
        List<Map<String, Object>> venues = new ArrayList<>();
        String query = "SELECT Venue_ID, Venue_Name FROM Venue";
        
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                Map<String, Object> venue = new HashMap<>();
                venue.put("id", resultSet.getInt("Venue_ID"));
                venue.put("name", resultSet.getString("Venue_Name"));
                venues.add(venue);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If no venues in database yet, provide sample data
        if (venues.isEmpty()) {
            Map<String, Object> venue1 = new HashMap<>();
            venue1.put("id", 1);
            venue1.put("name", "Main Music Hall");
            venues.add(venue1);
            
            Map<String, Object> venue2 = new HashMap<>();
            venue2.put("id", 2);
            venue2.put("name", "Small Music Hall");
            venues.add(venue2);
        }
        
        return venues;
    }
    
    /**
     * Gets all shows for ticket sales
     * @return List of show maps containing show details
     */
    public static List<Map<String, Object>> getAllShows() {
        List<Map<String, Object>> shows = new ArrayList<>();
        String query = "SELECT s.Show_ID, s.Show_Title, s.Show_Date, s.Hall_Type, s.Base_Price, " +
                      "v.Venue_Name, " +
                      "(SELECT COUNT(*) FROM Seat WHERE Venue_ID = s.Venue_ID) AS TotalSeats, " +
                      "(SELECT COUNT(*) FROM Seat_Availability sa " +
                      " JOIN Seat se ON sa.Seat_ID = se.Seat_ID " +
                      " WHERE sa.Show_ID = s.Show_ID AND sa.Status = 'Sold' AND se.Venue_ID = s.Venue_ID) AS SoldSeats " +
                      "FROM Shows s " +
                      "JOIN Venue v ON s.Venue_ID = v.Venue_ID " +
                      "ORDER BY s.Show_Date";
        
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                Map<String, Object> show = new HashMap<>();
                show.put("id", resultSet.getInt("Show_ID"));
                show.put("title", resultSet.getString("Show_Title"));
                show.put("date", resultSet.getString("Show_Date"));
                show.put("hallType", resultSet.getString("Hall_Type"));
                show.put("venue", resultSet.getString("Venue_Name"));
                show.put("price", resultSet.getDouble("Base_Price"));
                show.put("totalSeats", resultSet.getInt("TotalSeats"));
                show.put("soldSeats", resultSet.getInt("SoldSeats"));
                show.put("availableSeats", resultSet.getInt("TotalSeats") - resultSet.getInt("SoldSeats"));
                shows.add(show);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If no shows in database yet, provide sample data
        if (shows.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            
            Map<String, Object> show1 = new HashMap<>();
            show1.put("id", 1);
            show1.put("title", "Hamilton");
            show1.put("date", sdf.format(cal.getTime()));
            show1.put("hallType", "Main Hall");
            show1.put("venue", "Main Music Hall");
            show1.put("price", 99.99);
            show1.put("totalSeats", 500);
            show1.put("soldSeats", 358);
            show1.put("availableSeats", 142);
            shows.add(show1);
            
            cal.add(Calendar.DAY_OF_MONTH, 2);
            Map<String, Object> show2 = new HashMap<>();
            show2.put("id", 2);
            show2.put("title", "The Lion King");
            show2.put("date", sdf.format(cal.getTime()));
            show2.put("hallType", "Main Hall");
            show2.put("venue", "Main Music Hall");
            show2.put("price", 89.99);
            show2.put("totalSeats", 500);
            show2.put("soldSeats", 245);
            show2.put("availableSeats", 255);
            shows.add(show2);
            
            cal.add(Calendar.DAY_OF_MONTH, 3);
            Map<String, Object> show3 = new HashMap<>();
            show3.put("id", 3);
            show3.put("title", "Wicked");
            show3.put("date", sdf.format(cal.getTime()));
            show3.put("hallType", "Small Hall");
            show3.put("venue", "Small Music Hall");
            show3.put("price", 79.99);
            show3.put("totalSeats", 300);
            show3.put("soldSeats", 189);
            show3.put("availableSeats", 111);
            shows.add(show3);
        }
        
        return shows;
    }
    
    /**
     * Get patrons for ticket sales
     * @return List of patron maps containing patron details
     */
    public static List<Map<String, Object>> getPatrons() {
        List<Map<String, Object>> patrons = new ArrayList<>();
        String query = "SELECT Patron_ID, First_Name, Last_Name, Email, Phone FROM Patron";
        
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                Map<String, Object> patron = new HashMap<>();
                patron.put("id", resultSet.getInt("Patron_ID"));
                patron.put("firstName", resultSet.getString("First_Name"));
                patron.put("lastName", resultSet.getString("Last_Name"));
                patron.put("email", resultSet.getString("Email"));
                patron.put("phone", resultSet.getString("Phone"));
                patrons.add(patron);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // If no patrons in database yet, provide sample data
        if (patrons.isEmpty()) {
            Map<String, Object> patron1 = new HashMap<>();
            patron1.put("id", 1);
            patron1.put("firstName", "John");
            patron1.put("lastName", "Doe");
            patron1.put("email", "john.doe@example.com");
            patron1.put("phone", "07700900123");
            patrons.add(patron1);
            
            Map<String, Object> patron2 = new HashMap<>();
            patron2.put("id", 2);
            patron2.put("firstName", "Jane");
            patron2.put("lastName", "Smith");
            patron2.put("email", "jane.smith@example.com");
            patron2.put("phone", "07700900456");
            patrons.add(patron2);
            
            Map<String, Object> patron3 = new HashMap<>();
            patron3.put("id", 3);
            patron3.put("firstName", "Michael");
            patron3.put("lastName", "Johnson");
            patron3.put("email", "michael.johnson@example.com");
            patron3.put("phone", "07700900789");
            patrons.add(patron3);
        }
        
        return patrons;
    }
    
    /**
     * Add a new patron to the database
     * @param firstName First name of the patron
     * @param lastName Last name of the patron
     * @param email Email of the patron
     * @param phone Phone number of the patron
     * @return The ID of the newly created patron, or -1 if creation failed
     */
    public static int addNewPatron(String firstName, String lastName, String email, String phone) {
        String query = "INSERT INTO Patron (First_Name, Last_Name, Email, Phone) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, phone);
            
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Process ticket sale for a show
     * @param showId ID of the show
     * @param patronId ID of the patron
     * @param seatIds List of seat IDs to be sold
     * @param totalPrice Total price of all tickets
     * @return True if the sale was successful, false otherwise
     */
    public static boolean processSale(int showId, int patronId, List<Integer> seatIds, double totalPrice) {
        Connection connection = null;
        
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            
            // 1. Create sale record
            String saleQuery = "INSERT INTO Sales (Patron_ID, Sale_Date, Total_Amount) VALUES (?, NOW(), ?)";
            PreparedStatement salePreparedStatement = connection.prepareStatement(saleQuery, Statement.RETURN_GENERATED_KEYS);
            salePreparedStatement.setInt(1, patronId);
            salePreparedStatement.setDouble(2, totalPrice);
            
            int saleRowsAffected = salePreparedStatement.executeUpdate();
            if (saleRowsAffected == 0) {
                connection.rollback();
                return false;
            }
            
            int saleId;
            try (ResultSet generatedKeys = salePreparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    saleId = generatedKeys.getInt(1);
                } else {
                    connection.rollback();
                    return false;
                }
            }
            
            // 2. Create ticket records
            String ticketQuery = "INSERT INTO Tickets (Sale_ID, Show_ID, Seat_ID, Price) VALUES (?, ?, ?, ?)";
            PreparedStatement ticketPreparedStatement = connection.prepareStatement(ticketQuery);
            
            // Get base price
            String priceQuery = "SELECT Base_Price FROM Shows WHERE Show_ID = ?";
            PreparedStatement pricePreparedStatement = connection.prepareStatement(priceQuery);
            pricePreparedStatement.setInt(1, showId);
            ResultSet priceResultSet = pricePreparedStatement.executeQuery();
            
            double basePrice = 0;
            if (priceResultSet.next()) {
                basePrice = priceResultSet.getDouble("Base_Price");
            }
            
            for (Integer seatId : seatIds) {
                ticketPreparedStatement.setInt(1, saleId);
                ticketPreparedStatement.setInt(2, showId);
                ticketPreparedStatement.setInt(3, seatId);
                ticketPreparedStatement.setDouble(4, basePrice);
                ticketPreparedStatement.addBatch();
                
                // Update seat availability
                String availabilityQuery = "INSERT INTO Seat_Availability (Seat_ID, Show_ID, Status) VALUES (?, ?, 'Sold')";
                PreparedStatement availabilityPreparedStatement = connection.prepareStatement(availabilityQuery);
                availabilityPreparedStatement.setInt(1, seatId);
                availabilityPreparedStatement.setInt(2, showId);
                availabilityPreparedStatement.executeUpdate();
            }
            
            int[] ticketRowsAffected = ticketPreparedStatement.executeBatch();
            
            // Check if all tickets were added successfully
            boolean allTicketsAdded = true;
            for (int rowsAffected : ticketRowsAffected) {
                if (rowsAffected == 0) {
                    allTicketsAdded = false;
                    break;
                }
            }
            
            if (!allTicketsAdded) {
                connection.rollback();
                return false;
            }
            
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets all tickets with their related information
     * @return List of ticket maps containing ticket details
     */
    public static List<Map<String, Object>> getAllTickets() {
        List<Map<String, Object>> tickets = new ArrayList<>();
        String query = "SELECT t.*, s.Show_Title, s.Show_Date, s.Show_Start_Time, " +
                      "b.Booking_Date, b.TotalCost, b.IsCancelled, b.Discount_Applied " +
                      "FROM Ticket t " +
                      "LEFT JOIN Shows s ON t.Show_ID = s.Show_ID " +
                      "LEFT JOIN Booking b ON t.Booking_ID = b.Booking_ID " +
                      "ORDER BY t.Ticket_ID DESC";
        
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("ticketId", resultSet.getInt("Ticket_ID"));
                ticket.put("price", resultSet.getDouble("Ticket_Price"));
                ticket.put("seatCode", resultSet.getString("Seat_Code"));
                ticket.put("rowNumber", resultSet.getString("Row_Number"));
                ticket.put("discountAmount", resultSet.getDouble("Discount_Amount"));
                ticket.put("isWheelchairAccessible", resultSet.getBoolean("IsWheelchairAccessible"));
                ticket.put("isRestrictedView", resultSet.getBoolean("IsRestrictedView"));
                ticket.put("isSold", resultSet.getBoolean("IsSold"));
                ticket.put("showId", resultSet.getInt("Show_ID"));
                ticket.put("bookingId", resultSet.getInt("Booking_ID"));
                ticket.put("seatId", resultSet.getInt("Seat_ID"));
                ticket.put("showTitle", resultSet.getString("Show_Title"));
                ticket.put("showDate", resultSet.getString("Show_Date"));
                ticket.put("showTime", resultSet.getString("Show_Start_Time"));
                ticket.put("bookingDate", resultSet.getString("Booking_Date"));
                ticket.put("totalCost", resultSet.getDouble("TotalCost"));
                ticket.put("isCancelled", resultSet.getBoolean("IsCancelled"));
                ticket.put("discountApplied", resultSet.getDouble("Discount_Applied"));
                tickets.add(ticket);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tickets;
    }

    /**
     * Gets detailed ticket information including show, booking, and patron details
     * @return List of ticket maps containing all relevant information
     */
    public static List<Map<String, Object>> getDetailedTickets() {
        List<Map<String, Object>> tickets = new ArrayList<>();
        String query = "SELECT t.*, s.Show_Title, s.Show_Date, s.Show_Start_Time, " +
                      "b.Booking_Date, b.TotalCost, b.IsCancelled, b.Discount_Applied, " +
                      "p.First_Name, p.Last_Name, p.Email, p.Phone, " +
                      "se.Seat_Code, se.Row_Number, se.IsWheelchairAccessible, se.IsRestrictedView " +
                      "FROM Ticket t " +
                      "LEFT JOIN Shows s ON t.Show_ID = s.Show_ID " +
                      "LEFT JOIN Booking b ON t.Booking_ID = b.Booking_ID " +
                      "LEFT JOIN Patron p ON b.Patron_ID = p.Patron_ID " +
                      "LEFT JOIN Seat se ON t.Seat_ID = se.Seat_ID " +
                      "ORDER BY t.Ticket_ID DESC";
        
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                Map<String, Object> ticket = new HashMap<>();
                // Ticket details
                ticket.put("ticketId", resultSet.getInt("Ticket_ID"));
                ticket.put("price", resultSet.getDouble("Ticket_Price"));
                ticket.put("discountAmount", resultSet.getDouble("Discount_Amount"));
                ticket.put("isSold", resultSet.getBoolean("IsSold"));
                
                // Show details
                ticket.put("showId", resultSet.getInt("Show_ID"));
                ticket.put("showTitle", resultSet.getString("Show_Title"));
                ticket.put("showDate", resultSet.getString("Show_Date"));
                ticket.put("showTime", resultSet.getString("Show_Start_Time"));
                
                // Booking details
                ticket.put("bookingId", resultSet.getInt("Booking_ID"));
                ticket.put("bookingDate", resultSet.getString("Booking_Date"));
                ticket.put("totalCost", resultSet.getDouble("TotalCost"));
                ticket.put("isCancelled", resultSet.getBoolean("IsCancelled"));
                ticket.put("discountApplied", resultSet.getDouble("Discount_Applied"));
                
                // Patron details
                ticket.put("patronId", resultSet.getInt("Patron_ID"));
                ticket.put("firstName", resultSet.getString("First_Name"));
                ticket.put("lastName", resultSet.getString("Last_Name"));
                ticket.put("email", resultSet.getString("Email"));
                ticket.put("phone", resultSet.getString("Phone"));
                
                // Seat details
                ticket.put("seatId", resultSet.getInt("Seat_ID"));
                ticket.put("seatCode", resultSet.getString("Seat_Code"));
                ticket.put("rowNumber", resultSet.getString("Row_Number"));
                ticket.put("isWheelchairAccessible", resultSet.getBoolean("IsWheelchairAccessible"));
                ticket.put("isRestrictedView", resultSet.getBoolean("IsRestrictedView"));
                
                tickets.add(ticket);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tickets;
    }

    public static ResultSet getBoxOfficeReports() {
        String query = "SELECT s.Show_Title, COUNT(t.Ticket_ID) AS Tickets_Sold, " +
                "SUM(t.Ticket_Price) AS Total_Revenue " +
                "FROM Shows s " +
                "LEFT JOIN Ticket t ON s.Show_ID = t.Show_ID " +
                "WHERE t.IsSold = 1 " +
                "GROUP BY s.Show_Title " +
                "ORDER BY Total_Revenue DESC";

        try {
            Connection con = getConnection();
            PreparedStatement stmt = con.prepareStatement(query);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getAllBoxOfficeReports() {
        String query = "SELECT * FROM BoxOfficeReport";
        try {
            Connection con = getConnection();
            PreparedStatement stmt = con.prepareStatement(query);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }





    /**
     * Searches for tickets based on various criteria
     * @param showId The ID of the show to search for (null for any show)
     * @param startDate The start date to search for (null for any date)
     * @param endDate The end date to search for (null for any date)
     * @param ticketId The ID of the ticket to search for (null for any ticket)
     * @param patronName The name of the patron to search for (null for any patron)
     * @return List of matching tickets
     */
    public static List<Map<String, Object>> searchTickets(Integer showId, String startDate, String endDate, 
                                                        Integer ticketId, String patronName) {
        List<Map<String, Object>> tickets = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT t.*, s.Show_Title, s.Show_Date, s.Show_Start_Time, " +
            "b.Booking_Date, b.TotalCost, b.IsCancelled, b.Discount_Applied, " +
            "p.First_Name, p.Last_Name, p.Email, p.Phone, " +
            "se.Seat_Code, se.Row_Number, se.IsWheelchairAccessible, se.IsRestrictedView " +
            "FROM Ticket t " +
            "LEFT JOIN Shows s ON t.Show_ID = s.Show_ID " +
            "LEFT JOIN Booking b ON t.Booking_ID = b.Booking_ID " +
            "LEFT JOIN Patron p ON b.Patron_ID = p.Patron_ID " +
            "LEFT JOIN Seat se ON t.Seat_ID = se.Seat_ID " +
            "WHERE 1=1"
        );
        
        List<Object> parameters = new ArrayList<>();
        
        if (showId != null) {
            queryBuilder.append(" AND t.Show_ID = ?");
            parameters.add(showId);
        }
        
        if (startDate != null) {
            queryBuilder.append(" AND DATE(s.Show_Date) >= ?");
            parameters.add(startDate);
        }
        
        if (endDate != null) {
            queryBuilder.append(" AND DATE(s.Show_Date) <= ?");
            parameters.add(endDate);
        }
        
        if (ticketId != null) {
            queryBuilder.append(" AND t.Ticket_ID = ?");
            parameters.add(ticketId);
        }
        
        if (patronName != null && !patronName.isEmpty()) {
            queryBuilder.append(" AND (p.First_Name LIKE ? OR p.Last_Name LIKE ?)");
            parameters.add("%" + patronName + "%");
            parameters.add("%" + patronName + "%");
        }
        
        queryBuilder.append(" ORDER BY t.Ticket_ID DESC");
        
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
            
            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Map<String, Object> ticket = new HashMap<>();
                // Ticket details
                ticket.put("ticketId", resultSet.getInt("Ticket_ID"));
                ticket.put("price", resultSet.getDouble("Ticket_Price"));
                ticket.put("discountAmount", resultSet.getDouble("Discount_Amount"));
                ticket.put("isSold", resultSet.getBoolean("IsSold"));
                
                // Show details
                ticket.put("showId", resultSet.getInt("Show_ID"));
                ticket.put("showTitle", resultSet.getString("Show_Title"));
                ticket.put("showDate", resultSet.getString("Show_Date"));
                ticket.put("showTime", resultSet.getString("Show_Start_Time"));
                
                // Booking details
                ticket.put("bookingId", resultSet.getInt("Booking_ID"));
                ticket.put("bookingDate", resultSet.getString("Booking_Date"));
                ticket.put("totalCost", resultSet.getDouble("TotalCost"));
                ticket.put("isCancelled", resultSet.getBoolean("IsCancelled"));
                ticket.put("discountApplied", resultSet.getDouble("Discount_Applied"));
                
                // Patron details
                ticket.put("patronId", resultSet.getInt("Patron_ID"));
                ticket.put("firstName", resultSet.getString("First_Name"));
                ticket.put("lastName", resultSet.getString("Last_Name"));
                ticket.put("email", resultSet.getString("Email"));
                ticket.put("phone", resultSet.getString("Phone"));
                
                // Seat details
                ticket.put("seatId", resultSet.getInt("Seat_ID"));
                ticket.put("seatCode", resultSet.getString("Seat_Code"));
                ticket.put("rowNumber", resultSet.getString("Row_Number"));
                ticket.put("isWheelchairAccessible", resultSet.getBoolean("IsWheelchairAccessible"));
                ticket.put("isRestrictedView", resultSet.getBoolean("IsRestrictedView"));
                
                tickets.add(ticket);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tickets;
    }

    /**
     * Updates a ticket's information
     * @param ticketId The ID of the ticket to update
     * @param price The new ticket price
     * @param discountAmount The new discount amount
     * @param isWheelchairAccessible Whether the seat is wheelchair accessible
     * @param isRestrictedView Whether the seat has a restricted view
     * @param isSold Whether the ticket is sold
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTicket(int ticketId, double price, double discountAmount, 
                                     boolean isWheelchairAccessible, boolean isRestrictedView, 
                                     boolean isSold) {
        String query = "UPDATE Ticket SET " +
                      "Ticket_Price = ?, " +
                      "Discount_Amount = ?, " +
                      "IsWheelchairAccessible = ?, " +
                      "IsRestrictedView = ?, " +
                      "IsSold = ? " +
                      "WHERE Ticket_ID = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setDouble(1, price);
            preparedStatement.setDouble(2, discountAmount);
            preparedStatement.setBoolean(3, isWheelchairAccessible);
            preparedStatement.setBoolean(4, isRestrictedView);
            preparedStatement.setBoolean(5, isSold);
            preparedStatement.setInt(6, ticketId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}





