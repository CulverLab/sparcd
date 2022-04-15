package model.query;

import java.util.ArrayList;

/**
 * Class representing query result rows
 */
public class S3QueryResultRow extends ArrayList<String>
{
    /**
     * Creates a new row
     * 
     * @param path the first column
     * @param name the second column
     * @return {@link S3QueryResultRow} the initialized instance
     */
    public static S3QueryResultRow instance(final String path, final String name)
    {
        return new S3QueryResultRow(path, name);
    }

    /**
     * Default constructor
     */
    private S3QueryResultRow()
    {}

    /**
     * Constructor with parameters
     * 
     * @param path the first column
     * @param name the second column
     */
    private S3QueryResultRow(final String path, final String name)
    {
        this.add(path);
        this.add(name);
    }
}
