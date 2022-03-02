package model.query;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the built query
 */
public class S3QueryBuilder
{
    private final List<S3QueryBuilderCondition> conditions = new ArrayList<S3QueryBuilderCondition>();
    private boolean distinct = false;
    private boolean caseInsensitive = false;

    /**
     * Constructor
     * 
     * @param distinct {@code boolean} indicates if the query results are distinct, or not
     * @param caseInsensitive {@code boolean} indicates that queries are case insensitive, or not (when filtering)
     */
    public S3QueryBuilder(final boolean distinct, final boolean caseInsensitive)
    {
        this.distinct = distinct;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Add a general query condition to the builder query
     * 
     * @param part identifier of query field
     * @param operator operator to apply
     * @param value value to query on
     * @return {@link S3QueryBuilder} this instance
     * @throws InvalidParameterException if a parameter is invalid
     */
    public S3QueryBuilder addConditionAsQueryField(final S3QueryPart part, final S3QueryConditionOperators operator, final String value)
    {
        // Check parameters
        if (part == null)
            throw new InvalidParameterException("Query part is invalid for String condition");
        if (operator == null)
            throw new InvalidParameterException("Query operator is invalid for String condition");
        if (value == null)
            throw new InvalidParameterException("Query value is invalid for String condition");

        // Add this entry
        conditions.add(S3QueryBuilderCondition.instance(part, operator, value));
        return this;
    }

    /**
     * Add a general query condition to the builder query
     * 
     * @param part identifier of query field
     * @param operator operator to apply
     * @param value value to query on
     * @return {@link S3QueryBuilder} this instance
     */
    public S3QueryBuilder addConditionAsQueryField(final S3QueryPart part, final S3QueryConditionOperators operator, final int value)
    {
        // Check parameters
        if (part == null)
            throw new InvalidParameterException("Query part is invalid for integer condition");
        if (operator == null)
            throw new InvalidParameterException("Query operator is invalid for integer condition");

        // Add this entry
        conditions.add(S3QueryBuilderCondition.instance(part, operator, String.valueOf(value)));
        return this;
    }

    /**
     * Add a general query condition to the builder query
     * 
     * @param part identifier of query field
     * @param operator operator to apply
     * @param value value to query on
     * @return {@link S3QueryBuilder} this instance
     */
    public S3QueryBuilder addConditionAsQueryField(final S3QueryPart part, final S3QueryConditionOperators operator, final long value)
    {
        // Check parameters
        if (part == null)
            throw new InvalidParameterException("Query part is invalid for long condition");
        if (operator == null)
            throw new InvalidParameterException("Query operator is invalid for long condition");

        // Add this entry
        conditions.add(S3QueryBuilderCondition.instance(part, operator, String.valueOf(value)));
        return this;
    }

    /**
     * Returns the list of conditions
     * 
     * @return the list of conditions
     */
    public final List<S3QueryBuilderCondition> getConditions()
    {
        return this.conditions;
    }

    /**
     * Returns if the query is to have distinct results (no duplicates)
     * 
     * @return {@code boolean} whether the results are to be distinct
     */
    public final boolean isDistinct()
    {
        return this.distinct;
    }

    /**
     * Returns if the filtering is to be case insensitive
     * 
     * @return {@code boolean} whether the filtering is case insensitive
     */
    public final boolean isCaseInsensitive()
    {
        return this.caseInsensitive;
    }
}
