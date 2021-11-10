package ca.sozoservers.dev.core;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

public class ErrorManager {

    public ErrorManager(){

    }

    public static void throwError(Thread thread, Throwable ex){
        System.out.println("ERROR IN "+thread.getName()+"("+thread.getId()+")");
        createErrorDialog(ex).show();
    }

    
    public static Dialog<Boolean> createErrorDialog(Throwable ex) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Error");
        dialog.setHeaderText(ex.getMessage());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);

        TextArea textArea = new TextArea();
        textArea.setText(ex.getMessage());
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(textArea);
        dialog.getDialogPane().setContent(content);

        return dialog;
    }
    
}
