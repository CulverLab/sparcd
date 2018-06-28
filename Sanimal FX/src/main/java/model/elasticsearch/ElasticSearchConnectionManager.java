package model.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.cyverse.ImageCollection;
import model.image.CloudImageEntry;
import model.image.CloudUploadEntry;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.query.ElasticSearchQuery;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.SettingsData;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ElasticSearchConnectionManager
{
	// The IP of the elastic search index
	private static final String ELASTIC_SEARCH_HOST = "128.196.142.26";
	// The port of the elastic search index
	private static final Integer ELASTIC_SEARCH_PORT = 9200;
	// The scheme used to connect to the elastic search index
	private static final String ELASTIC_SEARCH_SCHEME = "http";

	// The name of the user's index
	private static final String INDEX_SANIMAL_USERS = "users";
	// The type for the sanimal user's index
	private static final String INDEX_SANIMAL_USERS_TYPE = "_doc";
	// The number of shards to be used by the users index, for development we just need 1
	private static final Integer INDEX_SANIMAL_USERS_SHARD_COUNT = 1;
	// The number of replicas to be created by the users index, for development we don't need any
	private static final Integer INDEX_SANIMAL_USERS_REPLICA_COUNT = 0;

	// The name of the metadata index
	private static final String INDEX_SANIMAL_METADATA = "metadata";
	// The type for the sanimal metadata index
	private static final String INDEX_SANIMAL_METADATA_TYPE = "_doc";
	// The number of shards to be used by the metadata index, for development we just need 1
	private static final Integer INDEX_SANIMAL_METADATA_SHARD_COUNT = 1;
	// The number of replicas to be created by the metadata index, for development we don't need any
	private static final Integer INDEX_SANIMAL_METADATA_REPLICA_COUNT = 0;

	// The name of the collections index
	private static final String INDEX_SANIMAL_COLLECTIONS = "collections";
	// The type for the sanimal collections index
	private static final String INDEX_SANIMAL_COLLECTIONS_TYPE = "_doc";
	// The number of shards to be used by the collections index, for development we just need 1
	private static final Integer INDEX_SANIMAL_COLLECTIONS_SHARD_COUNT = 1;
	// The number of replicas to be created by the collections index, for development we don't need any
	private static final Integer INDEX_SANIMAL_COLLECTIONS_REPLICA_COUNT = 0;

	// The type used to serialize a list of species through Gson
	private static final Type SPECIES_LIST_TYPE = new TypeToken<ArrayList<Species>>()
	{
	}.getType();
	// The type used to serialize a list of locations through Gson
	private static final Type LOCATION_LIST_TYPE = new TypeToken<ArrayList<Location>>()
	{
	}.getType();
	// The type used to serialize a list of cloud uploads
	private static final Type CLOUD_UPLOAD_ENTRY_LIST_TYPE = new TypeToken<ArrayList<CloudUploadEntry>>()
	{
	}.getType();
	// The type used to serialize a list of species entries through Gson
	private static final Type SPECIES_ENTRY_LIST_TYPE = new TypeToken<ArrayList<SpeciesEntry>>()
	{
	}.getType();

	// Create a new elastic search client
	private final RestHighLevelClient elasticSearchClient;

	// Create a new elastic search schema manager
	private final ElasticSearchSchemaManager elasticSearchSchemaManager;

	/**
	 * The constructor initializes the elastic search
	 */
	public ElasticSearchConnectionManager()
	{
		// Establish a connection to the elastic search server
		this.elasticSearchClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));
		this.elasticSearchSchemaManager = new ElasticSearchSchemaManager();
	}

	/**
	 * Destroys and rebuilds the entire user's index. All user data will be lost!
	 */
	public void nukeAndRecreateUserIndex()
	{
		// Delete the original index
		deleteIndex(INDEX_SANIMAL_USERS);

		// The index is gone now, so recreate it
		try
		{
			// Create a create index request
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_SANIMAL_USERS);
			// Make sure to set the number of shards and replicas
			createIndexRequest.settings(Settings.builder()
				.put("index.number_of_shards", INDEX_SANIMAL_USERS_SHARD_COUNT)
				.put("index.number_of_replicas", INDEX_SANIMAL_USERS_REPLICA_COUNT));
			// Add the users type mapping which defines our schema
			createIndexRequest.mapping(INDEX_SANIMAL_USERS_TYPE, this.elasticSearchSchemaManager.makeSanimalUsersIndexMapping(INDEX_SANIMAL_USERS_TYPE));
			// Execute the index request
			this.elasticSearchClient.indices().create(createIndexRequest);
		}
		catch (IOException e)
		{
			// Print any errors we may have encountered
			SanimalData.getInstance().getErrorDisplay().notify("Error creating '" + INDEX_SANIMAL_USERS + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Destroys and rebuilds entire metadata index. All metadata stored will be lost
	 */
	public void nukeAndRecreateMetadataIndex()
	{
		// Delete the original index
		deleteIndex(INDEX_SANIMAL_METADATA);

		// The index is gone now, so recreate it
		try
		{
			// Create a create index request
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_SANIMAL_METADATA);
			// Make sure to set the number of shards and replicas
			createIndexRequest.settings(Settings.builder()
					.put("index.number_of_shards", INDEX_SANIMAL_METADATA_SHARD_COUNT)
					.put("index.number_of_replicas", INDEX_SANIMAL_METADATA_REPLICA_COUNT));
			// Add the users type mapping which defines our schema
			createIndexRequest.mapping(INDEX_SANIMAL_METADATA_TYPE, this.elasticSearchSchemaManager.makeSanimalMetadataIndexMapping(INDEX_SANIMAL_METADATA_TYPE));
			// Execute the index request
			this.elasticSearchClient.indices().create(createIndexRequest);
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().notify("Error creating '" + INDEX_SANIMAL_METADATA + "' in the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	public void nukeAndRecreateCollectionsIndex()
	{
		// Delete the original index
		deleteIndex(INDEX_SANIMAL_COLLECTIONS);

		// The index is gone now, so recreate it
		try
		{
			// Create a create index request
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_SANIMAL_COLLECTIONS);
			// Make sure to set the number of shards and replicas
			createIndexRequest.settings(Settings.builder()
					.put("index.number_of_shards", INDEX_SANIMAL_COLLECTIONS_SHARD_COUNT)
					.put("index.number_of_replicas", INDEX_SANIMAL_COLLECTIONS_REPLICA_COUNT));
			// Add the users type mapping which defines our schema
			createIndexRequest.mapping(INDEX_SANIMAL_COLLECTIONS_TYPE, this.elasticSearchSchemaManager.makeSanimalCollectionsIndexMapping(INDEX_SANIMAL_COLLECTIONS_TYPE));
			// Execute the index request
			this.elasticSearchClient.indices().create(createIndexRequest);
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().notify("Error creating '" + INDEX_SANIMAL_COLLECTIONS + "' in the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Given an elastic search client and an index, this method removes the index from the client
	 *
	 * @param index The index to remove
	 */
	private void deleteIndex(String index)
	{
		try
		{
			// Create a delete request to remove the Sanimal Users index and execute it
			DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
			this.elasticSearchClient.indices().delete(deleteIndexRequest);
		}
		catch (IOException e)
		{
			// If the delete fails just print out an error message
			SanimalData.getInstance().getErrorDisplay().notify("Error deleting '" + index + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
		catch (ElasticsearchStatusException e)
		{
			// If the delete fails just print out an error message
			SanimalData.getInstance().getErrorDisplay().notify("Delete failed, status = " + e.status());
		}
	}

	/**
	 * Initializes the remote SANIMAL directory which is more like the remote SANIMAL index now. Indices are
	 * updated with default user settings if not present
	 */
	public void initSanimalRemoteDirectory()
	{
		try
		{
			// Get the document corresponding to this user's username. By doing this we get the exact document which contains our user settings
			GetRequest getRequest = new GetRequest();
			getRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					// Make sure the ID corresponds to our username
					.id(SanimalData.getInstance().getUsername())
					// Ignore source to speed up the fetch
					.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
			// Perform the GET request
			GetResponse getResponse = this.elasticSearchClient.get(getRequest);
			// If the user is not in the db... create an index entry for him
			if (!getResponse.isExists())
			{
				// Create an index request which we use to put data into the elastic search index
				IndexRequest indexRequest = new IndexRequest();
				indexRequest
						.index(INDEX_SANIMAL_USERS)
						.type(INDEX_SANIMAL_USERS_TYPE)
						// Make sure the ID is our username
						.id(SanimalData.getInstance().getUsername())
						// The source will be a new
						.source(this.elasticSearchSchemaManager.makeCreateUser(SanimalData.getInstance().getUsername()));
				// Perform the index request
				this.elasticSearchClient.index(indexRequest);
			}
		}
		catch (IOException e)
		{
			// Print an error if anything went wrong
			SanimalData.getInstance().getErrorDisplay().notify("Error initializing user '" + SanimalData.getInstance().getUsername() + "' in the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Fetches the user's settings from the ElasticSearch index
	 *
	 * @return The user's settings
	 */
	public SettingsData pullRemoteSettings()
	{
		// Pull the settings from the ElasticSearch cluster
		Object settings = fetchFieldForUser("settings");
		// Settings should be a map, so test that
		if (settings instanceof Map<?, ?>)
		{
			// Convert this HashMap to JSON, and finally from JSON into the SettingsData object. Once this is done, return!
			String json = SanimalData.getInstance().getGson().toJson(settings);
			if (json != null)
			{
				return SanimalData.getInstance().getGson().fromJson(json, SettingsData.class);
			}
		}

		return null;
	}

	/**
	 * Fetches the user's species from the ElasticSearch index
	 *
	 * @return The user's species
	 */
	public List<Species> pullRemoteSpecies()
	{
		// Pull the species list from the ElasticSearch cluster
		Object species = fetchFieldForUser("species");
		// Species should be a list of maps, so test that
		if (species instanceof List<?>)
		{
			String json = SanimalData.getInstance().getGson().toJson(species);
			// Convert this HashMap to JSON, and finally from JSON into the List<Species> object. Once this is done, return!
			if (json != null)
			{
				return SanimalData.getInstance().getGson().fromJson(json, SPECIES_LIST_TYPE);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Fetches the user's locations from the ElasticSearch index
	 *
	 * @return The user's locations
	 */
	@SuppressWarnings("unchecked")
	public List<Location> pullRemoteLocations()
	{
		// Pull the locations list from the ElasticSearch cluster
		Object locations = fetchFieldForUser("locations");
		// Location should be in the form of a list of maps, so check that
		if (locations instanceof List<?>)
		{
			// Iterate over each location map in the list
			((List<?>) locations).forEach(locationMapObj ->
			{
				// Make sure the object is a map
				if (locationMapObj instanceof Map<?, ?>)
				{
					// Convert from Object to actual map object. Here we do extra processing to ensure that the
					// position field is expanded to latitude and longitude locally
					Map<String, Object> locationsMap = (Map<String, Object>) locationMapObj;
					// Grab the position field which should be in the form of "latitude, longitude"
					Object position = locationsMap.remove("position");
					// Test if position is a string
					if (position instanceof String)
					{
						// Split that string by the central comma
						String[] split = ((String) position).split(", ");
						// Make sure the split was successful
						if (split.length == 2)
						{
							// Save latitude and longitude into the map to be used by Sanimal
							locationsMap.put("latitude", split[0]);
							locationsMap.put("longitude", split[1]);
						}
					}
				}
			});
			// Convert the locations list of maps into JSON, and finally into a list of locations ready to be returned
			String json = SanimalData.getInstance().getGson().toJson(locations);
			if (json != null)
			{
				return SanimalData.getInstance().getGson().fromJson(json, LOCATION_LIST_TYPE);
			}
		}

		// Default return nothing
		return Collections.emptyList();
	}

	/**
	 * Fetches the user's collections from the ElasticSearch index
	 *
	 * @return The user's collections
	 */
	public List<ImageCollection> pullRemoteCollections()
	{
		// A list of collections to return
		List<ImageCollection> toReturn = new ArrayList<>();

		// Because the collection list could be potentially long, we use a scroll to ensure reading results in reasonable chunks
		Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
		// Create a search request, and populate the fields
		SearchRequest searchRequest = new SearchRequest();
		searchRequest
				.indices(INDEX_SANIMAL_COLLECTIONS)
				.types(INDEX_SANIMAL_COLLECTIONS_TYPE)
				.scroll(scroll)
				.source(new SearchSourceBuilder()
					// Fetch results 10 at a time, and use a query that matches everything
					.size(10)
					.fetchSource(true)
					.query(QueryBuilders.matchAllQuery()));

		try
		{
			// Grab the search results
			SearchResponse searchResponse = this.elasticSearchClient.search(searchRequest);
			// Store the scroll id that was returned because we specified a scroll in the search request
			String scrollID = searchResponse.getScrollId();
			// Get a list of collections (hits)
			SearchHit[] searchHits = searchResponse.getHits().getHits();

			// Iterate while there are more collections to be read
			while (searchHits != null && searchHits.length > 0)
			{
				// Iterate over all current results
				for (SearchHit searchHit : searchHits)
				{
					// Grab the collection as a map object
					Map<String, Object> collection = searchHit.getSourceAsMap();
					// Convert the map to JSON, and then into an ImageCollection object. It's a bit of a hack but it works well
					String collectionJSON = SanimalData.getInstance().getGson().toJson(collection);
					toReturn.add(SanimalData.getInstance().getGson().fromJson(collectionJSON, ImageCollection.class));
				}

				// Now that we've processed this wave of results, get the next 10 results
				SearchScrollRequest scrollRequest = new SearchScrollRequest();
				// Setup the scroll request
				scrollRequest
						.scrollId(scrollID)
						.scroll(scroll);
				// Perform the scroll, yielding another set of results
				searchResponse = this.elasticSearchClient.searchScroll(scrollRequest);
				// Store the hits and the new scroll id
				scrollID = searchResponse.getScrollId();
				searchHits = searchResponse.getHits().getHits();
			}

			// Finish off the scroll request
			ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
			clearScrollRequest.addScrollId(scrollID);
			ClearScrollResponse clearScrollResponse = this.elasticSearchClient.clearScroll(clearScrollRequest);
			// If clearing the scroll request fails, show an error
			if (!clearScrollResponse.isSucceeded())
				SanimalData.getInstance().getErrorDisplay().notify("Could not clear the scroll when reading collections");
		}
		catch (IOException e)
		{
			// Something went wrong, so show an error
			SanimalData.getInstance().getErrorDisplay().notify("Error pulling remote collections, error was:\n" + ExceptionUtils.getStackTrace(e));
		}

		// Collections are deserialized and have null listeners, call initFromJSON to fix those listeners
		toReturn.forEach(imageCollection -> imageCollection.getUploads().forEach(CloudUploadEntry::initFromJSON));

		return toReturn;
	}

	/**
	 * Fetches a top level field from the user's elastic search index
	 *
	 * @param field The field to fetch
	 * @return An object representing the field. May be something concrete or a container such as a list/map
	 */
	private Object fetchFieldForUser(String field)
	{
		try
		{
			// Use a get request to pull the correct field
			GetRequest getRequest = new GetRequest();
			// We return the username field and the specified field.
			List<String> includes = Arrays.asList("username", field);
			// All other fields are excluded
			List<String> excludes = ListUtils.subtract(Arrays.asList("username", "species", "locations", "settings"), includes);
			// Setup our get request, make sure to specify the user we want to query for and the source fields we want to return
			getRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.fetchSourceContext(new FetchSourceContext(true, includes.toArray(new String[0]), excludes.toArray(new String[0])));
			// Store the response
			GetResponse getResponse = this.elasticSearchClient.get(getRequest);
			// If we got a good response, grab it
			if (getResponse.isExists() && !getResponse.isSourceEmpty())
			{
				// Result comes back as a map, search the map for our field and return it
				Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
				return sourceAsMap.get(field);
			}
			else
			{
				// Bad response, print out an error message. User probably doesnt exist
				SanimalData.getInstance().getErrorDisplay().notify("User not found on the DB. This should not be possible.");
			}
		}
		catch (IOException e)
		{
			// Error happened when executing a GET request. Print an error
			SanimalData.getInstance().getErrorDisplay().notify("Error pulling remote field '" + field + "' for the user '" + SanimalData.getInstance().getUsername() + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	/**
	 * Pushes a list of species into the cloud and to the user's index
	 *
	 * @param species The list of species to store
	 */
	public void pushLocalSpecies(List<Species> species)
	{
		try
		{
			// Create an update request that will update just the species field in the user's index
			UpdateRequest updateRequest = new UpdateRequest();
			// Set up the update's fields
			updateRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.doc(this.elasticSearchSchemaManager.makeSpeciesUpdate(species));

			// Send off the update
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// Test to make sure it went OK, if not, return an error
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().notify("Error syncing species list, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update fails
			SanimalData.getInstance().getErrorDisplay().notify("Error updating species list for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Pushes a list of locations into the cloud and to the user's index
	 *
	 * @param locations The list of locations to store
	 */
	public void pushLocalLocations(List<Location> locations)
	{
		try
		{
			// Create an update request that will update just the locations field in the user's index
			UpdateRequest updateRequest = new UpdateRequest();
			// Setup the location update request with data
			updateRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.doc(this.elasticSearchSchemaManager.makeLocationsUpdate(locations));

			// Fire off the update request
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// Print an error if the response code is not OK
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().notify("Error syncing location list, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update request fails
			SanimalData.getInstance().getErrorDisplay().notify("Error updating location list for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Pushes local settings into the user's index for safe keeping
	 *
	 * @param settingsData The settings to be saved which will overwrite the old ones
	 */
	public void pushLocalSettings(SettingsData settingsData)
	{
		try
		{
			// Create the update request to update settings
			UpdateRequest updateRequest = new UpdateRequest();
			// Initialize the update request with data
			updateRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.doc(this.elasticSearchSchemaManager.makeSettingsUpdate(settingsData));

			// Perform the update and test the response
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// If the response is OK, continue, if not print an error
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().notify("Error syncing settings, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update fails
			SanimalData.getInstance().getErrorDisplay().notify("Error updating settings for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Pushes a local collection into the collection index
	 *
	 * @param imageCollection The collection to save
	 */
	public void pushLocalCollection(ImageCollection imageCollection)
	{
		try
		{
			// Default index request which will automatically be used if document does not exist yet
			// This will just make a blank collection
			IndexRequest indexRequest = new IndexRequest();
			indexRequest
					.index(INDEX_SANIMAL_COLLECTIONS)
					.type(INDEX_SANIMAL_COLLECTIONS_TYPE)
					.id(imageCollection.getID().toString())
					.source(this.elasticSearchSchemaManager.makeCreateCollection(imageCollection));

			// Create the update request to update/create the collection
			UpdateRequest updateRequest = new UpdateRequest();
			// Initialize the update request with data
			updateRequest
					.index(INDEX_SANIMAL_COLLECTIONS)
					.type(INDEX_SANIMAL_COLLECTIONS_TYPE)
					.id(imageCollection.getID().toString())
					.doc(this.elasticSearchSchemaManager.makeCollectionUpdate(imageCollection))
					// Upsert means "if the collection does not exist, call this request"
					.upsert(indexRequest);

			// Perform the update and test the response
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// If the response is OK, continue, if not print an error
			if (updateResponse.status() != RestStatus.OK && updateResponse.status() != RestStatus.CREATED)
				SanimalData.getInstance().getErrorDisplay().notify("Error saving collection '" + imageCollection.getName() + "', error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update fails
			SanimalData.getInstance().getErrorDisplay().notify("Error saving collection '" + imageCollection.getName() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Downloads the upload list for a given collection
	 *
	 * @param imageCollection The image collection which we want to retrieve uploads of
	 */
	public void retrieveAndInsertUploadListFor(ImageCollection imageCollection)
	{
		// Get a map of 'upload'->[List of uploads]
		Map<String, Object> uploadsForCollection = getUploadsForCollection(imageCollection.getID().toString());
		if (uploadsForCollection != null)
		{
			// Make sure our map does in fact have the uploads key
			if (uploadsForCollection.containsKey("uploads"))
			{
				// Grab the JSON representing the uploads list
				String uploadJSON = SanimalData.getInstance().getGson().toJson(uploadsForCollection.get("uploads"));
				// Convert the JSON to a list of objects
				List<CloudUploadEntry> uploads = SanimalData.getInstance().getGson().fromJson(uploadJSON, CLOUD_UPLOAD_ENTRY_LIST_TYPE);
				// Because we deserialized our list from JSON, we need to initialize any extra fields using this call
				uploads.forEach(CloudUploadEntry::initFromJSON);
				// Update our collection's uploads
				Platform.runLater(() -> imageCollection.getUploads().setAll(uploads));
			}
		}
	}

	/**
	 * Utility function used to get a list of uploads fro a given collection ID
	 *
	 * @param collectionID The ID of the collection we want to retrieve uploads for
	 * @return A map containing a list of uploads
	 */
	private Map<String, Object> getUploadsForCollection(String collectionID)
	{
		try
		{
			// Get the document corresponding to this imageCollection's ID
			GetRequest getRequest = new GetRequest();
			getRequest
					.index(INDEX_SANIMAL_COLLECTIONS)
					.type(INDEX_SANIMAL_COLLECTIONS_TYPE)
					// Make sure the ID corresponds to the imageCollection ID
					.id(collectionID)
					// Only fetch the uploads part of the document
					.fetchSourceContext(new FetchSourceContext(true, new String[] { "uploads" }, new String[] { "name", "organization", "contactInfo", "description", "id", "permissions" }));
			// Perform the GET request
			GetResponse getResponse = this.elasticSearchClient.get(getRequest);
			// It should exist...
			if (getResponse.isExists() && !getResponse.isSourceEmpty())
			{
				// Return the response
				return getResponse.getSourceAsMap();
			}
		}
		catch (IOException e)
		{
			// If something went wrong, print out an error.
			SanimalData.getInstance().getErrorDisplay().notify("Error retrieving uploads for image collection '" + collectionID + "', error was:\n" + ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	/**
	 * Given a path, a collection ID, and a directory of images, this function indexes the directory of images into the ElasticSearch
	 * index.
	 * @param basePath The base path all images will be placed to on the datastore. Often will look like /iplant/home/user/uploads/
	 * @param collectionID The ID of the collection that these images will be uploaded to
	 * @param directory The directory containing all images awaiting upload
	 * @param uploadEntry The upload entry representing this upload, will be put into our collections index
	 */
	@SuppressWarnings("unchecked")
	public void indexImages(String basePath, String collectionID, ImageDirectory directory, CloudUploadEntry uploadEntry)
	{
		// List of images to be uploaded
		List<ImageEntry> imageEntries = directory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList());

		// Compute the absolute path of the image directory
		String localDirAbsolutePath = directory.getFile().getAbsolutePath();

		try
		{
			// Create a bulk index request to update all these images at once
			BulkRequest bulkRequest = new BulkRequest();

			// Convert the images to a map format ready to be converted to JSON
			for (ImageEntry imageEntry : imageEntries)
			{
				// Our image to JSON map will return 2 items, one is the ID of the document and one is the JSON request
				Tuple<String, XContentBuilder> idAndJSON = this.elasticSearchSchemaManager.imageToJSONMap(imageEntry, collectionID, basePath, localDirAbsolutePath);
				IndexRequest request = new IndexRequest()
						.index(INDEX_SANIMAL_METADATA)
						.type(INDEX_SANIMAL_METADATA_TYPE)
						.id(idAndJSON.v1())
						.source(idAndJSON.v2());
				bulkRequest.add(request);
			}

			// Execute the bulk insert
			BulkResponse bulkResponse = this.elasticSearchClient.bulk(bulkRequest);

			// Check if everything went OK, if not return an error
			if (bulkResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().notify("Error bulk inserting metadata, error response was: " + bulkResponse.status());

			// Now that we've updated our metadata index, update the collections uploads field

			// Update the uploads field
			UpdateRequest updateRequest = new UpdateRequest();

			// We do this update with a script, and it needs 1 argument. Create of map of that 1 argument now
			HashMap<String, Object> args = new HashMap<>();
			// We can't use XContentBuilder so just use hashmaps. We set all appropriate fields and insert it as the arguments parameter
			args.put("upload", new HashMap<String, Object>()
			{{
				put("imageCount", uploadEntry.getImageCount());
				put("imagesWithSpecies", uploadEntry.getImagesWithSpecies());
				put("uploadDate", uploadEntry.getUploadDate().atZone(ZoneId.systemDefault()).format(SanimalMetadataFields.INDEX_DATE_TIME_FORMAT));
				put("editComments", uploadEntry.getEditComments());
				put("uploadUser", uploadEntry.getUploadUser());
				put("uploadIRODSPath", uploadEntry.getUploadIRODSPath());
			}});
			updateRequest
				.index(INDEX_SANIMAL_COLLECTIONS)
				.type(INDEX_SANIMAL_COLLECTIONS_TYPE)
				.id(collectionID)
				// We use a script because we're updating nested fields. The script written out looks like:
				/*
				ctx._source.uploads.add(params.upload)
				 */
				.script(new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, "ctx._source.uploads.add(params.upload)", args));
			// Execute the update, and save the result
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);
			// If the response was not OK, print an error
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().notify("Could not update the Collection's index with a new upload!");
		}
		catch (IOException e)
		{
			// If the update failed for some reason, print that error
			SanimalData.getInstance().getErrorDisplay().notify("Could not insert the upload into the collection index!\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Called to update existing images already present in the index
	 *
	 * @param imagesToSave The list of images that need saving
	 * @param collectionID The ID of the collection that these images belong to
	 * @param cloudUploadEntry An upload entry representing upload metadata
	 */
	@SuppressWarnings("unchecked")
	public void updateIndexedImages(List<CloudImageEntry> imagesToSave, String collectionID, CloudUploadEntry cloudUploadEntry)
	{
		try
		{
			// Do the update in bulk
			BulkRequest bulkUpdate = new BulkRequest();

			// For each image entry, create an update request and add it to the bulk update
			for (CloudImageEntry cloudImageEntry : imagesToSave)
			{
				Tuple<String, XContentBuilder> idAndJSON = this.elasticSearchSchemaManager.imageToJSONMap(cloudImageEntry, collectionID, cloudImageEntry.getCyverseFile().getAbsolutePath());
				// The first update will update the metadata in the metadata index
				UpdateRequest updateMetaRequest = new UpdateRequest();
				updateMetaRequest
						.index(INDEX_SANIMAL_METADATA)
						.type(INDEX_SANIMAL_METADATA_TYPE)
						.id(idAndJSON.v1())
						// The new document will contain all new fields
						.doc(idAndJSON.v2());

				// The second update will update the collection upload metadata
				UpdateRequest updateCollectionRequest = new UpdateRequest();
				// We do this update with a script, and it needs 3 arguments. Create of map of those 3 arguments now
				HashMap<String, Object> args = new HashMap<>();
				args.put("pathID", cloudUploadEntry.getUploadIRODSPath());
				args.put("comment", cloudUploadEntry.getEditComments().get(cloudUploadEntry.getEditComments().size() - 1));
				args.put("imagesWithSpecies", cloudUploadEntry.getImagesWithSpecies());

				// Setup the collection update request
				updateCollectionRequest
						.index(INDEX_SANIMAL_COLLECTIONS)
						.type(INDEX_SANIMAL_COLLECTIONS_TYPE)
						.id(collectionID)
						// We use a script because we're updating nested fields. The script written out looks like:
						/*
						// Iterate over all uploads
						for (upload in ctx._source.uploads)
						{
							// If the IDs match
							if (upload.uploadIRODSPath == params.pathID)
							{
								// Add the edit comment, and update the images with species
								upload.editComments.add(params.comment);
								upload.imagesWithSpecies = params.imagesWithSpecies;
							}
						 }
						 */
						.script(new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG, "for (upload in ctx._source.uploads) { if (upload.uploadIRODSPath == params.pathID) { upload.editComments.add(params.comment); upload.imagesWithSpecies = params.imagesWithSpecies } }", args));

				// Add the updates to the bulk request
				bulkUpdate.add(updateMetaRequest);
				bulkUpdate.add(updateCollectionRequest);
			}

			// Fire off the bulk request and save the response
			BulkResponse bulkResponse = this.elasticSearchClient.bulk(bulkUpdate);

			// If the status was not OK, print an error
			if (bulkResponse.status() != RestStatus.OK)
				System.err.println("Bulk insert responded without an OK, status was: " + bulkResponse.status().toString());
		}
		catch (IOException e)
		{
			// If something went wrong while updating, print an error
			SanimalData.getInstance().getErrorDisplay().notify("Error updating the image index. Error was:\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Performs a query given an ElasticSearch query builder that returns a list of images that match that query
	 *
	 * @param queryBuilder The query builder used to specify query parameters
	 * @return The list of images that match the query
	 */
	public List<ImageEntry> performQuery(ElasticSearchQuery queryBuilder)
	{
		// The list of images to return
		List<ImageEntry> toReturn = new ArrayList<>();

		// We use a scroll to retrieve results 50 at a time instead of all at once
		Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10));

		// The search request to perform the query
		SearchRequest searchRequest = new SearchRequest();
		searchRequest
				.indices(INDEX_SANIMAL_METADATA)
				.types(INDEX_SANIMAL_METADATA_TYPE)
				// Set the scroll up so that we don't retrieve all results at once
				.scroll(scroll)
				// The source of the query will be our query builder
				.source(new SearchSourceBuilder()
					.query(queryBuilder.build())
					.size(50)
					.fetchSource(FetchSourceContext.FETCH_SOURCE));

		try
		{
			// Execute the query
			SearchResponse searchResponse = this.elasticSearchClient.search(searchRequest);
			// Grab the new scroll ID from the response used in the follow up search
			String scrollID = searchResponse.getScrollId();
			// Grab a list of hits from the search
			SearchHit[] searchHits = searchResponse.getHits().getHits();

			// A unique list of species and locations is used to ensure images with identical locations don't create two locations
			List<Location> uniqueLocations = new LinkedList<>();
			List<Species> uniqueSpecies = new LinkedList<>();

			// While we have results...
			while (searchHits != null && searchHits.length > 0)
			{
				// Iterate over all search hits and fetch the source
				for (SearchHit searchHit : searchHits)
				{
					// Grab the source for the image, and convert it into a usable format
					Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
					// Conver the image and store it
					ImageEntry imageEntry = this.convertSourceToImage(sourceAsMap, uniqueSpecies, uniqueLocations);
					if (imageEntry != null)
						toReturn.add(imageEntry);
				}

				// Test if there are more results by using a scroll request
				SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
				// Set the ID and scroll of this scroll request
				searchScrollRequest
					.scrollId(scrollID)
					.scroll(scroll);
				// Perform a search and save the result into the same object. ALso save hits and the next scroll ID
				// for the next iteration
				searchResponse = this.elasticSearchClient.searchScroll(searchScrollRequest);
				scrollID = searchResponse.getScrollId();
				searchHits = searchResponse.getHits().getHits();
			}

			// After the query is complete we clear the scroll request
			ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
			clearScrollRequest.addScrollId(scrollID);
			// Execute the scroll clear
			ClearScrollResponse clearScrollResponse = this.elasticSearchClient.clearScroll(clearScrollRequest);
			// If the clear fails, print an error
			if (!clearScrollResponse.isSucceeded())
				SanimalData.getInstance().getErrorDisplay().notify("Clearing the scroll after querying did not succeed!");
		}
		catch (IOException e)
		{
			// If something goes wrong with the query print an error
			SanimalData.getInstance().getErrorDisplay().notify("Error occurred when performing query!\n" + ExceptionUtils.getStackTrace(e));
		}

		return toReturn;
	}

	/**
	 * Utility function used to convert raw index metadata into a structured format
	 *
	 * @param source The raw metadata in key->value format
	 * @param uniqueSpecies A list of current species to be used. This ensures we don't allocate thousands of species objects
	 * @param uniqueLocations A list of current locations to be used. This ensures we don't allocate thousands of location objects
	 * @return An image entry representing the source, or null if something went wrong
	 */
	@SuppressWarnings("unchecked")
	private ImageEntry convertSourceToImage(Map<String, Object> source, List<Species> uniqueSpecies, List<Location> uniqueLocations)
	{
		try
		{
			// Source must have storage path and metadata fields
			if (source.containsKey("storagePath") && source.containsKey("imageMetadata"))
			{
				// Grab the two fields
				Object storagePathObj = source.get("storagePath");
				Object imageMetadataObj = source.get("imageMetadata");
				// Make sure the fields are of the correct type
				if (storagePathObj instanceof String && imageMetadataObj instanceof Map<?, ?>)
				{
					// Cast the objects to the correct type
					String storagePath = (String) storagePathObj;
					Map<String, Object> imageMetadata = (Map<String, Object>) imageMetadataObj;
					// Make sure the metadata map contains the correct fields
					if (imageMetadata.containsKey("dateTaken") && imageMetadata.containsKey("location") && imageMetadata.containsKey("speciesEntries"))
					{
						// Pull the metadata map fields
						Object dateTakenObj = imageMetadata.get("dateTaken");
						Object locationObj = imageMetadata.get("location");
						Object speciesListObj = imageMetadata.get("speciesEntries");
						// Make sure that the objects are of the correct type
						if (dateTakenObj instanceof String && locationObj instanceof Map<?, ?> && speciesListObj instanceof List<?>)
						{
							// Cast the objects to the correct type
							LocalDateTime dateTaken = ZonedDateTime.parse((String) dateTakenObj, SanimalMetadataFields.INDEX_DATE_TIME_FORMAT).toLocalDateTime();
							Map<String, Object> locationMap = (Map<String, Object>) locationObj;
							List<Object> speciesEntryList = (List<Object>) speciesListObj;
							// Make sure that our location map has a position field
							if (locationMap.containsKey("position"))
							{
								// Grab the position string from the map
								Object positionObj = locationMap.get("position");
								// Make sure the position object is of the right format
								if (positionObj instanceof String)
								{
									// Remove this field from the location map, to later replace it with a properly formatted string
									String locationPosition = (String) locationMap.remove("position");
									// Split the location string into 'lat' and 'long'
									String[] latLongArray = locationPosition.split(", ");
									// The split should return 2 results
									if (latLongArray.length == 2)
									{
										// Store the latitude and longitude back into the location. We do this so that
										locationMap.put("latitude", latLongArray[0]);
										locationMap.put("longitude", latLongArray[1]);

										// Convert our hashmaps into a usable format
										Gson gson = SanimalData.getInstance().getGson();
										Location tempLocation = gson.fromJson(gson.toJson(locationMap), Location.class);
										List<SpeciesEntry> tempSpeciesEntries = gson.fromJson(gson.toJson(speciesEntryList), SPECIES_ENTRY_LIST_TYPE);
										// Pull unique species off of the species entries list
										List<Species> tempSpeciesList = tempSpeciesEntries.stream().map(SpeciesEntry::getSpecies).distinct().collect(Collectors.toList());
										// Make sure each of the species has the default icon set
										tempSpeciesList.forEach(species -> species.setSpeciesIcon(Species.DEFAULT_ICON));

										// Compute a new location if we need to
										Boolean locationForImagePresent = uniqueLocations.stream().anyMatch(location -> location.getId().equals(tempLocation.getId()));
										// Do we have the location? If not add it
										if (!locationForImagePresent)
											uniqueLocations.add(tempLocation);

										// Compute a new species (s) if we need to
										for (Species tempSpecies : tempSpeciesList)
										{
											// Test if the species is present, if not add it
											Boolean speciesForImagePresent = uniqueSpecies.stream().anyMatch(species -> species.getScientificName().equalsIgnoreCase(tempSpecies.getScientificName()));
											if (!speciesForImagePresent)
												uniqueSpecies.add(tempSpecies);
										}

										// Grab the correct location for the image entry
										Location correctLocation = uniqueLocations.stream().filter(location -> location.getId().equals(tempLocation.getId())).findFirst().get();
										// Create the image entry
										ImageEntry entry = new ImageEntry(new File(storagePath));
										// Set the location and date taken
										entry.setLocationTaken(correctLocation);
										entry.setDateTaken(dateTaken);
										// Add the species to the image entries
										for (SpeciesEntry tempSpeciesEntry : tempSpeciesEntries)
										{
											// Grab the species based on scientific name
											Species correctSpecies = uniqueSpecies.stream().filter(species -> species.getScientificName().equals(tempSpeciesEntry.getSpecies().getScientificName())).findFirst().get();
											entry.addSpecies(correctSpecies, tempSpeciesEntry.getCount());
										}
										return entry;
									}
								}
							}
						}
					}
				}
			}
		}
		// This will happen if the numbers are in the wrong format. If so just ignore it and return null
		catch (NumberFormatException ignored)
		{
		}
		return null;
	}

	/**
	 * Finalize method is called like a deconstructor and can be used to clean up any floating objects
	 *
	 * @throws Throwable If finalization fails for some reason
	 */
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();

		// Close the elastic search connection
		try
		{
			this.elasticSearchClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().notify("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}
}
