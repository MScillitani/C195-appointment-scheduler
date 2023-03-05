package models;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Setters and getters (and constructors for Customer) */

public class Customer {
    private int customerId;
    private String customerName;
    private String customerAddress;
    private String customerPostalCode;
    private String customerPhone;
    private LocalDateTime customerCreateDate;
    private String customerCreatedBy;
    private Timestamp customerLastUpdate;
    private String customerLastUpdatedBy;
    private int customerDivisionId;
    private String customerDivision;

    private int customerCountryId;
    private int customerAddressId;
    private String customerCountry;

    private boolean active;
    public static ObservableList<Appointment> associatedAppointments = FXCollections.observableArrayList();

    private static ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    public static ObservableList<Customer> getAllCustomers() {
        return allCustomers;
    }

    /** Populates customers list */
    public static void populateCustomersList() {
        try {
            Connection connection = DBConnection.getConnection();

            if (DBConnection.getConnection() == null) {
                return;
            }

            getAllCustomers().clear();
            ResultSet customerSet = connection.createStatement().executeQuery(
                    "SELECT * FROM customers JOIN first_level_divisions ON customers.Division_ID=first_level_divisions.Division_ID\n" +
                            "JOIN countries ON first_level_divisions.COUNTRY_ID=countries.Country_ID\n" +
                            "ORDER BY Customer_ID");

            while(customerSet.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(customerSet.getInt("Customer_ID"));
                customer.setCustomerName(customerSet.getString("Customer_Name"));
                customer.setCustomerAddress(customerSet.getString("Address"));
                customer.setCustomerPostalCode(customerSet.getString("Postal_Code"));
                customer.setCustomerPhone(customerSet.getString("Phone"));
                customer.setLastUpdate(customerSet.getTimestamp("Last_Update"));
                customer.setLastUpdatedBy(customerSet.getString("Last_Updated_By"));
                customer.setDivisionId(customerSet.getInt("Division_ID"));
                customer.setCustomerCountry(customerSet.getString("Country"));
                customer.setCustomerDivision(customerSet.getString("Division"));

                getAllCustomers().add(customer);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    /** deletes selected customer from main screen customers tab */
    public static void deleteCustomer(Customer customer) throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }
        connection.createStatement().execute("DELETE FROM appointments WHERE Customer_ID=" + customer.getCustomerId());
        connection.createStatement().execute("DELETE FROM customers WHERE Customer_ID=" + customer.getCustomerId());
        allCustomers.remove(customer);

        /** Easiest way to refresh tables is to just call the populate methods here --
         *  otherwise, the user cannot see if any relevant appts were deleted.
         *  In each populate method, the lists are cleared before being populated with latest data */
        Appointment.populateAppointmentsList();
        Appointment.populateWeeklyAppointmentsList();
        Appointment.populateMonthlyAppointmentsList();

        //report one does NOT need to be called here -- it is populated in the MainScreenView class
        Appointment.populateReportTwo();
        Appointment.populateReportThree();
    }
    /** gets customer id */
    public int getCustomerId() {
        return customerId;
    }
    /** sets customer id */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    /** gets customer country id */
    public int getCountryId() {
        return customerCountryId;
    }
    /** sets customer country id */
    public void setCountryId(int countryId) {
        this.customerCountryId = countryId;
    }
    /** gets customer address id */
    public int getAddressId() {
        return customerAddressId;
    }
    /** sets customer address id */
    public void setAddressId(int addressId) {
        this.customerAddressId = addressId;
    }
    /** gets customer  */
    public String getCustomerName() {
        return customerName;
    }
    /** sets customer name */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    /** gets customer address */
    public String getCustomerAddress() {
        return customerAddress;
    }
    /** sets customer address */
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
    /** gets customer country */
    public String getCustomerCountry() {
        return customerCountry;
    }
    /** sets customer country */
    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }
    /** gets customer zip */
    public String getCustomerPostalCode() {
        return customerPostalCode;
    }
    /** sets customer zip */
    public void setCustomerPostalCode(String postalCode) {
        this.customerPostalCode = postalCode;
    }
    /** gets customer phone */
    public String getCustomerPhone() {
        return customerPhone;
    }
    /** sets customer phone */
    public void setCustomerPhone(String customerPhoneNumber) {
        this.customerPhone = customerPhoneNumber;
    }
    /** gets customer active status */
    public boolean isActive() {
        return active;
    }
    /** sets customer active status */
    public void setActive(boolean active) {
        this.active = active;
    }
    /** gets customer create date */
    public LocalDateTime getCreateDate() {
        return customerCreateDate;
    }
    /** sets customer create date */
    public void setCreateDate(LocalDateTime createDate) {
        this.customerCreateDate = createDate;
    }
    /** gets customer created by */
    public String getCreatedBy() {
        return customerCreatedBy;
    }
    /** sets customer created by */
    public void setCreatedBy(String createdBy) {
        this.customerCreatedBy = createdBy;
    }
    /** gets customer last update */
    public Timestamp getLastUpdate() {
        return customerLastUpdate;
    }
    /** sets customer last update */
    public void setLastUpdate(Timestamp lastUpdate) {
        this.customerLastUpdate = lastUpdate;
    }
    /** gets customer lsat updated by */
    public String getLastUpdatedBy() {
        return customerLastUpdatedBy;
    }
    /** sets customer last updated by */
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.customerLastUpdatedBy = lastUpdatedBy;
    }
    /** gets appointments */
    public static ObservableList<Appointment> getAssociatedAppointments() {
        return associatedAppointments;
    }

    /** Customer constructor */
    public Customer() {
    }
    /** gets customer division id */
    public int getDivisionId() {
        return customerDivisionId;
    }
    /** sets customer division id */
    public void setDivisionId(int divisionId) {
        this.customerDivisionId = divisionId;
    }
    /** gets customer division */
    public String getCustomerDivision() {
        return customerDivision;
    }
    /** sets customer division */
    public void setCustomerDivision(String customerDivision) {
        this.customerDivision = customerDivision;
    }
}