package model.query;

/**
 * Represents possible operator values for general query conditions used in the
 * gen query builder
 *
 * Derived from original file - author: Mike Conway - DICE (www.irods.org)
 *
 * @author Chris Schnaufer
 */
public enum S3QueryConditionOperators {

	NOT_EQUAL("<>"),
	LESS_THAN_OR_EQUAL_TO("<="),
	GREATER_THAN_OR_EQUAL_TO(">="),
/*	NOT_LIKE("not like"),
	SOUNDS_LIKE("sounds like"),
	SOUNDS_NOT_LIKE("sounds not like"),
*/	TABLE("table"),
	NUMERIC_LESS_THAN("n<"),
	NUMERIC_LESS_THAN_OR_EQUAL_TO("n<="),
	NUMERIC_GREATER_THAN_OR_EQUAL_TO("n>="),
	NUMERIC_GREATER_THAN("n>"),
	NUMERIC_EQUAL("n="),
	EQUAL("="),
	LESS_THAN("<"),
	GREATER_THAN(">"),
	IN("in"),
	NOT_IN("not in"),
	BETWEEN("between"),
	NOT_BETWEEN("not between")
/*	,
	LIKE("like")*/
	;

	private String operatorAsString;		// The operator string representation

	/**
	 * Constructor with a string
	 * 
	 * @param operatorAsString the the string representation of the operator
	 */
	S3QueryConditionOperators(final String operatorAsString)
	{
		this.operatorAsString = operatorAsString;
	}

	/**
	 * Returns the string representation of the operator
	 * 
	 * @return the operator as a string
	 */
	public String getOperatorAsString()
	{
		return operatorAsString;
	}

	/**
	 * Get the query operator as a string value, (e.g. ">=" representation)
	 * 
	 * @param stringValue the symbolic value
	 * @return the corresponding enum or null when not found
	 * @throws IllegalArgumentException if the string value is empty or null
	 */
	public static S3QueryConditionOperators getOperatorFromStringValue(final String stringValue) throws IllegalArgumentException
	{
		if (stringValue == null || stringValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty stringValue");
		}

		if (stringValue.equalsIgnoreCase(NOT_EQUAL.operatorAsString)) {
			return NOT_EQUAL;
		}

		if (stringValue.equalsIgnoreCase(LESS_THAN_OR_EQUAL_TO.operatorAsString)) {
			return LESS_THAN_OR_EQUAL_TO;
		}

		if (stringValue.equalsIgnoreCase(GREATER_THAN_OR_EQUAL_TO.operatorAsString)) {
			return GREATER_THAN_OR_EQUAL_TO;
		}
/*		if (stringValue.equalsIgnoreCase(NOT_LIKE.operatorAsString)) {
			return NOT_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_LIKE.operatorAsString)) {
			return SOUNDS_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_NOT_LIKE.operatorAsString)) {
			return SOUNDS_NOT_LIKE;
		}
*/		if (stringValue.equalsIgnoreCase(TABLE.operatorAsString)) {
			return TABLE;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN.operatorAsString)) {
			return NUMERIC_LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN_OR_EQUAL_TO.operatorAsString)) {
			return NUMERIC_LESS_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN_OR_EQUAL_TO.operatorAsString)) {
			return NUMERIC_GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN.operatorAsString)) {
			return NUMERIC_GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_EQUAL.operatorAsString)) {
			return NUMERIC_EQUAL;
		}
		if (stringValue.equalsIgnoreCase(EQUAL.operatorAsString)) {
			return EQUAL;
		}
		if (stringValue.equalsIgnoreCase(LESS_THAN.operatorAsString)) {
			return LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(GREATER_THAN.operatorAsString)) {
			return GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(IN.operatorAsString)) {
			return IN;
		}
		if (stringValue.equalsIgnoreCase(NOT_IN.operatorAsString)) {
			return NOT_IN;
		}
		if (stringValue.equalsIgnoreCase(BETWEEN.operatorAsString)) {
			return BETWEEN;
		}
		if (stringValue.equalsIgnoreCase(NOT_BETWEEN.operatorAsString)) {
			return NOT_BETWEEN;
		}
/*		if (stringValue.equalsIgnoreCase(LIKE.operatorAsString)) {
			return LIKE;
		}
*/
		return null;
	}

	/**
	 * Turn a string representation of the enum value to the actual enum value (e.g. "GREATER THAN" to ">")
	 * 
	 * @param stringValue the enum name as a string
	 * @return the corresponding enum value or null when not found
	 * @throws IllegalArgumentException if the string value is empty or null
	 */
	public static S3QueryConditionOperators getOperatorFromEnumStringValue(final String stringValue) throws IllegalArgumentException
	{
		if (stringValue == null || stringValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty stringValue");
		}

		if (stringValue.equalsIgnoreCase(NOT_EQUAL.toString())) {
			return NOT_EQUAL;
		}

		if (stringValue.equalsIgnoreCase(LESS_THAN_OR_EQUAL_TO.toString())) {
			return LESS_THAN_OR_EQUAL_TO;
		}

		if (stringValue.equalsIgnoreCase(GREATER_THAN_OR_EQUAL_TO.toString())) {
			return GREATER_THAN_OR_EQUAL_TO;
		}
/*		if (stringValue.equalsIgnoreCase(NOT_LIKE.toString())) {
			return NOT_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_LIKE.toString())) {
			return SOUNDS_LIKE;
		}
		if (stringValue.equalsIgnoreCase(SOUNDS_NOT_LIKE.toString())) {
			return SOUNDS_NOT_LIKE;
		}
*/		if (stringValue.equalsIgnoreCase(TABLE.toString())) {
			return TABLE;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN.toString())) {
			return NUMERIC_LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_LESS_THAN_OR_EQUAL_TO.toString())) {
			return NUMERIC_LESS_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN_OR_EQUAL_TO.toString())) {
			return NUMERIC_GREATER_THAN_OR_EQUAL_TO;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_GREATER_THAN.toString())) {
			return NUMERIC_GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(NUMERIC_EQUAL.toString())) {
			return NUMERIC_EQUAL;
		}
		if (stringValue.equalsIgnoreCase(EQUAL.toString())) {
			return EQUAL;
		}
		if (stringValue.equalsIgnoreCase(LESS_THAN.toString())) {
			return LESS_THAN;
		}
		if (stringValue.equalsIgnoreCase(GREATER_THAN.toString())) {
			return GREATER_THAN;
		}
		if (stringValue.equalsIgnoreCase(NOT_IN.toString())) {
			return NOT_IN;
		}
		if (stringValue.equalsIgnoreCase(IN.toString())) {
			return IN;
		}
		if (stringValue.equalsIgnoreCase(BETWEEN.toString())) {
			return BETWEEN;
		}
		if (stringValue.equalsIgnoreCase(NOT_BETWEEN.toString())) {
			return NOT_BETWEEN;
		}
/*		if (stringValue.equalsIgnoreCase(LIKE.toString())) {
			return LIKE;
		}
*/
		return null;
	}
}
