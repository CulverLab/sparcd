package model.query;

import model.constant.SanimalMetadataFields;
import model.image.Camtrap;
import model.image.CloudUploadEntry;
import model.image.Deployments;
import model.image.Observations;
import model.image.Media;
import model.s3.ImageCollection;

import org.apache.commons.io.FilenameUtils;
import java.util.function.BiFunction;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * Class that handles excuting a query against collections
 */
public class S3QueryExecute
{
    // The number of significant digits to use when comparing locations
    private static int LOCATION_DECIMAL_SIGNIFICANCE = 5;

    /**
     * Performs the query to filter collections and images
     * 
     * @param queryBuilder the query to run
     * @param collections the list of collections to check
     * @return the result set of found images
     */
    public static S3QueryResultSet executeQuery(S3QueryBuilder queryBuilder, final List<ImageCollection> collections)
    {
        S3QueryResultSet resultSet = new S3QueryResultSet();
        List<S3QueryBuilderCondition> conditions = queryBuilder.getConditions();
        boolean isDistinct = queryBuilder.isDistinct();
        boolean isCaseInsensitive = queryBuilder.isCaseInsensitive();

        // Check that we have something to work with
        if ((conditions.size() <= 0) || (collections.size() <= 0))
        {
            return null;
        }

        // Iterate through the collections and look at the metadata
        for (ImageCollection oneCollection: collections)
        {
            if (!oneCollection.uploadsWereSynced())
            {
                // TODO: Fetch remote -> see SanimalUploadController.java:210-234
            }

            List<CloudUploadEntry> uploads = oneCollection.getUploads();
            for (CloudUploadEntry oneEntry: uploads)
            {
                Camtrap metaData = oneEntry.getMetadata().getValue();
                List<String> matches = S3QueryExecute.queryMatches(conditions, metaData, isCaseInsensitive);
                if ((matches != null) && (matches.size() > 0))
                {
                    S3QueryExecute.addMatchesToResults(resultSet, matches, isDistinct);
                }
            }
        }

        return resultSet;
    }

    /**
     * Find the matches to the query
     * 
     * @param conditions the query filtering conditions
     * @param metadata the metadata to query
     * @param caseInsensitive whether to search strings in a case insensitive manner
     * @return a list of matching image paths
     */
    private static List<String> queryMatches(List<S3QueryBuilderCondition> conditions, Camtrap metadata, boolean caseInsensitive)
    {
        List<Media> mediaList = metadata.media;
        S3QueryBuilderCondition lastAttribute = null;
        S3QueryBuilderCondition lastValue = null;
        List<String> results = new ArrayList<String>();

        for (S3QueryBuilderCondition oneCondition: conditions)
        {
            if (oneCondition.getPart() == S3QueryPart.ATTRIBUTE)
                lastAttribute = oneCondition;
            if (oneCondition.getPart() == S3QueryPart.VALUE)
                lastValue = oneCondition;

            if ((lastAttribute != null) && (lastValue != null))
            {
                List<Media> newMedia = null;

                // Store local values and reset the attribute-value pair
                S3QueryBuilderCondition curAttribute = lastAttribute;
                S3QueryBuilderCondition curValue = lastValue;
                lastAttribute = null;
                lastValue = null;

                switch (curAttribute.getValue())
                {
                    case SanimalMetadataFields.A_DATE_TIME_TAKEN:
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), Long.parseLong(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return obs.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_YEAR_TAKEN:
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), Long.parseLong(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return Long.valueOf(obs.timestamp.getYear());
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_MONTH_TAKEN:
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), Long.parseLong(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return Long.valueOf(obs.timestamp.getMonth().getValue());
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_HOUR_TAKEN:
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), Long.parseLong(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return Long.valueOf(obs.timestamp.getHour());
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_DAY_OF_YEAR_TAKEN:
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), Long.parseLong(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return Long.valueOf(obs.timestamp.getDayOfYear());
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_DAY_OF_WEEK_TAKEN:
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), Long.parseLong(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return Long.valueOf(obs.timestamp.getDayOfWeek().getValue());
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_LOCATION_NAME:
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(), curValue.getValue(), caseInsensitive, mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        for (Deployments dep: curMetadata.deployments)
                                        {
                                            if (obs.deploymentID.equals(dep.deploymentID))
                                            {
                                                return dep.locationName;
                                            }
                                        }
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_LOCATION_ID:
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(), curValue.getValue(), caseInsensitive, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        for (Deployments dep: curMetadata.deployments)
                                        {
                                            if (obs.deploymentID.equals(dep.deploymentID))
                                            {
                                                return dep.locationID;
                                            }
                                        }
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_LOCATION_LATITUDE:
                        newMedia = S3QueryExecute.filterDouble(curValue.getOperator(), Double.parseDouble(curValue.getValue()), LOCATION_DECIMAL_SIGNIFICANCE, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        for (Deployments dep: curMetadata.deployments)
                                        {
                                            if (obs.deploymentID.equals(dep.deploymentID))
                                            {
                                                return dep.latitude;
                                            }
                                        }
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_LOCATION_LONGITUDE:
                        newMedia = S3QueryExecute.filterDouble(curValue.getOperator(), Double.parseDouble(curValue.getValue()), LOCATION_DECIMAL_SIGNIFICANCE, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        for (Deployments dep: curMetadata.deployments)
                                        {
                                            if (obs.deploymentID.equals(dep.deploymentID))
                                            {
                                                return dep.longitude;
                                            }
                                        }
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_LOCATION_ELEVATION:
                        newMedia = S3QueryExecute.filterDouble(curValue.getOperator(), Double.parseDouble(curValue.getValue()), LOCATION_DECIMAL_SIGNIFICANCE, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        for (Deployments dep: curMetadata.deployments)
                                        {
                                            if (obs.deploymentID.equals(dep.deploymentID))
                                            {
                                                return dep.cameraHeight;
                                            }
                                        }
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME:
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(), curValue.getValue(), caseInsensitive, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return obs.scientificName;
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_SPECIES_COMMON_NAME:
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(), curValue.getValue(), caseInsensitive, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                String commonNameTag = "[COMMONNAME:";

                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        // Check if the comment has the common name information
                                        if (obs.comments.startsWith(commonNameTag))
                                        {
                                            int endIndex = obs.comments.indexOf("]");
                                            if (endIndex > -1)
                                            {
                                                return obs.comments.substring(commonNameTag.length(), endIndex);
                                            }
                                        }
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_SPECIES_COUNT:
                        newMedia = S3QueryExecute.filterInteger(curValue.getOperator(), Integer.parseInt(curValue.getValue()), mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return obs.count;
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_COLLECTION_ID:
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(), curValue.getValue(), caseInsensitive, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return obs.deploymentID;
                                    }
                                }

                                return null;
                            }
                            );
                        break;
                }

                mediaList = newMedia;
                if (mediaList.size() <= 0)
                {
                    break;
                }
            }
        }

        // Return the results
        for (Media med: mediaList)
        {
            results.add(med.filePath);
        }

        return results;
    }

