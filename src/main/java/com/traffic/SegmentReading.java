package com.traffic;

/*
    The SegmentReading class stores one traffic reading.

    Data
        SegmentID   -   Unique ID used to identify the segment
        Street      -   Street name where the traffic reading was taken
        Direction   -   The direction of the traffic
        FromStreet  -   The starting cross street
        ToStreet    -   The ending cross street
        Length      -   The distance from the FromStreet to the ToStreet
        StreetHeading   -   The street's direction
        Comments        -   Any comments
        StartLongitude  -   The starting longitude of where the traffic reading starts
        StartLatitude   -   The starting latitude of where the traffic reading starts
        EndLongitude    -   The ending longitude of where the traffic reading ends
        EndLatitude     -   The ending latitude of where the traffic reading ends
        CurrentSpeed    -   The current speed of the traffic segment
        LastUpdated     -   When the traffic reading was updated

    Methods
        SegmentReading  -   Class constructor that requires the attributes that will be stored.
        describe        -   Method that outputs the object's attributes to the console.

    Notes
        None
 */
public class SegmentReading
{
    String SegmentID;
    String Street;
    String Direction;
    String FromStreet;
    String ToStreet;
    String Length;
    String StreetHeading;
    String Comments;
    String StartLongitude;
    String StartLatitude;
    String EndLongitude;
    String EndLatitude;
    String CurrentSpeed;
    String LastUpdated;

    public SegmentReading(String p_SegmentID, String p_Street, String p_Direction, String p_FromStreet, String p_ToStreet, String p_Length, String p_StreetHeading,
                          String p_Comments, String p_StartLongitude, String p_StartLatitude, String p_EndLongitude, String p_EndLatitude, String p_CurrentSpeed,
                          String p_LastUpdated)
    {
        SegmentID      = p_SegmentID;
        Street         = p_Street;
        Direction      = p_Direction;
        FromStreet     = p_FromStreet;
        ToStreet       = p_ToStreet;
        Length         = p_Length;
        StreetHeading  = p_StreetHeading;
        Comments       = p_Comments;
        StartLongitude = p_StartLongitude;
        StartLatitude  = p_StartLatitude;
        EndLongitude   = p_EndLongitude;
        EndLatitude    = p_EndLatitude;
        CurrentSpeed   = p_CurrentSpeed;
        LastUpdated    = p_LastUpdated;
    }

    public void describe()
    {
        System.out.println("SegmentID      = " + SegmentID);
        System.out.println("Street         = " + Street);
        System.out.println("Direction      = " + Direction);
        System.out.println("FromStreet     = " + FromStreet);
        System.out.println("ToStreet       = " + ToStreet);
        System.out.println("Length         = " + Length);
        System.out.println("StreetHeading  = " + StreetHeading);
        System.out.println("Comments       = " + Comments);
        System.out.println("StartLongitude = " + StartLongitude);
        System.out.println("StartLatitude  = " + StartLatitude);
        System.out.println("EndLongitude   = " + EndLongitude);
        System.out.println("EndLatitude    = " + EndLatitude);
        System.out.println("CurrentSpeed   = " + CurrentSpeed);
        System.out.println("LastUpdated    = " + LastUpdated);
    }
}
