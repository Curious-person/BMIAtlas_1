package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Database;

public class ResultsController implements Initializable {
    @FXML
    private TextField heighttext, weighttext;

    @FXML
    Stage stage;

    @FXML
    private Label calculation, calculation1, results;
    
    @FXML
    private Button addtohistory; 

    @FXML
    private AnchorPane anchorRoot;

    @FXML
    private StackPane parentContainer;

    @FXML
    private ImageView history;

    double height, weight;
    

    // EXIT & MIN BUTTONS-------------------------------------------------------------------
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
    }


    @FXML
    private void addtohistory(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/History.fxml"));
            Parent root = loader.load();
            
            Scene scene = addtohistory.getScene();
            Stage currentStage = (Stage) scene.getWindow();
    
            scene.setRoot(root);
            
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void history(MouseEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/History.fxml"));
            Parent root = loader.load();
            
            Scene scene = history.getScene();
            Stage currentStage = (Stage) scene.getWindow();
    
            scene.setRoot(root);
            
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
            height = Double.parseDouble(heightText);
            weight = Double.parseDouble(weightText);

            // Check if height and weight are positive numbers
            if (height <= 0 || weight <= 0) {
                showAlert("Error", "Height and weight must be positive.");
                return;
            }

            // Calculate BMI
            double result = calculateResult(height, weight);
            String category = calculateCategory(result);

            Connection connection = Database.DBConnect();
            if (connection != null) {
                int latestBmiId = getLatestBmiId(connection);

                if (latestBmiId > 0) {
                    // Update existing data
                    String updateQuery = "UPDATE bmi_calculation SET height=?, weight=?, bmi=?, category=? WHERE calculationID=?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setDouble(1, height);
                    updateStatement.setDouble(2, weight);
                    updateStatement.setDouble(3, result);
                    updateStatement.setString(4, category);
                    updateStatement.setInt(5, latestBmiId);
                    updateStatement.executeUpdate();

                    updateStatement.close();
                } else {
                    // Insert new data
                    String insertQuery = "INSERT INTO bmi_calculation (height, weight, bmi, category) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setDouble(1, height);
                    insertStatement.setDouble(2, weight);
                    insertStatement.setDouble(3, result);
                    insertStatement.setString(4, category);
                    insertStatement.executeUpdate();

                    insertStatement.close();
                }

                connection.close();

                // Update UI to reflect changes
                loadDataFromDatabase();
            }
        } catch (NumberFormatException e) {
            results.setVisible(true);
            results.setText("Please input a number");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getLatestBmiId(Connection connection) throws SQLException {
        int latestBmiId = 0;
        String query = "SELECT MAX(calculationID) AS latest_id FROM bmi_calculation";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            latestBmiId = resultSet.getInt("latest_id");
        }
        resultSet.close();
        preparedStatement.close();
        return latestBmiId;
    }

    private String calculateCategory(double result) {
        if (result < 18.5) {
            return "Underweight";
        } else if (result < 25.0) {
            return "Normal weight";
        } else if (result < 30.0) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    private double calculateResult(double height, double weight) {
        double heightconvert = height / 100; 
        double r = (weight) / (heightconvert * heightconvert);
        r = Math.round(r * 10.0) / 10.0;
        return r;
    }


        private void loadDataFromDatabase() {
        try {
            Connection connection = Database.DBConnect();
            if (connection != null) {
                String query = "SELECT * FROM bmi_calculation ORDER BY calculationID DESC LIMIT 1";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    double bmi = resultSet.getDouble("bmi");
                    double height = resultSet.getDouble("height");
                    double weight = resultSet.getDouble("weight");
                    String category = resultSet.getString("category");

                    heighttext.setText(String.valueOf(height));
                    weighttext.setText(String.valueOf(weight));
                    calculation.setText(String.valueOf(bmi));
                    calculation1.setText(String.valueOf(bmi));
                    results.setText(category);
                } else {
                    System.out.println("No data found in bmi_calculation table.");
                }
                resultSet.close();
                preparedStatement.close();
                connection.close();
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
