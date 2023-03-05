package models;

import controllers.LoginController;
import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import utilities.Alerts;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {

    /** From appointments table in MySQL database */
    private int appointmentId;
    private String appointmentTitle;
    private String appointmentDescription;
    private String appointmentLocation;
    private String appointmentType;
    private ZonedDateTime appointmentStart;
    private ZonedDateTime appointmentEnd;
    private Date appointmentCreateDate;
    private String appointmentCreatedBy;
    private Timestamp appointmentLastUpdate;
    private String appointmentLastUpdatedBy;
    private int appointmentCustomerId;
    private int appointmentUserId;
    private int appointmentContactId;

    /** fancy start and end datetimes for readabillity */
    private String fancyStart;
    private String fancyEnd;

    /** From customers table in MySQL database (JOIN statement) */
    private String appointmentCustomerName;

    /** From contacts table in MySQL database (JOIN statement) */
    private String appointmentContactName;

    /** used for report three */
    private String activeAppointments;

    /** used for report two */
    private String reportType;
    private String reportMonth;
    private String reportTotal;

    private static ZoneId localZoneId = ZoneId.systemDefault();

    private static ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
    private static ObservableList<Appointment> weeklyAppointments = FXCollections.observableArrayList();
    private static ObservableList<Appointment> monthlyAppointments = FXCollections.observableArrayList();

    private static ObservableList<Appointment> reportTwo = FXCollections.observableArrayList();
    private static ObservableList<Appointment> reportThree = FXCollections.observableArrayList();

    /** Check for upcoming appointments */
    public static Appointment checkForUpcomingAppointment() throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return null;
        }

        Appointment appointmentIn15;

        LocalDateTime timeNow = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = timeNow.atZone(zoneId);
        LocalDateTime localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        LocalDateTime localDateTimePlus15 = localDateTime.plusMinutes(15);

        ResultSet checkAppointmentResultSet = connection.createStatement().executeQuery(
                "SELECT * FROM appointments " +
                        "JOIN users ON appointments.User_ID=users.User_ID " +
                        "WHERE Start BETWEEN '" + localDateTime + "' AND '" + localDateTimePlus15 +
                        "' AND appointments.User_ID=" + User.getUserId());

        if (checkAppointmentResultSet.next()) {
            appointmentIn15 = new Appointment();
            appointmentIn15.setAppointmentId(checkAppointmentResultSet.getInt("Appointment_ID"));
            LocalDateTime startLocal = checkAppointmentResultSet.getTimestamp("Start").toLocalDateTime();
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm a"); //formats date
            appointmentIn15.setFancyStart(startLocal.format(dateFormat));
            return appointmentIn15;
        }
        return null;
    }

    /** Checks if there is a conflict between the requested appt time and existing appt times */
    public static boolean checkIfAppointmentConflict(ZonedDateTime startDateTime, ZonedDateTime endDateTime, int contactId, int customerId) {
        Connection connection = DBConnection.getConnection();

        try {
            ResultSet appointmentTimesResultSet = connection.createStatement().executeQuery("SELECT * FROM appointments "
                    + "WHERE('"+ Timestamp.valueOf(startDateTime.toLocalDateTime()) + "' BETWEEN Start AND End OR '"
                    + endDateTime.toLocalDateTime() + "' BETWEEN Start AND End OR '" +
                    Timestamp.valueOf(startDateTime.toLocalDateTime()) + "'  < Start AND '" + endDateTime.toLocalDateTime() +
                    "' > End) AND (Contact_ID='" + contactId + "' OR Customer_ID='" + customerId + "')");

            //System.out.println(Timestamp.valueOf(startDateTime.toLocalDateTime()));

            if(appointmentTimesResultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error in Appointment.checkIfAppointmentConflict: " + e.getMessage());
        }
        return false;
    }

    /** gets report #1 in main screen */
    public static ObservableList<Appointment> getTypeAndMonthReport() {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return null;
        }

        ObservableList<Appointment> typeAndMonthList = FXCollections.observableArrayList();

        try {
            ResultSet tAMReportSet = connection.createStatement().executeQuery(
                    "SELECT MONTHNAME(`Start`) AS Month, Type, COUNT(*) as Total\n" +
                            "FROM appointments\n" +
                            "GROUP BY MONTHNAME(`Start`), Type\n" +
                            "ORDER BY Month DESC");
            while(tAMReportSet.next()) {
                Appointment typeAndMonth = new Appointment();
                typeAndMonth.setReportType(tAMReportSet.getString("Type"));
                typeAndMonth.setReportMonth(tAMReportSet.getString("Month"));
                typeAndMonth.setReportTotal(tAMReportSet.getString("Total"));
                typeAndMonthList.add(typeAndMonth);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return typeAndMonthList;
    }

    /** populates report #3 in main screen */
    public static void populateReportThree() {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }
        getReportThree().clear();
        try {
            ResultSet ccrReportSet = connection.createStatement().executeQuery(
                    "SELECT Customer_Name, COUNT(Appointment_ID) AS Active_Appointments\n" +
                            "FROM appointments, customers\n" +
                            "WHERE appointments.Customer_ID=customers.Customer_ID\n" +
                            "GROUP BY Customer_Name");

            while(ccrReportSet.next()){
                Appointment appointment = new Appointment();
                appointment.setAppointmentCustomerName(ccrReportSet.getString("Customer_Name"));
                appointment.setActiveAppointments(ccrReportSet.getString("Active_Appointments"));
                getReportThree().add(appointment);
            }
        } catch (SQLException e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** populates report 2 in main screen */
    public static void populateReportTwo() {

        try {

            Connection connection = DBConnection.getConnection();

            if (DBConnection.getConnection() == null) {
                return;
            }
            getReportTwo().clear();
            ResultSet appointmentSet = connection.createStatement().executeQuery("SELECT * FROM appointments JOIN customers ON appointments.Customer_ID=customers.Customer_ID\n" +
                    "JOIN users ON appointments.User_ID=users.User_ID\n" +
                    "JOIN contacts ON appointments.Contact_ID=contacts.Contact_ID\n" +
                    "ORDER BY Contact_Name");

            while(appointmentSet.next()){
                Appointment appointment = new Appointment();
                appointment.setAppointmentContactName(appointmentSet.getString("Contact_Name"));
                appointment.setAppointmentId(appointmentSet.getInt("Appointment_ID"));
                appointment.setAppointmentTitle(appointmentSet.getString("Title"));
                appointment.setAppointmentDescription(appointmentSet.getString("Description"));
                appointment.setAppointmentType(appointmentSet.getString("Type"));
                LocalDateTime startLDT = appointmentSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endLDT = appointmentSet.getTimestamp("End").toLocalDateTime();
                appointment.setAppointmentCustomerId(appointmentSet.getInt("Customer_ID"));

                /** Converts time back to UTC */
                ZonedDateTime startUTC = startLDT.atZone(ZoneId.of("UTC"));
                ZonedDateTime endUTC = endLDT.atZone(ZoneId.of("UTC"));

                /** Sets the user's start and end DATE & TIME to UTC for background processes */
                appointment.setAppointmentStart(startUTC);
                appointment.setAppointmentEnd(endUTC);

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm a"); //formats date

                String fancyStart = startLDT.format(dateFormat);
                String fancyEnd = endLDT.format(dateFormat);

                appointment.setFancyStart(fancyStart);
                appointment.setFancyEnd(fancyEnd);

                getReportTwo().add(appointment);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** gets all appts */
    public static ObservableList<Appointment> getAllAppointments(){
        return allAppointments;
    }
    /** populates the appts table in main screen */
    public static void populateAppointmentsList() {

        try {

            Connection connection = DBConnection.getConnection();

            if (DBConnection.getConnection() == null) {
                return;
            }

            getAllAppointments().clear();
            ResultSet appointmentSet = connection.createStatement().executeQuery("SELECT * FROM appointments JOIN customers ON appointments.Customer_ID=customers.Customer_ID\n" +
                    "JOIN users ON appointments.User_ID=users.User_ID\n" +
                    "JOIN contacts ON appointments.Contact_ID=contacts.Contact_ID\n" +
                    "ORDER BY Appointment_ID");

            while(appointmentSet.next()){
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(appointmentSet.getInt("Appointment_ID"));
                appointment.setAppointmentTitle(appointmentSet.getString("Title"));
                appointment.setAppointmentDescription(appointmentSet.getString("Description"));
                appointment.setAppointmentLocation(appointmentSet.getString("Location"));
                appointment.setAppointmentContactName(appointmentSet.getString("Contact_Name"));
                appointment.setAppointmentType(appointmentSet.getString("Type"));
                LocalDateTime startLDT = appointmentSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endLDT = appointmentSet.getTimestamp("End").toLocalDateTime();
                appointment.setAppointmentCustomerId(appointmentSet.getInt("Customer_ID"));
                appointment.setAppointmentUserId(appointmentSet.getInt("User_ID"));
                appointment.setAppointmentContactId(appointmentSet.getInt("Contact_ID"));
                appointment.setAppointmentCustomerName(appointmentSet.getString("Customer_Name"));

                /** Converts time back to UTC */
                ZonedDateTime startUTC = startLDT.atZone(ZoneId.of("UTC"));
                ZonedDateTime endUTC = endLDT.atZone(ZoneId.of("UTC"));

                /** Sets the user's start and end DATE & TIME to UTC for background processes */
                appointment.setAppointmentStart(startUTC);
                appointment.setAppointmentEnd(endUTC);

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm a"); //formats date

                String fancyStart = startLDT.format(dateFormat);
                String fancyEnd = endLDT.format(dateFormat);

                appointment.setFancyStart(fancyStart);
                appointment.setFancyEnd(fancyEnd);

                getAllAppointments().add(appointment);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    /** populates appt table in main screen with weekly appts when radio button is clicked */
    public static void populateWeeklyAppointmentsList() {

        try {

            Connection connection = DBConnection.getConnection();

            if (DBConnection.getConnection() == null) {
                return;
            }

            getWeeklyAppointments().clear();
            ResultSet appointmentSet = connection.createStatement().executeQuery("SELECT * FROM appointments JOIN customers ON appointments.Customer_ID=customers.Customer_ID\n" +
                    "JOIN users ON appointments.User_ID=users.User_ID\n" +
                    "JOIN contacts ON appointments.Contact_ID=contacts.Contact_ID\n" +
                    "WHERE START BETWEEN NOW() AND (SELECT ADDDATE(NOW(), INTERVAL 7 DAY))");

            while(appointmentSet.next()){
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(appointmentSet.getInt("Appointment_ID"));
                appointment.setAppointmentTitle(appointmentSet.getString("Title"));
                appointment.setAppointmentDescription(appointmentSet.getString("Description"));
                appointment.setAppointmentLocation(appointmentSet.getString("Location"));
                appointment.setAppointmentContactName(appointmentSet.getString("Contact_Name"));
                appointment.setAppointmentType(appointmentSet.getString("Type"));
                LocalDateTime startLDT = appointmentSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endLDT = appointmentSet.getTimestamp("End").toLocalDateTime();
                appointment.setAppointmentCustomerId(appointmentSet.getInt("Customer_ID"));
                appointment.setAppointmentUserId(appointmentSet.getInt("User_ID"));
                appointment.setAppointmentContactId(appointmentSet.getInt("Contact_ID"));
                appointment.setAppointmentCustomerName(appointmentSet.getString("Customer_Name"));

                /** Converts time back to UTC */
                ZonedDateTime startUTC = startLDT.atZone(ZoneId.of("UTC"));
                ZonedDateTime endUTC = endLDT.atZone(ZoneId.of("UTC"));

                /** Sets the user's start and end DATE & TIME to UTC for background processes */
                appointment.setAppointmentStart(startUTC);
                appointment.setAppointmentEnd(endUTC);

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm a"); //formats date

                String fancyStart = startLDT.format(dateFormat);
                String fancyEnd = endLDT.format(dateFormat);

                appointment.setFancyStart(fancyStart);
                appointment.setFancyEnd(fancyEnd);

                getWeeklyAppointments().add(appointment);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    /** populates the appt table with appts this month in main screen when radio button is clicked */
    public static void populateMonthlyAppointmentsList() {

        try {

            Connection connection = DBConnection.getConnection();

            if (DBConnection.getConnection() == null) {
                return;
            }

            getMonthlyAppointments().clear();
            ResultSet appointmentSet = connection.createStatement().executeQuery("SELECT * FROM appointments JOIN customers ON appointments.Customer_ID=customers.Customer_ID\n" +
                    "JOIN users ON appointments.User_ID=users.User_ID\n" +
                    "JOIN contacts ON appointments.Contact_ID=contacts.Contact_ID\n" +
                    "WHERE start between (SELECT DATE_ADD(DATE_ADD(LAST_DAY(current_date()), INTERVAL 1 DAY), INTERVAL - 1 MONTH)) and DATE_ADD(LAST_DAY(current_date()), INTERVAL 1 DAY)");

            while(appointmentSet.next()){
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(appointmentSet.getInt("Appointment_ID"));
                appointment.setAppointmentTitle(appointmentSet.getString("Title"));
                appointment.setAppointmentDescription(appointmentSet.getString("Description"));
                appointment.setAppointmentLocation(appointmentSet.getString("Location"));
                appointment.setAppointmentContactName(appointmentSet.getString("Contact_Name"));
                appointment.setAppointmentType(appointmentSet.getString("Type"));
                LocalDateTime startLDT = appointmentSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endLDT = appointmentSet.getTimestamp("End").toLocalDateTime();
                appointment.setAppointmentCustomerId(appointmentSet.getInt("Customer_ID"));
                appointment.setAppointmentUserId(appointmentSet.getInt("User_ID"));
                appointment.setAppointmentContactId(appointmentSet.getInt("Contact_ID"));
                appointment.setAppointmentCustomerName(appointmentSet.getString("Customer_Name"));

                /** Converts time back to UTC */
                ZonedDateTime startUTC = startLDT.atZone(ZoneId.of("UTC"));
                ZonedDateTime endUTC = endLDT.atZone(ZoneId.of("UTC"));

                /** Sets the user's start and end DATE & TIME to UTC for background processes */
                appointment.setAppointmentStart(startUTC);
                appointment.setAppointmentEnd(endUTC);

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm a"); //formats date

                String fancyStart = startLDT.format(dateFormat);
                String fancyEnd = endLDT.format(dateFormat);

                appointment.setFancyStart(fancyStart);
                appointment.setFancyEnd(fancyEnd);

                getMonthlyAppointments().add(appointment);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** Removes appointment info from database */
    public static void deleteAppointment(Appointment appointment) throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (DBConnection.getConnection() == null) {
            return;
        }

        connection.createStatement().execute("DELETE FROM appointments WHERE Appointment_ID=" + appointment.getAppointmentId());
        populateAppointmentsList();
        populateWeeklyAppointmentsList();
        populateMonthlyAppointmentsList();
        Alerts.information(
                "Success", "Deletion completed","The " +
                        appointment.getAppointmentDescription() + " appointment with ID of " +
                        appointment.getAppointmentId() + " has been successfully deleted");
    }

    /** gets weekly appts */
    public static ObservableList<Appointment> getWeeklyAppointments() {
        return weeklyAppointments;
    }
    /** sets weekly appts */
    public static void setWeeklyAppointments(ObservableList<Appointment> weeklyAppointments) {
        Appointment.weeklyAppointments = weeklyAppointments;
    }
    /** gets monthly appts */
    public static ObservableList<Appointment> getMonthlyAppointments() {
        return monthlyAppointments;
    }
    /** sets monthly appts */
    public static void setMonthlyAppointments(ObservableList<Appointment> monthlyAppointments) {
        Appointment.monthlyAppointments = monthlyAppointments;
    }
    /** gets report 3 */
    public static ObservableList<Appointment> getReportThree() {
        return reportThree;
    }
    /** sets report 3 */
    public static void setReportThree(ObservableList<Appointment> reportThree) {
        Appointment.reportThree = reportThree;
    }
    /** gets report 2 */
    public static ObservableList<Appointment> getReportTwo() {
        return reportTwo;
    }
    /** sets report 2 */
    public static void setReportTwo(ObservableList<Appointment> reportTwo) {
        Appointment.reportTwo = reportTwo;
    }
    /** gets appt ID */
    public int getAppointmentId() {
        return appointmentId;
    }
    /** sets appt ID */
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }
    /** gets appt title */
    public String getAppointmentTitle() {
        return appointmentTitle;
    }
    /** sets appt title */
    public void setAppointmentTitle(String appointmentTitle) {
        this.appointmentTitle = appointmentTitle;
    }
    /** gets appt description */
    public String getAppointmentDescription() {
        return appointmentDescription;
    }
    /** sets appt description */
    public void setAppointmentDescription(String appointmentDescription) {
        this.appointmentDescription = appointmentDescription;
    }
    /** gets appt location */
    public String getAppointmentLocation() {
        return appointmentLocation;
    }
    /** sets appt location */
    public void setAppointmentLocation(String appointmentLocation) {
        this.appointmentLocation = appointmentLocation;
    }
    /** gets appt type */
    public String getAppointmentType() {
        return appointmentType;
    }
    /** sets appt type */
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }
    /** gets appt start */
    public ZonedDateTime getAppointmentStart() {
        return appointmentStart;
    }
    /** sets appt start */
    public void setAppointmentStart(ZonedDateTime appointmentStart) {
        this.appointmentStart = appointmentStart;
    }
    /** gets appt end */
    public ZonedDateTime getAppointmentEnd() {
        return appointmentEnd;
    }
    /** sets appt end */
    public void setAppointmentEnd(ZonedDateTime appointmentEnd) {
        this.appointmentEnd = appointmentEnd;
    }
    /** gets appt create date */
    public Date getAppointmentCreateDate() {
        return appointmentCreateDate;
    }
    /** sets appt create date */
    public void setAppointmentCreateDate(Date appointmentCreateDate) {
        this.appointmentCreateDate = appointmentCreateDate;
    }
    /** gets appt created by */
    public String getAppointmentCreatedBy() {
        return appointmentCreatedBy;
    }
    /** sets appt created by */
    public void setAppointmentCreatedBy(String appointmentCreatedBy) {
        this.appointmentCreatedBy = appointmentCreatedBy;
    }
    /** gets appt last update */
    public Timestamp getAppointmentLastUpdate() {
        return appointmentLastUpdate;
    }
    /** sets appt last update */
    public void setAppointmentLastUpdate(Timestamp appointmentLastUpdate) {
        this.appointmentLastUpdate = appointmentLastUpdate;
    }
    /** gets appt last updated by */
    public String getAppointmentLastUpdatedBy() {
        return appointmentLastUpdatedBy;
    }
    /** sets appt last updated by */
    public void setAppointmentLastUpdatedBy(String appointmentLastUpdatedBy) {
        this.appointmentLastUpdatedBy = appointmentLastUpdatedBy;
    }
    /** gets appt customer ID */
    public int getAppointmentCustomerId() {
        return appointmentCustomerId;
    }
    /** sets appt customer ID */
    public void setAppointmentCustomerId(int appointmentCustomerId) {
        this.appointmentCustomerId = appointmentCustomerId;
    }
    /** get appointment user id */
    public int getAppointmentUserId() {
        return appointmentUserId;
    }
    /** set appt user id */
    public void setAppointmentUserId(int appointmentUserId) {
        this.appointmentUserId = appointmentUserId;
    }
    /** get appointment contact id */
    public int getAppointmentContactId() {
        return appointmentContactId;
    }
    /** set appt contact id */
    public void setAppointmentContactId(int appointmentContactId) {
        this.appointmentContactId = appointmentContactId;
    }
    /** get appointment customer name */
    public String getAppointmentCustomerName() {
        return appointmentCustomerName;
    }
    /** set appt customer name */
    public void setAppointmentCustomerName(String appointmentCustomerName) {
        this.appointmentCustomerName = appointmentCustomerName;
    }
    /** get appointment contact name */
    public String getAppointmentContactName() {
        return appointmentContactName;
    }
    /** set appt contact name */
    public void setAppointmentContactName(String appointmentContactName) {
        this.appointmentContactName = appointmentContactName;
    }
    /** set appt id */
    public Appointment(int appointmentId) {
        this.appointmentId = appointmentId;
    }
    /** appt constructor */
    public Appointment() {
    }
    /** get appt  start */
    public String getFancyStart() {
        return fancyStart;
    }
    /** set appt fancy start */
    public void setFancyStart(String fancyStart) {
        this.fancyStart = fancyStart;
    }
    /** get appointment fancy end */
    public String getFancyEnd() {
        return fancyEnd;
    }
    /** set appt fancy end */
    public void setFancyEnd(String fancyEnd) {
        this.fancyEnd = fancyEnd;
    }
    /** get active appointment  */
    public String getActiveAppointments() {
        return activeAppointments;
    }
    /** set active appt  */
    public void setActiveAppointments(String activeAppointments) {
        this.activeAppointments = activeAppointments;
    }
    /** get appointment report type */
    public String getReportType() {
        return reportType;
    }
    /** set appt report type */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    /** get appointment report month */
    public String getReportMonth() {
        return reportMonth;
    }
    /** set appt report month */
    public void setReportMonth(String reportMonth) {
        this.reportMonth = reportMonth;
    }
    /** get appointment report total */
    public String getReportTotal() {
        return reportTotal;
    }
    /** set appt report total */
    public void setReportTotal(String reportTotal) {
        this.reportTotal = reportTotal;
    }
}
