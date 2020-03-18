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
import java.util.Optional;

import static kpi.manfredi.utils.Dialogs.*;
import static kpi.manfredi.utils.FileManipulation.*;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final String AUTOMATICALLY = "Automatically";
    private final String MANUALLY = "Manually";
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
    private MenuItem menuDelete;

    @FXML
    private MenuItem menuAbout;

    ///
    // Images
    //
    @FXML
    private ListView<File> imagesListView;

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
        imagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setListViewCellFactory();
        set_image_list_view_key_listener();
        set_images_list_view_activity_listener();

        // Menu
        initMenuListeners();

        // Image preview
        set_open_image_button_listener();

        // Renaming
        set_add_prefix_checkbox_listener();
        init_basis_choice_box();
        set_basis_choice_box_listener();
        set_add_postfix_checkbox_listener();
        set_basis_text_field_key_listener();
        set_rename_button_listener();
        set_rename_all_selected_button_listener();
    }

    private Stage getMainStage() {
        if (mainStage == null) mainStage = (Stage) vBoxContainer.getScene().getWindow();
        return mainStage;
    }

    //
    // List View
    //
    private void setListViewCellFactory() {
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
                    set_preview_image_of_focused_item();
                }
            });
            return cell;
        });
    }

    private void set_image_list_view_key_listener() {
        imagesListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                case DOWN:
                    set_preview_image_of_focused_item();
                    break;
            }
        });
    }

    private boolean set_preview_image_of_focused_item() {
        File focusedItem = imagesListView.getFocusModel().getFocusedItem();
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
            imagesListView.getItems().remove(focusedItem);
            showFileNotFoundAlert(focusedItem);
        }
        return true;
    }

    private void set_images_list_view_activity_listener() {

        imagesListView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                logger.info("Image ListView gained focused. Basis choice box value set null.");
                basis_choice_box.setValue(null);
                set_to_default_fields_dependent_on_choice_box();
            }
        });
    }

    private void replaceRenamedItemsInListView(ObservableList<File> newItems) {
        ObservableList<File> selectedItems = imagesListView.getSelectionModel().getSelectedItems();
        imagesListView.getItems().removeAll(selectedItems);
        imagesListView.getItems().addAll(newItems);
        logger.info("Were replaced renamed items in ListView.");
        set_preview_image_of_focused_item();
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
        ObservableList<Integer> selectedIndices = imagesListView.getSelectionModel().getSelectedIndices();
        if (!selectedIndices.isEmpty()) {
            imagesListView.getFocusModel().focus(selectedIndices.get(0));
            logger.info("Set focus on first selected item - {}", imagesListView.getFocusModel().getFocusedItem());
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
        File itemToRename = imagesListView.getFocusModel().getFocusedItem();
        int focusedIndex = imagesListView.getFocusModel().getFocusedIndex();

        String prefix = add_prefix_checkbox.isSelected() ? prefix_text_field.getText() : "";
        String basis = basis_text_field.getText();
        String postfix = add_postfix_checkbox.isSelected() ? postfix_text_field.getText() : "";
        String newName = prefix + basis + postfix;

        Optional<ButtonType> result = showRenameConfirmationDialog(itemToRename.getName(), newName);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("Pressed on rename button. Result is OK!");
            try {
                itemToRename = renameFile(itemToRename, newName);
                imagesListView.getItems().set(focusedIndex, itemToRename);
                imagesListView.getSelectionModel().clearSelection(focusedIndex);
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
        String name = imagesListView.getFocusModel().getFocusedItem().getName();
        name = name.substring(0, name.lastIndexOf('.'));
        basis_text_field.setText(name);
        basis_text_field.selectAll();
    }

    private void set_rename_all_selected_button_listener() {
        rename_all_selected_button.setOnMouseClicked(event -> {
            ObservableList<File> itemsToRename = imagesListView.getSelectionModel().getSelectedItems();
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
        preview_image.setImage(image);
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
        menuDelete.setOnAction(event ->
                MenuCommands.delete(
                        imagesListView.getItems(),
                        imagesListView.getSelectionModel().getSelectedItems(),
                        true));
        menuDelete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menuAbout.setOnAction(event -> showAboutWindow());
    }

}
