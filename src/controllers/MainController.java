package controllers;

import database.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Appointment;
import models.Customer;
import utilities.Alerts;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;

/** controls main screen -- this screen displays customers, appointments, three reports, and the login attempts log */
public class MainController implements Initializable {

    //customers table
    public TableView<Customer> customersTableView;
    public TableColumn customerIdColumn;
    public TableColumn customerNameColumn;
    public TableColumn customerAddressColumn;
    public TableColumn customerDivisionColumn;
    public TableColumn customerPostalCodeColumn;
    public TableColumn customerPhoneNumberColumn;
    public TableColumn customerCountryColumn;

    //appointments table
    public TableView appointmentsTableView;
    public TableColumn appointmentIdColumn;
    public TableColumn appointmentTitleColumn;
    public TableColumn appointmentDescriptionColumn;
    public TableColumn appointmentLocationColumn;
    public TableColumn appointmentContactColumn;
    public TableColumn appointmentTypeColumn;
    public TableColumn appointmentStartColumn;
    public TableColumn appointmentEndColumn;
    public TableColumn appointmentCustomerIdColumn;
    public TableColumn appointmentUserIdColumn;

    //displays latest login attempt
    public Text loginActivity;

    //radio buttons
    public RadioButton weeklyRadioButton;
    public RadioButton monthlyRadioButton;
    public RadioButton allRadioButton;

    //report #3 - contacts/customers ratio
    public TableView ccrTableView;
    public TableColumn<Appointment, String> ccrContactColumn;
    public TableColumn<Appointment, String> ccrCustomersColumn;

    //report #1 - customer appointments by month and type
    public TableView typeAndMonthTableView;
    public TableColumn reportByMonthColumn;
    public TableColumn reportByAppointmentColumn;
    public TableColumn reportTotalColumn;

    //report #2 - contact schedule
    public TableView contactScheduleTableView;
    public TableColumn cSAppIdColumn;
    public TableColumn cSTitleColumn;
    public TableColumn cSTypeColumn;
    public TableColumn cSDescriptionColumn;
    public TableColumn cSContactColumn;
    public TableColumn cSStartColumn;
    public TableColumn cSEndColumn;
    public TableColumn cSCustomerIdColumn;

    Parent scene;
    Stage stage;

    public static Customer modifyCustomer;
    public static Appointment modifyAppointment;

    /** LAMBDA #1 -- when a button in the main screen is clicked, that action is announced in the console with
     *  "Button clicked: " followed by the substring of the relevant button information.
     *  As a developer, this is useful for checking that the proper button methods are being called */
    EventHandler<ActionEvent> clickedButton = e -> System.out.println("Button clicked: " + e.toString().substring(24));

