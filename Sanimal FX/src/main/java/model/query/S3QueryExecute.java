package model.query;

import model.constant.SanimalMetadataFields;
import model.image.Camtrap;
import model.image.CloudUploadEntry;
import model.image.Deployments;
import model.image.Observations;
import model.image.Media;
import model.s3.ImageCollection;
import model.SanimalData;

import org.apache.commons.io.FilenameUtils;
import java.util.function.BiFunction;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Class that handles excuting a query against collections
 */
public class S3QueryExecute
{
    // The number of significant digits to use when comparing locations
    private static Double LOCATION_DECIMAL_MAX_DIFFERENCE = 0.00001;

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

        System.out.println("executeQuery(): BEGIN");

        // Check that we have something to work with
        if ((conditions.size() <= 0) || (collections.size() <= 0))
        {
            System.out.println("executeQuery(): no collections");
            return null;
        }

        // Iterate through the collections and look at the metadata
        for (ImageCollection oneCollection: collections)
        {
            System.out.println("executeQuery(): collection: '" + oneCollection.getName() + "'");
            if (!oneCollection.uploadsWereSynced())
            {
                System.out.println("    non-synched collection");
            }

            List<CloudUploadEntry> uploads = oneCollection.getUploads();
            System.out.println("executeQuery(): checking uploads: " + uploads.size());
            for (CloudUploadEntry oneEntry: uploads)
            {
                System.out.println("executeQuery(): checking camtrap data");
                Camtrap metaData = oneEntry.getMetadata().getValue();
                System.out.println("executeQuery(): query match");
                List<String> matches = S3QueryExecute.queryMatches(conditions, metaData, isCaseInsensitive);
                System.out.println("    after query match: " + matches.size());
                if ((matches != null) && (matches.size() > 0))
                {
                    System.out.println("executeQuery(): adding matches");
                    S3QueryExecute.addMatchesToResults(resultSet, oneEntry.getBucket(), matches, isDistinct);
                    System.out.println("    after adding matches");
                }
            }
        }

        System.out.println("executeQuery(): result count: " + resultSet);
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

        System.out.println("queryMatches(): CONDITIONS");
        for (S3QueryBuilderCondition oneCondition: conditions)
        {
            if (oneCondition.getPart() == S3QueryPart.ATTRIBUTE)
                System.out.println("   a: " + oneCondition.getValue() + "  " + oneCondition.getOperator());
            if (oneCondition.getPart() == S3QueryPart.VALUE)
                System.out.println("   v: " + oneCondition.getValue() + "  " + oneCondition.getOperator());
        }

