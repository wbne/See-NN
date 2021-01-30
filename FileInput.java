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
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.Dragboard;
import javafx.scene.control.ComboBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.io.File;
import java.nio.file.FileSystems;
public class FileInput
{
    private TextField filearea;
    private String filename;
    private String dir;

    //The program asks the user to input the filename if no file is found
    public FileInput()
    {
        //basic window setting up
        Group root = new Group();
        Scene sc = new Scene(root);
        Stage popup = new Stage();
        popup.setScene(sc);
        popup.setTitle("Input Filename");
        
        //gets directory of the program
        dir = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();

        //adds all local csv files found to a menu
        ComboBox cb = new ComboBox();
        File local = new File(dir);
        for(File file : local.listFiles())
        {
            if(file.getName().endsWith(".csv"))
            {
                cb.getItems().add(file.toString());
            }
            else if(file.isDirectory()) //incase you put the files in a folder
            {
                File sub = file;
                for(File subFile : sub.listFiles())
                {
                    if(subFile.getName().endsWith(".csv"))
                        cb.getItems().add(subFile.toString());
                }
            }
        }
        
        try
        {
            cb.getSelectionModel().select(0); //defaults to the first option
        }
        catch(Exception e)
        {
            cb.setValue("No CSV Files Found!"); //if there's no files available
        }
        
        //sets the filename on click
        cb.setOnAction(new EventHandler<ActionEvent>(){
            @Override public void handle(ActionEvent e)
            {
                filename = cb.getValue().toString();
            }
        });
        
        Label message = new Label("Please select a file from the drop down menu");
        Label message2 = new Label("(The file/folder must be in the same directory as this program)");
        Label message5halves = new Label("OR drag and drop the file into this window");
        Label message3 = new Label();
        Label spacer = new Label("");
        //checks to see if the file is legit and sends it off
        Button button = new Button("OK");
        button.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e)
                {
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
                        File file = new File(filename);
                        message3.setText("");
                        if(file.exists())
                            popup.close();
                        else
                            message3.setText("The file could not be found!");
                    }
                }
            });
            
        //adds everything to the window
        VBox vb = new VBox(message, message2, message5halves, message3, cb, spacer, button);
        vb.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(10, 20, 20, 20));
        
        //additional drag and drop feature for fun
        vb.setOnDragOver(new EventHandler<DragEvent>(){
            @Override
            public void handle(DragEvent ev) 
            {
                if(ev.getGestureSource() != vb && ev.getDragboard().hasFiles()) {
                    ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                ev.consume();
            }
        });
        
        vb.setOnDragDropped(new EventHandler<DragEvent>(){
            @Override
            public void handle(DragEvent ev)
            {
                Dragboard db = ev.getDragboard();
                if(db.hasFiles()) 
                {
                    filename = db.getFiles().toString();
                    filename = filename.substring(1, filename.length() - 1);
                    File file = new File(filename);
                    message3.setText("");
                    if(file.exists())
                        popup.close();
                    else
                        message3.setText("The file could not be dropped!");
                }
            }
        });
        
        root.getChildren().add(vb);
        popup.showAndWait();
    }

    public String getFileName()
    {
        return filename;
    }
}
