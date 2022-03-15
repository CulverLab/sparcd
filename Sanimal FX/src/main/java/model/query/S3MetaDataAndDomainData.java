package model.query;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.util.Objects;

/**
 * Class containing metadata from the query
 */
public class S3MetaDataAndDomainData
{
    private String attribute;
    private String value;
    private String unit;

    /**
     * Creates an initialized instance of the class
     * 
     * @param attribute the attribute associated with the data
     * @param value the value of the attribute
     * @return an initialized instance
     */
    public static S3MetaDataAndDomainData instance(final String attribute, final String value)
    {
        return new S3MetaDataAndDomainData(attribute, value, null);
    }

    /**
     * Creates an initialized instance of the class with the valueID as the hash of the attribute 
     * represented as the string of a long integer
     * 
     * @param attribute the attribute associated with the data
     * @param value the value of the attribute
     * @return an initialized instance
     * @throws NoSuchAlgorithmException if the hashing algorithm is not supported
     * @throws UnsupportedEncodingException if the UTF-8 encoding is not supported
     */
    public static S3MetaDataAndDomainData instanceWithUnits(final String attribute, final String value)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        md.update(value.getBytes("UTF-8"));

        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);

        String valueID = Objects.toString(bigInt.longValue());

        return new S3MetaDataAndDomainData(attribute, value, valueID);
    }

    /**
     * Default constructor is not available
     */
    private S3MetaDataAndDomainData()
    {
        attribute = null;
        value = null;
        unit = null;
    }

    /**
     * Constructor with parameters
     * 
     * @param attribute the attribute associated with the data
     * @param value the value of the attribute
     * @param unit a unique value associated with the value (may be null)
     * @return an initialized instance
     */
    public S3MetaDataAndDomainData(final String attribute, final String value, final String unit)
    {
        this.attribute = attribute;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the attribute string
     * 
     * @return the attribute string
     */
    public String getAttribute()
    {
        return this.attribute;
    }

    /**
     * Returns the value
     * 
     * @return the string value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Returns the unit
     * 
     * @return the unit Integer
     */
    public String getUnit()
    {
        return this.unit;
    }
}
