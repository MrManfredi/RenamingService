package kpi.manfredi.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kpi.manfredi.commands.MenuCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static kpi.manfredi.utils.Dialogs.showAboutWindow;
import static kpi.manfredi.utils.Dialogs.showFileNotFoundAlert;
import static kpi.manfredi.utils.FileManipulation.convertToFile;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private Stage mainStage;

    //
    // Container
    //
    @FXML
    private VBox vBoxContainer;

    //
    // Menu
    //
    @FXML
    private MenuItem menuOpen;

    @FXML
    private MenuItem menuSelectAll;

    @FXML
    private MenuItem menuClearAll;

    @FXML
    private MenuItem menuClearSelected;

    @FXML
    private MenuItem menuRenameIteratively;

    @FXML
    private MenuItem menuDelete;

    @FXML
    private MenuItem menuAbout;

    ///
    // Images
    //
    @FXML
    private ListView<File> imagesListView;

    //
    // Preview
    //
    @FXML
    private ImageView previewImage;

    @FXML
    private Button openImageButton;

    //
    // Main method
    //
    public void initialize() {
        // List View
        imagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initListViewCellFactory();
        initImagesLvKeyListener();

        // Menu
        initMenuListeners();

        // Image preview
        initOpenImageButtonListener();
    }

    private Stage getMainStage() {
        if (mainStage == null) mainStage = (Stage) vBoxContainer.getScene().getWindow();
        return mainStage;
    }

    //
    // List View
    //
    private void initListViewCellFactory() {
        imagesListView.setCellFactory(lv -> {
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
                    setFocusedImagePreview();
                }
            });
            return cell;
        });
    }

    private void initImagesLvKeyListener() {
        imagesListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                case DOWN:
                    setFocusedImagePreview();
                    break;
            }
        });
    }

    //
    // Image preview
    //
    private void setFocusedImagePreview() {
        File focusedItem = imagesListView.getFocusModel().getFocusedItem();
        if (focusedItem == null) {
            setDefaultPreviewImage();
        } else if (focusedItem.exists()) {
            try {
                setPreviewImage(focusedItem);
            } catch (MalformedURLException ex) {
                logger.error(ex.getMessage());
            }
        } else {
            logger.warn("File {} does not exist!", focusedItem);
            imagesListView.getItems().remove(focusedItem);
            showFileNotFoundAlert(focusedItem);
        }
    }

    private void setPreviewImage(File file) throws MalformedURLException {
        Image image = new Image(file.toURI().toURL().toString());
        previewImage.setImage(image);
        logger.info("Set preview image to {}", file);
    }

    private void initOpenImageButtonListener() {
        openImageButton.setOnMouseClicked(event -> {
            if (previewImage != null) {
                File file = convertToFile(previewImage.getImage());
                if (file != null) {
                    if (file.exists()) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.open(file);
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                        }
                    } else {
                        logger.warn("File {} does not exist!", file);
                        imagesListView.getItems().remove(file);
                        showFileNotFoundAlert(file);
                        setDefaultPreviewImage();
                    }
                }
            }
        });
    }

    private void setDefaultPreviewImage() {
        Image image = new Image("preview_image.jpg");
        previewImage.setImage(image);
    }

    //
    // Menu
    //
    private void initMenuListeners() {
        menuOpen.setOnAction(event ->
                MenuCommands.open(imagesListView.getItems(), getMainStage()));
        menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        menuSelectAll.setOnAction(event ->
                imagesListView.getSelectionModel().selectAll());
        menuClearAll.setOnAction(event ->
                imagesListView.getItems().clear());
        menuClearSelected.setOnAction(event ->
                MenuCommands.delete(
                        imagesListView.getItems(),
                        imagesListView.getSelectionModel().getSelectedItems(),
                        false));
        menuRenameIteratively.setOnAction(event ->
                MenuCommands.renameIteratively(imagesListView));
        menuDelete.setOnAction(event ->
                MenuCommands.delete(
                        imagesListView.getItems(),
                        imagesListView.getSelectionModel().getSelectedItems(),
                        true));
        menuDelete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menuAbout.setOnAction(event -> showAboutWindow());
    }

}
