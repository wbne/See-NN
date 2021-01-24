import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.File;
import java.lang.ref.SoftReference;

public class OneChannel extends Application
{
    //dimensions of the network
    public static int[][] dimensions;

    //name of the file (might add final back later)
    //public static String file_name = "MAPn02050568_10086.csv";
    public static String file_name = "";

    //global variables that are here to make the coding easier for me
    public static double size = 4; //length of a tile/square
    public static int layer = 0; //layer number (starts at 0)
    public static int max_layer; //layer amount
    public static int square; //length and width of the layer
    public static int depth; //depth of the layer
    public static int SELECTED_CHANNEL = 0; //the channel we want to observe
    public static GraphicsContext gc; //just the gui canvas thing
    public static Slider s; //slider that lets people navigate the channels
    public static boolean bwMode = true;
    public static double[] potato; //double values from the layer
    public static FileReader fr; //the other thread that will read the file
    public static Label layernum; //label that says which layer the user is on
    public static Label chanSpars; //shows the single channel sparsity in a given layer
    public static Label layerSpars; //shows the entire layer sparsity
    public static Button c; //go up a layer
    public static Button d; //go down a layer

    /* TODO (coding wise):
     * General cleanup and patching
     * make the disabled button more obvious ormaybe just have an audio queue happen
     * add option to save the filename
     * maybe have a drop down to select csv files locally?
     * change file option
     * maybe dynamic multithreading for the rendering
     * 
     * Things from meeting:
     * Try to eyeball patterns across multiple images
     * Legend for the colors
     * Plot consecutive rows and columns maybe or at least compute them
     *      Maybe not kinda lazy
     * Maybe recommend an optimal hardware vector size
     * Include analysis of the data like patterns 
     * Talk about how sparsity usually looks in each layer
     */

    public static void main(String []args)
    {
        launch(args);
    }

    public void start(Stage stage)
    {
        if(file_name.length() == 0 || file_name == null)
        {
            SoftReference<FileInput> fi = new SoftReference<>(new FileInput());
            file_name = fi.get().getFileName();
        }

        //this just sets up the javafx window and the dimensions
        dimensions = VGG19Dims();
        square = dimensions[layer][0]; //dimension of the length and width
        depth = dimensions[layer][1]; //dimensinon of the channel
        max_layer = dimensions.length;
        int winLength = 950;
        int winHeight = 850;
        int layerLength = 800;
        int layerHeight = 800;
        size = (double)layerHeight/(double)square; 

        stage.setTitle("Visualizing CNN Layers");
        Group root = new Group();
        //sets the dimension of the window
        Scene scene = new Scene(root, winLength, winHeight);

        stage.setScene(scene);
        Canvas canvas = new Canvas(layerLength, layerLength); //size of the area
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        Label channel_l = new Label("Channel controls: ");
        s = createChannelSli(); //creates the slider
        Button a = createChannelAdd(); //creates an addition button
        Button b = createChannelSub(); //creates a subtraction button
        Label layer_l = new Label("Layer controls: ");
        c = createLayerAdd(); 
        d = createLayerSub();

        //The controls to navigate the network
        HBox hbox = new HBox(channel_l, s, a, b, layer_l, c, d); //puts them into a horizontal box
        hbox.setLayoutY(layerHeight + 5); //places the stuff at the bottom
        root.getChildren().add(hbox); //attaches it to the scene

        //The informational panel
        Label prelegend = new Label("file name: " + file_name);
        prelegend.setWrapText(true);
        prelegend.setMaxWidth(140);
        Label legend = new Label("zero values are white");
        Label legend2 = new Label("nonzero values are black");
        Button ch = changeMode(); //adds a mode option
        layernum = new Label("Layer: " + (layer + 1) + "/" + max_layer); //tells the user which layer they're on
        chanSpars = new Label();
        layerSpars = new Label();
        VBox sidePanel = new VBox(prelegend, ch, legend, legend2, layernum, chanSpars, layerSpars);
        sidePanel.setLayoutX(layerLength + 5);
        root.getChildren().add(sidePanel);

        //Some multithreading
        fr = new FileReader(file_name, max_layer);
        fr.start(); //starts the thread

        parseLayer();

        stage.show();
    }

    public static void parseLayer()
    {
        //formats the string to not boom me
        try
        {
            Scanner sc = new Scanner(new File(file_name));
            String temp = "";
            //goes to the desired layer and reads the data
            //multithreading attempt
            while(!fr.isReady()) //waits until the thread is ready
            {
                try{Thread.sleep(1000);}
                catch(Exception e){System.out.println("error");}
            }
            enableButtons();
            
            //checks to see the difference between the main thread and itself
            //then corrects to be equal
            if(layer < fr.getLayer())
            {
                temp = fr.getPrev();
                fr.start();
            }
            else if(layer > fr.getLayer())
            {
                temp = fr.getNext();
                fr.start();
            }
            else
            {
                temp = fr.getCurr();
            }

            depth = dimensions[layer][1]; //reinitialize depth
            square = dimensions[layer][0]; //reinitialize width and height
            size = 800.0/square; 

            //array to store the values
            //uses a tokenizer instead of .split() becuase of memory issues
            //larger files may still be a problem memory-wise
            potato = new double[square * square * depth];
            StringTokenizer st = new StringTokenizer(temp, ",");
            for(int i = 0; st.hasMoreElements() && i < potato.length; i++)
            {
                potato[i] = Double.parseDouble(st.nextToken());
            }

            //finds the channel and draws the value of that square onto the canvas
            render();
        }
        catch(Exception e) //incase something catches on fire
        {
            e.printStackTrace();
        }
    }

