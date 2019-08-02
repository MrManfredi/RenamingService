package kpi.manfredi.gui;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.file.FileTypeDirectory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

    @FXML
    private ImageView preview_image;

    @FXML
    private Button open_image_button;

    public void initialize() {
        images_list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initMy();
        handle_add_button();
        handle_remove_selected_button();
        handle_remove_duplicates_button();
        handle_delete_selected_button();
        handle_about_menuItem();
        handle_open_image_button();
    }

    private void initMy() {
        images_list_view.setCellFactory(lv -> {
            ListCell<File> cell = new ListCell<>() {

                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null)
                        setText(item.getName());
                    else
                        setText("");
                }

            };

            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {
                    logger.info("You clicked on " + cell.getItem());
                    handle_click_on_image_in_list(cell.getItem());
//                    e.consume();
                }
            });
            return cell;
        });
    }

    private void handle_click_on_image_in_list(File file) {
        try {
            Image image = new Image(file.toURI().toURL().toString());
            preview_image.setImage(image);
        } catch (MalformedURLException ex) {
            logger.error(ex.getMessage());
        }
    }

    private void handle_add_button() {
        add_button.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            Stage currentStage = (Stage) add_button.getScene().getWindow();
            List<File> files = fileChooser.showOpenMultipleDialog(currentStage);
            files = filterImages(files);
            setImagesListView(files);
        });
    }

    private List<File> filterImages(List<File> files) {
        return files.stream().filter(this::isImage).collect(Collectors.toList());
    }

    private boolean isImage(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            FileTypeDirectory fileTypeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
            ArrayList<Tag> tags = new ArrayList<>(fileTypeDirectory.getTags());
            String description = tags.get(0).getDescription();
            switch (description) {
                case "PNG":
                    logger.info("PNG file. {}", file);
                    break;
                case "JPEG":
                    logger.info("JPEG file. {}", file);
                    break;
                default:
                    logger.info("File is not image! {}", file);
                    return false;
            }
            return true;
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
            return false;
        }
    }

    private void setImagesListView(List<File> filesList) {
        ObservableList<File> filesObservableList = FXCollections.observableList(filesList);
        images_list_view.getItems().addAll(filesObservableList);
    }

    private void handle_remove_selected_button() {
        remove_selected_button.setOnMouseClicked(event -> {
            ObservableList<File> filesToRemove = images_list_view.getSelectionModel().getSelectedItems();
            images_list_view.getItems().removeAll(filesToRemove);
            setDefaultIfImageNotExists();
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
        setDefaultIfImageNotExists();
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
                setDefaultIfImageNotExists();
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

    private void setDefaultIfImageNotExists() {
        if (preview_image != null) {
            File file = convertToFile(preview_image.getImage());
            if (file != null && !file.exists()) {
                logger.info("Set to default because the following image does not exist: {}", file);
                Image image = new Image("preview_image.jpg");
                preview_image.setImage(image);
            }
        }
    }

    private void handle_open_image_button() {
        open_image_button.setOnMouseClicked(event -> {
            if (preview_image != null) {
                File file = convertToFile(preview_image.getImage());
                if (file != null) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.open(file);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        });
    }

    private File convertToFile(Image image) {
        try {
            URL url = new URL(image.getUrl());
            URI uri = url.toURI();
            return new File(uri);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
