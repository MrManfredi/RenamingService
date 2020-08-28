package kpi.manfredi.gui.controllers;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kpi.manfredi.commands.MenuCommands;
import kpi.manfredi.tags.TagsAdapter;
import kpi.manfredi.tags.TagsCustodian;
import kpi.manfredi.tags.tree.TagsTree;
import org.controlsfx.control.CheckTreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import static kpi.manfredi.utils.DialogsUtil.showAlert;
import static kpi.manfredi.utils.DialogsUtil.showFileNotFoundAlert;
import static kpi.manfredi.utils.FileManipulation.convertToFile;
import static kpi.manfredi.utils.MessageUtil.getMessage;

public class ProcessingEnvironmentController {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingEnvironmentController.class);
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

    //
    // Left Panel
    //
    @FXML
    private CheckTreeView<Object> tagsTree;

    @FXML
    private TextArea oldName;

    @FXML
    private TextArea newName;

    @FXML
    private Button renameButton;

    ///
    // Images list
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

        // Menu
        initMenuListeners();

        // Left Panel
        initTagsTree();
        initTagsSelectionListener();

        // Images List
        initImagesListView();

        // Image preview
        initOpenImageButtonListener();

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
        menuAbout.setOnAction(event -> showAlert(
                Alert.AlertType.INFORMATION,
                getMessage("about.title"),
                getMessage("about.header"),
                getMessage("about.content")
        ));
    }

    //
    // Left Panel
    //
    private void initTagsTree() {
        TagsTree tagsTree = null;

        try {
            tagsTree = TagsCustodian.getTags();
        } catch (FileNotFoundException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    getMessage("error.title"),
                    e.getMessage());
        } catch (JAXBException jbe) {
            showAlert(
                    Alert.AlertType.ERROR,
                    getMessage("error.title"),
                    jbe.getMessage(),
                    getMessage("tags.validation.error.content")
            );
        }

        if (tagsTree == null) {
            this.tagsTree.setRoot(new CheckBoxTreeItem<>("Error"));
            this.tagsTree.setShowRoot(true);
        } else {
            CheckBoxTreeItem<Object> rootItem = TagsAdapter.getRootItem(tagsTree);
            this.tagsTree.setRoot(rootItem);
        }
    }

    private void initTagsSelectionListener() {
        tagsTree.getCheckModel().getCheckedItems().addListener((ListChangeListener<TreeItem<Object>>) c ->
                newName.setText(tagsTree.getCheckModel().getCheckedItems().toString())
        );
    }

    private Stage getMainStage() {
        if (mainStage == null) mainStage = (Stage) vBoxContainer.getScene().getWindow();
        return mainStage;
    }

    //
    // Images List
    //
    private void initImagesListView() {
        imagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        initImagesLvKeyListener();

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
                    logger.debug("You clicked on " + cell.getItem());
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
        logger.debug("Set preview image to {}", file);
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

}
