package kpi.manfredi.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.Optional;

public abstract class Dialogs {

    public static void showIOErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showFileNotFoundAlert(File file) {
        showIOErrorAlert("File:\n" + file + "\n not found! Maybe it was deleted or moved from outside.");
    }

    public static void showInstructiveRemovalInformation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instruction");
        alert.setHeaderText(null);
        alert.setContentText("You should select images before deleting!");
        alert.showAndWait();
    }

    public static Optional<ButtonType> showDeleteConfirmationDialog(int amountOfDeletingFiles) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deleting");
        alert.setHeaderText("The image will be permanently deleted without recovery!");
        alert.setContentText("Are you sure you want to delete (" + amountOfDeletingFiles + ") image(s)?");
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showRenameConfirmationDialog(String oldName, String newName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Renaming");
        alert.setHeaderText("The image will be renamed from \n" +
                oldName + "\n to \n" + newName + "\n");
        alert.setContentText("Are you sure you want to rename this image?");
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showRenameAllSelectedConfirmationDialog(int amountOfRenamingFiles, String newNameTemplate) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Renaming");
        alert.setHeaderText("The images will be renamed as follows: \"" +
                newNameTemplate + "\"!\n *(auto incremented number)");
        alert.setContentText("Are you sure you want to rename (" + amountOfRenamingFiles + ") image(s)?");
        return alert.showAndWait();
    }

    public static void showAboutWindow() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("Hi! This program was developed by MrManfredi to help with " +
                "image processing (renaming, deleting, etc.).");
        alert.showAndWait();
    }

}
