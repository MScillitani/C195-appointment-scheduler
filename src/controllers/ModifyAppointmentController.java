package controllers;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Appointment;
import models.User;
import utilities.Alerts;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static controllers.MainController.modifyAppointment;

/** modifies user-selected appt from main screen */
public class ModifyAppointmentController implements Initializable {

    public TextField AppointmentIdField;
    public DatePicker appointmentDateBox;
    public ComboBox<String> appointmentStartComboBox;
    public ComboBox<String> appointmentEndComboBox;
    public TextField appointmentTitleField;
    public TextField appointmentDescriptionField;
    public TextField appointmentLocationField;
    public ComboBox<String> appointmentContactComboBox;
    public TextField appointmentTypeField;
    public ComboBox appointmentCustomerIdComboBox;
    public ComboBox appointmentUserIdComboBox;

    Parent scene;
    Stage stage;

    private final ZoneId systemZoneId = ZoneId.of("America/New_York");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    private final ObservableList<String> startTimesList = FXCollections.observableArrayList();
    private final ObservableList<String> endTimesList = FXCollections.observableArrayList();

    /** initializes fields and combo boxes with appt data */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        /** Populates contact combo box */
        try {
            ResultSet contactNameResultSet = connection.createStatement().executeQuery("SELECT * FROM contacts");
            while (contactNameResultSet.next()) {
                appointmentContactComboBox.getItems().addAll(contactNameResultSet.getString("Contact_Name"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        /** Populates start and end time combo box */
        LocalTime time = LocalTime.of(8, 0);

        while (!time.equals(LocalTime.of(22, 15))) {
            startTimesList.add(time.format(timeFormat));
            endTimesList.add(time.format(timeFormat));
            time = time.plusMinutes(15);
        }

        startTimesList.remove(startTimesList.size() - 1);
        endTimesList.remove(0);

        appointmentStartComboBox.setItems(startTimesList);
        appointmentEndComboBox.setItems(endTimesList);
        appointmentStartComboBox.getSelectionModel().select(LocalTime.of(8, 0).format(timeFormat));
        appointmentEndComboBox.getSelectionModel().select(LocalTime.of(8, 15).format(timeFormat));

        /** Populates customer ID combo box */
        try {
            ResultSet contactNameResultSet = connection.createStatement().executeQuery("SELECT * FROM customers");
            while (contactNameResultSet.next()) {
                appointmentCustomerIdComboBox.getItems().addAll(contactNameResultSet.getInt("Customer_ID"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        /** Populates user ID combo box */
        try {
            ResultSet contactNameResultSet = connection.createStatement().executeQuery("SELECT * FROM users");
            while (contactNameResultSet.next()) {
                appointmentUserIdComboBox.getItems().addAll(contactNameResultSet.getInt("User_ID"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        /** Gets variables needed to set the date combo box */
        String year = modifyAppointment.getAppointmentStart().toString().substring(0, 4);
        String month = modifyAppointment.getAppointmentStart().toString().substring(5, 7);
        String day = modifyAppointment.getAppointmentStart().toString().substring(8, 10);

        /** Formats the time combo boxes */
        DateTimeFormatter timeComboFormatter = DateTimeFormatter.ofPattern("h:mm a");

        /** Sets all of the fields */
        AppointmentIdField.setText(String.valueOf(modifyAppointment.getAppointmentId()));
        appointmentDateBox.setValue(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day)));
        appointmentStartComboBox.getSelectionModel().select(modifyAppointment.getAppointmentStart().format(timeComboFormatter));
        appointmentEndComboBox.getSelectionModel().select(modifyAppointment.getAppointmentEnd().format(timeComboFormatter));
        appointmentTitleField.setText(modifyAppointment.getAppointmentTitle());
        appointmentDescriptionField.setText(modifyAppointment.getAppointmentDescription());
        appointmentLocationField.setText(modifyAppointment.getAppointmentLocation());
        appointmentContactComboBox.getSelectionModel().select(modifyAppointment.getAppointmentContactName());
        appointmentTypeField.setText(modifyAppointment.getAppointmentType());
        appointmentCustomerIdComboBox.setValue(modifyAppointment.getAppointmentCustomerId());
        appointmentUserIdComboBox.setValue(modifyAppointment.getAppointmentUserId());
    }

    /** Saves appointment data inputted by user */
    public void saveButtonClicked(ActionEvent actionEvent) throws SQLException {

        try {

            Connection connection = DBConnection.getConnection();

            if (DBConnection.getConnection() == null) {
                return;
            }

            if(hasValidInputs()) {
                /** Setting up variables */
                String title = appointmentTitleField.getText();
                String description = appointmentDescriptionField.getText();
                String location = appointmentLocationField.getText();
                String contactName = String.valueOf(appointmentContactComboBox.getSelectionModel().getSelectedItem());
                String type = appointmentTypeField.getText();
                LocalDate selectedDate = appointmentDateBox.getValue();
                LocalTime selectedStartTime = LocalTime.parse(appointmentStartComboBox.getSelectionModel().getSelectedItem(), timeFormat);
                LocalTime selectedEndTime = LocalTime.parse(appointmentEndComboBox.getSelectionModel().getSelectedItem(), timeFormat);
                int customerId = Integer.parseInt(String.valueOf(appointmentCustomerIdComboBox.getSelectionModel().getSelectedItem()));
                int userId = Integer.parseInt(String.valueOf(appointmentUserIdComboBox.getSelectionModel().getSelectedItem()));
                int contactId = -1;
                String userName = User.getUsername();

                /** Gets contact ID from name */
                ResultSet ContactIdResultSet = connection.createStatement().executeQuery(
                        "SELECT Contact_ID FROM contacts WHERE Contact_Name='" + contactName + "'");
                while(ContactIdResultSet.next()) {
                    contactId = ContactIdResultSet.getInt("Contact_ID");
                }

                /** This takes the user's selected date and combos it with their selected start and end times */
                LocalDateTime appointmentStartDateTime = LocalDateTime.of(selectedDate, selectedStartTime);
                LocalDateTime appointmentEndDateTime = LocalDateTime.of(selectedDate, selectedEndTime);

                /** Converts the previous DateTime into UTC to check against the database */
                ZonedDateTime appointmentStartUTC = appointmentStartDateTime.atZone(systemZoneId).withZoneSameInstant(ZoneId.of("UTC"));
                ZonedDateTime appointmentEndUTC= appointmentEndDateTime.atZone(systemZoneId).withZoneSameInstant(ZoneId.of("UTC"));

                /** Converts the UTC DateTime into a timestamp */
                Timestamp appointmentStartTimeStamp = Timestamp.valueOf(appointmentStartUTC.toLocalDateTime());
                Timestamp appointmentEndTimeStamp = Timestamp.valueOf(appointmentEndUTC.toLocalDateTime());

                connection.createStatement().execute("UPDATE appointments SET " +
                        "Title='" + title + "', Description='" + description + "', Location='" +
                        location + "', Type='" + type + "', Start='" + appointmentStartTimeStamp +
                        "', End='" + appointmentEndTimeStamp + "', Last_Update=NOW(), Last_Updated_By='" +
                        userName + "', Customer_ID=" + customerId + ", User_ID=" + userId + ", Contact_ID=" +
                        contactId + " WHERE Appointment_ID=" + Integer.parseInt(String.valueOf(modifyAppointment.getAppointmentId())));

                Appointment.populateAppointmentsList();
                Appointment.populateMonthlyAppointmentsList();
                Appointment.populateWeeklyAppointmentsList();

                stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
                scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main-view.fxml")));
                stage.setScene(new Scene(scene));
                stage.show();
            }
        } catch(Exception e) {
            System.out.println("Error in ModifyAppointmentController: " + e.getMessage());
        }
    }

    /** verifies that the inputs are valid */
    public boolean hasValidInputs() throws SQLException {
        Connection connection = DBConnection.getConnection();

        String title = appointmentTitleField.getText();
        String description = appointmentDescriptionField.getText();
        String location = appointmentLocationField.getText();
        String contactName = appointmentContactComboBox.getSelectionModel().getSelectedItem();
        String type = appointmentTypeField.getText();
        LocalDate selectedDate = appointmentDateBox.getValue();
        LocalTime selectedStartTime = LocalTime.parse(appointmentStartComboBox.getSelectionModel().getSelectedItem(), timeFormat);
        LocalTime selectedEndTime = LocalTime.parse(appointmentEndComboBox.getSelectionModel().getSelectedItem(), timeFormat);
        int contactId = -1;
        int customerId = (int) appointmentCustomerIdComboBox.getSelectionModel().getSelectedItem();

        boolean isCustomerIdSelected = false;
        boolean isUserIdSelected = false;

        if (appointmentCustomerIdComboBox.getValue() != null) {
            isCustomerIdSelected = true;
        }
        if (appointmentUserIdComboBox.getValue() != null) {
            isUserIdSelected = true;
        }

        /** This takes the user's selected date and combos it with their selected start and end times */
        LocalDateTime appointmentStartDateTime = LocalDateTime.of(selectedDate, selectedStartTime);
        LocalDateTime appointmentEndDateTime = LocalDateTime.of(selectedDate, selectedEndTime);

        /** Converts the previous DateTime into UTC to check against the database */
        ZonedDateTime appointmentStartUTC = appointmentStartDateTime.atZone(systemZoneId).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime appointmentEndUTC= appointmentEndDateTime.atZone(systemZoneId).withZoneSameInstant(ZoneId.of("UTC"));

        /** Gets contact ID from name */
        ResultSet ContactIdResultSet = connection.createStatement().executeQuery(
                "SELECT * FROM contacts WHERE Contact_Name='" + contactName + "'");
        while(ContactIdResultSet.next()) {
            contactId = ContactIdResultSet.getInt("Contact_ID");
        }

        if (title.equals("") || description.equals("") || location.equals("") || contactName == null||
                type.equals("") || appointmentStartUTC == null || appointmentEndUTC == null || !isCustomerIdSelected ||
                !isUserIdSelected) {
            Alerts.error("Error", "Missing appointment information", "Make sure no fields are blank and try again");
            return false;
        }
        if (appointmentStartUTC.equals(appointmentEndUTC) || appointmentEndUTC.isBefore(appointmentStartUTC)) {
            Alerts.error("Error", "Scheduling error", "The end time cannot come before the start time");
            return false;
        }
        if (checkIfAppointmentConflict(appointmentStartUTC, appointmentEndUTC, contactId, customerId)) {
            Alerts.error("Error", "Scheduling error", "The time you're attempting to schedule conflicts with a pre-existing appointment");
            return false;
        }
        return true;
    }
    /** checks the databse for appt conflicts -- this differs from the method in the Appointment class becuase an extra condiiton had to be added
     * -- excluding the current appt ID */
    public boolean checkIfAppointmentConflict(ZonedDateTime startDateTime, ZonedDateTime endDateTime, int contactId, int customerId) {
        Connection connection = DBConnection.getConnection();

        try {
            ResultSet appointmentTimesResultSet = connection.createStatement().executeQuery("SELECT * FROM appointments "
                    + "WHERE('"+ Timestamp.valueOf(startDateTime.toLocalDateTime()) + "' BETWEEN Start AND End OR '"
                    + endDateTime.toLocalDateTime() + "' BETWEEN Start AND End OR '" +
                    Timestamp.valueOf(startDateTime.toLocalDateTime()) + "'  < Start AND '" + endDateTime.toLocalDateTime() +
                    "' > End) AND (Contact_ID='" + contactId + "' OR Customer_ID='" + customerId + "') AND Appointment_ID !='" + modifyAppointment.getAppointmentId() + "'");

            if(appointmentTimesResultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error in Appointment.checkIfAppointmentConflict: " + e.getMessage());
        }
        return false;
    }
    /** sends user back to main screen */
    public void cancelButtonClicked(ActionEvent actionEvent) throws IOException {
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main-view.fxml")));
        stage.setScene(new Scene(scene));
        stage.show();
    }
}