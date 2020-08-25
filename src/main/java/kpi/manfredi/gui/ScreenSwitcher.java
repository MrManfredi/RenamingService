package kpi.manfredi.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class is used to switching between scenes
 */
public abstract class ScreenSwitcher {

    private final static Logger logger = LoggerFactory.getLogger(ScreenSwitcher.class);

    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 200;

    /**
     * This method is used to activate certain screen.
     *
     * @param screen screen that will be activated
     * @param stage  stage in which screen will be shown
     */
    public static void activateScreen(Screen screen, Stage stage) {

        Scene scene;
        try {
            scene = new Scene(FXMLLoader.load(ScreenSwitcher.class.getResource(screen.getPath())));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return;
        }

        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
    }

    /**
     * This method is used to create a modal screen.
     *
     * @param screen           screen
     * @param parent           parent stage
     * @param loaderController loader controller
     * @return modal window stage
     */
    public static Stage showModalScreen(Screen screen, Stage parent, Object loaderController) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);

        // Load the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(ScreenSwitcher.class.getResource(screen.getPath()));

            // Set this class as the controller
            loader.setController(loaderController);

            // Load the scene
            stage.setScene(new Scene(loader.load()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stage;
    }
}
