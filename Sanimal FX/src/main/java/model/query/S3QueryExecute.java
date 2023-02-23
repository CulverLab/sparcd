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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    public static S3QueryResultSet executeQuery(S3QueryBuilder queryBuilder, final List<ImageCollection> collections) throws  InterruptedException, ExecutionException
    {
        S3QueryResultSet resultSet = new S3QueryResultSet();
        List<S3QueryBuilderCondition> conditions = queryBuilder.getConditions();
        boolean isDistinct = queryBuilder.isDistinct();
        boolean isCaseInsensitive = queryBuilder.isCaseInsensitive();

        // Check that we have something to work with
        if (collections.size() <= 0)
        {
            return null;
        }

        // Define class to hold search results
        class InternalResults
        {
            public String bucket;
            public List<String> matches;
        }

        // Iterate through the collections and look at the metadata
        List<CompletableFuture<List<InternalResults>>> allFutures = new ArrayList<CompletableFuture<List<InternalResults>>>();
        for (ImageCollection oneCollection: collections)
        {
            if (!oneCollection.uploadsWereSynced())
            {
                System.out.println("executeQuery(): non-synched collection '" + oneCollection.getName() + "'");
            }

            CompletableFuture<List<InternalResults>> queryFuture = CompletableFuture.supplyAsync(() -> {
                List<InternalResults> allMatches = new ArrayList<InternalResults>();
                List<CloudUploadEntry> uploads = oneCollection.getUploads();
                for (CloudUploadEntry oneEntry: uploads)
                {
                    Camtrap metaData = oneEntry.getMetadata().getValue();
                    List<String> matches = S3QueryExecute.queryMatches(conditions, metaData, isCaseInsensitive);
                    if ((matches != null) && (matches.size() > 0))
                    {
                        InternalResults res = new InternalResults();
                        res.bucket = oneEntry.getBucket();
                        res.matches = matches;
                        allMatches.add(res);
                    }
                }

                return allMatches;
            }
            );

            allFutures.add(queryFuture);
        }

        if (allFutures.size() > 0)
        {
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[allFutures.size()]));
            combinedFuture.get();

            for (CompletableFuture<List<InternalResults>> oneFuture: allFutures)
            {
                for (InternalResults allMatches: oneFuture.get())
                {
                    if ((allMatches.matches != null) && (allMatches.matches.size() > 0))
                    {
                        S3QueryExecute.addMatchesToResults(resultSet, allMatches.bucket, allMatches.matches, isDistinct);
                    }
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

        // Perform the query
        if (conditions.size() > 0)
        {
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
                            newMedia = S3QueryExecute.filterLong(curValue.getOperator(), 
                                S3QueryExecute.getLongValuesArray(curValue.getValue()),
                                mediaList, metadata,
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
                            newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                                S3QueryExecute.getStringValuesArray(curValue.getValue()),
                                caseInsensitive, mediaList, metadata,
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
                            newMedia = S3QueryExecute.filterString(curValue.getOperator(),
                                S3QueryExecute.getStringValuesArray(curValue.getValue()),
                                caseInsensitive, mediaList, metadata,
                                (media, curMetadata) -> 
                                {
                                    for (Observations obs: curMetadata.observations)
                                    {
                                        if (obs.mediaID.equals(media.mediaID))
                                        {
                                            int index = obs.deploymentID.indexOf(":");
                                            if (index < 0)
                                            {
                                                return obs.deploymentID;
                                            }
                                            else
                                            {
                                                return obs.deploymentID.substring(0, index);
                                            }
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

        for (Media med: curMedia)
        {
            Long curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
            {
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                if (curValue - value == 0)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
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
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
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
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                if (curValue - value != 0)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (curValue - value < 0)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                if (curValue - value > 0)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (curValue - value <= 0)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (curValue - value >= 0)
                {
                    matches.add(med);
                }
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
        for (Media med: curMedia)
        {
            String curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
            {
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                if (caseInsensitive)
                    if (value.equalsIgnoreCase(curValue))
                        matches.add(med);
                else
                    if (value.equals(curValue))
                        matches.add(med);
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
                boolean matched = false;
                for (String oneValue: values)
                {
                    if (caseInsensitive)
                    {
                        if (oneValue.compareToIgnoreCase(curValue) == 0)
                        {
                            matched = true;
                            break;
                        }
                    }
                    else
                    {
                        if (oneValue.compareTo(curValue) == 0)
                        {
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched && (operator == S3QueryConditionOperators.IN))
                {
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
                boolean matched = false;
                if (values.size() >= 2)
                {
                    String first = values.get(0);
                    String second = values.get(1);

                    if (caseInsensitive)
                        if ((first.compareToIgnoreCase(curValue) >= 0) && (second.compareToIgnoreCase(curValue) <= 0))
                        {
                            matched = true;
                            break;
                        }
                    else
                        if ((first.compareTo(curValue) >= 0) && (second.compareTo(curValue) <= 0))
                        {
                            matched = true;
                            break;
                        }

                    if (matched && (operator == S3QueryConditionOperators.BETWEEN))
                    {
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
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

        for (Media med: curMedia)
        {
            Double curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
            {
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                if (Math.abs(value - curValue) <= decimalDiffMax)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
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
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
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
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                // Also check for not "close enough to be equal"
                if (Math.abs(value - curValue) > decimalDiffMax)
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                // Also check that they are not considered equal
                if ((curValue - value < 0) && (Math.abs(curValue - value) > decimalDiffMax))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                // Also check that they are not considered equal
                if ((curValue - value > 0) && (Math.abs(curValue - value) > decimalDiffMax))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                // Also check for "close enough to be equal"
                if ((curValue - value <= 0) || (Math.abs(value - curValue) <= decimalDiffMax))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                // Also check for "close enough to be equal"
                if ((curValue - value >= 0) || (Math.abs(value - curValue) <= decimalDiffMax))
                {
                    matches.add(med);
                }
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

        for (Media med: curMedia)
        {
            Integer curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
            {
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

        for (Media med: curMedia)
        {
            LocalDateTime curValue = getFilterValue.apply(med, metadata);

            // Skip missing values
            if (curValue == null)
            {
                continue;
            }

            if ((operator == S3QueryConditionOperators.EQUAL) || (operator == S3QueryConditionOperators.NUMERIC_EQUAL))
            {
                if (curValue.isEqual(value))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.IN) || (operator == S3QueryConditionOperators.NOT_IN))
            {
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
                    matches.add(med);
                }
                else if (!matched && (operator == S3QueryConditionOperators.NOT_IN))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.BETWEEN) || (operator == S3QueryConditionOperators.NOT_BETWEEN))
            {
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
                        matches.add(med);
                    }
                    else if (!matched && (operator == S3QueryConditionOperators.NOT_BETWEEN))
                    {
                        matches.add(med);
                    }
                }
            }
            else if (operator == S3QueryConditionOperators.NOT_EQUAL)
            {
                if (!curValue.isEqual(value))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN))
            {
                if (curValue.isBefore(value))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN))
            {
                if (curValue.isAfter(value))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.LESS_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO))
            {
                if (curValue.isBefore(value) || curValue.isEqual(value))
                {
                    matches.add(med);
                }
            }
            else if ((operator == S3QueryConditionOperators.GREATER_THAN_OR_EQUAL_TO) || (operator == S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO))
            {
                if (curValue.isAfter(value) || curValue.isEqual(value))
                {
                    matches.add(med);
                }
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

        if (value.startsWith("(") && value.endsWith(")"))
        {
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
            valueList = new ArrayList<String>();
            valueList.add(value);
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
