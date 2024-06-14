import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
            stage.getIcons().add(new Image(App.class.getResourceAsStream("Resources/Pictures/Calculator/ProfileIcon2.png")));
            stage.setScene(scene);
            stage.setFullScreen(false);
            stage.setWidth(1920);
            stage.setHeight(1080); 
            stage.initStyle(StageStyle.UNDECORATED);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
