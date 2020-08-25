package kpi.manfredi.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFxMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        Context.getInstance().setPrimaryStage(primaryStage);

        ScreenSwitcher.activateScreen(
                Screen.PROCESSING_ENVIRONMENT,
                primaryStage
        );

        primaryStage.setX(100);
        primaryStage.setY(50);
        primaryStage.setTitle("Image Handler");
        primaryStage.show();
    }
}
