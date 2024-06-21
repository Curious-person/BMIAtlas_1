package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginManager {
    private static boolean loggedIn = false;
    private static String username;
    private static int userID;

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean loggedIn) {
        LoginManager.loggedIn = loggedIn;
    }

    public static void logout() {
        loggedIn = false;
        username = null;
        userID = 0; // Reset userID on logout
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        LoginManager.username = username;
    }

    public static int getUserID() {
        return userID;
    }

    public static void setUserID(int userID) {
        LoginManager.userID = userID;
    }

    // Method to validate login credentials and retrieve username and userID from database
    public static boolean validateLogin(String identifier, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = Database.DBConnect();
            if (connection != null) {
                String query = "SELECT UserID, Username FROM user_login WHERE (Username = ? OR Email = ?) AND Password = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, identifier);
                preparedStatement.setString(2, identifier);
                preparedStatement.setString(3, password);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    username = resultSet.getString("Username");
                    userID = resultSet.getInt("UserID");
                    loggedIn = true;
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
