package model.query;

/**
 * Class representing a query condition
 */
public class S3QueryBuilderCondition
{
    private final S3QueryPart part;
    private final S3QueryConditionOperators operator;
    private final String value;

    /**
     * Returns an initialized instance of this class
     * 
     * @param part identifier of query field
     * @param operator operator to apply
     * @param value value to query on
     * @return {@link S3QueryBuilderCondition} the populated instance
     */
    public static S3QueryBuilderCondition instance(final S3QueryPart part, final S3QueryConditionOperators operator, final String value)
    {
        return new S3QueryBuilderCondition(part, operator, value);
    }

    /**
     * Default constructor
     */
    private S3QueryBuilderCondition()
    {
        this.part = null;
        this.operator = null;
        this.value = null;
    }

    /**
     * Populated constructor
     * 
     * @param part identifier of query field
     * @param operator operator to apply
     * @param value value to query on
     */
    public S3QueryBuilderCondition(final S3QueryPart part, final S3QueryConditionOperators operator, final String value)
    {
        this.part = part;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Returns the part identifier of the query
     * 
     * @return {@code S3QueryPart} the query part identifier
     */
    final public S3QueryPart getPart()
    {
        return this.part;
    }

    /**
     * Returns the operator of the query
     * 
     * @return {@code S3QueryConditionOperators} the operator
     */
    final public S3QueryConditionOperators getOperator()
    {
        return this.operator;
    }

    /**
     * Returns the value of the query
     * 
     * @return {@code String} the value
     */
    final public String getValue()
    {
        return this.value;
    }
}
