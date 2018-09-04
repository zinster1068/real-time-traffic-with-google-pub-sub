package com.traffic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.cloud.ServiceOptions;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.protobuf.ByteString;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import com.socrata.api.Soda2Consumer;
import com.socrata.api.HttpLowLevel;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.socrata.model.soql.SoqlQuery;
import com.sun.jersey.api.client.ClientResponse;



public class SegmentList
{
    Map<String, SegmentReading> segments;
    String                      sourceFile;
    String                      msgTopicName;
    String                      sourceURL;

    private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();

    public SegmentList(String p_sourceFile, String p_sourceURL, String p_msgTopicName)
    {
        segments     = new HashMap<>();
        sourceFile   = p_sourceFile;
        msgTopicName = p_msgTopicName;
        sourceURL    = p_sourceURL;
    }

    public Map<String, SegmentReading> getSegments()
    {
        return (segments);
    }

    public boolean deleteAllSegements()
    {
        SegmentReading cSegmentReading;

        for(Map.Entry<String,SegmentReading> entry : segments.entrySet())
        {
            cSegmentReading = segments.get(entry.getValue().SegmentID.trim());
            cSegmentReading = null;
        }
        segments.clear();

        return true;
    }

    public boolean loadSegments(String p_resourceID)
    {
        JSONParser     parser = new JSONParser();
        JSONArray      citySegments;
        JSONObject     currentCitySegment;
        SegmentReading cSegmentReading;
        Soda2Consumer  consumer = Soda2Consumer.newConsumer(sourceURL);

        try
        {
            ClientResponse response = consumer.query(p_resourceID, HttpLowLevel.JSON_TYPE, SoqlQuery.SELECT_ALL);

            String payload = response.getEntity(String.class);

            try
            {
                citySegments = (JSONArray) parser.parse(payload);

                for(int i = 0; i < citySegments.size(); i++)
                {
                    currentCitySegment = (JSONObject) citySegments.get(i);

                    cSegmentReading = new SegmentReading((String) currentCitySegment.get("segmentid"),
                            (String) currentCitySegment.get("street"),
                            (String) currentCitySegment.get("_direction"),
                            (String) currentCitySegment.get("_fromst"),
                            (String) currentCitySegment.get("_tost"),
                            (String) currentCitySegment.get("_length"),
                            (String) currentCitySegment.get("_strheading"),
                            (String) currentCitySegment.get("_comments"),
                            (String) currentCitySegment.get("start_lon"),
                            (String) currentCitySegment.get("_lif_lat"),
                            (String) currentCitySegment.get("_lit_lon"),
                            (String) currentCitySegment.get("_lit_lat"),
                            (String) currentCitySegment.get("_traffic"),
                            (String) currentCitySegment.get("_last_updt"));

                    //cSegmentReading.describe();
                    segments.put((String) currentCitySegment.get("segmentid"),cSegmentReading);
                }
            }
            catch (ParseException e)
            {
                System.out.println("JSON parse exception at " + e.getPosition());
                e.printStackTrace();
            }
            System.out.println("Received " + segments.size() + " segments from Internet API.");

            //System.out.println(payload);
        }
        catch (LongRunningQueryException e)
        {
            System.out.println("Error:  Long Running Soda Query Exception");
            e.printStackTrace();
        }
        catch (SodaError e)
        {
            System.out.println("Error:  Soda Exception");
            e.printStackTrace();
        }
        return true;
    }

