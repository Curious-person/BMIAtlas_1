package Controller;

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
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Database;
import models.LoginManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CalculatorController implements Initializable {
    @FXML
    private Button calculatebtn;

    @FXML
    private AnchorPane anchorRoot;

    @FXML
    private StackPane parentContainer;

    @FXML
    private ImageView history;

    @FXML
    private Label userdisplay;

    @FXML
    private TextField heighttext, weighttext;

    private double height, weight;

    public void closeWindow(ActionEvent event) {
        Platform.exit();
    }

    public void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        displayUsername();
    }

    private void displayUsername() {
        String username = LoginManager.getUsername();
        if (username != null) {
            userdisplay.setText(username);
        }
    }

    @FXML
    private void btncalculate(ActionEvent event) {
        String heightText = heighttext.getText();
        String weightText = weighttext.getText();

        if (heightText.isEmpty() || weightText.isEmpty()) {
            showAlert("Error", "Please input both height and weight.");
            return;
        }

        try {
            height = Double.parseDouble(heightText);
            weight = Double.parseDouble(weightText);

            if (height <= 0 || weight <= 0) {
                showAlert("Error", "Height and weight must be positive.");
                return;
            }

            double result = calculateBMI(height, weight);

            int categoryID = determineCategoryID(result);

            int userID = LoginManager.getUserID();
            if (userID == 0) {
                showAlert("Error", "User information not available. Please log in.");
                return;
            }

            insertDataIntoDatabase(height, weight, result, categoryID, userID);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Results.fxml"));
            Parent root = loader.load();

            Scene scene = calculatebtn.getScene();
            root.translateXProperty().set(scene.getWidth());

            parentContainer.getChildren().add(root);

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(root.translateXProperty(), 0, Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(0.5), kv);

            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(t -> parentContainer.getChildren().remove(anchorRoot));

            timeline.play();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please input valid numbers for height and weight.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Results screen.");
        }
    }

    private void insertDataIntoDatabase(double height, double weight, double bmi, int categoryID, int userID) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = Database.DBConnect();
            if (connection != null) {
                String query = "INSERT INTO bmi_calculation (BMI, Height, Weight, UserID, CategoryID) VALUES (?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setDouble(1, bmi);
                preparedStatement.setDouble(2, height);
                preparedStatement.setDouble(3, weight);
                preparedStatement.setInt(4, userID);
                preparedStatement.setInt(5, categoryID);
                preparedStatement.executeUpdate();

                System.out.println("Data inserted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to insert data into database.");
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private double calculateBMI(double height, double weight) {
        double heightInMeters = height / 100.0;
        return Math.round((weight / (heightInMeters * heightInMeters)) * 10.0) / 10.0;
    }

    private int determineCategoryID(double bmi) {
        if (bmi < 18.5) {
            return 1;
        } else if (bmi < 25.0) {
            return 2;
        } else if (bmi < 30.0) {
            return 3;
        } else {
            return 4;
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
    }

    @FXML
    private void history(MouseEvent event) {
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

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            LoginManager.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();

            Scene scene = calculatebtn.getScene();
            Stage currentStage = (Stage) scene.getWindow();

            scene.setRoot(root);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
