package ca.sozoservers.dev.gui.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;

import ca.sozoservers.dev.config.Config.Attachment;
import ca.sozoservers.dev.config.Config.Body;
import ca.sozoservers.dev.config.Config.EmailConfig;
import ca.sozoservers.dev.config.Config.EmailInfo;
import ca.sozoservers.dev.config.Config.Sponsor;
import ca.sozoservers.dev.config.Config.SubjectLine;
import ca.sozoservers.dev.core.MailManager;
import ca.sozoservers.dev.core.SheetsManager;
import ca.sozoservers.dev.core.SheetsManager.SheetID;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class SponsorsContorller implements Initializable {

    public static SponsorsContorller controller;

    private TableColumn<Sponsor, String> selectedColumn;
    private Sponsor selectedSponsor;
    private EmailingController emailingController;

    @FXML
    Text list_totalContacted;

    @FXML
    Text list_entries;
    
    @FXML
    TableView<Sponsor> list_table;

    @FXML
    TableColumn<Sponsor, Boolean> list_contacted;

    @FXML
    TableColumn<Sponsor, String> list_name;

    @FXML
    TableColumn<Sponsor, String> list_email;

    @FXML
    TableColumn<Sponsor, String> list_phone;

    @FXML
    TableColumn<Sponsor, String> list_website;

    @FXML
    TableColumn<Sponsor, String> list_other;

    @FXML
    TableColumn<Sponsor, String> list_notes;

    /*********************************************/

    @FXML
    RadioButton add_contacted;

    @FXML
    TextField add_name;

    @FXML
    TextField add_email;

    @FXML
    TextField add_phone;

    @FXML
    TextField add_website;

    @FXML
    TextArea add_other;

    @FXML
    Text add_errorText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        emailingController = EmailingController.getInstance();
        list_contacted.setCellValueFactory(new PropertyValueFactory<>("contacted"));
        list_contacted.setCellFactory(createContactedCellFactory());
        list_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        list_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        list_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        list_website.setCellValueFactory(new PropertyValueFactory<>("website"));
        list_other.setCellValueFactory(new PropertyValueFactory<>("other"));
        list_notes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        list_table.setOnContextMenuRequested(createSponsorTableContextMenu());
        
    }

    public static SponsorsContorller getInstance(){
        return controller;
    }

    public void refreshSponsorList(ActionEvent event){
        ExecutorService threadpool = Executors.newCachedThreadPool();
        threadpool.submit(() ->{
            list_contacted.setCellFactory(createContactedCellFactory());
            Sheet sheet = SheetsManager.readSheet("Contact-Info", Arrays.asList("A2:G"), true, SheetID.Sponsors);
            GridData gridData = sheet.getData().get(0);
            list_table.getItems().clear();
            for (RowData rowData : gridData.getRowData()) {
                Sponsor sponsor = Sponsor.createSponsor(rowData);
                addToTable(sponsor);
            }
            int totalContacted = list_table.getItems().filtered(spon -> spon.contacted).size();
            list_totalContacted.setText(totalContacted+"/"+list_table.getItems().size()+" - Contacted");
            list_entries.setText("Total Entries: "+list_table.getItems().size());
        });   
    }

    public void addToTable(Sponsor sponsor){
        list_table.getItems().add(sponsor);
    }

    public void addSponsorSubmit(ActionEvent event){
        if(add_email.getText().isBlank() || add_name.getText().isBlank()){
            add_errorText.setText("EMAIL OR NAME CAN NOT BE BLANK");
            return;
        }

        for (Sponsor sponsor : list_table.getItems()) {
            if(sponsor.email.equalsIgnoreCase(add_email.getText())) {
                add_errorText.setText("EMAIL IS ALREADY IN DATABASE");
                return;
            }
        }

        Sponsor sponsor = new Sponsor(){{
            contacted = add_contacted.isSelected();
            name = add_name.getText();
            email = add_email.getText();
            phone = add_phone.getText();
            website = add_website.getText();
            other = add_other.getText();
        }};

        SheetsManager.writeSheet(sponsor.toRowData(), SheetID.Sponsors);

        add_name.setText("");
        add_email.setText("");
        add_phone.setText("");
        add_website.setText("");
        add_other.setText("");
        add_errorText.setText("");
        add_contacted.setSelected(false);
        refreshSponsorList(null);
    }
    private static Callback<TableColumn<Sponsor, Boolean>, TableCell<Sponsor, Boolean>> createContactedCellFactory(){
        return new Callback<TableColumn<Sponsor, Boolean>, TableCell<Sponsor, Boolean>>(){
            @Override
            public TableCell<Sponsor, Boolean> call(TableColumn<Sponsor, Boolean> param) {
                return new TableCell<Sponsor, Boolean>(){
                    @Override
                    protected void updateItem(Boolean bool, boolean empty) {
                        super.updateItem(bool, empty);    
                        if(empty) {
                            backgroundProperty().set(Background.EMPTY);
                            return;
                        }
                        setText(null);
                        String style = bool ? "green" : "red";
                        backgroundProperty().set(new Background(new BackgroundFill(Paint.valueOf(style), CornerRadii.EMPTY, Insets.EMPTY)));
                    }
                }; 
            }
            
        };
    }

    private void setSelectedColumn(String id){
        if(id != null){
            if(id.equals(list_contacted.getId())){
                TableColumn<Sponsor, String> contactedColumn = new TableColumn<Sponsor, String>();
                contactedColumn.setId(id+"temp");
                selectedColumn = contactedColumn;
                return;
            }else if(id.equals(list_name.getId())){
                selectedColumn = list_name;
                return;
            }else if(id.equals(list_email.getId())){
                selectedColumn = list_email;
                return;
            }else if(id.equals(list_phone.getId())){
                selectedColumn = list_phone;
                return;
            }else if(id.equals(list_website.getId())){
                selectedColumn = list_website;
                return;
            }else if(id.equals(list_other.getId())){
                selectedColumn = list_other;
                return;
            }
        }
        selectedColumn = null;
    }

    private void updateColumnCell(String id, Object content){
        if(id != null && selectedSponsor != null){
            String value = new String(String.valueOf(content));
            if(id.equals(list_contacted.getId()+"temp")){
                selectedSponsor.contacted = Boolean.valueOf(value);
                return;
            }else if(id.equals(list_name.getId())){
                selectedSponsor.name = value;
                return;
            }else if(id.equals(list_email.getId())){
                selectedSponsor.email = value;
                return;
            }else if(id.equals(list_phone.getId())){
                selectedSponsor.phone = value;
                return;
            }else if(id.equals(list_website.getId())){
                selectedSponsor.website = value;
                return;
            }else if(id.equals(list_other.getId())){
                selectedSponsor.other = value;
                return;
            }
        }
    }

    private EventHandler<ContextMenuEvent> createSponsorTableContextMenu(){
        return new EventHandler<ContextMenuEvent>(){
            @Override
            public void handle(ContextMenuEvent event) {
                Parent parent = event.getPickResult().getIntersectedNode().getParent();
                try{
                    if(parent.getId() == null){
                        parent = (Parent)event.getPickResult().getIntersectedNode();
                    }
                    
                    setSelectedColumn(parent.getId());
                    selectedSponsor = list_table.getSelectionModel().getSelectedItem();
                    
                }catch(ClassCastException ex){
                    return;
                }
                
              ContextMenu menu = new ContextMenu();
              MenuItem remove = new MenuItem("Remove");
              MenuItem edit = new MenuItem("Edit");
              MenuItem email = new MenuItem("Email");
              remove.setOnAction(removeMenuItemAction());
              edit.setOnAction(editMenuItemAction());
              email.setOnAction(emailMenuItemAction());
              menu.getItems().addAll(remove, edit, email);
              menu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
            }
        };
    }

    private EventHandler<ActionEvent> editMenuItemAction(){
        return new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(selectedSponsor == null || selectedColumn == null) return;
                if(selectedColumn.getId().equals((list_contacted.getId()+"temp"))){
                        createEditDialog(list_contacted.getCellData(list_table.getSelectionModel().getSelectedIndex()), true);
                }else{
                    createEditDialog(selectedColumn.getCellData(list_table.getSelectionModel().getSelectedIndex()), false);
                }
            }
          };
    }

    private EventHandler<ActionEvent> emailMenuItemAction(){
        return new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                emailingController = EmailingController.getInstance();
                EmailConfig config = new EmailConfig();
                Optional<SubjectLine> subjectLine = showDialog(emailingController.emailingSubjectLine);
                if(!subjectLine.isPresent()) return;
                Optional<Body> body = showDialog(emailingController.emailingBody);   
                if(!body.isPresent()) return;       
                Optional<Attachment> attachment = showDialog(emailingController.emailingAttachment);
                if(!attachment.isPresent()) return;          
                String pass = emailingController.passwordDialog();
                System.out.println(pass);
                System.out.println(new String(pass.getBytes())  );
                System.out.println(EmailInfo.PASSWORD);
                System.out.println(EmailInfo.comparePassword(pass.getBytes()));
                if(!EmailInfo.comparePassword(pass.getBytes())){
                    emailingController.emailingWarningDialog("Wrong password!");
                    return;
                }
                
                config.setSubjectLine(subjectLine.get());
                config.setBody(body.get());
                config.setAttachment(attachment.get());
                config.setSponsor(list_table.getSelectionModel().getSelectedItem());
                try {
                    config.setTo(new InternetAddress(config.getSponsor().getEmail()));
                    MailManager.sendEmail(config);
                    config.getSponsor().contacted = true;
                    config.getSponsor().notes = "Email sent successfully";
                } catch (AddressException e) {
                    config.getSponsor().notes = "Invalid Address - "+e.getMessage();
                   return;
                }
                emailingController.emailingEndDialog();
                CopyOnWriteArrayList<Sponsor> list = new CopyOnWriteArrayList<>(list_table.getItems());
                int row = list.indexOf(config.getSponsor()) + 1;
                SheetsManager.updateRowSheet(config.getSponsor().toRowData(), row, SheetID.Sponsors);
                refreshSponsorList(null);
            }
          };
    }

    private <T> Optional<T> showDialog(ComboBox<T> box){
        Dialog<T> dialog = new Dialog<>();
        String label = ((Label)box.getParent()).getText();
        dialog.setTitle(label);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ComboBox<T> combo = new ComboBox<>();
        combo.setItems(box.getItems());
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(new Label(label+": "),combo);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return combo.getSelectionModel().getSelectedItem();
            }
            return null;
        });   
        return dialog.showAndWait();
    }


    private void createEditDialog(Object value, boolean contactedColumn){
        String text = new String(String.valueOf(value));
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Edit");
        dialog.setHeaderText("Edit "+ (contactedColumn ? list_contacted.getText() : selectedColumn.getText())+" Value");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        HBox content = new HBox(new Label("Edit: "));
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        if(!contactedColumn){
            TextField textField = new TextField(text);
            content.getChildren().add(textField);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return textField.getText();
                }
                return null;
            });       
        }else{
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(Boolean.valueOf(text));
            content.getChildren().add(checkBox);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return checkBox.isSelected();
                }
                return null;
            });        
        }    
        dialog.getDialogPane().setContent(content);
               
        Optional<Object> result = dialog.showAndWait();
        if(result.isPresent()){           
            updateColumnCell(selectedColumn.getId(), result.get());
            SheetsManager.updateRowSheet(selectedSponsor.toRowData(),(list_table.getSelectionModel().getSelectedIndex() + 1), SheetID.Sponsors);
            refreshSponsorList(null);
        }
    }

    private EventHandler<ActionEvent> removeMenuItemAction(){
        return new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(selectedSponsor == null) return;
                SheetsManager.deleteRowSheet((list_table.getSelectionModel().getSelectedIndex() + 1), SheetID.Sponsors);
                refreshSponsorList(null);
            }
        };
    }
}