    public boolean loadSegments(int p_startRow)
    {
        SegmentReading cSegmentReading;

        BufferedReader br = null;
        String line       = "";
        String cvsSplitBy = ",";
        int currentLine   = 0;
        boolean success   = true;

        try
        {
            br = new BufferedReader(new FileReader(sourceFile));

            while(currentLine < p_startRow)
            {
                line = br.readLine();
                if(line == null)
                    currentLine = p_startRow;
                else
                    currentLine++;
            }

            while((line = br.readLine()) != null)
            {
                String[] lineContents = line.split(cvsSplitBy);
                cSegmentReading = new SegmentReading(lineContents[0].trim(),lineContents[1],lineContents[2], lineContents[3], lineContents[4], lineContents[5],lineContents[6],
                                                     lineContents[7], lineContents[8], lineContents[9], lineContents[10], lineContents[11], lineContents[12], lineContents[13]);

                cSegmentReading.describe();
                segments.put(lineContents[0],cSegmentReading);
            }
            br.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Could not find the segment file at " + sourceFile);
            success = false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            success = false;
        }
        return (success);
    }

    public boolean sendSegments()
    {
        ProjectTopicName topicName      = ProjectTopicName.of(PROJECT_ID, msgTopicName);
        Publisher        publisher      = null;
        List<ApiFuture<String>> futures = new ArrayList<>();

        SegmentReading cSegmentReading;
        String         message;

        try
        {
            publisher = Publisher.newBuilder(topicName).build();

            for(Map.Entry<String,SegmentReading> entry : segments.entrySet())
            {
                cSegmentReading = segments.get(entry.getValue().SegmentID.trim());

                message = "{\"SEGMENTID\":\"" + cSegmentReading.SegmentID + "\"," +
                           "\"STREET\":\"" + cSegmentReading.Street + "\"," +
                           "\"DIRECTION\":\"" + cSegmentReading.Direction + "\"," +
                           "\"FROM_STREET\":\"" + cSegmentReading.FromStreet + "\"," +
                           "\"TO_STREET\":\"" + cSegmentReading.ToStreet + "\"," +
                           "\"LENGTH\":\"" + cSegmentReading.Length + "\"," +
                           "\"_STREET_HEADING\":\"" + cSegmentReading.StreetHeading + "\"," +
                           "\"_COMMENTS\":\"" + cSegmentReading.Comments + "\"," +
                           "\"START_LONGITUDE\":\"" + cSegmentReading.StartLongitude + "\"," +
                           "\"_START_LATITUDE\":\"" + cSegmentReading.StartLatitude + "\"," +
                           "\"END_LONGITUDE\":\"" + cSegmentReading.EndLongitude + "\"," +
                           "\"_END_LATITUDE\":\"" + cSegmentReading.EndLatitude + "\"," +
                           "\"_CURRENT_SPEED\":\"" + cSegmentReading.CurrentSpeed + "\"," +
                           "\"_LAST_UPDATED\":\"" + cSegmentReading.LastUpdated + "\"}";

                ByteString data = ByteString.copyFromUtf8(message);
                PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                        .setData(data)
                        .build();

                ApiFuture<String> future = publisher.publish(pubsubMessage);
                futures.add(future);
            }
        }
        catch (IOException e)
        {
            System.out.println("SendSegments:  IOException");
            System.out.println("SendSegments:  Cause->" + e.getCause());
            e.printStackTrace();

        }
        catch (Exception e)
        {
            System.out.println("SendSegments:  Exception");
            System.out.println("SendSegments:  Cause->" + e.getCause());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                List<String> messageIds = ApiFutures.allAsList(futures).get();

                System.out.println("Received Cloud Pub/Sub message transaction IDs.");
                //for (String messageId : messageIds) {
                //    System.out.println(messageId);
                //}

                if (publisher != null)
                {
                    try
                    {
                        publisher.shutdown();
                    }
                    catch (Exception e)
                    {
                        System.out.println("SendSegments:  Exception");
                        System.out.println("SendSegments:  Cause->" + e.getCause());
                        e.printStackTrace();
                    }
                }
            }
            catch (InterruptedException e)
            {
                System.out.println("SendSegments:  InterruptedException");
                System.out.println("SendSegments:  Cause->" + e.getCause());
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                System.out.println("SendSegments:  ExecutionException");
                System.out.println("SendSegments:  Cause->" + e.getCause());
                e.printStackTrace();
            }
        }
        return true;
    }
}
