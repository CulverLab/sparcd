package model.image;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Representation of Camtrap DP format. See https://tdwg.github.io/camtrap-dp/
 * 
 * @author Chris Schnaufer
 */

public final class Camtrap
{
    // The name of the deployments file
    private final static String CAMTRAP_DEPLOYMENTS_FILE = "deployments.csv";

    // The name of the media file
    private final static String CAMTRAP_MEDIA_FILE = "media.csv";

    // The name of the observations file
    private final static String CAMTRAP_OBSERVATIONS_FILE = "observations.csv";

    // The collection this data belongs to
    private String collectionID;

    // The list of deployments
    public List<Deployments>    deployments;

    // The list of media
    public List<Media>          media;

    // The list of observations
    public List<Observations>   observations;

    /**
     * Returns an instance of Camtrap after loading the data from the specified folder
     * 
     * @param folderPath The path of the folder to load
     * @throws InvalidParameterException If the folder path doesn't contain the expected files, or data is invalid
     * @throws NumberFormatException if a value can't be converted
     */
    public static final Camtrap instance(String folderPath)
    {
        Camtrap returnValue = null;
        List<Deployments> deployments = null;
        List<Media> media = null;
        List<Observations> observations = null;

        // Check for files existing
        String depPath = String.join("/", folderPath, CAMTRAP_DEPLOYMENTS_FILE);
        File inFile = new File(depPath);
        if (inFile.exists() && !inFile.isDirectory())
        {
            // Read in the deployments file
            CSVReader reader = new CSVReader(new FileReader(inFile));
            String[] nextLine;

            deployments = new Vector<Deployments>();
            while ((nextLine = reader.readNext()) != null)
            {
                deployments.add(Deployments.instance(nextLine));
            }
        }
        else 
        {
            throw new InvalidParameterException("Missing deployments file on Camtrap folder path '" + folderPath + "'");
        }

        String mediaPath = String.join("/", folderPath, CAMTRAP_MEDIA_FILE);
        inFile = new File(mediaPath);
        if (inFile.exists() && !inFile.isDirectory())
        {
            // Read in the media file
            CSVReader reader = new CSVReader(new FileReader(inFile));
            String[] nextLine;

            media = new Vector<Media>();
            while ((nextLine = reader.readNext()) != null)
            {
                media.add(Media.instance(nextLine));
            }
        }
        else 
        {
            throw new InvalidParameterException("Missing media file on Camtrap folder path '" + folderPath + "'");
        }

        String obsPath = String.join("/", folderPath, CAMTRAP_OBSERVATIONS_FILE);
        inFile = new File(obsPath);
        if (inFile.exists() && !inFile.isDirectory())
        {
            // Read in the observations file
            CSVReader reader = new CSVReader(new FileReader(inFile));
            String[] nextLine;

            observations = new Vector<Observations>();
            while ((nextLine = reader.readNext()) != null)
            {
                observations.add(Observations.instance(nextLine));
            }
        }
        else 
        {
            throw new InvalidParameterException("Missing observations file on Camtrap folder path '" + folderPath + "'");
        }

        returnValue = new Camtrap();

        returnValue.deployments = deployments;
        returnValue.media = media;
        returnValue.observations = observations;

        return returnValue;
    }

    /**
     * Constructor
     */
    private Camtrap()
    {
        collectionID = "";
        deployments = new Vector<Deployments>();
        media = new Vector<Media>();
        observations = new Vector<Observations>();
    }

    /**
     * Writes Camtrap data to the specified folder, overwriting any existing Camtrap files
     * 
     * @param saveFolder path to the folder to save data to
     */
    public final void saveTo(String savePath)
    {
        // Save deployment data
        String outFile = String.join("/", savePath, CAMTRAP_DEPLOYMENTS_FILE);
        CSVWriter outCsv = new CSVWriter(new FileWriter(outFile));

        for (Deployments oneDep: deployments)
        {
            outCsv.writeNext(oneDep.toArray());
        }
        outCsv.close();

        // Save media data
        outFile = String.join("/", savePath, CAMTRAP_MEDIA_FILE);
        outCsv = new CSVWriter(new FileWriter(outFile));

        for (Media oneMed: media)
        {
            outCsv.writeNext(oneMed.toArray());
        }
        outCsv.close();

        // Save observation data
        outFile = String.join("/", savePath, CAMTRAP_OBSERVATIONS_FILE);
        outCsv = new CSVWriter(new FileWriter(outFile));

        for (Observations oneObs: observations)
        {
            outCsv.writeNext(oneObs.toArray());
        }
        outCsv.close();
    }
}
