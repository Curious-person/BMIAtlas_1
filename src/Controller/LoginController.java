package Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Database;
import models.LoginManager;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorMessageLabel, signup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorMessageLabel.setVisible(false);
    }

    // EXIT & MIN BUTTONS-------------------------------------------------------------------
    public void closeWindow(ActionEvent event) {
        Platform.exit();
    }

    public void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    //Sign Up Button
    @FXML
    private void signup(MouseEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Signup.fxml"));
            Parent root = loader.load();

            Scene scene = signup.getScene();
            Stage currentStage = (Stage) scene.getWindow();

            scene.setRoot(root);

            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Login Button
    @FXML
    private void login(ActionEvent event) throws IOException {
        String identifier = usernameTextField.getText();
        String password = passwordTextField.getText();

        try (Connection connection = Database.DBConnect()) {
            String loginSql = "SELECT UserID, Username FROM user_login WHERE (Username = ? OR Email = ?) AND Password = ?";
            try (PreparedStatement loginStatement = connection.prepareStatement(loginSql)) {
                loginStatement.setString(1, identifier);
                loginStatement.setString(2, identifier);
                loginStatement.setString(3, password);
                ResultSet resultSet = loginStatement.executeQuery();

                if (resultSet.next()) {
                    LoginManager.setLoggedIn(true);
                    LoginManager.setUsername(resultSet.getString("Username"));
                    LoginManager.setUserID(resultSet.getInt("UserID"));
                    errorMessageLabel.setVisible(false);
                    loadCalculatorView();
                } else {
                    errorMessageLabel.setVisible(true);
                    errorMessageLabel.setText("Invalid username/email or password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method for transitioning to Calculator.fxml
    private void loadCalculatorView() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/View/Calculator.fxml"));
        Scene scene = loginButton.getScene();
        root.translateXProperty().set(scene.getHeight());

        StackPane parentContainer = (StackPane) scene.getRoot();
        parentContainer.getChildren().add(root);

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(root.translateXProperty(), 0, Interpolator.EASE_IN);
        KeyFrame kf = new KeyFrame(Duration.seconds(0.5), kv);

        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(t -> parentContainer.getChildren().remove(scene.getRoot()));

        timeline.play();
    }
}
