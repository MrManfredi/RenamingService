package kpi.manfredi.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kpi.manfredi.gui.Context;
import kpi.manfredi.gui.Screen;
import kpi.manfredi.gui.ScreenSwitcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static kpi.manfredi.utils.DialogsUtil.showConfirmationDialog;
import static kpi.manfredi.utils.FileManipulation.renameFilesByTemplate;
import static kpi.manfredi.utils.MessageUtil.formatMessage;
import static kpi.manfredi.utils.MessageUtil.getMessage;

public class IterativeRenamingController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(IterativeRenamingController.class);

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

    private static IterativeRenamingController instance;
    private final Stage thisStage;
    private List<File> files;

    public static IterativeRenamingController getInstance() {
        if (instance == null) instance = new IterativeRenamingController();
        return instance;
    }

    private IterativeRenamingController() {
        thisStage = ScreenSwitcher.showModalScreen(
                Screen.ITERATIVE_RENAMING,
                Context.getInstance().getPrimaryStage(),
                this);
        thisStage.setTitle("Iterative Renaming");
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

            Optional<ButtonType> result = showConfirmationDialog(
                    getMessage("renaming.iterative.title"),
                    formatMessage("renaming.iterative.header", prefix, postfix),
                    formatMessage("renaming.iterative.content", files.size())
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info(formatMessage("log.dialog.confirm.ok", "rename", files.size()));
                files = renameFilesByTemplate(files, prefix, zeroPad.getValue(), postfix);
            } else {
                logger.info(formatMessage("log.dialog.confirm.cancel", "rename"));
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
