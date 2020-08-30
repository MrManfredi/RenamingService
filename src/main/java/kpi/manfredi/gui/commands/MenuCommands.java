package kpi.manfredi.gui.commands;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kpi.manfredi.gui.controllers.IterativeRenamingController;
import kpi.manfredi.utils.DialogsUtil;
import kpi.manfredi.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static kpi.manfredi.utils.DialogsUtil.showAlert;
import static kpi.manfredi.utils.DialogsUtil.showConfirmationDialog;
import static kpi.manfredi.utils.FileManipulation.*;
import static kpi.manfredi.utils.MessageUtil.formatMessage;
import static kpi.manfredi.utils.MessageUtil.getMessage;

public abstract class MenuCommands {
    private static final Logger logger = LoggerFactory.getLogger(MenuCommands.class);

    public static void open(List<File> addTo, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home") + File.separator + "Pictures"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));

        List<File> files = fileChooser.showOpenMultipleDialog(parentStage);
        if (files != null) {
            files = filterImages(files);
            files = removeDuplicates(files);
            addTo.addAll(files);
        }
    }

    public static void delete(List<File> deleteFrom, List<File> filesToBeDeleted, boolean deleteFromHardDrive) {
        if (filesToBeDeleted.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    getMessage("warning.title"),
                    getMessage("deleting.info")
            );
        } else {

            Optional<ButtonType> result = showConfirmationDialog(
                    getMessage("deleting.title"),
                    getMessage("deleting.header"),
                    formatMessage("deleting.content", filesToBeDeleted.size())
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info(formatMessage("log.dialog.confirm.ok", "delete", filesToBeDeleted.size()));
                if (deleteFromHardDrive) {
                    deleteFiles(filesToBeDeleted);
                }
                deleteFrom.removeAll(filesToBeDeleted);
            } else {
                logger.info(formatMessage("log.dialog.confirm.cancel", "delete"));
            }
        }
    }

    public static void renameIteratively(ListView<File> files) {
        List<File> selectedItems = files.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            DialogsUtil.showAlert(
                    Alert.AlertType.WARNING,
                    getMessage("warning.title"),
                    MessageUtil.getMessage("renaming.info"));
            return;
        }

        List<File> renamedFiles = IterativeRenamingController.getInstance().
                startRenamingProcedure(selectedItems);

        if (selectedItems != renamedFiles) {
            files.getItems().removeAll(selectedItems);
            files.getItems().addAll(renamedFiles);
        }
    }
}
