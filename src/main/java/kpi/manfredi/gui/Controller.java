package kpi.manfredi.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @FXML
    private ListView<File> images_list_view;

    @FXML
    private Button add_button;

    @FXML
    private Button remove_selected_button;

    @FXML
    private Button delete_selected_button;

    @FXML
    private Button remove_duplicates_button;

    @FXML
    private MenuItem about_menuItem;

    public void initialize() {
        images_list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        handle_add_button();
        handle_remove_selected_button();
        handle_remove_duplicates_button();
        handle_delete_selected_button();
        handle_about_menuItem();
    }

    private void handle_add_button() {
        add_button.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            Stage currentStage = (Stage) add_button.getScene().getWindow();
            List<File> filesList = fileChooser.showOpenMultipleDialog(currentStage);
            setImagesListView(filesList);
        });
    }

    private void setImagesListView(List<File> filesList) {
        ObservableList<File> filesObservableList = FXCollections.observableList(filesList);
        images_list_view.getItems().addAll(filesObservableList);
    }

    private void handle_remove_selected_button() {
        remove_selected_button.setOnMouseClicked(event -> {
            ObservableList<File> filesToRemove = images_list_view.getSelectionModel().getSelectedItems();
            images_list_view.getItems().removeAll(filesToRemove);
        });
    }

    private void handle_remove_duplicates_button() {
        remove_duplicates_button.setOnMouseClicked(event -> removeDuplicates());
    }

    private void removeDuplicates() {
        Stream<File> distinct = images_list_view.getItems().stream().distinct();
        List<File> filesList = distinct.collect(Collectors.toList());
        ObservableList<File> filesObservableList = FXCollections.observableArrayList(filesList);
        images_list_view.setItems(filesObservableList);
    }

    private void handle_delete_selected_button() {
        delete_selected_button.setOnMouseClicked(event -> {
            ArrayList<File> filesToDelete = new ArrayList<>(images_list_view.getSelectionModel().getSelectedItems());

            if (filesToDelete.isEmpty()) {
                showInstructiveRemovalInformation();
                return;
            }

            Optional<ButtonType> result = showDeleteConfirmationDialog(filesToDelete.size());
            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info("Pressed on delete button. Result is OK! {} files to be deleted. ", filesToDelete.size());
                deleteFiles(filesToDelete);
            } else {
                logger.info("Pressed on delete button. Result is CANCEL!");
            }
        });
    }

    private void showInstructiveRemovalInformation() {
        logger.info("Pressed on delete button. Images not selected!");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("You should select images before deleting!");
        alert.showAndWait();
    }

    private Optional<ButtonType> showDeleteConfirmationDialog(int amountOfDeletingFiles) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("The image will be permanently deleted without recovery!");
        alert.setContentText("Are you sure you want to delete (" + amountOfDeletingFiles + ") image(s)?");
        return alert.showAndWait();
    }

    private void deleteFiles(ArrayList<File> fileArrayList) {
        fileArrayList.forEach(file -> {
            if (file.exists()) {
                logger.info("-- Deleting file " + file);
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    images_list_view.getItems().remove(file);
                    logger.info("---- File was deleted successfully");
                } else {
                    logger.error("---- Error. The file was not deleted!");
                }
            } else {
                logger.error("-- File does not exist! " + file);
            }
        });
    }

    private void handle_about_menuItem() {
        about_menuItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText(null);
            alert.setContentText("Hi! This program was developed by MrManfredi to help with " +
                    "image processing (renaming, deleting, etc.).");
            alert.showAndWait();
        });
    }
}
