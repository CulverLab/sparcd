package model.image;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Representation of Camtrap DP format. See https://tdwg.github.io/camtrap-dp/
 * 
 * @author Chris Schnaufer
 */

public final class Camtrap
{
    // The name of the deployments file
    public final static String CAMTRAP_DEPLOYMENTS_FILE = "deployments.csv";

    // The name of the media file
    public final static String CAMTRAP_MEDIA_FILE = "media.csv";

    // The name of the observations file
    public final static String CAMTRAP_OBSERVATIONS_FILE = "observations.csv";

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
     * @throws NumberFormatException If a value can't be converted
     * @throws FileNotFoundException If a file is not found
     * @throws IOException If a problem ocurrs when accessing a file
     * @throws CsvValidationException If there's a problem reading CSV files
     */
    public static final Camtrap instance(String folderPath) throws FileNotFoundException, IOException, CsvValidationException
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
    public Camtrap()
    {
        collectionID = "";
        deployments = new Vector<Deployments>();
        media = new Vector<Media>();
        observations = new Vector<Observations>();
    }

    /**
     * Populates the Deployments from the string passed in. Each CSV line is separated by a new line
     * 
     * @param csvData a string containing the data to load
     * @throws IOException if a problem occurs while accessing a file
     * @throws CsvValidationException if there's a problem with the CSV file
     */
    public final void setDeployments(String csvData) throws IOException, CsvValidationException
    {
        List<Deployments> deployments = new Vector<Deployments>();

        CSVReader reader = new CSVReader(new StringReader(csvData));
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null)
        {
            deployments.add(Deployments.instance(nextLine));
        }

        this.deployments = deployments;
    }

    /**
     * Populates the Media from the string passed in. Each CSV line is separated by a new line
     * 
     * @param csvData a string containing the data to load
     * @throws IOException if a problem occurs while accessing a file
     * @throws CsvValidationException if there's a problem with the CSV file
     */
    public final void setMedia(String csvData) throws IOException, CsvValidationException
    {
        List<Media> media = new Vector<Media>();

        CSVReader reader = new CSVReader(new StringReader(csvData));
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null)
        {
            media.add(Media.instance(nextLine));
        }

        this.media = media;
    }

    /**
     * Populates the Observations from the string passed in. Each CSV line is separated by a new line
     * 
     * @param csvData a string containing the data to load
     * @throws IOException if a problem occurs while accessing a file
     * @throws CsvValidationException if there's a problem with the CSV file
     */
    public final void setObservations(String csvData) throws IOException, CsvValidationException
    {
        List<Observations> observations = new Vector<Observations>();

        CSVReader reader = new CSVReader(new StringReader(csvData));
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null)
        {
            observations.add(Observations.instance(nextLine));
        }

        this.observations = observations;
    }

    /**
     * Writes Camtrap data to the specified folder, overwriting any existing Camtrap files
     * 
     * @param saveFolder path to the folder to save data to
     * @throws IOException if a problem occurs while accessing a file
     */
    public final void saveTo(String savePath) throws IOException
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

    /**
     * Returns the paths to the Camtrap files using the given starting path
     * 
     * @param savePath path to the folder to use as the base for the file names
     * @return an array of the files that would be generated. No check is made if the file paths returned actually exist
     */
    public String[] getFilePaths(String savePath)
    {
        String[] filePaths = new String[3];

        filePaths[0] = String.join("/", savePath, CAMTRAP_DEPLOYMENTS_FILE);
        filePaths[1] = String.join("/", savePath, CAMTRAP_MEDIA_FILE);
        filePaths[2] = String.join("/", savePath, CAMTRAP_OBSERVATIONS_FILE);

        return filePaths;
    }

    /**
     * Returns whether or not this instance has been populated
     * 
     * @return returns true if any entries exist, otherwise false
     */
    public boolean isPopulated()
    {
        return deployments.size() > 0 || media.size() > 0 || observations.size() > 0;
    }
}
