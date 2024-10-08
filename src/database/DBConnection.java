package database;

import models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

/** opens, gets, and closes the connection to the MySQL database */
public class DBConnection {

    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER"; // LOCAL
    private static final String driver = "com.mysql.cj.jdbc.Driver"; // Driver reference
    private static final String userName = "sqlUser"; // Username
    private static final String password = "Passw0rd!"; // Password
    public static Connection connection;  // Connection Interface
    public static User activeUser;

    /** opens the databse connection */
    public static void openConnection() {
        try {
            Class.forName(driver); // Locate Driver
            connection = DriverManager.getConnection(jdbcUrl, userName, password); // Reference Connection object
            System.out.println("Connected to server");
        } catch(SQLException | ClassNotFoundException e) {
            if(Locale.getDefault().toString().contains("fr")) {
                System.out.println("Erreur: Échec de la liaison de communication");
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /** gets the pre-existing connection */
    public static Connection getConnection() { return connection; }

    /** closes out the connection */
    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Disconnected from server");
        } catch(Exception e) {
            if(Locale.getDefault().toString().contains("fr")) {
                System.out.println("Erreur: Impossible d'invoquer \"java.sql.Connection.close()\" car \"database.DBConnection.connection\" est nul");
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}