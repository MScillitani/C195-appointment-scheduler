package controllers;

import database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Customer;
import models.User;
import utilities.Alerts;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

/** Allows user to add customer. This screen appears when user clicks "Add" in the main screen's customer tab */
public class AddCustomerController implements Initializable {

    public TextField customerNameField;
    public TextField customerAddressField;
    public TextField customerPostalCodeField;
    public TextField customerPhoneNumberField;
    public ComboBox customerDivisionBox;
    public ComboBox customerCountryBox;

    Parent scene;
    Stage stage;

    /** Initializes the screen by adding the contents of the country combo box */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        /** shows user available countries and sets new customer's country to user's selection */
        ResultSet customerCountrySet = null;
        try {
            customerCountrySet = connection.createStatement().executeQuery("SELECT Country FROM countries");
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());;
        }
        while (true) {
            try {
                if (!customerCountrySet.next()) break;
            } catch (SQLException throwables) {
                System.out.println(throwables.getMessage());;
            }
            try {
                customerCountryBox.getItems().addAll(customerCountrySet.getString("Country"));
            } catch (SQLException throwables) {
                System.out.println(throwables.getMessage());;
            }
        }
    }

    /** Shows divisions when user clicks on the customer country box */
    public void clickedCustomerCountryBox(ActionEvent actionEvent) throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        customerDivisionBox.getItems().clear(); //clears items from previous selection

        String customerCountry = (String) customerCountryBox.getValue();
        int customerCountryId = -1;

        System.out.print("User selected: " + customerCountry);

        ResultSet countryIdResultSet = connection.createStatement().executeQuery(
                "SELECT Country_ID FROM countries WHERE Country='" + customerCountry + "'");

        while(countryIdResultSet.next()) {
            customerCountryId = countryIdResultSet.getInt("Country_ID");
            System.out.print(" with Country_ID of " + customerCountryId + "\n");
        }

        /** shows user available divisions and sets new customer's division to user's selection */
        ResultSet customerDivisionSet = connection.createStatement().executeQuery(
                "SELECT Division FROM first_level_divisions\n" +
                        "WHERE COUNTRY_ID=" + customerCountryId);
        while (customerDivisionSet.next()) {
            customerDivisionBox.getItems().addAll(customerDivisionSet.getString("Division"));
        }

    }

    /** Saves the new user */
    public void saveButtonClicked(ActionEvent actionEvent) throws SQLException {

        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        int customerId = 1;
        String customerName = customerNameField.getText();
        String address = customerAddressField.getText();
        String postalCode = customerPostalCodeField.getText();
        String phoneNumber = customerPhoneNumberField.getText();
        String country = (String) customerCountryBox.getSelectionModel().getSelectedItem();
        String division = (String) customerDivisionBox.getSelectionModel().getSelectedItem();
        int divisionId = 1;
        String userName = User.getUsername();

        /** Parses Customer_IDs and then adds 1 to new customer */
        ResultSet customerIdSet = connection.createStatement().executeQuery("SELECT * FROM customers");
        while (customerIdSet.next()) {
            customerId = (customerIdSet.getInt("Customer_ID") + 1);
        }

        /** Gets division ID */
        ResultSet customerDivisionIdSet = connection.createStatement().executeQuery(
                "SELECT * FROM first_level_divisions\n" +
                        "WHERE Division='" + division + "'");
        while (customerDivisionIdSet.next()) {
            divisionId = (customerDivisionIdSet.getInt("Division_ID"));
        }

        try {
            if(checkForBlanks(customerName, address, postalCode, phoneNumber, country, division)) {

                connection.createStatement().execute("INSERT INTO customers\n" +
                        "VALUES ('" + customerId + "', '" + customerName + "', '" + address + "', '" + postalCode + "', '" + phoneNumber + "', NOW(), '" + userName + "', NOW(), '"  + userName + "', '" + divisionId + "')");

                Customer.populateCustomersList();

                stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
                scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main-view.fxml")));
                stage.setScene(new Scene(scene));
                stage.show();
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** Checks if there are any blank fields before customer can be saved */
    public boolean checkForBlanks (String customerName, String address, String postalCode, String phone, String country, String division) {
        if(customerName.equals("") || address.equals("") || postalCode.equals("") || phone.equals("") ||country == null || division == null) {
            Alerts.error("Error", "Missing customer information", "Make sure no fields are blank and try again");
            return false;
        }
        return true;
    }

    /** Takes user back to main screen */
    public void cancelButtonClicked(ActionEvent actionEvent) throws IOException {
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main-view.fxml")));
        stage.setScene(new Scene(scene));
        stage.show();
    }
}
