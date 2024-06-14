package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Database;

public class CalculatorController implements Initializable{
    @FXML
    private Button calculatebtn; 
    
    @FXML
    private AnchorPane anchorRoot;

    @FXML
    private StackPane parentContainer;

    @FXML
    private ImageView history;

    @FXML
    private TextField heighttext, weighttext;

    double height, weight;


     public void closeWindow(ActionEvent event) {
        Platform.exit();
    }
    
    public void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void loadSecond(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/History.fxml"));
            Parent root = loader.load();
            
            Scene scene = calculatebtn.getScene();
            Stage currentStage = (Stage) scene.getWindow();
    
            scene.setRoot(root);
            
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void btncalculate(ActionEvent event) throws IOException {
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

            // Check if height and weight are integers
            int heightInt = (int) height;
            int weightInt = (int) weight;
            if (height != heightInt || weight != weightInt) {
                showAlert("Error", "Please input integers only.");
                return;
            }

            // Calculate bmi
            double result = calculateResult(height, weight);
            String category = calculateCategory(result);

            // Insert data into the database
            insertDataIntoDatabase(height, weight, result, category);

            // Load the RESULTS.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Results.fxml"));
            Parent root = loader.load();

        Scene scene = calculatebtn.getScene();
        root.translateXProperty().set(scene.getHeight());

        parentContainer.getChildren().add(root);

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(root.translateXProperty(), 0, Interpolator.EASE_IN);
        KeyFrame kf = new KeyFrame(Duration.seconds(.5), kv);

        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(t -> {
            parentContainer.getChildren().remove(anchorRoot);
        });

        timeline.play();

    } catch (NumberFormatException e) {
        showAlert("Error", "Please input integers only.");
    } catch (Exception e) {
        System.out.println(e);
    }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void insertDataIntoDatabase(double height, double weight, double bmi, String category) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = Database.DBConnect();
            if (connection != null) {
                String query = "INSERT INTO bmi_calculation (height, weight, bmi, category) VALUES (?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setDouble(1, height);
                preparedStatement.setDouble(2, weight);
                preparedStatement.setDouble(3, bmi);
                preparedStatement.setString(4, category);
                preparedStatement.executeUpdate();
                System.out.println("Data inserted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
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
}