        System.out.println("queryMatches(): conditions count -> " + conditions.size());
        for (S3QueryBuilderCondition oneCondition: conditions)
        {
            if (oneCondition.getPart() == S3QueryPart.ATTRIBUTE)
                lastAttribute = oneCondition;
            if (oneCondition.getPart() == S3QueryPart.VALUE)
                lastValue = oneCondition;

            if ((lastAttribute != null) && (lastValue != null))
            {
                System.out.println("queryMatches(): attr: '" + lastAttribute.getValue() + "'  value: '" + lastValue.getValue() + "'");
                List<Media> newMedia = null;

                // Store local values and reset the attribute-value pair
                S3QueryBuilderCondition curAttribute = lastAttribute;
                S3QueryBuilderCondition curValue = lastValue;
                lastAttribute = null;
                lastValue = null;

                switch (curAttribute.getValue())
                {
                    case SanimalMetadataFields.A_DATE_TIME_TAKEN:
                        System.out.println("queryMatches(): DATE TIME check");
                        newMedia = S3QueryExecute.filterDate(curValue.getOperator(), 
                            S3QueryExecute.getDateValuesArray(curValue.getValue()),
                            mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        return obs.timestamp;
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_YEAR_TAKEN:
                        System.out.println("queryMatches(): DATE YEAR check");
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), 
                            S3QueryExecute.getLongValuesArray(curValue.getValue()),
                            mediaList, metadata,
                            (media, curMetadata) ->
                            {
                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        System.out.println("   timestamp: " + obs.timestamp + "  year: " + obs.timestamp.getYear());
                                        return Long.valueOf(obs.timestamp.getYear());
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_DATE_MONTH_TAKEN:
                        System.out.println("queryMatches(): DATE MONTH check");
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), 
                            S3QueryExecute.getLongValuesArray(curValue.getValue()), 
                            mediaList, metadata,
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
                        System.out.println("queryMatches(): DATE HOUR check");
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), 
                            S3QueryExecute.getLongValuesArray(curValue.getValue()),
                            mediaList, metadata,
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
                        System.out.println("queryMatches(): DATE DAY OF YEAR check");
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), 
                            S3QueryExecute.getLongValuesArray(curValue.getValue()), 
                            mediaList, metadata,
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
                        System.out.println("queryMatches(): DATE DDAY OF WEEK check");
                        newMedia = S3QueryExecute.filterLong(curValue.getOperator(), 
                            S3QueryExecute.getLongValuesArray(curValue.getValue()), 
                            mediaList, metadata,
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
                        System.out.println("queryMatches(): LOCATION NAME check");
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                            S3QueryExecute.getStringValuesArray(curValue.getValue()),
                            caseInsensitive, mediaList, metadata,
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
                        System.out.println("queryMatches(): LOCATION ID check");
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                            S3QueryExecute.getStringValuesArray(curValue.getValue()),
                            caseInsensitive, mediaList, metadata,
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
                        System.out.println("queryMatches(): LATITUDE check");
                        newMedia = S3QueryExecute.filterDouble(curValue.getOperator(), 
                            S3QueryExecute.getDoubleValuesArray(curValue.getValue()),
                            LOCATION_DECIMAL_MAX_DIFFERENCE, mediaList, metadata,
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
                        System.out.println("queryMatches(): LONGITUDE check");
                        newMedia = S3QueryExecute.filterDouble(curValue.getOperator(), 
                            S3QueryExecute.getDoubleValuesArray(curValue.getValue()),
                            LOCATION_DECIMAL_MAX_DIFFERENCE, mediaList, metadata,
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
                        System.out.println("queryMatches(): ELEVATION check");
                        newMedia = S3QueryExecute.filterDouble(curValue.getOperator(), 
                            S3QueryExecute.getDoubleValuesArray(curValue.getValue()), 
                            LOCATION_DECIMAL_MAX_DIFFERENCE, mediaList, metadata,
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
                        System.out.println("queryMatches(): SPECIES check");
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                            S3QueryExecute.getStringValuesArray(curValue.getValue()),
                            caseInsensitive, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                System.out.println("   compare find media -> '" + media.mediaID + "' obs count: " + curMetadata.observations.size());
                                for (Observations obs: curMetadata.observations)
                                {
                                    System.out.println("    media compare to: '" + obs.mediaID + "'");
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        System.out.println("    FOUND '" + obs.scientificName + "'");
                                        return obs.scientificName;
                                    }
                                }

                                return null;
                            }
                            );
                        break;

                    case SanimalMetadataFields.A_SPECIES_COMMON_NAME:
                        System.out.println("queryMatches(): COMMON NAME check");
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                            S3QueryExecute.getStringValuesArray(curValue.getValue()),
                            caseInsensitive, mediaList, metadata,
                            (media, curMetadata) -> 
                            {
                                final String commonNameTag = "[COMMONNAME:";
                                final String commonNameEndTag = "]";

                                for (Observations obs: curMetadata.observations)
                                {
                                    if (obs.mediaID.equals(media.mediaID))
                                    {
                                        // Check if the comment has the common name information
                                        if (obs.comments.startsWith(commonNameTag))
                                        {
                                            int endIndex = obs.comments.indexOf(commonNameEndTag);
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
                        System.out.println("queryMatches(): SPECIES COUNT check");
                        newMedia = S3QueryExecute.filterInteger(curValue.getOperator(), 
                            S3QueryExecute.getIntegerValuesArray(curValue.getValue()), 
                            mediaList, metadata,
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
                        System.out.println("queryMatches(): COLLECTION ID check");
                        newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                            S3QueryExecute.getStringValuesArray(curValue.getValue()),
                            caseInsensitive, mediaList, metadata,
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
                    System.out.println("queryMatches(): breaking out due to no more matches");
                    break;
                }
            }
        }

        // Return the results
        for (Media med: mediaList)
        {
            results.add(med.filePath);
        }

        System.out.println("queryMatches(): DONE -> " + results.size());
        return results;
    }

    /**
     * Filters the media list on a long value
     * 
     * @param operator the comparison operator
     * @param values the value(s) to compare against
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterLong(final S3QueryConditionOperators operator, final List<Long> values,
                            final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, Long> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        // Check that there is something to work with
        if ((values.size() <= 0) || (curMedia.size() <= 0))
            return matches;

        // Get the first value to make some comparisons easier
        Long value = values.get(0);

        System.out.println("filterLong(): media count -> " + curMedia.size());

        for (Media med: curMedia)
        {
            Long curValue = getFilterValue.apply(med, metadata);
            System.out.println("filterLong(): curValue -> " + curValue);

            // Skip missing values
            if (curValue == null)
            {
                System.out.println("filterLong(): skipping null value");
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                System.out.println("filterLong(): EQUAL, NUMERIC_EQUAL -> " + value + "  " + curValue);
                if (curValue - value == 0)
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
                System.out.println("filterLong(): IN, NOT_IN");
                // Find the match and handle the codition after
                boolean matched = false;
                for (Long oneValue: values)
                {
                    if (oneValue - curValue == 0)
                    {
                        matched = true;
                        break;
                    }
                }

                // Handle the condition
                if (matched && (operator == S3QueryConditionOperators.IN))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                System.out.println("filterLong(): BETWEEN, NOT_BETWEEN");
                // Find the match and handle the codition after
                boolean matched = false;
                if (values.size() >= 2)
                {
                    Long first = values.get(0);
                    Long second = values.get(1);

                    if ((curValue - first >= 0) && (curValue - second <= 0))
                    {
                        matched = true;
                    }

                    // Handle the condition
                    if (matched && (operator == S3QueryConditionOperators.BETWEEN))
                    {
                        System.out.println("    adding");
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        System.out.println("    adding");
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                System.out.println("filterLong(): NOT_EQUAL");
                if (curValue - value != 0)
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                System.out.println("filterLong(): LESS_THAN, NUMERIC_LESS_THAN");
                if (curValue - value < 0)
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                System.out.println("filterLong(): GREATER_THAN, NUMERIC_GREATER_THAN");
                if (curValue - value > 0)
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterLong(): LESS_THAN_OR_EQUAL_TO, NUMERIC_LESS_THAN_OR_EQUAL_TO");
                if (curValue - value <= 0)
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterLong(): GREATER_THAN_OR_EQUAL_TO, NUMERIC_GREATER_THAN_OR_EQUAL_TO: " + value + " " + curValue + "  dif: " + (curValue - value));
                if (curValue - value >= 0)
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                System.out.println("filterLong(): TABLE (ignored)");
                /* noop */
            }
        }

        System.out.println("filterLong(): DONE -> " + matches.size());
        return matches;
    }

