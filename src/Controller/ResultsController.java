package Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Database;
import models.LoginManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ResultsController implements Initializable {
    @FXML
    private TextField heighttext, weighttext;

    @FXML
    private Label calculation, calculation1, results, results2, userdisplay, info;

    @FXML
    private Button addtohistory, retrybtn;

    @FXML
    private AnchorPane anchorRoot;

    @FXML
    private StackPane parentContainer;

    @FXML
    private ImageView history, learnmore;

    @FXML
    private double height, weight;

    // EXIT & MIN BUTTONS
    public void closeWindow(ActionEvent event) {
        Platform.exit();
    }

    public void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDataFromDatabase();
        displayLoggedInUser();

        learnmore.setOnMouseClicked(e -> {
            BMICategory bmiCategory = calculateCategory(Double.parseDouble(calculation.getText()));
            openWebsite(bmiCategory.getUrl());
        });
    }

    @FXML
    private void addtohistory() {
        try (Connection connection = Database.DBConnect()) {
            if (connection != null) {
                connection.setAutoCommit(false);

                String selectLatestQuery = "SELECT * FROM bmi_calculation ORDER BY CalculationID DESC LIMIT 1";
                try (PreparedStatement selectStatement = connection.prepareStatement(selectLatestQuery);
                    ResultSet resultSet = selectStatement.executeQuery()) {

                    if (resultSet.next()) {
                        double height = resultSet.getDouble("height");
                        double weight = resultSet.getDouble("weight");
                        double bmi = resultSet.getDouble("bmi");
                        int categoryID = resultSet.getInt("CategoryID");
                        int CalculationID = resultSet.getInt("CalculationID");

                        // Fetch CategoryName and UserID
                        String categoryName = getCategoryName(categoryID);
                        int userID = getLoggedInUserID();

                        // Insert into bmi_history
                        String insertQuery = "INSERT INTO bmi_history (height, weight, BMI, CategoryID, CalculationID, CategoryName, UserID, Date) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setDouble(1, height);
                            insertStatement.setDouble(2, weight);
                            insertStatement.setDouble(3, bmi);
                            insertStatement.setInt(4, categoryID);
                            insertStatement.setInt(5, CalculationID);
                            insertStatement.setString(6, categoryName);
                            insertStatement.setInt(7, userID);
                            insertStatement.executeUpdate();
                        }

                        connection.commit();
                        showAlert2("Successful", "Added to History");
                    } else {
                        System.out.println("No data found in bmi_calculation table.");
                    }
                }
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getLoggedInUserID() {
        return LoginManager.getUserID();
    }

    @FXML
    private void history(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/History.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load History.fxml");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Resources/Style.css").toExternalForm());
        alert.showAndWait();
        alert.showAndWait();
    }

    private void showAlert2(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Resources/Style.css").toExternalForm());
        alert.showAndWait();
        alert.showAndWait();
    }

    @FXML
    private void btncalculate1(ActionEvent event) {
        String heightText = heighttext.getText();
        String weightText = weighttext.getText();

        if (heightText.isEmpty()) {
            showAlert("Error", "Please input height.");
            return;
        }

        if (weightText.isEmpty()) {
            showAlert("Error", "Please input weight.");
            return;
        }

        try {
            double height = Double.parseDouble(heightText);
            double weight = Double.parseDouble(weightText);

            if (height <= 0 || weight <= 0) {
                showAlert("Error", "Height and weight must be positive.");
                return;
            }

            double result = calculateResult(height, weight);
            BMICategory bmiCategory = calculateCategory(result);
            int categoryID = determineCategoryID(result);

            String category = bmiCategory.getCategory();
            String description = bmiCategory.getDescription();
            String url = bmiCategory.getUrl();


            int userID = LoginManager.getUserID();

            try (Connection connection = Database.DBConnect()) {
                if (connection != null) {
                    int latestBmiId = getLatestBmiId(connection);

                    if (latestBmiId > 0) {
                        // Update existing data
                        String updateQuery = "UPDATE bmi_calculation SET height = ?, weight = ?, bmi = ?, CategoryID = ?, UserID = ? WHERE CalculationID = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setDouble(1, height);
                            updateStatement.setDouble(2, weight);
                            updateStatement.setDouble(3, result);
                            updateStatement.setInt(4, categoryID);
                            updateStatement.setInt(5, userID);
                            updateStatement.setInt(6, latestBmiId);
                            updateStatement.executeUpdate();
                        }
                    } else {
                        // Insert new data
                        String insertQuery = "INSERT INTO bmi_calculation (height, weight, bmi, CategoryID, UserID) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setDouble(1, height);
                            insertStatement.setDouble(2, weight);
                            insertStatement.setDouble(3, result);
                            insertStatement.setInt(4, categoryID);
                            insertStatement.setInt(5, userID);
                            insertStatement.executeUpdate();
                        }
                    }

                    // Update UI
                    loadDataFromDatabase();

                    Platform.runLater(() -> {
                        calculation.setText(String.valueOf(result));
                        calculation1.setText(String.valueOf(result));
                        results.setText(category);
                        info.setText(description);
                        results2.setText(category);
                        learnmore.setOnMouseClicked(e -> openWebsite(url));
                        System.out.println("URL: " + url);
                    });
                } else {
                    showAlert("Error", "Failed to connect to the database.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please input valid numbers for height and weight.");
            e.printStackTrace();
        } catch (SQLException e) {
            showAlert("Error", "Database operation failed.");
            e.printStackTrace();
        }
    }

    private int getLatestBmiId(Connection connection) throws SQLException {
        int latestBmiId = 0;
        String query = "SELECT MAX(CalculationID) AS latest_id FROM bmi_calculation";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                latestBmiId = resultSet.getInt("latest_id");
            }
        }
        return latestBmiId;
    }

    private double calculateResult(double height, double weight) {
        double heightInMeters = height / 100; // Convert cm to meters
        return Math.round((weight) / (heightInMeters * heightInMeters) * 10.0) / 10.0;
    }

    //BMI Category
    private BMICategory calculateCategory(double result) {
        String category;
        String description;
        String url;
    
        if (result < 18.5) {
            category = "Underweight";
            description = "Underweight individuals may negatively impact immunity, fertility, mental wellbeing, and other health aspects. To maintain health, it's crucial to consume energy-rich foods, including protein for muscle repair and vitamins and minerals for overall well-being.";
            url = "https://www.medicalnewstoday.com/articles/321612";
        } else if (result < 25.0) {
            category = "Normal weight";
            description = "This weight is considered as healthy and normal weight. By maintaining a healthy weight, they can lower their risk of developing serious health problems. Maintain your weight with adequate exercises and healthy foods. Keep up the good work!";
            url = "https://mana.md/what-is-a-healthy-weight/";
        } else if (result < 30.0) {
            category = "Overweight";
            description = "Overweight individuals have excess body fat, often a BMI over 25, which increases the risk of health issues like heart disease, diabetes, and high blood pressure. To improve health, a balanced diet, regular exercise, and consistent sleep are crucial.";
            url = "https://www.who.int/news-room/fact-sheets/detail/obesity-and-overweight";
        } else {
            category = "Obese";
            description = "Obesity, characterized by a BMI over 30, increases the risk of serious health issues like type 2 diabetes, heart disease, and certain cancers. To combat obesity, a healthy lifestyle involving regular physical activity, nutritious food, and stress management is crucial.";
            url = "https://www.who.int/health-topics/obesity#:~:text=Overweight%20and%20obesity%20are%20defined,and%20over%2030%20is%20obese.";
        }
    
        return new BMICategory(category, description, url);
    }

    private int determineCategoryID(double result) {
        if (result < 18.5) {
            return 1; // Underweight
        } else if (result < 25.0) {
            return 2; // Normal weight
        } else if (result < 30.0) {
            return 3; // Overweight
        } else {
            return 4; // Obesity
        }
    }

    private void loadDataFromDatabase() {
        int userID = LoginManager.getUserID();
    
        String query = "SELECT * FROM bmi_calculation WHERE UserID = ? ORDER BY CalculationID DESC LIMIT 1";
        try (Connection connection = Database.DBConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
    
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                double height = resultSet.getDouble("height");
                double weight = resultSet.getDouble("weight");
                double bmi = resultSet.getDouble("bmi");
                
                heighttext.setText(String.valueOf(height));
                weighttext.setText(String.valueOf(weight));
                calculation.setText(String.valueOf(bmi));
                calculation1.setText(String.valueOf(bmi));
    
                BMICategory bmiCategory = calculateCategory(bmi);
                String category = bmiCategory.getCategory();
                String description = bmiCategory.getDescription();
                String url = bmiCategory.getUrl();
    
                results.setText(category);
                results2.setText(category);
                info.setText(description);
    
                learnmore.setOnMouseClicked(e -> openWebsite(url));
            } else {
                System.out.println("No data found for the specified user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void displayLoggedInUser() {
        String loggedInUser = LoginManager.getUsername();
        if (userdisplay != null) {
            userdisplay.setText("" + loggedInUser);
        }
        // int userID = LoginManager.getUserID();
        // try (Connection connection = Database.DBConnect();
        //      PreparedStatement preparedStatement = connection.prepareStatement("SELECT Username FROM users WHERE UserID = ?")) {
        //     preparedStatement.setInt(1, userID);
        //     ResultSet resultSet = preparedStatement.executeQuery();
        //     if (resultSet.next()) {
        //         String username = resultSet.getString("Username");
        //         userdisplay.setText(username);
        //     }
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }
    }


    private String getCategoryName(int categoryID) {
        String categoryName = "Unknown";
        String query = "SELECT CategoryName FROM bmi_category WHERE CategoryID = ?";

        try (Connection connection = Database.DBConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, categoryID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    categoryName = resultSet.getString("CategoryName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categoryName;
    }



    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            LoginManager.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();

            Scene scene = addtohistory.getScene();
            Stage currentStage = (Stage) scene.getWindow();

            scene.setRoot(root);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class BMICategory {
        private String category;
        private String description;
        private String url;
    
        public BMICategory(String category, String description, String url) {
            this.category = category;
            this.description = description;
            this.url = url;
        }
    
        public String getCategory() {
            return category;
        }
    
        public String getDescription() {
            return description;
        }

        public String getUrl() {
            return url;
        }
    }

    @FXML
    private void openWebsite(String url) {
    try {
        System.out.println("Attempting to open url: " + url);
        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
    } catch (Exception e) {
        showAlert("Error", "Failed to open the website.");
        e.printStackTrace();
    }
}

    
}
