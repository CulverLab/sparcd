package model.query.conditions;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.query.ElasticSearchQuery;
import model.query.IQueryCondition;
import model.util.SettingsData;

/**
 * Data model used by the "Elevation filter" query condition
 */
public class ElevationCondition implements IQueryCondition
{
	// The elevation to compute on
	private DoubleProperty elevation = new SimpleDoubleProperty(0);
	// The units to interpret elevation as
	private ObjectProperty<SettingsData.DistanceUnits> units = new SimpleObjectProperty<>(SettingsData.DistanceUnits.Meters);
	// The comparison operator
	private ObjectProperty<ElevationComparisonOperators> comparisonOperator = new SimpleObjectProperty<>(ElevationComparisonOperators.Equal);

	// A list of possible comparison operators to filter
	private ObservableList<ElevationComparisonOperators> operatorList = FXCollections.observableArrayList(ElevationComparisonOperators.values());
	// A list of possible units to filter
	private ObservableList<SettingsData.DistanceUnits> unitList = FXCollections.observableArrayList(SettingsData.DistanceUnits.values());

	/**
	 * This query condition ensures only selected years are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(ElasticSearchQuery query)
	{
		if (this.comparisonOperator.getValue() != null)
		{
			Double distanceInMeters = this.units.getValue().formatToMeters(this.elevation.getValue());
			query.addElevationCondition(distanceInMeters, this.comparisonOperator.getValue());
		}
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "ElevationCondition.fxml";
	}

	/**
	 * The elevation property as an integer
	 *
	 * @return Elevation to query
	 */
	public DoubleProperty elevationProperty()
	{
		return this.elevation;
	}

	/**
	 * The units used by elevation
	 *
	 * @return The units of elevation
	 */
	public ObjectProperty<SettingsData.DistanceUnits> unitsProperty()
	{
		return this.units;
	}

	/**
	 * The operator used to compare the current elevation and the one on cyverse
	 *
	 * @return The comparison operator
	 */
	public ObjectProperty<ElevationComparisonOperators> comparisonOperatorProperty()
	{
		return this.comparisonOperator;
	}

	/**
	 * Getter for all possible distance units
	 *
	 * @return A list of possible units
	 */
	public ObservableList<SettingsData.DistanceUnits> getUnitList()
	{
		return this.unitList;
	}

	/**
	 * Getter for all possible operators
	 *
	 * @return A list of possible operators
	 */
	public ObservableList<ElevationComparisonOperators> getOperatorList()
	{
		return this.operatorList;
	}

	/**
	 * Enum of elevation comparison operators
	 */
	public enum ElevationComparisonOperators
	{
		Equal("Equal To"),
		GreaterThan("Greater Than"),
		GreaterThanOrEqual("Greater Than or Equal To"),
		LessThan("Less Than"),
		LessThanOrEqual("Less Than or Equal To");

		private String displayName;

		/**
		 * Constructor takes the name to display and an operator that is the query condition operator equivelant
		 *
		 * @param displayName The name to visually display
		 */
		ElevationComparisonOperators(String displayName)
		{
			this.displayName = displayName;
		}

		/**
		 * Returns the display name as the elevation condition toString
		 *
		 * @return The display name
		 */
		@Override
		public String toString()
		{
			return this.displayName;
		}
	}
}
