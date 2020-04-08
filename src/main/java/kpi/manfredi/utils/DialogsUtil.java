package kpi.manfredi.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.Optional;

import static kpi.manfredi.utils.MessageUtil.formatMessage;
import static kpi.manfredi.utils.MessageUtil.getMessage;

/**
 * This class is used to provide methods to show alerts and dialogs
 *
 * @author manfredi
 */
public abstract class DialogsUtil {

    /**
     * This method is used to show alert with the given {@link Alert.AlertType AlertType}
     *
     * @param alertType   alert type
     * @param title       title of window
     * @param contentText content text
     */
    public static void showAlert(Alert.AlertType alertType,
                                 String title,
                                 String contentText) {
        showAlert(alertType, title, null, contentText);
    }

    /**
     * This method is used to show alert with the given {@link Alert.AlertType AlertType}
     *
     * @param alertType   alert type
     * @param title       title of window
     * @param headerText  header text
     * @param contentText content text
     */
    public static void showAlert(Alert.AlertType alertType,
                                 String title,
                                 String headerText,
                                 String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    /**
     * This method is used to show dialog with two cases:
     * <ui>
     * <li>OK</li>
     * <li>CANCEL</li>
     * </ui>
     *
     * @param title       title of window
     * @param contentText content text
     * @return result of the user's choice
     */
    public static Optional<ButtonType> showConfirmationDialog(
            String title,
            String contentText) {
        return showConfirmationDialog(title, null, contentText);
    }

    /**
     * This method is used to show dialog with two cases:
     * <ui>
     * <li>OK</li>
     * <li>CANCEL</li>
     * </ui>
     *
     * @param title       title of window
     * @param headerText  header text
     * @param contentText content text
     * @return result of the user's choice
     */
    public static Optional<ButtonType> showConfirmationDialog(
            String title,
            String headerText,
            String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

    /**
     * This method is used to notify user that file not found
     *
     * @param file file which not found
     */
    public static void showFileNotFoundAlert(File file) {
        showAlert(
                Alert.AlertType.WARNING,
                getMessage("warning.title"),
                formatMessage("file.not.found", file)
        );
    }
}
