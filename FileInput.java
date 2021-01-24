import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.io.File;
public class FileInput
{
    private TextField filearea;
    private String filename;
    private boolean ready;

    //The program asks the user to input the filename if no file is found(?)
    public FileInput()
    {
        ready = false;
        Group root = new Group();
        Scene sc = new Scene(root);
        Stage popup = new Stage();
        popup.setScene(sc);
        popup.setTitle("Input Filename");

        Label message = new Label("Please type the filename in the textfield. Eg. file.csv");
        Label message2 = new Label("(The file must be in the same directory as this program)");
        Label message3 = new Label();
        TextField filearea = new TextField();
        Button button = new Button("OK");
        button.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e)
                {
                    filename = filearea.getText();
                    File file = new File(filename);
                    message3.setText("");
                    if(file.exists())
                        popup.close();
                    else
                        message3.setText("The file could not be found!");
                }
            });
        sc.setOnKeyPressed(new EventHandler<KeyEvent>(){
                @Override
                public void handle(KeyEvent e)
                {
                    if(e.getCode() == KeyCode.ENTER)
                    {
                        filename = filearea.getText();
                        File file = new File(filename);
                        message3.setText("");
                        if(file.exists())
                            popup.close();
                        else
                            message3.setText("The file could not be found!");
                    }
                }
            });

        VBox vb = new VBox(message, message2, message3, filearea, button);
        vb.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(10, 20, 20, 20));
        root.getChildren().add(vb);

        popup.showAndWait();
    }

    public String getFileName()
    {
        return filename;
    }

    public boolean isReady()
    {
        return ready;
    }

    public void commence()
    {

    }
}
