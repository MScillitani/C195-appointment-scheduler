package controllers;

import database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Appointment;
import models.Customer;
import models.User;
import utilities.Alerts;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/** This is the first screen the user sees. Allows user to login to the main screen
 *  IF user is offline, puts them into offline mode and lets them go into the main screen anyway (no data, obviously) */
public class LoginController implements Initializable {

    public Text loginLabel;
    public Button loginButton;
    public TextField usernameField;
    public Text usernameLabel;
    public Text passwordLabel;
    public PasswordField passwordField;
    public Text userLocation;
    public Text onlineStatus;
    public static User user;

    Parent scene;
    Stage stage;

    ZoneId userZoneId = ZoneId.systemDefault(); //THIS IS THE USER'S LOCATION

    /** Sets the login location, date, and time to local user's. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userLocation.setText(String.valueOf(userZoneId));

        //tells user if they are online or offline
        if (DBConnection.getConnection() != null) {
            onlineStatus.setText("Status: online");
        }

        //checks user locale and sets language to French if contains "fr"
        if(Locale.getDefault().toString().contains("fr")) {
            toFrench();
        }
    }

    /** Gets username and password from text fields, checks if login credentials are valid
     *  Sends user to main screen if true or alerts user if false
     *  In offline mode, takes user to main screen anyway (for testing) */
    public void clickedLoginButton(ActionEvent actionEvent) throws IOException, SQLException {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateLogin(username, password)) {
            Customer.populateCustomersList();
            Appointment.populateAppointmentsList();
            Appointment.populateWeeklyAppointmentsList();
            Appointment.populateMonthlyAppointmentsList();

            //report one does NOT need to be called here -- it is populated in the MainScreenView class
            Appointment.populateReportTwo();
            Appointment.populateReportThree();

            stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main-view.fxml")));
            stage.setScene(new Scene(scene));
            stage.show();
            Appointment checkForAppointment = null;
            try {
                checkForAppointment = Appointment.checkForUpcomingAppointment();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            if (checkForAppointment != null) {
                Alerts.information("Welcome message", "You have an upcoming appointment", User.getUsername() + ", your appointment with ID " + checkForAppointment.getAppointmentId() + " is scheduled for\n" + checkForAppointment.getFancyStart() + " " + ZoneId.systemDefault() + " local time");
            } else {
                Alerts.information("Welcome message", "You do not have any upcoming appointments", User.getUsername() + ", you have no upcoming appointments at this time");
            }

        } else {
            if(Locale.getDefault().toString().contains("fr")) {
                Alerts.error("Erreur", "Identifiants de connexion incorrects", "Veuillez retaper votre nom d'utilisateur et votre mot de passe");
            } else {
                Alerts.error("Error", "Incorrect login credentials", "Please re-type your username and password");
            }
        }
    }

    /** logs user login attempt. Note to self: Need to add this to the "clickedLoginButton"
     *  method's IF/THEN statement */
    public static void logToFile(String username, boolean success) {
        try (FileWriter fileWriter = new FileWriter("login_activity.txt", true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)) {
            printWriter.println("[Timestamp: " + ZonedDateTime.now() + "] [User: " + username + "] [Log-in Attempt: " + (success ? "Success]" : "Failure]"));
        } catch (IOException e) {
            System.out.println("Log Error: " + e.getMessage());
        }
    }

    /** Changes all the labels and buttons to display as French */
    public void toFrench() {
        loginLabel.setText("Connexion");
        loginButton.setText("Connexion");
        usernameLabel.setText("Nom d'utilisateur");
        passwordLabel.setText("Mot de passe");

        if(DBConnection.getConnection() == null) {
            onlineStatus.setText("Statut: hors ligne");
        } else {
            onlineStatus.setText("Statut: en ligne");
        }
    }

    /** Checks to make sure the user has input valid login credentials */
    public static Boolean validateLogin(String username, String password) throws SQLException {
        Connection connection = DBConnection.getConnection();

        /** Setting this to return true if it is NOT connected so that
         *  I can continue tinkering with my app outside of the VM lab */
        if (connection == null) {
            return true;
        }

        ResultSet userSet = connection.createStatement().executeQuery("SELECT * FROM users WHERE User_Name='" + username + "' AND Password='" + password + "'");

        try {
            if(userSet.next()) {
                user = new User();
                user.setUsername(userSet.getString("User_Name"));
                user.setUserId(userSet.getInt("User_ID"));
                logToFile(username, true);
                return true;
            } else {
                logToFile(username, false);
                return false;
            }
        } catch (SQLException e) {
            if(Locale.getDefault().toString().contains("fr")) {
                System.out.println("Erreur");
            } else {
                System.out.println("Error: " + e.getMessage());
            } return false;
        }
    }

    /** gets ALL user information, not just name or ID */
    public static User getUser(){
        return user;
    }
}