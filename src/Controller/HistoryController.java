package Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import models.Database;
import models.LoginManager;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML
    private TableView<ObservableList<String>> tableView;

    @FXML
    private TableColumn<ObservableList<String>, String> heightCol;

    @FXML
    private TableColumn<ObservableList<String>, String> weightCol;

    @FXML
    private TableColumn<ObservableList<String>, String> bmiResultCol;

    @FXML
    private TableColumn<ObservableList<String>, String> categoryCol;

    @FXML
    private TableColumn<ObservableList<String>, String> dateAddedCol;

    @FXML
    private LineChart<String, Number> bmiChart;

    private XYChart.Series<String, Number> bmiSeries;

    @FXML
    private Stage stage;

    @FXML
    private Button deleteButton, handleLogout, refreshButton;

    @FXML
    private ImageView back;

    @FXML
    private Label userdisplay;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeLineChart();
        loadDataFromDatabase();
        displayLoggedInUser();
    }

    // EXIT & MIN BUTTONS-------------------------------------------------------------------
    @FXML
    public void closeWindow(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void minimizeWindow(ActionEvent event) {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.setIconified(true);
    }

     // BACK BUTTON-------------------------------------------------------------------
    @FXML
    private void back(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/Calculator.fxml"));
            Scene scene = new Scene(root);

            Stage currentStage = (Stage) tableView.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        ObservableList<String> selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            tableView.getItems().remove(selectedItem);
            deleteFromDatabase(selectedItem);
            showAlert2("Success", "Data deleted from history");
        } else {
            showAlert("Error", "Please select a row.");
        }
    }

    public void deleteFromDatabase(ObservableList<String> deletedData) {
        try {
            Connection connection = Database.DBConnect();
            if (connection != null) {
                String deleteQuery = "DELETE FROM bmi_history WHERE Date=?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, deletedData.get(4));
                preparedStatement.executeUpdate();

                preparedStatement.close();
                connection.close();
            } else {
                showAlert("Error", "Failed to connect to the database.");
            }
        } catch (SQLException e) {
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
    private void handleRefreshButton(ActionEvent event) {
        refreshData();
        System.out.println("Data refreshed");
    }

    public void loadDataFromDatabase() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int currentUserID = LoginManager.getUserID();
        try {
            Connection connection = Database.DBConnect();
            if (connection != null) {
                String query = "SELECT h.height, h.weight, h.bmi, c.CategoryName, h.Date " +
                        "FROM bmi_history h " +
                        "JOIN bmi_category c ON h.CategoryID = c.CategoryID " +
                        "WHERE h.UserID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, currentUserID);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    row.add(resultSet.getString("height"));
                    row.add(resultSet.getString("weight"));
                    row.add(resultSet.getString("bmi"));
                    row.add(resultSet.getString("CategoryName"));
                    row.add(resultSet.getString("Date"));
                    data.add(row);

                    // Add data to the line chart series
                    addDataToLineChart(resultSet.getString("Date"), Double.parseDouble(resultSet.getString("bmi")));
                }

                resultSet.close();
                preparedStatement.close();
                connection.close();
            } else {
                showAlert("Error", "Failed to connect to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView.setItems(data);

        heightCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(0)));
        weightCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(1)));
        bmiResultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(2)));
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(3)));
        dateAddedCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(4)));
    }

    //clear existing data in the table view and line chart
    private void refreshData() {
        
        tableView.getItems().clear();
        bmiSeries.getData().clear();
        loadDataFromDatabase();
    }

    private void displayLoggedInUser() {
        String loggedInUser = LoginManager.getUsername();
        if (userdisplay != null) {
            userdisplay.setText("" + loggedInUser);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            LoginManager.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Login.fxml"));
            Parent root = loader.load();

            Scene scene = deleteButton.getScene();
            Stage currentStage = (Stage) scene.getWindow();

            scene.setRoot(root);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeLineChart() {
        bmiSeries = new XYChart.Series<>();
        bmiSeries.setName("BMI Over Time");
        bmiChart.getData().add(bmiSeries);
    }

    private void addDataToLineChart(String date, Double bmiValue) {
        bmiSeries.getData().add(new XYChart.Data<>(date, bmiValue));
    }

}