    //Generates the heatmap based on the data from the 3d array
    public static void createSquare(int xIndex, int yIndex, double value, double density, boolean color, GraphicsContext gc)
    {
        double x = size * xIndex;
        double y = size * yIndex;
        int threshold = 0;
        //CHANNEL VALUES MONOCHROMATIC
        //int roundedValue = (value < 0) ? 255 : Math.abs(255 - Math.round((int)value));
        if(color)
        {
            //SPARSITY REPRESENTED AS YES OR NO
            int roundedValue = (value <= threshold) ? 255 : 0;
            gc.setFill(Color.rgb(roundedValue, roundedValue, roundedValue));
        }
        else
        {
            //CHANNEL DENSITY REPRESENTED AS COLORS?
            //Currently doing Black and White since it doesn't hurt my eyeballs
            int roundedValue = 255;
            int colorValue = (int)(density * 255);
            int temp = Math.max(roundedValue - colorValue, 0);
            gc.setFill(Color.rgb(temp, temp, temp));
        }
        gc.fillRect(x, y, x + size, y + size);
    }

    //updates the image to match the desired channel
    public static void render()
    {
        gc.clearRect(0, 0, size*square, size*square);
        double layerSparsity = 0;
        double channelSparsity = 0;
        int biggerSquare = square * square;
        for(int i = 0; i < square; i++)
            for(int j = 0; j < square; j++)
            {
                double channelDensity = 0;
                if(potato[j + i * square + SELECTED_CHANNEL * biggerSquare] > 0)
                    channelSparsity++;
                for(int k = 0; k < depth; k++)
                {
                    if(potato[j + i * square + k * biggerSquare] > 0)
                    {
                        channelDensity++;
                        layerSparsity++;
                    }
                }
                channelDensity /= depth;
                createSquare(j, i, potato[j + i * square + SELECTED_CHANNEL * biggerSquare], channelDensity, bwMode, gc);
            }

        layerSparsity = Math.round(100.0 - layerSparsity * 100.0 / potato.length) / 100.0;
        channelSparsity = Math.round(100.0 - channelSparsity * 100.0 / biggerSquare) / 100.0;
        //updates the information on the side
        //TODO: add useful hover tips
        layernum.setText("Layer: " + (layer + 1) + "/" + max_layer);
        chanSpars.setText("Channel Sparsity: " + channelSparsity);
        layerSpars.setText("Layer Sparsity: " + layerSparsity);
    }

    //Makes the disabled layer buttons way more obvious and disabled
    public static void disableButtons()
    {
        c.setDisable(true); //set so you can't click too fast
        d.setDisable(true);
    }

    //reverses the work done by the above method
    public static void enableButtons()
    {
        c.setDisable(false); //set so you can't click too fast
        d.setDisable(false);
    }

    //Creates a slider that allows the user to traverse the channels
    private static Slider createChannelSli()
    {
        Slider slider = new Slider(0, depth - 1, 0);
        slider.valueProperty().addListener(new ChangeListener<Number>(){
                public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    int new_value = (int)Math.round((double)new_val);
                    if(SELECTED_CHANNEL != new_value)
                    {
                        SELECTED_CHANNEL = new_value;
                        render();
                    }
                }
            });
        return slider;
    }

    //creates a button that allows the user to just go down one channel at a time
    private static Button createChannelAdd()
    {
        Button add = new Button("Down Channel");
        add.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e) {
                    if(SELECTED_CHANNEL < depth - 1)
                    {
                        SELECTED_CHANNEL++;
                        s.adjustValue(SELECTED_CHANNEL);
                        render();
                    }
                }
            });
        return add;
    }

    //creates a button that allows the user to just go up one channel at a time
    private static Button createChannelSub()
    {
        Button sub = new Button("Up Channel");
        sub.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e) {
                    if(SELECTED_CHANNEL > 0)
                    {
                        SELECTED_CHANNEL--;
                        s.adjustValue(SELECTED_CHANNEL);
                        render();
                    }
                }
            });
        return sub;
    }

    //creates a button that allows the user to just go down one layer at a time
    private static Button createLayerAdd()
    {
        Button add = new Button("Next Layer");
        add.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e) {
                    if(layer < max_layer - 1)
                    {
                        layer++;
                        disableButtons();
                        parseLayer();
                        s.setMax(depth - 1);
                    }
                }
            });
        return add;
    }

    //creates a button that allows the user to just go up one channel at a time
    private static Button createLayerSub()
    {
        Button sub = new Button("Previous Layer");
        sub.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e) {
                    if(layer > 0)
                    {
                        layer--;
                        disableButtons();
                        parseLayer();
                        s.setMax(depth - 1);
                    }
                }
            });
        return sub;
    }

    //Changes the color modes from bw to color
    //The difference is that color shows the sparsity channel-wise
    //Black and White mode represents the value of just the channel
    private static Button changeMode()
    {
        Button chMode = new Button("Change Mode");
        chMode.setOnAction(new EventHandler<ActionEvent>(){
                @Override public void handle(ActionEvent e) {
                    bwMode = !bwMode;
                    render();
                }
            });
        return chMode;
    }

    //reads the dimensions for the vgg19 dimensions
    private static int[][] VGG19Dims()
    {
        int[][] temp = new int[17][2];
        try
        {
            Scanner sc = new Scanner(new File("vgg19dims.txt"));
            for(int i = 0; sc.hasNext(); i++)
            {
                temp[i][0] = sc.nextInt();
                temp[i][1] = sc.nextInt();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return temp;
    }
}