    /**
     * Filters the media list on a string value
     * 
     * @param operator the comparison operator
     * @param values the value(s) to compare against
     * @param caseInsensitive indicator for case insensitive comparisons
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterString(final S3QueryConditionOperators operator, final List<String> values, final boolean caseInsensitive,
                                            final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, String> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        // Check that there is something to work with
        if ((values.size() <= 0) || (curMedia.size() <= 0))
            return matches;

        // Get the first value to make some comparisons easier
        String value = values.get(0);

        // Find all matching media
        System.out.println("filterString(): media count -> " + curMedia.size());
        for (Media med: curMedia)
        {
            String curValue = getFilterValue.apply(med, metadata);
            System.out.println("filterString(): cur value -> '" + curValue + "'");

            // Skip missing values
            if (curValue == null)
            {
                System.out.println("filterString(): skipping missing value");
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                System.out.println("filterString(): EQUAL,NUMERIC_EQUAL -> insensitive: " + caseInsensitive);
                if (caseInsensitive)
                    if (value.equalsIgnoreCase(curValue))
                        matches.add(med);
                else
                    if (value.equals(curValue))
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
                System.out.println("filterString(): IN, NOT IN -> insensitive: " + caseInsensitive);
                boolean matched = false;
                for (String oneValue: values)
                {
                    if (caseInsensitive)
                    {
                        if (oneValue.compareToIgnoreCase(curValue) == 0)
                        {
                            System.out.println("    match");
                            matched = true;
                            break;
                        }
                    }
                    else
                    {
                        if (oneValue.compareTo(curValue) == 0)
                        {
                            System.out.println("    match");
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched && (operator == S3QueryConditionOperators.IN))
                {
                    System.out.println("    adding match (BETWEEN)");
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    System.out.println("    adding un-matched (NOT BETWEEN)");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                System.out.println("filterString(): BETWEEN, NOT BETWEEN -> insensitive: " + caseInsensitive);
                boolean matched = false;
                if (values.size() >= 2)
                {
                    String first = values.get(0);
                    String second = values.get(1);
                    System.out.println("    values: '" + first + "'  '" + second + "'");

                    if (caseInsensitive)
                        if ((first.compareToIgnoreCase(curValue) >= 0) && (second.compareToIgnoreCase(curValue) <= 0))
                        {
                            System.out.println("    match");
                            matched = true;
                            break;
                        }
                    else
                        if ((first.compareTo(curValue) >= 0) && (second.compareTo(curValue) <= 0))
                        {
                            System.out.println("    match");
                            matched = true;
                            break;
                        }

                    if (matched && (operator == S3QueryConditionOperators.BETWEEN))
                    {
                        System.out.println("    adding match (BETWEEN)");
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        System.out.println("    adding un-matched (NOT BETWEEN)");
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                System.out.println("filterString(): NOT EQUAL -> insensitive: " + caseInsensitive);
                if (caseInsensitive)
                    if (!value.equalsIgnoreCase(curValue))
                        matches.add(med);
                else
                    if (!value.equals(curValue))
                            matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                System.out.println("filterString(): LESS THAN -> insensitive: " + caseInsensitive);
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) < 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) < 0)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                System.out.println("filterString(): GREATER THAN -> insensitive: " + caseInsensitive);
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) > 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) > 0)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterString(): LESS THAN OR EQUAL -> insensitive: " + caseInsensitive);
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) <= 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) <= 0)
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterString(): GREATER THAN OR EQUAL -> insensitive: " + caseInsensitive);
                if (caseInsensitive)
                    if (value.compareToIgnoreCase(curValue) >= 0)
                        matches.add(med);
                else
                    if (value.compareTo(curValue) >= 0)
                        matches.add(med);
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                System.out.println("filterString(): TABLE is ignored");
                /* noop */
            }
        }

        System.out.println("filterString(): DONE -> num matches: " + matches.size());
        return matches;
    }

    /**
     * Filters the media list on a double value
     * 
     * @param operator the comparison operator
     * @param values the value(s) to compare against
     * @param decimalDiffMax the maximum difference between two numbers for them to be considered the same
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterDouble(final S3QueryConditionOperators operator, final List<Double> values, final Double decimalDiffMax, 
                                            final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, Double> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        // Check that there is something to work with
        if ((values.size() <= 0) || (curMedia.size() <= 0))
            return matches;

        // Get the first value to make some comparisons easier
        Double value = values.get(0);

        System.out.println("filterDouble(): media count -> " + curMedia.size() + "  max diff: " + decimalDiffMax);

        for (Media med: curMedia)
        {
            Double curValue = getFilterValue.apply(med, metadata);
            System.out.println("filterDouble(): cur value -> '" + curValue + "'");

            // Skip missing values
            if (curValue == null)
            {
                System.out.println("filterDouble(): skipping null value");
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                System.out.println("filterDouble(): EQUAL, NUMERIC_EQUAL -> " + value + "  " + curValue + " dif: " + Math.abs(value - curValue));
                if (Math.abs(value - curValue) <= decimalDiffMax)
                {
                    System.out.println("    equal and adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
                System.out.println("filterDouble(): IN, NOT_IN -> " + value + "  " + curValue + " dif: " + Math.abs(value - curValue));
                // Find the match and handle the condition after
                boolean matched = false;
                for (Double oneValue: values)
                {
                    if (Math.abs(curValue - oneValue) <= decimalDiffMax)
                    {
                        matched = true;
                        break;
                    }
                }

                // Handle the condition
                if (matched && (operator == S3QueryConditionOperators.IN))
                {
                    System.out.println("    IN: equal and adding");
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    System.out.println("    NOT IN: not equal and adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                System.out.println("filterDouble(): BETWEEN, NOT_BETWEEN -> " + value + "  " + curValue + " dif: " + Math.abs(value - curValue));
                // Find the match and handle the condition after
                boolean matched = false;
                if (values.size() >= 2)
                {
                    Double first = values.get(0);
                    Double second = values.get(1);

                    // We need to check for the condition as well as checking for "equal"
                    if ((curValue - first >= 0) || (Math.abs(curValue - first) <= decimalDiffMax))
                    {
                        if ((curValue - second <= 0) || (Math.abs(curValue - second) <= decimalDiffMax))
                        {
                            matched = true;
                        }
                    }

                    // Handle the condiition
                    if (matched && (operator == S3QueryConditionOperators.BETWEEN))
                    {
                        System.out.println("    BETWEEN: equal and adding");
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        System.out.println("    NOT BETWEEN: not equal and adding");
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                System.out.println("filterDouble(): NOT_EQUAL -> " + value + "  " + curValue + " dif: " + Math.abs(value - curValue));
                // Also check for not "close enough to be equal"
                if (Math.abs(value - curValue) > decimalDiffMax)
                {
                    System.out.println("    not equal and adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                System.out.println("filterDouble(): LESS_THAN, NUMERIC_LESS_THAN -> " + value + "  " + curValue);
                // Also check that they are not considered equal
                if ((curValue - value < 0) && (Math.abs(curValue - value) > decimalDiffMax))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                System.out.println("filterDouble(): GREATER_THAN, NUMERIC_GREATER_THAN -> " + value + "  " + curValue);
                // Also check that they are not considered equal
                if ((curValue - value > 0) && (Math.abs(curValue - value) > decimalDiffMax))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterDouble(): LESS_THAN_OR_EQUAL_TO, NUMERIC_LESS_THAN_OR_EQUAL_TO -> " + value + "  " + curValue + " dif: " + Math.abs(value - curValue));
                // Also check for "close enough to be equal"
                if ((curValue - value <= 0) || (Math.abs(value - curValue) <= decimalDiffMax))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterDouble(): GREATER_THAN_OR_EQUAL_TO, NUMERIC_GREATER_THAN_OR_EQUAL_TO -> " + value + "  " + curValue + " dif: " + Math.abs(value - curValue));
                // Also check for "close enough to be equal"
                if ((curValue - value >= 0) || (Math.abs(value - curValue) <= decimalDiffMax))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                /* noop */
            }
        }

        System.out.println("filterDouble(): DONE -> " + matches.size());
        return matches;
    }

    /**
     * Filters the media list on an Integer value
     * 
     * @param operator the comparison operator
     * @param values the value(s) to compare against
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterInteger(final S3QueryConditionOperators operator, final List<Integer> values, 
                                             final List<Media> curMedia, final Camtrap metadata, final BiFunction<Media, Camtrap, Integer> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        // Check that there is something to work with
        if ((values.size() <= 0) || (curMedia.size() <= 0))
            return matches;

        // Get the first value to make some comparisons easier
        Integer value = values.get(0);

        System.out.println("filterInteger(): media count -> " + curMedia.size());

        for (Media med: curMedia)
        {
            Integer curValue = getFilterValue.apply(med, metadata);
            System.out.println("filterInteger(): cur value -> '" + curValue + "'");

            // Skip missing values
            if (curValue == null)
            {
                System.out.println("filterInteger(): skipping null value");
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                if (curValue - value == 0)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
                // Find the match and handle the condition after
                boolean matched = false;
                for (Integer oneValue: values)
                {
                    if (oneValue - curValue == 0)
                    {
                        matched = true;
                        break;
                    }
                }

                // Handle the condition
                if (matched && (operator == S3QueryConditionOperators.IN))
                    matches.add(med);
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                // Find the match and handle the condition after
                boolean matched = false;
                if (values.size() >= 2)
                {
                    Integer first = values.get(0);
                    Integer second = values.get(1);

                    if ((curValue - first >= 0) && (curValue - second <= 0))
                    {
                        matched = true;
                    }

                    // Handle the condition
                    if (matched && (operator == S3QueryConditionOperators.BETWEEN))
                        matches.add(med);
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                        matches.add(med);
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                if (curValue - value != 0)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (curValue - value < 0)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                if (curValue - value > 0)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (curValue - value <= 0)
                    matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (curValue - value >= 0)
                    matches.add(med);
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                /* noop */
            }
        }

        System.out.println("filterInteger(): DONE -> " + matches.size());
        return matches;
    }

    /**
     * Filters the media list on an Date value
     * 
     * @param operator the comparison operator
     * @param values the value(s) to compare against
     * @param curMedia the list of media to search
     * @param metadata the metadata associated with the collection
     * @param getFilterValue function returning value to compare against
     * @return a list of matching Media
     */
    private static List<Media> filterDate(final S3QueryConditionOperators operator, final List<LocalDateTime> values, 
                                             final List<Media> curMedia, final Camtrap metadata, 
                                             final BiFunction<Media, Camtrap, LocalDateTime> getFilterValue)
    {
        List<Media> matches = new ArrayList<Media>();

        // Check that there is something to work with
        if ((values.size() <= 0) || (curMedia.size() <= 0))
            return matches;

        // Get the first value to make some comparisons easier
        LocalDateTime value = values.get(0);

        System.out.println("filterDate(): media count -> " + curMedia.size());

        for (Media med: curMedia)
        {
            LocalDateTime curValue = getFilterValue.apply(med, metadata);
            System.out.println("filterDate(): cur value -> '" + curValue + "'");

            // Skip missing values
            if (curValue == null)
            {
                System.out.println("filterDate(): skipping null value");
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                System.out.println("filterDate(): EQUAL, NUMERIC_EQUAL -> " + curValue + "  " + value);
                if (curValue.isEqual(value))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
                System.out.println("filterDate(): IN, NOT_IN -> " + curValue + "  " + values);
                // Find the match and handle the condition after
                boolean matched = false;
                for (LocalDateTime oneValue: values)
                {
                    if (oneValue.isEqual(curValue))
                    {
                        matched = true;
                        break;
                    }
                }

                // Handle the condition
                if (matched && (operator == S3QueryConditionOperators.IN))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                System.out.println("filterDate(): BETWEEN, NOT_BETWEEN -> " + curValue + "  " + values);
                // Find the match and handle the condition after
                boolean matched = false;
                if (values.size() >= 2)
                {
                    LocalDateTime first = values.get(0);
                    LocalDateTime second = values.get(1);

                    if ((curValue.isAfter(first) && curValue.isBefore(second)) || curValue.isEqual(first) || curValue.isEqual(second))
                    {
                        matched = true;
                    }

                    // Handle the condition
                    if (matched && (operator == S3QueryConditionOperators.BETWEEN))
                    {
                        System.out.println("    adding");
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        System.out.println("    adding");
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                System.out.println("filterDate(): NOT_EQUAL -> " + curValue + "  " + value);
                if (!curValue.isEqual(value))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                System.out.println("filterDate(): LESS_THAN, NUMERIC_LESS_THAN -> " + curValue + "  " + value);
                if (curValue.isBefore(value))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                System.out.println("filterDate(): GREATER_THAN, NUMERIC_GREATER_THAN -> " + curValue + "  " + value);
                if (curValue.isAfter(value))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterDate(): LESS_THAN_OR_EQUAL, NUMERIC_LESS_THAN_OR_EQUAL -> " + curValue + "  " + value);
                if (curValue.isBefore(value) || curValue.isEqual(value))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                System.out.println("filterDate(): GREATER_THAN_OR_EQUAL, NUMERIC_GREATER_THAN_OR_EQUAL -> " + curValue + "  " + value);
                if (curValue.isAfter(value) || curValue.isEqual(value))
                {
                    System.out.println("    adding");
                    matches.add(med);
                }
            }
            else if (operator == S3QueryConditionOperators.TABLE)
            {
                System.out.println("filterDate(): TABLE (ignoring)");
                /* noop */
            }
        }

        System.out.println("filterDate(): DONE -> " + matches.size());
        return matches;
    }

    /**
     * Adds found matches to the result set
     * 
     * @param curResults the current result set
     * @param bucket the bucket the results belong to
     * @param matches the potential matches to add
     * @param isDistinct whether to ensure the reset set is distinct (no duplicates)
     */
    private static void addMatchesToResults(S3QueryResultSet curResults, final String bucket, final List<String> matches, boolean isDistinct)
    {
        // Iterate through the matches
        for (String oneMatch: matches)
        {
            String path = bucket + "::" +FilenameUtils.getPath(oneMatch);
            String name = FilenameUtils.getName(oneMatch);

            if (!isDistinct || !curResults.findRow(path, name))
            {
                curResults.addRow(path, name);
            }
        }
    }

    /**
     * Returns the value as an array. Values must be in the form of "(value1,value2,...)" to be split.
     * Otherwise, the value is returned as the single element in the array. Enclosing single quotes on values
     * are stripped
     * 
     * @param value the value to split
     * @return the array of the split value, or containing the original value
     */
    private static List<String> getStringValuesArray(final String value)
    {
        List<String> valueList = null;

        System.out.println("getStringValuesArray: -> '" + value + "'");
        if (value.startsWith("(") && value.endsWith(")"))
        {
            System.out.println("    array");
            valueList = List.of((value.substring(1, value.length() - 1)).split(","));

            // Check for a list of strings and fix those up
            int idx = 0;
            List<String> newList = new ArrayList<String>();
            while (idx < valueList.size())
            {
                String curValue = valueList.get(idx);

                if (curValue.startsWith("'") && curValue.endsWith("'"))
                {
                    newList.add(curValue.substring(1, curValue.length() - 1));
                }
                else
                {
                    newList.add(curValue);
                }
                idx++;
            }

            valueList = newList;
        }
        else
        {
            System.out.println("    value");
            valueList = new ArrayList<String>();
            valueList.add(value);
        }

        System.out.println("getStringValuesArray: return count -> " + valueList.size());
        return valueList;
    }

    /**
     * Returns the value as an array. Values must be in the form of "(value1,value2,...)" to be split.
     * Otherwise, the value is returned as the single element in the array
     * 
     * @param value the value to split
     * @return the array of the split value, or containing the original value
     */
    private static List<Long> getLongValuesArray(final String value)
    {
        List<String> stringList = S3QueryExecute.getStringValuesArray(value);

        List<Long> valueList = new ArrayList<Long>();

        for (String oneValue: stringList)
        {
            valueList.add(Long.parseLong(oneValue)); 
        }

        return valueList;
    }

    /**
     * Returns the value as an array. Values must be in the form of "(value1,value2,...)" to be split.
     * Otherwise, the value is returned as the single element in the array
     * 
     * @param value the value to split
     * @return the array of the split value, or containing the original value
     */
    private static List<Double> getDoubleValuesArray(final String value)
    {
        List<String> stringList = S3QueryExecute.getStringValuesArray(value);

        List<Double> valueList = new ArrayList<Double>();

        for (String oneValue: stringList)
        {
            valueList.add(Double.parseDouble(oneValue)); 
        }

        return valueList;
    }

    /**
     * Returns the value as an array. Values must be in the form of "(value1,value2,...)" to be split.
     * Otherwise, the value is returned as the single element in the array
     * 
     * @param value the value to split
     * @return the array of the split value, or containing the original value
     */
    private static List<Integer> getIntegerValuesArray(final String value)
    {
        List<String> stringList = S3QueryExecute.getStringValuesArray(value);

        List<Integer> valueList = new ArrayList<Integer>();

        for (String oneValue: stringList)
        {
            valueList.add(Integer.parseInt(oneValue)); 
        }

        return valueList;
    }

    /**
     * Returns the value as an array. Values must be in the form of "(value1,value2,...)" to be split.
     * Otherwise, the value is returned as the single element in the array
     * 
     * @param value the value to split
     * @return the array of the split value, or containing the original value
     */
    private static List<LocalDateTime> getDateValuesArray(final String value)
    {
        List<Long> longList = S3QueryExecute.getLongValuesArray(value);

        List<LocalDateTime> valueList = new ArrayList<LocalDateTime>();

        for (Long oneValue: longList)
        {
            valueList.add(LocalDateTime.ofInstant(Instant.ofEpochMilli(oneValue.longValue()), ZoneId.systemDefault()));
        }

        return valueList;
    }
}
