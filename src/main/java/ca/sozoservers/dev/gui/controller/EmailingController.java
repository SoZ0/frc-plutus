package ca.sozoservers.dev.gui.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import ca.sozoservers.dev.config.Config;
import ca.sozoservers.dev.config.Config.Attachment;
import ca.sozoservers.dev.config.Config.Body;
import ca.sozoservers.dev.config.Config.EmailConfig;
import ca.sozoservers.dev.config.Config.Sponsor;
import ca.sozoservers.dev.config.Config.SubjectLine;
import ca.sozoservers.dev.core.FileManager;
import ca.sozoservers.dev.core.MailManager;
import ca.sozoservers.dev.core.SheetsManager;
import ca.sozoservers.dev.core.SheetsManager.SheetID;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class EmailingController implements Initializable {

    public static EmailingController controller;
    private SponsorsContorller sponsorsContorller;
    public static boolean sendingEmails = false;

    @FXML
    ComboBox<SubjectLine> emailingSubjectLine;

    @FXML
    ComboBox<Body> emailingBody;

    @FXML
    ComboBox<Attachment> emailingAttachment;

    @FXML
    TextField emailingName;

    @FXML
    TextField emailingEmail;

    @FXML
    Label emailingEmailsSent;

    @FXML
    Label emailingEmailsLeft;

    @FXML
    Label emailingErrorText;

    @FXML
    Button startButton;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        controller = this;
        sponsorsContorller = SponsorsContorller.getInstance();

        addSubjectLine(new SubjectLine("No Subject Line", " "));
        addBody(new Body("No Body", " "));
        addAttachment(new Attachment("No Attachment", null));

        try {
            HashMap<String, File> subjects = FileManager.getFilesInDirectory("subjectlines");
            for (String fileName : subjects.keySet()) 
            {
                File file = subjects.get(fileName);
                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                XWPFDocument  document = new XWPFDocument (fis);
                List<XWPFParagraph> fileData = document.getParagraphs();
                String content = new String();
                for (XWPFParagraph xwpfParagraph : fileData) {
                    content += xwpfParagraph.getText() + "\n";
                }                
               addSubjectLine(new SubjectLine(fileName, content));
               document.close();
            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }

        
        try {
            HashMap<String, File> bodys = FileManager.getFilesInDirectory("emailbody");
            for (String fileName : bodys.keySet()) 
            {
                File file = bodys.get(fileName);
                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                XWPFDocument  document = new XWPFDocument (fis);
                List<XWPFParagraph> fileData = document.getParagraphs();
                String content = new String();
                for (XWPFParagraph xwpfParagraph : fileData) {
                    content += xwpfParagraph.getText() + "\n";
                }
                addBody(new Body(fileName, content));
                document.close();
            }
        } catch (IOException e) { 
            System.out.println(e.getStackTrace());
        }

        HashMap<String, File> attchment = FileManager.getFilesInDirectory("attachments");
        for (String fileName : attchment.keySet()) 
        {
            File file = attchment.get(fileName);
            addAttachment(new Attachment(fileName, file));
        }
    }

    public static EmailingController getInstance(){
        return controller;
    }

    private EmailConfig emailConfig = new EmailConfig();

    public void emailingEndDialog(){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Emailing has been stopped");
        dialog.setHeaderText("Emailing Stopped");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        dialog.show();
    }

    public boolean emailingWarningDialog(String text){
        Alert alert = new Alert(AlertType.WARNING);
        ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        alert.setTitle("Warining");
        alert.getDialogPane().getButtonTypes().add(cancel);
        alert.setHeaderText(text);
        ButtonType result = alert.showAndWait().get(); 
        return !result.getButtonData().equals(ButtonData.CANCEL_CLOSE);
    }

    public void onEmailingStart(ActionEvent event){

        if(sendingEmails){
            startButton.setText("Start");
            sendingEmails = false;
            emailingEndDialog();
            return;
        }

        emailingErrorText.setText("");
        updateEmailStatus();
        emailConfig.setSubjectLine(emailingSubjectLine.getSelectionModel().getSelectedItem());
        emailConfig.setBody(emailingBody.getSelectionModel().getSelectedItem());
        emailConfig.setAttachment(emailingAttachment.getSelectionModel().getSelectedItem());
        if(!emailConfig.isReady()){
            emailingErrorText.setText("SUBJECT LINE, BODY, OR ATTACHMENT IS NOT SET");
            return;
        }

        if(emailingSubjectLine.getSelectionModel().getSelectedIndex() == 0){
            if(!emailingWarningDialog("Warning! No Subject Line Set!")) return;
        }

        if(emailingBody.getSelectionModel().getSelectedIndex() == 0){
            if(!emailingWarningDialog("Warning! No Body Set!")) return;
        }

        if(emailingAttachment.getSelectionModel().getSelectedIndex() == 0){
            if(!emailingWarningDialog("Warning! No Attachment Set!")) return;
        }

        if(!Config.EmailInfo.comparePassword(passwordDialog().getBytes())){
            emailingWarningDialog("Wrong password!");
            return;
        }

        startButton.setText("Stop");

        sendEmails();
    }

    private void sendEmails(){
        sendingEmails = true;
        CopyOnWriteArrayList<Sponsor> list = new CopyOnWriteArrayList<>(sponsorsContorller.list_table.getItems());
        new Thread(){
            public void run() {
                for (Sponsor sponsor : list) {                    
                    if(!sendingEmails) break;

                    if(sponsor.contacted) continue;

                    int row = list.indexOf(sponsor) + 1;

                    updateCurrentlyEmailing(sponsor.email, sponsor.name);
                    try {
                        emailConfig.setSponsor(sponsor);
                        emailConfig.setTo(new InternetAddress(sponsor.email));
                        ExecutorService threadpool = Executors.newCachedThreadPool();
                        threadpool.submit(() ->MailManager.sendEmail(emailConfig));
                        sponsor.contacted = true;
                        sponsor.notes = "Email sent successfully";
                        SheetsManager.updateRowSheet(sponsor.toRowData(), row, SheetID.Sponsors);
                    } catch (AddressException e) {
                        sponsor.notes = "Invalid Address - "+e.getMessage();
                    }
                    updateEmailStatus();
                }
                updateCurrentlyEmailing("", "");
                onEmailingStart(null);
            };
        }.run();
    }

    synchronized private void updateCurrentlyEmailing(String email, String name){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                emailingEmail.setText(email);
                emailingName.setText(name);
            }
        });
    }

    public String passwordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Password");
        dialog.setHeaderText("A password is required before sending any emails");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField pwd = new PasswordField();
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(new Label("Password: "), pwd);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return pwd.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return "";
    }

    private void updateEmailStatus(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sponsorsContorller.refreshSponsorList(null);
                TableView<Sponsor> sponsorTable = sponsorsContorller.list_table;
                int total = sponsorTable.getItems().size();
                int totalContacted = sponsorTable.getItems().filtered(spon -> spon.contacted).size();
        
                emailingEmailsLeft.setText(String.valueOf(total-totalContacted));
                emailingEmailsSent.setText(String.valueOf(totalContacted));
            }
        }); 
    }

    private void addSubjectLine(SubjectLine subj){
        emailingSubjectLine.getItems().add(subj);
    }

    private void addBody(Body body){
        emailingBody.getItems().add(body);
    }

    private void addAttachment(Attachment attch){
        emailingAttachment.getItems().add(attch);
    }    
}

    