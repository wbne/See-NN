import java.util.Scanner;
import java.io.File;
public class FileReader implements Runnable
{
    private Scanner sc;
    private int layerNumber;
    private int maxLayer;
    private String prev, curr, next, file;
    private Thread t;
    boolean ready;
    public FileReader(String filename, int ml)
    {
        file = filename;
        maxLayer = ml;
        layerNumber = 0;
        ready = false;
    }

    public void run()
    {
        try{sc = new Scanner(new File(file));}
        catch(Exception e){System.out.println("file not found");}
        curr="";
        for(int i = 0; i <= layerNumber + 1 && sc.hasNext(); i++)
        {
            if(i == layerNumber - 1)
            {
                prev = sc.nextLine();
            }
            else if(layerNumber == 0 && i == layerNumber)
            {
                curr = sc.nextLine();
            }
            else if(i == layerNumber + 1)
            {
                next = sc.nextLine();
                break;
            }
            else
            {
                sc.nextLine();
            }
        }
        ready = true;
    }

    public void start()
    {
        if(t == null || !t.isAlive())
        {
            t = new Thread(this, "FileReader");
            t.start();
            ready = false;
        }
    }

    public int getLayer()
    {
        return layerNumber;
    }

    public boolean isReady()
    {
        return ready;
    }

    public String getPrev()
    {
        layerNumber = Math.max(layerNumber - 1, 0);
        //layerNumber--;
        return prev;
    }

    public String getCurr()
    {
        return curr;
    }

    public String getNext()
    {
        layerNumber = Math.min(layerNumber + 1, maxLayer);
        //layerNumber++;
        return next;
    }
}