    /**
     * Fills every table with data upon initialization
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("login_activity.txt"));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        while (true) {
            assert scanner != null;
            if (!scanner.hasNextLine()) break;
            loginActivity.setText(scanner.nextLine()); //grabs latest login attempt and displays it in main screen
        }

        populateCustomersTable(); //populates customers table in Customers tab
        populateAppointmentsTable(); //populates appointments table in Appointments tab

        populateTypeAndMonthTableView(); //populates first report in reports tab
        populateContactScheduleTableView(); //populates second report in reports tab
        populateCcrTableView(); //populates third report in reports tab
    }

    /** Populates the customers table, vars found in Customer class, items set in Tables class */
    public void populateCustomersTable() {
        if (DBConnection.getConnection() == null) {
            return;
        }

        customersTableView.setItems(Customer.getAllCustomers());

        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        customerPostalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("customerPostalCode"));
        customerPhoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        customerCountryColumn.setCellValueFactory(new PropertyValueFactory<>("customerCountry"));
        customerDivisionColumn.setCellValueFactory(new PropertyValueFactory<>("CustomerDivision"));
    }

    /** populates the appt table with data -- called by init */
    public void populateAppointmentsTable() {
        if (DBConnection.getConnection() == null) {
            return;
        }

        if (weeklyRadioButton.isSelected()) {
            appointmentsTableView.setItems(Appointment.getWeeklyAppointments());
        } else if (monthlyRadioButton.isSelected()) {
            appointmentsTableView.setItems(Appointment.getMonthlyAppointments());
        } else {
            appointmentsTableView.setItems(Appointment.getAllAppointments());
        }

        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        appointmentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
        appointmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDescription"));
        appointmentLocationColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentLocation"));
        appointmentContactColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentContactName"));
        appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        appointmentStartColumn.setCellValueFactory(new PropertyValueFactory<>("fancyStart"));
        appointmentEndColumn.setCellValueFactory(new PropertyValueFactory<>("fancyEnd"));
        appointmentCustomerIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCustomerId"));
        appointmentUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentUserId"));
    }

    /** Sends user to the add customers screen -- allows them to add a customer */
    public void addCustomerButtonClicked(ActionEvent actionEvent) throws IOException {

        clickedButton.handle(actionEvent);

        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/add-customer-view.fxml")));
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /** sends user to the modify customer screen -- allows user to modify a pre-existing customer.
     *  If no customer is selected, displays error message */
    public void modifyCustomerButtonClicked(ActionEvent actionEvent) throws IOException {

        clickedButton.handle(actionEvent);

        if (customersTableView.getSelectionModel().getSelectedItem() == null) {
            Alerts.error("Error", "No customer selected", "Select a customer and try again");
        } else {
            modifyCustomer = customersTableView.getSelectionModel().getSelectedItem();
            stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/modify-customer-view.fxml")));
            stage.setScene(new Scene(scene));
            stage.show();
        }
    }

    /** Deletes selected customer or displays an error message if none is selected.
     *  Refreshes table after deletion */
    public void deleteCustomerButtonClicked(ActionEvent actionEvent) throws SQLException {

        clickedButton.handle(actionEvent);

        if (customersTableView.getSelectionModel().getSelectedItem() == null) {
            Alerts.error("Error", "You have not selected a customer", "Select a customer and try again");
        } else {
            if (Alerts.confirmation("Confirm", "There is no going back", "Are you certain you want to delete? This is permanent").get() == ButtonType.OK) {
                Customer customer = (Customer) customersTableView.getSelectionModel().getSelectedItem();
                Customer.deleteCustomer(customer);
                customersTableView.setItems(Customer.getAllCustomers());
            }
        }
    }

    /** Displays log file containing all login attempts when clicked */
    public void logsButtonClicked(ActionEvent actionEvent) {

        clickedButton.handle(actionEvent);

        File file = new File("login_activity.txt");
        if(file.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /** Takes user to add appt screen -- allows user to add an appt */
    public void addAppointmentButtonClicked(ActionEvent actionEvent) throws IOException {

        clickedButton.handle(actionEvent);

        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/add-appointment-view.fxml")));
        stage.setScene(new Scene(scene));
        stage.show();
    }

    /** Takes user to modify appt screen -- allows user to modify appt or displays an error msg if no appt selected */
    public void modifyAppointmentButtonClicked(ActionEvent actionEvent) throws IOException {

        clickedButton.handle(actionEvent);

        if (appointmentsTableView.getSelectionModel().getSelectedItem() == null) {
            Alerts.error("Error", "No appointment selected", "Select an appointment and try again");
        } else {
            modifyAppointment = (Appointment) appointmentsTableView.getSelectionModel().getSelectedItem();
            stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/modify-appointment-view.fxml")));
            stage.setScene(new Scene(scene));
            stage.show();
        }
    }

    /** Deletes a selected appointment. When none is selected, displays an error code instead.
     *  After deletion, refreshes table based on which radio button is selected */
    public void deleteAppointmentButtonClicked(ActionEvent actionEvent) throws SQLException {

        clickedButton.handle(actionEvent);

        if (appointmentsTableView.getSelectionModel().getSelectedItem() == null) {
            Alerts.error("Error", "You have not selected an appointment", "Select an appointment and try again");
        } else {
            if (Alerts.confirmation("Confirm", "There is no going back", "Are you certain you want to delete? This is permanent").get() == ButtonType.OK) {
                Appointment appointment = (Appointment) appointmentsTableView.getSelectionModel().getSelectedItem();
                Appointment.deleteAppointment(appointment);
                if (allRadioButton.isSelected()) {
                    appointmentsTableView.setItems(Appointment.getAllAppointments());
                }
                if (weeklyRadioButton.isSelected()) {
                    appointmentsTableView.setItems(Appointment.getWeeklyAppointments());
                }
                if (monthlyRadioButton.isSelected()) {
                    appointmentsTableView.setItems(Appointment.getMonthlyAppointments());
                }
            }
        }
    }

    /** radio button -- displays every appt this week */
    public void weeklyRadioButtonClicked(ActionEvent actionEvent) {

        clickedButton.handle(actionEvent);

        allRadioButton.setSelected(false);
        monthlyRadioButton.setSelected(false);
        populateAppointmentsTable();
    }

    /** radio button -- displays every appt this month */
    public void monthlyRadioButtonClicked(ActionEvent actionEvent) {

        clickedButton.handle(actionEvent);

        weeklyRadioButton.setSelected(false);
        allRadioButton.setSelected(false);
        populateAppointmentsTable();
    }

    /** radio button - displays every appt ever */
    public void allRadioButtonClicked(ActionEvent actionEvent) {

        clickedButton.handle(actionEvent);

        weeklyRadioButton.setSelected(false);
        monthlyRadioButton.setSelected(false);
        populateAppointmentsTable();
    }

    /** This is the first report in the main screen, displaying customer appointments by month and type */
    public void populateTypeAndMonthTableView() {

        typeAndMonthTableView.getItems().clear();

        typeAndMonthTableView.setItems(Appointment.getTypeAndMonthReport());
        reportByMonthColumn.setCellValueFactory(new PropertyValueFactory<>("reportMonth"));
        reportByAppointmentColumn.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        reportTotalColumn.setCellValueFactory(new PropertyValueFactory<>("reportTotal"));
    }

    /** This is the second report in the main screen -- shows ever appt grouped by each contact */
    public void populateContactScheduleTableView() {

        contactScheduleTableView.getItems().clear();

        contactScheduleTableView.setItems(Appointment.getReportTwo());
        cSContactColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentContactName"));
        cSAppIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        cSTitleColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
        cSTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        cSDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDescription"));
        cSStartColumn.setCellValueFactory(new PropertyValueFactory<>("fancyStart"));
        cSEndColumn.setCellValueFactory(new PropertyValueFactory<>("fancyEnd"));
        cSCustomerIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCustomerId"));
    }

    /**LAMBDA #2 -- Used lambda expressions ot populate the cells in this report table with data
     * This is the third report in the main screen, displaying how many appointments each customer has */
    public void populateCcrTableView() {

        ccrTableView.getItems().clear();

        ccrTableView.setItems(Appointment.getReportThree());

        ccrContactColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentCustomerName()));
        ccrCustomersColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActiveAppointments()));

        //old method of filling out table before lambda expression was introduced
        //ccrContactColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCustomerName"));
        //ccrCustomersColumn.setCellValueFactory(new PropertyValueFactory<>("activeAppointments"));
    }

    /** When clicked, exits the application with status code 0 */
    public void exitButtonClicked(ActionEvent actionEvent) {

        clickedButton.handle(actionEvent);

        System.exit(0);
    }
}