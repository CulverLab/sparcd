package model.query;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.query.conditions.AddQueryCondition;

import java.util.ArrayList;
import java.util.List;

public class QueryEngine
{
	private ObservableList<IQueryCondition> queryConditions = FXCollections.observableArrayList(condition -> new Observable[] {});
	private ObservableList<QueryFilters> QUERY_FILTERS = FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(QueryFilters.values()));

	public QueryEngine()
	{
		queryConditions.add(new AddQueryCondition());
	}

	public ObservableList<IQueryCondition> getQueryConditions()
	{
		return this.queryConditions;
	}

	public ObservableList<QueryFilters> getQueryFilters()
	{
		return QUERY_FILTERS;
	}

	public enum QueryFilters
	{
		SPECIES_FILTER("Species Filter"),
		LOCATION_FILTER("Location Filter"),
		START_TIME_FILTER("Start Date Filter"),
		END_TIME_FILTER("End Date Filter");

		private String displayName;

		QueryFilters(String displayName)
		{
			this.displayName = displayName;
		}

		@Override
		public String toString()
		{
			return this.displayName;
		}
	}
}
