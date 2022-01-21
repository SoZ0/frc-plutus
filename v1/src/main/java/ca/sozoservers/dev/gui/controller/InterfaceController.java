package ca.sozoservers.dev.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import ca.sozoservers.dev.gui.GUIManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;

public class InterfaceController implements Initializable {

    public static InterfaceController controller;

    @FXML
    Tab sponsorTab;

    @FXML
    Tab emailingTab;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        controller = this;

        sponsorTab.setContent(GUIManager.getSponsorScene());
        emailingTab.setContent(GUIManager.getEmailingScene());
    }

    public static InterfaceController getInstance(){
        return controller;
    }
}

    