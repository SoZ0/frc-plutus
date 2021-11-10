package ca.sozoservers.dev.gui;

import java.io.IOException;

import ca.sozoservers.dev.core.ErrorManager;
import ca.sozoservers.dev.core.FileManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class GUIManager extends Application {

    public static void run(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(ErrorManager::throwError);
        stage.setTitle("Bot Interface");
        stage.setScene(new Scene(FXMLLoader.load(FileManager.getResource("fxml/Interface.fxml"))));
        stage.setResizable(false);
        stage.setOnCloseRequest(window -> {
            System.exit(0);
        });
        stage.show();
    }

    public static TabPane getSponsorScene(){
        try {
            return FXMLLoader.load(FileManager.getResource("fxml/Sponsors.fxml"));
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    public static TabPane getEmailingScene(){
        try {
            return FXMLLoader.load(FileManager.getResource("fxml/Emailing.fxml"));
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }
    
}
