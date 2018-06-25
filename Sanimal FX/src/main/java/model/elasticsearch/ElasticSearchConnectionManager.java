package model.elasticsearch;

import com.google.gson.reflect.TypeToken;
import model.SanimalData;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.util.SettingsData;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
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
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
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

	// The type used to serialize a list of species through Gson
	private static final Type SPECIES_LIST_TYPE = new TypeToken<ArrayList<Species>>()
	{
	}.getType();
	// The type used to serialize a list of locations through Gson
	private static final Type LOCATION_LIST_TYPE = new TypeToken<ArrayList<Location>>()
	{
	}.getType();

	// Create a new elastic search client
	private final RestHighLevelClient elasticSearchClient;

	public ElasticSearchConnectionManager()
	{
		// Establish a connection to the elastic search server
		this.elasticSearchClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));
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
			createIndexRequest.mapping(INDEX_SANIMAL_USERS_TYPE, this.makeSanimalUsersIndexMapping());
			// Execute the index request
			this.elasticSearchClient.indices().create(createIndexRequest);
		}
		catch (IOException e)
		{
			// Print any errors we may have encountered
			SanimalData.getInstance().getErrorDisplay().printError("Error creating '" + INDEX_SANIMAL_USERS + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Helper function which returns the JSON required to create the user's index mapping
	 *
	 * @return An XContentBuilder which can be used to create JSON in Java
	 */
	private XContentBuilder makeSanimalUsersIndexMapping() throws IOException
	{
		// Well, it's the builder design pattern. RIP me
		return XContentFactory.jsonBuilder()
		.startObject()
			.startObject(INDEX_SANIMAL_USERS_TYPE)
				.startObject("properties")
					.startObject("username")
						.field("type", "keyword")
					.endObject()
					.startObject("species")
						.field("type", "nested")
						.startObject("properties")
							.startObject("name")
								.field("type", "text")
							.endObject()
							.startObject("scientificName")
								.field("type", "text")
							.endObject()
							.startObject("keyBinding")
								.field("type", "keyword")
							.endObject()
							.startObject("speciesIconURL")
								.field("type", "keyword")
							.endObject()
						.endObject()
					.endObject()
					.startObject("locations")
						.field("type", "nested")
						.startObject("properties")
							.startObject("name")
								.field("type", "text")
							.endObject()
							.startObject("id")
								.field("type", "keyword")
							.endObject()
							.startObject("position")
								.field("type", "geo_point")
							.endObject()
							.startObject("elevation")
								.field("type", "double")
							.endObject()
						.endObject()
					.endObject()
					.startObject("settings")
						.field("type", "object")
						.startObject("properties")
							.startObject("dateFormat")
								.field("type", "keyword")
							.endObject()
							.startObject("timeFormat")
								.field("type", "keyword")
							.endObject()
							.startObject("locationFormat")
								.field("type", "keyword")
							.endObject()
							.startObject("distanceUnits")
								.field("type", "keyword")
							.endObject()
							.startObject("popupDisplaySec")
								.field("type", "double")
							.endObject()
							.startObject("drSandersonDirectoryCompatibility")
								.field("type", "boolean")
							.endObject()
							.startObject("drSandersonOutput")
								.field("type", "boolean")
							.endObject()
							.startObject("automaticNextImage")
								.field("type", "boolean")
							.endObject()
							.startObject("backgroundImageLoading")
								.field("type", "boolean")
							.endObject()
							.startObject("noPopups")
								.field("type", "boolean")
							.endObject()
						.endObject()
					.endObject()
				.endObject()
			.endObject()
		.endObject();
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
			createIndexRequest.mapping(INDEX_SANIMAL_METADATA_TYPE, this.makeSanimalMetadataIndexMapping());
			// Execute the index request
			this.elasticSearchClient.indices().create(createIndexRequest);
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error creating '" + INDEX_SANIMAL_METADATA + "' in the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Helper function which returns the JSON required to create the metadata index mapping
	 *
	 * @return An XContentBuilder which can be used to create JSON in Java
	 */
	private XContentBuilder makeSanimalMetadataIndexMapping() throws IOException
	{
		// Well, it's the builder design pattern. RIP me
		return XContentFactory.jsonBuilder()
		.startObject()
			.startObject(INDEX_SANIMAL_METADATA_TYPE)
				.startObject("properties")
					.startObject("storageType")
						.field("type", "keyword")
					.endObject()
					.startObject("storagePath")
						.field("type", "text")
					.endObject()
					.startObject("collectionID")
						.field("type", "keyword")
					.endObject()
					.startObject("imageMetadata")
						.field("type", "object")
						.startObject("properties")
							.startObject("dateTaken")
								.field("type", "date")
								.field("format", "yyyy-MM-dd HH:mm:ss")
							.endObject()
							.startObject("location")
								.field("type", "object")
								.startObject("properties")
									.startObject("name")
										.field("type", "text")
									.endObject()
									.startObject("id")
										.field("type", "text")
									.endObject()
									.startObject("elevation")
										.field("type", "double")
									.endObject()
									.startObject("position")
										.field("type", "geo_point")
									.endObject()
								.endObject()
							.endObject()
							.startObject("species")
								.field("type", "nested")
								.startObject("properties")
									.startObject("commonName")
										.field("type", "text")
									.endObject()
									.startObject("scientificName")
										.field("type", "text")
									.endObject()
									.startObject("count")
										.field("type", "integer")
									.endObject()
								.endObject()
							.endObject()
						.endObject()
					.endObject()
				.endObject()
			.endObject()
		.endObject();
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
			SanimalData.getInstance().getErrorDisplay().printError("Error deleting '" + index + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
		catch (ElasticsearchStatusException e)
		{
			// If the delete fails just print out an error message
			SanimalData.getInstance().getErrorDisplay().printError("Delete failed, status = " + e.status());
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
						.source(this.makeCreateUser(SanimalData.getInstance().getUsername()));
				// Perform the index request
				this.elasticSearchClient.index(indexRequest);
			}
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error initializing user '" + SanimalData.getInstance().getUsername() + "' in the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Given a username, this function returns the JSON representing a default user with that username
	 *
	 * @param username The username of the user to be added into an index
	 * @return A JSON blob containing all default values ready to setup a user's account
	 */
	private XContentBuilder makeCreateUser(String username)
	{
		XContentBuilder builder = null;
		// Massive try-with-resources block used to read 3 default JSON files, one containing settings, one containing locations, and one
		// containing species.
		try (InputStreamReader inputStreamSettingsReader = new InputStreamReader(this.getClass().getResourceAsStream("/settings.json"));
			 BufferedReader settingsFileReader = new BufferedReader(inputStreamSettingsReader);
			 InputStreamReader inputStreamLocationsReader = new InputStreamReader(this.getClass().getResourceAsStream("/locations.json"));
			 BufferedReader locationsFileReader = new BufferedReader(inputStreamLocationsReader);
			 InputStreamReader inputStreamSpeciesReader = new InputStreamReader(this.getClass().getResourceAsStream("/species.json"));
			 BufferedReader speciesFileReader = new BufferedReader(inputStreamSpeciesReader))
		{
			// Read the settings json file
			String settingsJSON = settingsFileReader.lines().collect(Collectors.joining("\n"));
			XContentParser settingsParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, settingsJSON.getBytes());

			// Read the locations json file
			String locationsJSON = locationsFileReader.lines().collect(Collectors.joining("\n"));
			XContentParser locationsParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, locationsJSON.getBytes());

			// Read the species json file
			String speciesJSON = speciesFileReader.lines().collect(Collectors.joining("\n"));
			XContentParser speciesParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, speciesJSON.getBytes());

			// Setup the JSON by using the default values found in the JSON files
			builder = XContentFactory.jsonBuilder()
			.startObject()
				.field("username", username)
				.field("settings")
				.copyCurrentStructure(settingsParser)
				.field("species")
				.copyCurrentStructure(speciesParser)
				.field("locations")
				.copyCurrentStructure(locationsParser)
			.endObject();
		}
		catch (IOException e)
		{
			// Print an error if something went wrong internally
			SanimalData.getInstance().getErrorDisplay().printError("Could not insert a new user into the index!\n" + ExceptionUtils.getStackTrace(e));
		}
		return builder;
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
				SanimalData.getInstance().getErrorDisplay().printError("User not found on the DB. This should not be possible.");
			}
		}
		catch (IOException e)
		{
			// Error happened when executing a GET request. Print an error
			SanimalData.getInstance().getErrorDisplay().printError("Error pulling remote field '" + field + "' for the user '" + SanimalData.getInstance().getUsername() + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
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
					.doc(this.makeSpeciesUpdate(species));

			// Send off the update
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// Test to make sure it went OK, if not, return an error
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error syncing species list, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update fails
			SanimalData.getInstance().getErrorDisplay().printError("Error updating species list for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Helper function to create a species update JSON blob given a list of species as the replacement
	 *
	 * @param species A list of species to replace the existing list
	 * @return A JSON blob ready to be executed by an update request
	 */
	private XContentBuilder makeSpeciesUpdate(List<Species> species) throws IOException
	{
		// Convert the species list to JSON in preperatation
		String speciesJSON = SanimalData.getInstance().getGson().toJson(species);
		// Create a species field with the value as the JSON blob we just created above
		return XContentFactory.jsonBuilder()
		.startObject()
			.field("species")
			.copyCurrentStructure(XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, speciesJSON.getBytes()))
		.endObject();
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
					.doc(this.makeLocationsUpdate(locations));

			// Fire off the update request
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// Print an error if the response code is not OK
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error syncing location list, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update request fails
			SanimalData.getInstance().getErrorDisplay().printError("Error updating location list for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Helper function that takes a list of locations and turns them into a map of string->object format ready to
	 * by converted into JSON by the elasticsearch client.
	 *
	 * @param locations The list of locations to be converted
	 * @return A map of key value pairs to be converted into JSON
	 */
	private Map<String, Object> makeLocationsUpdate(List<Location> locations)
	{
		// We need to do some post-processing here, We store locations as [lat, long] points, but ElasticSearch
		// uses a special geo point data type, so we need to convert our lat and long fields into a position field
		List<Map<String, Object>> indexedLocations = locations.stream().map(location ->
		{
			// Create a new map which contains name, id, and elevation as usual. It also contains a position field which is
			// built from lat and long.
			Map<String, Object> map = new HashMap<>();
			map.put("name", location.getName());
			map.put("id", location.getId());
			map.put("elevation", location.getElevation());
			map.put("position", location.getLatitude().toString() + ", " + location.getLongitude().toString());
			return map;
		}).collect(Collectors.toList());
		// Return a wrapper around this location list ready to be indexed
		return new HashMap<String, Object>()
		{{
			put("locations", indexedLocations);
		}};
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
					.doc(this.makeSettingsUpdate(settingsData));

			// Perform the update and test the response
			UpdateResponse updateResponse = this.elasticSearchClient.update(updateRequest);

			// If the response is OK, continue, if not print an error
			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error syncing settings, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the update fails
			SanimalData.getInstance().getErrorDisplay().printError("Error updating settings for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Given a settings object, this function will create a JSON blob contain all settings info
	 *
	 * @param settingsData The data to be stored in the JSON blob
	 * @return A content builder ready to be exported as JSON
	 */
	private XContentBuilder makeSettingsUpdate(SettingsData settingsData) throws IOException
	{
		// Convert the settings data to JSON. Store the JSON and convert it to a content factory object
		String settingsJSON = SanimalData.getInstance().getGson().toJson(settingsData);
		// The field is called settings, and the value is the JSON we just produced
		return XContentFactory.jsonBuilder()
				.startObject()
				.field("settings")
				.copyCurrentStructure(XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, settingsJSON.getBytes()))
				.endObject();
	}

	/**
	 * Given a path, a collection ID, and a directory of images, this function indexes the directory of images into the ElasticSearch
	 * index.
	 *
	 * @param basePath The base path all images will be placed to on the datastore. Often will look like /iplant/home/user/uploads/
	 * @param collectionID The ID of the collection that these images will be uploaded to
	 * @param directory The directory containing all images awaiting upload
	 */
	public void indexImages(String basePath, String collectionID, ImageDirectory directory)
	{
		// List of images to be uploaded
		List<ImageEntry> imageEntries = directory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList());

		// Compute the absolute path of the image directory
		String localDirAbsolutePath = directory.getFile().getAbsolutePath();

		// Convert the images to a map format ready to be converted to JSON
		List<Map<String, Object>> imageMetadata = imageEntries.stream().map(imageEntry ->
		{
			Map<String, Object> metadata = new HashMap<>();
			// For now we only support storing data in the CyVerse datastore format
			metadata.put("storageType", "CyVerse Datastore");
			// Compute the final path the image will have once uploaded on the datastore. It should look something like:
			// /iplant/home/user/.../Uploads/imgxyz.jpg
			String fileAbsolutePath = basePath + StringUtils.substringAfter(imageEntry.getFile().getAbsolutePath(), localDirAbsolutePath);
			fileAbsolutePath = fileAbsolutePath.replace('\\', '/');
			// Store the absolute path to the image file
			metadata.put("storagePath", fileAbsolutePath);
			// Store the ID of the collection that this image belongs into
			metadata.put("collectionID", collectionID);
			// Store image EXIF metadata
			metadata.put("imageMetadata", new HashMap<String, Object>()
			{{
				// The date the image was taken
				put("dateTaken", imageEntry.getDateTaken().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				// The location the image was taken at. Has an ID field, a name field, a position and elevation
				put("location", new HashMap<String, Object>()
				{{
					put("elevation", imageEntry.getLocationTaken().getElevation());
					put("id", imageEntry.getLocationTaken().getId());
					put("name", imageEntry.getLocationTaken().getName());
					put("position", imageEntry.getLocationTaken().getLatitude() + ", " + imageEntry.getLocationTaken().getLongitude());
				}});
				// The species present in the image, stored as a list of objects containing common name, scientific name, and count
				put("species", imageEntry.getSpeciesPresent().stream().map(speciesEntry ->
				{
					Map<String, Object> speciesData = new HashMap<>();
					speciesData.put("commonName", speciesEntry.getSpecies().getName());
					speciesData.put("scientificName", speciesEntry.getSpecies().getScientificName());
					speciesData.put("count", speciesEntry.getAmount());
					return speciesData;
				}).collect(Collectors.toList()));
			}});
			return metadata;
		}).collect(Collectors.toList());

		// Create a bulk index request to update all these images at once
		BulkRequest bulkRequest = new BulkRequest();

		// For each item we want to index, store it into our bulk index request
		imageMetadata.forEach(metadata ->
		{
			IndexRequest request = new IndexRequest()
					.index(INDEX_SANIMAL_METADATA)
					.type(INDEX_SANIMAL_METADATA_TYPE)
					.source(metadata);
			bulkRequest.add(request);
		});

		try
		{
			// Execute the bulk insert
			BulkResponse bulkResponse = this.elasticSearchClient.bulk(bulkRequest);

			// Check if everything went OK, if not return an error
			if (bulkResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error bulk inserting metadata, error response was: " + bulkResponse.status());
		}
		catch (IOException e)
		{
			// Print an error if the bulk insert fails
			SanimalData.getInstance().getErrorDisplay().printError("Error bulk inserting metadata, response was: '\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Finalize method is called like a deconstructor and can be used to clean up any floating objects
	 *
	 * @throws Throwable
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
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}
}
