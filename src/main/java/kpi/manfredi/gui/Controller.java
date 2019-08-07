package kpi.manfredi.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kpi.manfredi.gui.Dialogs.*;
import static kpi.manfredi.gui.FileManipulation.*;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final String AUTOMATICALLY = "Automatically";
    private final String MANUALLY = "Manually";

    //
    // Menu
    //
    @FXML
    private MenuItem about_menuItem;

    ///
    // Images
    //
    @FXML
    private ListView<File> images_list_view;

    @FXML
    private Button add_button;

    @FXML
    private Button select_all_button;

    @FXML
    private Button remove_selected_button;

    @FXML
    private Button delete_selected_button;

    //
    // Renaming
    //
    @FXML
    private CheckBox add_prefix_checkbox;

    @FXML
    private TextField prefix_text_field;

    @FXML
    private ChoiceBox<String> basis_choice_box;

    @FXML
    private Label basis_label;

    @FXML
    private TextField basis_text_field;

    @FXML
    private CheckBox add_postfix_checkbox;

    @FXML
    private TextField postfix_text_field;

    @FXML
    private Button rename_button;

    @FXML
    private Button rename_all_selected_button;

    //
    // Preview
    //
    @FXML
    private ImageView preview_image;

    @FXML
    private Button open_image_button;

    //
    // Main method
    //
    public void initialize() {
        // List View
        images_list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setListViewCellFactory();
        set_image_list_view_key_listener();
        set_images_list_view_activity_listener();

        // List View control buttons
        set_add_button_listener();
        set_select_all_button_listener();
        set_remove_selected_button_listener();
        set_delete_selected_button_listener();

        // Image preview
        set_open_image_button_listener();

        // Menu
        set_about_menuItem_listener();

        // Renaming
        set_add_prefix_checkbox_listener();
        init_basis_choice_box();
        set_basis_choice_box_listener();
        set_add_postfix_checkbox_listener();
        set_basis_text_field_key_listener();
        set_rename_button_listener();
        set_rename_all_selected_button_listener();
    }

    //
    // List View
    //
    private void setListViewCellFactory() {
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
                    set_preview_image_of_focused_item();
                }
            });
            return cell;
        });
    }

    private void set_image_list_view_key_listener() {
        images_list_view.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                case DOWN:
                    set_preview_image_of_focused_item();
                    break;
            }
        });
    }

    private boolean set_preview_image_of_focused_item() {
        File focusedItem = images_list_view.getFocusModel().getFocusedItem();
        if (focusedItem == null) {
            setDefaultPreviewImage();
            return false;
        }
        if (focusedItem.exists()) {
            try {
                setPreviewImage(focusedItem);
            } catch (MalformedURLException ex) {
                logger.error(ex.getMessage());
                return false;
            }
        } else {
            logger.warn("File {} does not exist!", focusedItem);
            images_list_view.getItems().remove(focusedItem);
            showFileNotFoundAlert(focusedItem);
        }
        return true;
    }

    private void setImagesListView(List<File> filesList) {
        ObservableList<File> filesObservableList = FXCollections.observableList(filesList);
        images_list_view.getItems().addAll(filesObservableList);
    }

    private void removeDuplicates() {
        Stream<File> distinct = images_list_view.getItems().stream().distinct();
        List<File> filesList = distinct.collect(Collectors.toList());
        ObservableList<File> filesObservableList = FXCollections.observableArrayList(filesList);
        images_list_view.setItems(filesObservableList);
        set_preview_image_of_focused_item();
    }

    private void set_images_list_view_activity_listener() {

        images_list_view.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                logger.info("Image ListView gained focused. Basis choice box value set null.");
                basis_choice_box.setValue(null);
                set_to_default_fields_dependent_on_choice_box();
            }
        });
    }

    private void replaceRenamedItemsInListView(ObservableList<File> newItems) {
        ObservableList<File> selectedItems = images_list_view.getSelectionModel().getSelectedItems();
        images_list_view.getItems().removeAll(selectedItems);
        images_list_view.getItems().addAll(newItems);
        logger.info("Were replaced renamed items in ListView.");
        set_preview_image_of_focused_item();
    }

    //
    // List View control buttons
    //
    private void set_add_button_listener() {
        add_button.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            Stage currentStage = (Stage) add_button.getScene().getWindow();
            List<File> files = fileChooser.showOpenMultipleDialog(currentStage);
            if (files != null) {
                files = filterImages(files);
                setImagesListView(files);
                removeDuplicates();
            }
        });
    }

    private void set_select_all_button_listener() {
        select_all_button.setOnMouseClicked(event -> images_list_view.getSelectionModel().selectAll());
        set_preview_image_of_focused_item();
    }

    private void set_remove_selected_button_listener() {
        remove_selected_button.setOnMouseClicked(event -> {
            ObservableList<File> filesToRemove = images_list_view.getSelectionModel().getSelectedItems();
            images_list_view.getItems().removeAll(filesToRemove);
            set_preview_image_of_focused_item();
        });
    }

    private void set_delete_selected_button_listener() {
        delete_selected_button.setOnMouseClicked(event -> {
            ArrayList<File> filesToDelete = new ArrayList<>(images_list_view.getSelectionModel().getSelectedItems());

            if (filesToDelete.isEmpty()) {
                showInstructiveRemovalInformation();
                return;
            }

            Optional<ButtonType> result = showDeleteConfirmationDialog(filesToDelete.size());
            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info("Pressed on delete button. Result is OK! {} files to be deleted. ", filesToDelete.size());
                ArrayList<File> notDeletedFiles = deleteFiles(filesToDelete);
                filesToDelete.removeAll(notDeletedFiles);
                images_list_view.getItems().removeAll(filesToDelete);
                set_preview_image_of_focused_item();
            } else {
                logger.info("Pressed on delete button. Result is CANCEL!");
            }
        });
    }

    //
    // Renaming
    //
    private void set_add_prefix_checkbox_listener() {
        add_prefix_checkbox.setOnAction(event -> prefix_text_field.setVisible(add_prefix_checkbox.isSelected()));
    }

    private void init_basis_choice_box() {
        basis_choice_box.setItems(FXCollections.observableArrayList(AUTOMATICALLY, MANUALLY));
    }

    private void set_basis_choice_box_listener() {
        basis_choice_box.setOnAction(event -> {
            set_to_default_fields_dependent_on_choice_box();
            switch (basis_choice_box.getSelectionModel().getSelectedItem()) {
                case AUTOMATICALLY:
                    basis_label.setVisible(true);
                    rename_all_selected_button.setDisable(false);
                    break;
                case MANUALLY:
                    basis_text_field.setVisible(true);
                    rename_button.setDisable(false);
                    set_focus_on_first_selected_item();
                    if (set_preview_image_of_focused_item()) {
                        setNameOfFocusedFileInBasisTextField();
                    }
                    break;
                default:
                    logger.error("Basis choice box selected item is {}", basis_choice_box.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void set_focus_on_first_selected_item() {
        ObservableList<Integer> selectedIndices = images_list_view.getSelectionModel().getSelectedIndices();
        if (!selectedIndices.isEmpty()) {
            images_list_view.getFocusModel().focus(selectedIndices.get(0));
            logger.info("Set focus on first selected item - {}", images_list_view.getFocusModel().getFocusedItem());
        }
    }

    private void set_to_default_fields_dependent_on_choice_box() {
        basis_label.setVisible(false);
        basis_text_field.setVisible(false);
        rename_button.setDisable(true);
        rename_all_selected_button.setDisable(true);
    }

    private void set_add_postfix_checkbox_listener() {
        add_postfix_checkbox.setOnAction(event -> postfix_text_field.setVisible(add_postfix_checkbox.isSelected()));
    }

    private void set_basis_text_field_key_listener() {
        basis_text_field.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                handleManuallyRenaming();
            }
        });
    }

    private void set_rename_button_listener() {
        rename_button.setOnMouseClicked(event -> handleManuallyRenaming());
    }

    private void handleManuallyRenaming() {
        File itemToRename = images_list_view.getFocusModel().getFocusedItem();
        int focusedIndex = images_list_view.getFocusModel().getFocusedIndex();

        String prefix = add_prefix_checkbox.isSelected() ? prefix_text_field.getText() : "";
        String basis = basis_text_field.getText();
        String postfix = add_postfix_checkbox.isSelected() ? postfix_text_field.getText() : "";
        String newName = prefix + basis + postfix;

        Optional<ButtonType> result = showRenameConfirmationDialog(itemToRename.getName(), newName);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("Pressed on rename button. Result is OK!");
            try {
                itemToRename = renameFile(itemToRename, newName);
                images_list_view.getItems().set(focusedIndex, itemToRename);
                images_list_view.getSelectionModel().clearSelection(focusedIndex);
                set_focus_on_first_selected_item();
                if (set_preview_image_of_focused_item()) {
                    setNameOfFocusedFileInBasisTextField();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                showIOErrorAlert(e.getMessage());
            }
        } else {
            logger.info("Pressed on rename button. Result is CANCEL!");
        }
    }

    private void setNameOfFocusedFileInBasisTextField() {
        String name = images_list_view.getFocusModel().getFocusedItem().getName();
        name = name.substring(0, name.lastIndexOf('.'));
        basis_text_field.setText(name);
        basis_text_field.selectAll();
    }

    private void set_rename_all_selected_button_listener() {
        rename_all_selected_button.setOnMouseClicked(event -> {
            ObservableList<File> itemsToRename = images_list_view.getSelectionModel().getSelectedItems();
            String prefix = add_prefix_checkbox.isSelected() ? prefix_text_field.getText() : "";
            String postfix = add_postfix_checkbox.isSelected() ? postfix_text_field.getText() : "";

            Optional<ButtonType> result = showRenameAllSelectedConfirmationDialog(itemsToRename.size(), prefix + "*" + postfix);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                logger.info("Pressed on rename all selected button. Result is OK! {} files to be rename. ", itemsToRename.size());
                ObservableList<File> renamedItems = renameFilesByTemplate(itemsToRename, prefix, postfix);
                replaceRenamedItemsInListView(renamedItems);
            } else {
                logger.info("Pressed on rename all selected button. Result is CANCEL!");
            }

        });
    }

    //
    // Image preview
    //
    private void setPreviewImage(File file) throws MalformedURLException {
        Image image = new Image(file.toURI().toURL().toString());
        preview_image.setImage(image);
        logger.info("Set preview image to {}", file);
    }

    private void set_open_image_button_listener() {
        open_image_button.setOnMouseClicked(event -> {
            if (preview_image != null) {
                File file = convertToFile(preview_image.getImage());
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
                        images_list_view.getItems().remove(file);
                        showFileNotFoundAlert(file);
                        setDefaultPreviewImage();
                    }
                }
            }
        });
    }

    private void setDefaultPreviewImage() {
        Image image = new Image("preview_image.jpg");
        preview_image.setImage(image);
    }

    //
    // Menu
    //
    private void set_about_menuItem_listener() {
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
