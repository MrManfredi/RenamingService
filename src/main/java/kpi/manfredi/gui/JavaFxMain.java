package kpi.manfredi.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFxMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Interface.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setMinHeight(640);
        primaryStage.setMinWidth(800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Image Handler");
        primaryStage.show();
    }
}
