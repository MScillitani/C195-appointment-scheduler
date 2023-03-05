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

/** Gets modify customer from the MainController class
 *  Need this to set the text boxes in this screen to the relevent customer
 *  info selected by the user */
import static controllers.MainController.modifyCustomer;
/** modifies pre-existing customer that user selected from the main screen */
public class ModifyCustomerController implements Initializable {

    public TextField customerIdField;
    public TextField customerNameField;
    public TextField customerAddressField;
    public TextField customerPostalCodeField;
    public TextField customerPhoneNumberField;
    public ComboBox customerDivisionBox;
    public ComboBox customerCountryBox;

    Parent scene;
    Stage stage;

    /** Initializes fields with selected customer data */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        /** Sets the combo boxes EXCEPT for country and division (see bottom of this method) */
        customerIdField.setText(String.valueOf(modifyCustomer.getCustomerId()));
        customerNameField.setText(modifyCustomer.getCustomerName());
        customerAddressField.setText(modifyCustomer.getCustomerAddress());
        customerPostalCodeField.setText(modifyCustomer.getCustomerPostalCode());
        customerPhoneNumberField.setText(modifyCustomer.getCustomerPhone());

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

        /** The below try/catch takes the modifyCustomer's division ID and uses it to find the relevant division
         *  to populate the division combo box */
        String customerDivision = "";

        try {
            ResultSet customerDivisionResultSet = connection.createStatement().executeQuery("SELECT Division FROM first_level_divisions\n" +
                    "WHERE Division_ID='" + modifyCustomer.getDivisionId() + "'");
            while(customerDivisionResultSet.next()) {
                customerDivision = customerDivisionResultSet.getString("Division");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        /** Sets the country and division combo boxes */
        customerCountryBox.getSelectionModel().select(modifyCustomer.getCustomerCountry());
        customerDivisionBox.getSelectionModel().select(customerDivision);

        /** Fills the division box -- otherwise it starts out blank until user selects a country */

        String customerCountry = (String) customerCountryBox.getValue();
        int customerCountryId = -1;

        ResultSet countryIdResultSet = null;
        try {
            countryIdResultSet = connection.createStatement().executeQuery(
                    "SELECT Country_ID FROM countries WHERE Country='" + customerCountry + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        while(true) {
            try {
                if (!countryIdResultSet.next()) break;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            try {
                customerCountryId = countryIdResultSet.getInt("Country_ID");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        /** shows user available divisions and sets new customer's division to user's selection */
        ResultSet customerDivisionSet = null;
        try {
            customerDivisionSet = connection.createStatement().executeQuery(
                    "SELECT Division FROM first_level_divisions\n" +
                            "WHERE COUNTRY_ID=" + customerCountryId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        while (true) {
            try {
                if (!customerDivisionSet.next()) break;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            try {
                customerDivisionBox.getItems().addAll(customerDivisionSet.getString("Division"));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
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
        customerDivisionBox.getSelectionModel().clearSelection();

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
    /** Saves the revisions made to customer */
    public void saveButtonClicked(ActionEvent actionEvent) throws SQLException {

        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        String customerName = customerNameField.getText();
        String address = customerAddressField.getText();
        String postalCode = customerPostalCodeField.getText();
        String phoneNumber = customerPhoneNumberField.getText();
        String country = (String) customerCountryBox.getSelectionModel().getSelectedItem();
        String division = (String) customerDivisionBox.getSelectionModel().getSelectedItem();
        int divisionId = 1;
        String userName = User.getUsername();

        /** Gets division ID */
        ResultSet customerDivisionIdSet = connection.createStatement().executeQuery(
                "SELECT * FROM first_level_divisions\n" +
                        "WHERE Division='" + division + "'");
        while (customerDivisionIdSet.next()) {
            divisionId = (customerDivisionIdSet.getInt("Division_ID"));
        }

        try {
            if(checkForBlanks(customerName, address, postalCode, phoneNumber, country, division)) {

                connection.createStatement().execute("UPDATE customers SET Customer_Name='" + customerName +
                        "', Address='" + address + "', Postal_Code='" + postalCode + "', Phone='" + phoneNumber +
                        "', Last_Update=NOW(), Last_Updated_By='"  + userName + "', Division_ID='" + divisionId +
                        "' WHERE Customer_ID='" + modifyCustomer.getCustomerId() + "'");

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
    /** Makes sure there are no blank fields */
    public boolean checkForBlanks (String customerName, String address, String postalCode, String phone, String country, String division) {
        if(customerName.equals("") || address.equals("") || postalCode.equals("") || phone.equals("") ||country == null || division == null) {
            Alerts.error("Error", "Missing customer information", "Make sure no fields are blank and try again");
            return false;
        }
        return true;
    }
    /** sends user back to main screen */
    public void cancelButtonClicked(ActionEvent actionEvent) throws IOException {
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main-view.fxml")));
        stage.setScene(new Scene(scene));
        stage.show();
    }
}