    /**
     * Filters the media list on a long value
     * 
     * @param operator the comparison operator
     * @param value the value to compare against
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterLong(final S3QueryConditionOperators operator, final Long value,
                            final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, Long> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        for (Media med: curMedia)
        {
            Long curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
                continue;

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL) ||
                (operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.BETWEEN))
            {
                if (value == curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.NOT_EQUAL) || (operator == S3QueryConditionOperators.NOT_IN) ||
                    (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                if (value != curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (value < curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                if (value > curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (value <= curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (value >= curValue)
                    matches.add(med);
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                /* noop */
            }
        }

        return matches;
    }

    /**
     * Filters the media list on a string value
     * 
     * @param operator the comparison operator
     * @param value the value to compare against
     * @param caseInsensitive indicator for case insensitive comparisons
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterString(final S3QueryConditionOperators operator, final String value, final boolean caseInsensitive,
                                            final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, String> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        for (Media med: curMedia)
        {
            String curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
                continue;

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL) ||
                (operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.BETWEEN))
            {
                if (caseInsensitive)
                    if (value.equalsIgnoreCase(curValue))
                        matches.add(med);
                else
                    if (value.equals(curValue))
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.NOT_EQUAL) || (operator == S3QueryConditionOperators.NOT_IN) ||
                    (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                if (caseInsensitive)
                    if (!value.equalsIgnoreCase(curValue))
                        matches.add(med);
                else
                    if (!value.equals(curValue))
                            matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) < 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) < 0)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) > 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) > 0)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) <= 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) <= 0)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) >= 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) >= 0)
                        matches.add(med);
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                /* noop */
            }
        }

        return matches;
    }

    /**
     * Filters the media list on a double value
     * 
     * @param operator the comparison operator
     * @param value the value to compare against
     * @param decimalSig the number of significant decimal digits to compare against
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterDouble(final S3QueryConditionOperators operator, final Double value, final int decimalSig, 
                                            final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, Double> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        for (Media med: curMedia)
        {
            Double curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
                continue;

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL) ||
                (operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.BETWEEN))
            {
                if (value == curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.NOT_EQUAL) || (operator == S3QueryConditionOperators.NOT_IN) ||
                    (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                if (value != curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (value < curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                    if (value > curValue)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (value <= curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (value >= curValue)
                    matches.add(med);
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                /* noop */
            }
        }

        return matches;
    }

    /**
     * Filters the media list on an Integer value
     * 
     * @param operator the comparison operator
     * @param value the value to compare against
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterInteger(final S3QueryConditionOperators operator, final Integer value, 
                                             final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, Integer> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        for (Media med: curMedia)
        {
            Integer curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
                continue;

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL) ||
                (operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.BETWEEN))
            {
                if (value == curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.NOT_EQUAL) || (operator == S3QueryConditionOperators.NOT_IN) ||
                    (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                if (value != curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (value < curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                if (value > curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (value <= curValue)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (value >= curValue)
                    matches.add(med);
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                /* noop */
            }
        }

        return matches;
    }

    /**
     * Adds found matches to the result set
     * 
     * @param curResults the current result set
     * @param matches the potential matches to add
     * @param isDistinct whether to ensure the reset set is distinct (no duplicates)
     */
    private static void addMatchesToResults(S3QueryResultSet curResults, final List<String> matches, boolean isDistinct)
    {
        // Iterate through the matches
        for (String oneMatch: matches)
        {
            String path = FilenameUtils.getPath(oneMatch);
            String name = FilenameUtils.getName(oneMatch);

            if (!isDistinct || !curResults.findRow(path, name))
            {
                curResults.addRow(path, name);
            }
        }
    }
}
