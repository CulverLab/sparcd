package model.image;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

/**
 * Representation of Camtrap DP deployments data. See https://tdwg.github.io/camtrap-dp/data/#deployments
 * 
 * @author Chris Schnaufer
 */

public final class Deployments
{
    // The ID of the deployment
    public String deploymentID;

    // The ID of the location
    public String locationID;

    // The name of the location
    public String locationName;

    // The longitude of the deployment
    public double longitude;

    // The latitude of the deployment
    public double latitude;

    // The coordinate uncertainty in meters
    public int coordinateUncertainty;

    // Starting timestamp of deployment
    public LocalDateTime start; 

    // Ending timestamp of deployment
    public LocalDateTime end; 

    // Name of unique identifier of person setting up deployment
    public String setupBy;

    // Unique camera ID 
    public String cameraID;

    // Manufacturer and model of the camera in "manufacturer-model" format
    public String cameraModel;

    // Time specified between shutter triggers
    public int cameraInterval;

    // Height of camera deployment in meters
    public double cameraHeight;

    // The attached angle of camera
    public double cameraTilt;

    // The angle of camera deployment on horizontal plane in degrees clockwise from north (0 = north)
    public int cameraHeading;

    // Flag indicating timestamp issues in media or observations data (when unknown, missing timezone, AM/PM switch, etc)
    public boolean timestampIssues;

    // Type of bait that was used (if any)
    public String baitUse;

    // Temporal deployment group, such as seasons, months, etc.
    public String session;

    // Spatial deployment group
    public String array;

    // Type of feature of camera deployment such as "none", "road paved", "culvert", etc
    public String featureType;

    // Characterization of habitat
    public String habitat;

    // Defined tags
    public String tags;

    // Comments and notes
    public String notes;

    // Internal unique identifier of deployment, as assigned by data management system. e.g.: database ID
    public String _id;

    /**
     * Constructor
     */
    public Deployments()
    {
        // Must be externally supplied
        deploymentID = null;
        start = null;
        end = null;

        // Default values
        locationID = "";
        locationName = "";
        longitude = 0.0;
        latitude = 0.0;
        coordinateUncertainty = 0;
        setupBy = "";
        cameraID = "";
        cameraModel = "";
        cameraInterval = 0;
        cameraHeight = 0.0;
        cameraTilt = 0.0;
        cameraHeading = 0;
        timestampIssues = true;
        baitUse = "";
        session = "";
        array = "";
        featureType = "";
        habitat = "";
        tags = "";
        notes = "";
        _id = "";
    }

    /**
     * Creates a new instance from the string array
     * 
     * @param values The array of string values
     * @return An initialized instance of the Deployments class
     * @throws InvalidParameterException if there are elements missing from the array
     */
    public final static Deployments instance(String[] values)
    {
        // Make sure we have enouigh elements to assign
        if (values.length != 23)
        {
            throw new InvalidParameterException("Missing elements for Camtrap Deployment specification");
        }

        // Create class instance for returning
        Deployments dep = new Deployments();

        // Assign the values
        if (values[0].length() == 0)
            dep.deploymentID = null;
        else
            dep.deploymentID = values[0];

        dep.locationID = values[1];
        dep.locationName = values[2];
        dep.longitude = Double.parseDouble(values[3]);
        dep.latitude = Double.parseDouble(values[4]);
        dep.coordinateUncertainty = Integer.parseInt(values[5]);

        if ((values[6] != null) && (values[6].length() > 0))
            dep.start = LocalDateTime.parse(values[6], DateTimeFormatter.ISO_DATE_TIME);
        else
            dep.start = null;

        if ((values[7] != null) && (values[7].length() > 0))
            dep.end = LocalDateTime.parse(values[7], DateTimeFormatter.ISO_DATE_TIME);
        else
            dep.end = null;

        dep.setupBy = values[8];
        dep.cameraID = values[9];
        dep.cameraModel = values[10];
        dep.cameraInterval = Integer.parseInt(values[11]);
        dep.cameraHeight = Double.parseDouble(values[12]);
        dep.cameraTilt = Double.parseDouble(values[13]);
        dep.cameraHeading = Integer.parseInt(values[14]);

        if ((values[15] == "true") || (values[15] == "1"))
            dep.timestampIssues = true;
        else
            dep.timestampIssues = false;

        dep.baitUse = values[16];
        dep.session = values[17];
        dep.array = values[18];
        dep.featureType = values[19];
        dep.habitat = values[20];
        dep.tags = values[21];
        dep.notes = values[22];

        // Return new instance
        return dep;
    }

    /**
     * Returns the data as an array of strings
     * 
     * @return An array of strings representing the data
     */
    public String[] toArray()
    {
        List<String> data = new Vector<String>();

        if (deploymentID != null)
            data.add(deploymentID.toString());
        else
            data.add("");

        data.add(locationID.toString());
        data.add(locationName.toString());
        data.add(String.format("%f", longitude));
        data.add(String.format("%f", latitude));
        data.add(String.format("%d", coordinateUncertainty));
        
        if (start != null)
            data.add(start.format(DateTimeFormatter.ISO_DATE_TIME));
        else
            data.add("");
        if (end != null)
            data.add(end.format(DateTimeFormatter.ISO_DATE_TIME));
        else
            data.add("");

        data.add(setupBy.toString());
        data.add(cameraID.toString());
        data.add(cameraModel.toString());
        data.add(String.format("%d", cameraInterval));
        data.add(String.format("%f", cameraHeight));
        data.add(String.format("%f", cameraTilt));
        data.add(String.format("%d", cameraHeading));

        if (timestampIssues == true)
            data.add("true");
        else
            data.add("false");

        data.add(baitUse.toString());
        data.add(session.toString());
        data.add(array.toString());
        data.add(featureType.toString());
        data.add(habitat.toString());
        data.add(tags.toString());
        data.add(notes.toString());

        String[] returnData = new String[data.size()];
        data.toArray(returnData);
        return returnData;
    }
}
