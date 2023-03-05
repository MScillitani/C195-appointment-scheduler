package main;

import database.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.time.*;
import java.util.Locale;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login-view.fxml")));
        Scene loginScene = new Scene(root);
        if (Locale.getDefault().toString().contains("fr")) {
            stage.setTitle("Demande de planification");
        } else {
            stage.setTitle("Scheduling Application");
        }
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        //Locale.setDefault(new Locale("fr")); //for testing if the user locale is working
        try {
            DBConnection.openConnection();
            launch(args);
            DBConnection.closeConnection();
        } catch (Exception e) {
            if (Locale.getDefault().toString().contains("fr")) {
                System.out.println("Erreur: connexion à la base de données");
            } else {
                System.out.println(e.getMessage());
            }
        }
    }
}