package utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/** has common alert types to save space in methods where alerts are needed */
public class Alerts {

    /** shows error alert */
    public static void error(String title, String header, String comment) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(comment);
        alert.showAndWait();
    }
    /** shows confirmation alert AND returns the button pressed by user */
    public static Optional<ButtonType> confirmation(String title, String header, String comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(comment);
        return alert.showAndWait();
    }
    /** shows informational alert */
    public static void information(String title, String header, String comment)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(comment);
        alert.showAndWait();
    }
}