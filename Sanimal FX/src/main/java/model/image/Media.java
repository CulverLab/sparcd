package model.image;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

/**
 * Representation of Camtrap DP media data. See https://tdwg.github.io/camtrap-dp/data/#media
 * 
 * @author Chris Schnaufer
 */

public final class Media
{
    // Project-wide media file unique identifier
    public String mediaID;

    // Deployment ID media is associated with
    public String deploymentID;

    // Project-wide unique sequence ID of this media file
    public String sequenceID;

    // Capture method
    public String captureMethod;

    // Capture timestamp
    public LocalDateTime timestamp;

    // File path to media
    public String filePath;

    // The name of media file
    public String fileName;

    // The type of media (e.g.: "image/jpeg")
    public String fileMediaType;

    // The EXIF data of the file
    public Object exifData;

    // Flag indicating if media file can vbe considered exemplary
    public boolean favorite;

    // Comments and notes on the media file
    public String comments;

    // Internal unique ID of media, as assigned by data management system. e.g.: database ID
    public String _id;

    /**
     * Constructor
     */
    public Media()
    {
        // Must be externally supplied
        mediaID = null;
        deploymentID = null;
        timestamp = null;
        exifData = null;

        // Default values
        sequenceID = "";
        captureMethod = "";
        filePath = "";
        fileName = "";
        fileMediaType = "";
        favorite = false;
        comments = "";
        _id = "";
    }

    /**
     * Creates a new instance from a string array
     * 
     * @param values An array of string values
     * @return An initialized instance of the Media class
     * @throws InvalidParameterException if there are elements missing from the array
     */
    public static final Media instance(String[] values)
    {
        // Check that we have enough elements
        if (values.length < 11)
        {
            throw new InvalidParameterException("Missing elements for Captrap Media specification");
        }

        // Create a new instance to return
        Media med = new Media();

        // Assign the values
        if (values[0] == "")
            med.mediaID = null;
        else
            med.mediaID = values[0];

        if (values[1] == "")
            med.deploymentID = null;
        else
            med.deploymentID = values[1];

        med.sequenceID = values[2];
        med.captureMethod = values[3];

        if ((values[4] != null) && (values[4].length() > 0))
            med.timestamp = LocalDateTime.parse(values[4], DateTimeFormatter.ISO_DATE_TIME);
        else
            med.timestamp = null;

        med.filePath = values[5];
        med.fileName = values[6];
        med.fileMediaType = values[7];

        if (values[8] == "")
            med.exifData = null;
        else
            med.exifData = values[8];

        if ((values[9] == "true") || (values[9] == "1"))
            med.favorite = true;
        else
            med.favorite = false;

        med.comments = values[10];

        return med;
    }

    /**
     * Returns the values as a string array
     * 
     * @return A string array of values
     */
    public final String[] toArray()
    {
        List<String> data = new Vector<String>();

        if (mediaID == null)
            data.add(null);
        else
            data.add(mediaID.toString());

        if (deploymentID == null)
            data.add("");
        else
            data.add(deploymentID.toString());

        data.add(sequenceID.toString());
        data.add(captureMethod.toString());

        if (timestamp != null)
            data.add(timestamp.format(DateTimeFormatter.ISO_DATE_TIME));
        else
            data.add("");

        data.add(filePath.toString());
        data.add(fileName.toString());
        data.add(fileMediaType.toString());

        if (exifData == null)
            data.add("");
        else
            data.add(exifData.toString());

        if (favorite == true)
            data.add("true");
        else
            data.add("false");

        data.add(comments.toString());

        String[] returnData = new String[data.size()];
        data.toArray(returnData);
        return returnData;
    }
}
