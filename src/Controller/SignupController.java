package Controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.sql.Date;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Database;

public class SignupController implements Initializable {

    @FXML
    private Button signup; 

    @FXML
    private AnchorPane anchorRoot;

    @FXML
    private StackPane parentContainer;

    @FXML
    private ImageView defaultMaleImage;

    @FXML
    private ImageView highlightedMaleImage;

    @FXML
    private ImageView defaultFemaleImage;

    @FXML
    private ImageView highlightedFemaleImage;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private DatePicker birthdateDatePicker;

    @FXML
    private Label errorMessageLabel;


    // EXIT & MIN BUTTONS
    public void closeWindow(ActionEvent event) {
        Platform.exit();
    }

    public void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    //Sign Up Button
    @FXML
    private void signup(ActionEvent event) throws IOException {
        String username = usernameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        LocalDate birthdate = birthdateDatePicker.getValue();
        String sex;

        if (highlightedMaleImage.isVisible()) {
            sex = "Male";
        } else if (highlightedFemaleImage.isVisible()) {
            sex = "Female";
        } else {
            sex = "Unknown";
        }

        // Checks if any field is empty
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || birthdate == null || sex.equals("Unknown")) {
            errorMessageLabel.setText("All fields are required");
            errorMessageLabel.setVisible(true);
            return;
        }

        if (!email.contains("@") || (!email.endsWith(".com"))) {
        showAlert1("Invalid email format. Please enter a valid email address.", AlertType.ERROR);
        return;
        }

        try (Connection connection = Database.DBConnect()) {
            if (doesUserExists(connection, username, email)) {
                errorMessageLabel.setText("Username or email is already used");
                errorMessageLabel.setVisible(true);
                return;
            }
        
            String loginSql = "INSERT INTO user_login (Username, Email, Password, Birthdate, Sex) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement loginStatement = connection.prepareStatement(loginSql)) {
                loginStatement.setString(1, username);
                loginStatement.setString(2, email);
                loginStatement.setString(3, password);
                loginStatement.setDate(4, Date.valueOf(birthdate));
                loginStatement.setString(5, sex);
                
                // Execute the statement
                loginStatement.executeUpdate();
            }

            login2(event);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Checks database if taken na si username or email
    private boolean doesUserExists(Connection connection, String username, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user_login WHERE Username = ? OR Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    //Login Button
    @FXML
    private void login1(MouseEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();

            Scene scene = signup.getScene();
            Stage currentStage = (Stage) scene.getWindow();

            scene.setRoot(root);

            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Redirect to Login.fxml after Sign Up
    @FXML
    private void login2(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();

            Scene scene = signup.getScene();
            Stage currentStage = (Stage) scene.getWindow();

            scene.setRoot(root);

            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method para sa pag select ng Sex
    @FXML
    private void toggleMale() {
        boolean isDefaultVisible = defaultMaleImage.isVisible();
        defaultMaleImage.setVisible(!isDefaultVisible);
        highlightedMaleImage.setVisible(isDefaultVisible);

        defaultFemaleImage.setVisible(true);
        highlightedFemaleImage.setVisible(false);
    }

    @FXML
    private void toggleFemale() {
        boolean isDefaultVisible = defaultFemaleImage.isVisible();
        defaultFemaleImage.setVisible(!isDefaultVisible);
        highlightedFemaleImage.setVisible(isDefaultVisible);

        defaultMaleImage.setVisible(true);
        highlightedMaleImage.setVisible(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    private void showAlert1(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Registration Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
