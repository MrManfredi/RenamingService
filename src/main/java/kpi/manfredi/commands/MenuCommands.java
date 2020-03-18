package kpi.manfredi.commands;

import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static kpi.manfredi.utils.Dialogs.showDeleteConfirmationDialog;
import static kpi.manfredi.utils.Dialogs.showInstructiveRemovalInformation;
import static kpi.manfredi.utils.FileManipulation.*;

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
            showInstructiveRemovalInformation();
            return;
        }

        Optional<ButtonType> result = showDeleteConfirmationDialog(filesToBeDeleted.size());
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("Pressed on delete button. Result is OK! {} deleteFrom to be deleted.", filesToBeDeleted.size());
            if (deleteFromHardDrive) {
                deleteFiles(filesToBeDeleted);
            }
            deleteFrom.removeAll(filesToBeDeleted);
        } else {
            logger.info("Pressed on delete button. Result is CANCEL!");
        }
    }
}
