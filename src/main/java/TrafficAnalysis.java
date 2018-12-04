import com.traffic.*;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrafficAnalysis
{
    public static Properties applicationProperties;

    public TrafficAnalysis()
    {

    }

    /*
        The loadProperties method is a generic method that will load the application's properties from the provide properties file.  The following properties will be loaded:
            - sourceFile:  The data file that will be used to generate the Chicago traffic segments.  This data file should be downloaded from
                           https://data.cityofchicago.org/Transportation/Chicago-Traffic-Tracker-Congestion-Estimates-by-Se/n4j6-wkkf
            - msgTopicName:  The Google Pub/Sub topic name that will be used.  One must first create this topic in the GCP console.
     */
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
        SegmentList       ChicagoSegments;  // Holds the data pulled from data.cityofchicago.org
        boolean           loadSuccess;      // Was the data successfully loaded from the file / data.cityofchicago.org?
        boolean           sendSuccess;      // Was the data successfully sent to the Google Pub/Sub topic?
        boolean           deleteSuccess;    // Was the removal of the traffic segments successfully removed from the ChicagoSegments structure?
        int               delay = 60;       // Delay between calls to the data.cityofchicago.org API
        DateTimeFormatter dtf   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("Starting application...");

        if(args.length > 0)
        {
            loadProperties(args[0]);  // Load the application properties

            // Create the segments object with the data filename, data URL, and Google Pub/Sub topic name.
            // Depending on which "loadSegments" method is called later, either the datafile or the API will be used to populate the object.
            ChicagoSegments = new SegmentList(applicationProperties.getProperty("sourceFile"), "https://data.cityofchicago.org", applicationProperties.getProperty("msgTopicName"));

            while(true)
            {
                // Uncomment this line to load the Chicago traffic segments from a flat file.  Location and filename of the data file is defined in the resources/config.properties files.
                //loadSuccess = ChicagoSegments.loadSegments(1);

                // Uncomment this line to load the Chicago traffic segments from data.cityofchicago.org website.  The resource ID can be found at
                // https://data.cityofchicago.org/Transportation/Chicago-Traffic-Tracker-Congestion-Estimates-by-Se/n4j6-wkkf
                loadSuccess = ChicagoSegments.loadSegments("8v9j-bter");

                if (loadSuccess)
                {
                    try
                    {
                        LocalDateTime now = LocalDateTime.now();
                        System.out.println(dtf.format(now));

                        System.out.println("Sending updated segments at " + dtf.format(now));

                        // Send the data to Google's Pub/Sub topic
                        sendSuccess = ChicagoSegments.sendSegments();

                        // Remove the traffic information from the ChicagoSegments object as it will be populated with updated information on the next call to loadSegments.
                        deleteSuccess = ChicagoSegments.deleteAllSegements();

                        System.out.println("Waiting " + delay + " minutes to execute");
                        System.out.println("========================================");

                        // Pause to wait for the data to be updated at data.cityofchicago.org.
                        // Please note that the same data will be used if one is using the datafile as the information source.
                        TimeUnit.MINUTES.sleep(delay);
                    }
                    catch (Exception e)
                    {
                        System.out.println("sendSegments:  Exception caught.  Is the resource ID correct when calling ChicagoSegments.loadSegments on line 90?");
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
