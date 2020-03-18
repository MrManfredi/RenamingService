package kpi.manfredi.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static kpi.manfredi.utils.Dialogs.showRenameAllSelectedConfirmationDialog;
import static kpi.manfredi.utils.FileManipulation.renameFilesByTemplate;

public class IterativeRenaming implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(IterativeRenaming.class);

    //
    // Renaming
    //
    @FXML
    private CheckBox prefixCheckBox;

    @FXML
    private TextField prefixTf;

    @FXML
    private CheckBox zeroPadCheckBox;

    @FXML
    private Spinner<Integer> zeroPad;

    @FXML
    private TextField postfixTf;

    @FXML
    private CheckBox postfixCheckBox;

    @FXML
    private Button renameButton;

    private static IterativeRenaming instance;
    private Stage thisStage;
    private List<File> files;

    public static IterativeRenaming getInstance() {
        if (instance == null) instance = new IterativeRenaming();
        return instance;
    }

    private IterativeRenaming() {
        // Create the new stage
        thisStage = new Stage();

        // Load the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/IterativeRenamingWindow.fxml"));

            // Set this class as the controller
            loader.setController(this);

            // Load the scene
            thisStage.setScene(new Scene(loader.load()));

            thisStage.setTitle("Iterative Renaming");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initCheckBoxListeners();
        initZeroPad();
        initRenameButtonListener();
    }

    private void initCheckBoxListeners() {
        prefixCheckBox.setOnAction(event -> prefixTf.setDisable(!prefixCheckBox.isSelected()));
        zeroPadCheckBox.setOnAction(event -> zeroPad.setDisable(!zeroPadCheckBox.isSelected()));
        postfixCheckBox.setOnAction(event -> postfixTf.setDisable(!postfixCheckBox.isSelected()));
    }

    private void initZeroPad() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 6, 3);
        zeroPad.setValueFactory(valueFactory);
    }

    private void initRenameButtonListener() {
        renameButton.setOnAction(event -> {

            String prefix = prefixCheckBox.isSelected() ? prefixTf.getText() : "";
            String postfix = postfixCheckBox.isSelected() ? postfixTf.getText() : "";

            Optional<ButtonType> result =
                    showRenameAllSelectedConfirmationDialog(files.size(), prefix + "*" + postfix);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info("Pressed on rename button. Result is OK! {} files to be rename. ", files.size());
                files = renameFilesByTemplate(files, prefix, zeroPad.getValue(), postfix);
            } else {
                logger.info("Pressed on rename button. Result is CANCEL!");
            }
            thisStage.close();
        });
    }

    public List<File> startRenamingProcedure(List<File> filesToRename) {
        files = filesToRename;
        thisStage.showAndWait();
        return files;
    }
}
