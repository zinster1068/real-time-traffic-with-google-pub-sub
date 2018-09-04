import com.traffic.*;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Main
{
    public static Properties applicationProperties;

    public Main()
    {

    }

    public static void loadProperties(String p_PropertyFileName)
    {
        InputStream input = null;

        try
        {
            input = new FileInputStream(p_PropertyFileName);

            applicationProperties = new Properties();
            applicationProperties.load(input);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)
    {
        SegmentList       ChicagoSegments;
        boolean           loadSuccess;
        boolean           sendSuccess;
        boolean           deleteSuccess;
        int               delay = 60;
        DateTimeFormatter dtf   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("Starting application...");

        if(args.length > 0)
        {
            loadProperties(args[0]);

            ChicagoSegments = new SegmentList(applicationProperties.getProperty("sourceFile"), "https://data.cityofchicago.org", applicationProperties.getProperty("msgTopicName"));

            while(true)
            {
                //loadSuccess = ChicagoSegments.loadSegments(1);
                loadSuccess = ChicagoSegments.loadSegments("8v9j-bter");

                if (loadSuccess)
                {
                    try
                    {
                        LocalDateTime now = LocalDateTime.now();
                        System.out.println(dtf.format(now));

                        System.out.println("Sending updated segments at " + dtf.format(now));
                        sendSuccess = ChicagoSegments.sendSegments();
                        deleteSuccess = ChicagoSegments.deleteAllSegements();

                        System.out.println("Waiting " + delay + " minutes to execute");
                        System.out.println("========================================");

                        TimeUnit.MINUTES.sleep(delay);
                    }
                    catch (Exception e)
                    {
                        System.out.println("sendSegments:  Exception caught");
                        e.printStackTrace();
                    }
                }
                else
                {
                    System.out.println("Unsuccessfully loaded the segment telemetry information from " + applicationProperties.getProperty("sourceFile"));
                    System.exit(1);
                }
            }
        }
    }
}
