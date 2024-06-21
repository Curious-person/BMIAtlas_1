package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    
    public static Connection DBConnect() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish the database connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/bmiatlas", "root", "");
            System.out.println("Database Connected");
            return con;
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load MySQL JDBC driver: " + e.getMessage());
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
            throw e;
        }
    }
}
