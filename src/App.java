import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class App extends Application {
    

    public static void main(String[] args) {

        launch(args);
    }

    public void start(Stage stage) throws Exception {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/Login.fxml"));
            stage.setTitle("BMI Atlas");
            stage.setResizable(false);
            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(App.class.getResourceAsStream("Resources/Pictures/Login/Logo.png")));
            stage.setScene(scene);
            stage.setFullScreen(false);
            stage.setWidth(1920);
            stage.setHeight(1080); 
            stage.initStyle(StageStyle.UNDECORATED);

            stage.show();

            showDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Medical Disclaimer");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);


        
        dialog.setContentText("BMI Calculators are not a substitute for professional medical advice, diagnosis, or treatment. If you have any concerns or questions about your health, you should always consult with a physician or other healthcare professional. Do not disregard, avoid or delay obtaining medical or health related advice from your health-care professional because of something you may have read on this site. The use of any information provided on this site is solely at your own risk.");
        ImageView icon = new ImageView(new Image(App.class.getResourceAsStream("Resources/Pictures/Login/Logo.png")));
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        dialog.setGraphic(icon);


        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Resources/Style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-content"); 

        dialog.showAndWait();
    }
}
    

