package model.image;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

/**
 * Representation of Camtrap DP observationss data. See https://tdwg.github.io/camtrap-dp/data/#observations
 * 
 * @author Chris Schnaufer
 */


public final class Observations
{
    // Project-wide unique observation ID
    public String observationID;

    // Deployment ID observation belongs to
    public String deploymentID;

    // Project-wide unique identifier of observation sequence
    public String sequenceID;

    // The unique media ID of media associated with the observation ID
    public String mediaID;

    // Date and time of observation
    public LocalDateTime timestamp;

    // The type of observation
    public String observationType;

    // Flag indicating the observation is part of the camera setup procedure
    public boolean cameraSetup;

    // Taxon identifier
    public String taxonID;

    // Scientific name of observed individuals
    public String scientificName;

    // The number of observed individuals
    public int count;

    // Number of previously uncounted individuals in associated media file
    public int countNew;

    // Lifestage or age of observed individuals
    public String lifeStage;

    // The gender of observed individuals
    public String sex;

    // Dominant behaviour of observed individuals
    public String behaviour;

    // Project-wide unique individual identifiers
    public String individualID;

    // Classification method used
    public String classificationMethod;

    // Name or unique identifier of person or algorithm that classified observation
    public String classifiedBy;

    // Date and time of classification
    public LocalDateTime classificationTimestamp;

    // Accuracy confidence of classification between (0.0 and 1.0)
    public double classificationConfidence;

    // Comments and notes
    public String comments;

    // Internal unique ID of observation, as assigned by data management system. e.g.: database ID
    public String _id;

    /**
     * Constructor
     */
    public Observations()
    {
        // Must be externally supplied
        observationID = null;
        timestamp = null;
        classificationTimestamp = null;

        // Default values
        deploymentID = "";
        sequenceID = "";
        mediaID = "";
        observationType = "";
        cameraSetup =  false;
        taxonID = "";
        scientificName = "";
        count = 0;
        countNew = 0;
        lifeStage = "";
        sex = "";
        behaviour = "";
        individualID = "";
        classificationMethod = "";
        classifiedBy = "";
        classificationConfidence = 1.0;
        comments = "";
        _id = "";
    }

    /**
     * Returns an initialized instance of the Observations class
     * 
     * @param values the array of values
     * @return the initialized instance
     * @throws InvalidParameterException if there are elements missing from the array
     * @throws NumberFormatException if a value can't be converted
     */
    public static final Observations instance(String[] values)
    {
        // Check that we have enough fields
        if (values.length < 20)
        {
            throw new InvalidParameterException("Missing elements for Captrap Observations specification");
        }

        // Create a new instance
        Observations obs = new Observations();

        // Populate the data
        if ((values[0] == null) || (values[0] == ""))
            obs.observationID  = null;
        else
            obs.observationID = values[0];

        obs.deploymentID = values[1];
        obs.sequenceID = values[2];
        obs.mediaID = values[3];

        if ((values[4] != null) && (values[4].length() > 0))
            obs.timestamp = LocalDateTime.parse(values[4], DateTimeFormatter.ISO_DATE_TIME);
        else
            obs.timestamp = null;

        obs.observationType = values[5];

        if ((values[6] == "true") || (values[6] == "1"))
            obs.cameraSetup = true;
        else
            obs.cameraSetup = false;

        obs.taxonID = values[7];
        obs.scientificName = values[8];
        obs.count = Integer.parseInt(values[9]);
        obs.countNew = Integer.parseInt(values[10]);
        obs.lifeStage = values[11];
        obs.sex = values[12];
        obs.behaviour = values[13];
        obs.individualID = values[14];
        obs.classificationMethod = values[15];
        obs.classifiedBy = values[16];

        if ((values[17] != null) && (values[17].length() > 0))
            obs.classificationTimestamp = LocalDateTime.parse(values[17], DateTimeFormatter.ISO_DATE_TIME);
        else
            obs.classificationTimestamp = null;

        obs.classificationConfidence = Double.parseDouble(values[18]);
        obs.comments = values[19];

        return obs;
    }

    /**
     * Returns the data as a string array
     * 
     * @return the array of values
     */
    public final String[] toArray()
    {
        List<String> data = new Vector<String>();

        if (observationID == null)
            data.add("");
        else
            data.add(observationID.toString());

        data.add(deploymentID.toString());
        data.add(sequenceID.toString());
        data.add(mediaID.toString());

        if (timestamp != null)
            data.add(timestamp.format(DateTimeFormatter.ISO_DATE_TIME));
        else
            data.add("");

        data.add(observationType.toString());

        if (cameraSetup == true)
            data.add("true");
        else
            data.add("false");

        data.add(taxonID.toString());
        data.add(scientificName.toString());
        data.add(String.format("%d", count));
        data.add(String.format("%d", countNew));
        data.add(lifeStage.toString());
        data.add(sex.toString());
        data.add(behaviour.toString());
        data.add(individualID.toString());
        data.add(classificationMethod.toString());
        data.add(classifiedBy.toString());

        if (classificationTimestamp != null)
            data.add(classificationTimestamp.format(DateTimeFormatter.ISO_DATE_TIME));
        else
            data.add("");

        data.add(String.format("%f", classificationConfidence));
        data.add(comments.toString());

        String[] returnData = new String[data.size()];
        data.toArray(returnData);
        return returnData;
    }
}
