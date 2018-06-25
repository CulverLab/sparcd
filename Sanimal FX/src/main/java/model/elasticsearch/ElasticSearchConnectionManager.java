package model.elasticsearch;

import com.google.gson.reflect.TypeToken;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import model.util.SettingsData;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
	private static final Integer INDEX_SANIMAL_USERS_SHARD_COUNT = 1;
	private static final Integer INDEX_SANIMAL_USERS_REPLICA_COUNT = 0;

	// The type used to serialize a list of species through Gson
	private static final Type SPECIES_LIST_TYPE = new TypeToken<ArrayList<Species>>()
	{
	}.getType();
	// The type used to serialize a list of locations through Gson
	private static final Type LOCATION_LIST_TYPE = new TypeToken<ArrayList<Location>>()
	{
	}.getType();

	public void nukeAndRecreateIndex()
	{
		RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));

		try
		{
			DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(INDEX_SANIMAL_USERS);
			esClient.indices().delete(deleteIndexRequest);
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error deleting '" + INDEX_SANIMAL_USERS + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}
		catch (ElasticsearchStatusException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Delete failed, status = " + e.status());
		}

		try
		{
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_SANIMAL_USERS);
			createIndexRequest.settings(Settings.builder()
				.put("index.number_of_shards", INDEX_SANIMAL_USERS_SHARD_COUNT)
				.put("index.number_of_replicas", INDEX_SANIMAL_USERS_REPLICA_COUNT));
			createIndexRequest.mapping(INDEX_SANIMAL_USERS_TYPE, this.makeSanimalUsersIndexMapping());
			esClient.indices().create(createIndexRequest);
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error creating '" + INDEX_SANIMAL_USERS + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}

		try
		{
			esClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	private XContentBuilder makeSanimalUsersIndexMapping()
	{
		XContentBuilder builder = null;
		try
		{
			builder = XContentFactory.jsonBuilder()
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
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not build sanimal users index mapping!\n" + ExceptionUtils.getStackTrace(e));
		}
		return builder;
	}

	public void initSanimalRemoteDirectory()
	{
		RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));

		try
		{
			GetRequest getRequest = new GetRequest();
			getRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
			GetResponse getResponse = esClient.get(getRequest);
			// If the user is not in the db... create an index entry for him
			if (!getResponse.isExists())
			{
				try
				{
					IndexRequest indexRequest = new IndexRequest();
					indexRequest
							.index(INDEX_SANIMAL_USERS)
							.type(INDEX_SANIMAL_USERS_TYPE)
							.id(SanimalData.getInstance().getUsername())
							.source(this.makeCreateUser(SanimalData.getInstance().getUsername()));
					esClient.index(indexRequest);
				}
				catch (IOException e)
				{
					SanimalData.getInstance().getErrorDisplay().printError("Error creating '" + INDEX_SANIMAL_USERS + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
				}
			}
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error initializing user '" + SanimalData.getInstance().getUsername() + "' in the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}

		try
		{
			esClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	private XContentBuilder makeCreateUser(String username)
	{
		XContentBuilder builder = null;
		try (InputStreamReader inputStreamSettingsReader = new InputStreamReader(this.getClass().getResourceAsStream("/settings.json"));
			 BufferedReader settingsFileReader = new BufferedReader(inputStreamSettingsReader);
			 InputStreamReader inputStreamLocationsReader = new InputStreamReader(this.getClass().getResourceAsStream("/locations.json"));
			 BufferedReader locationsFileReader = new BufferedReader(inputStreamLocationsReader);
			 InputStreamReader inputStreamSpeciesReader = new InputStreamReader(this.getClass().getResourceAsStream("/species.json"));
			 BufferedReader speciesFileReader = new BufferedReader(inputStreamSpeciesReader))
		{
			// Read the Json file
			String settingsJSON = settingsFileReader.lines().collect(Collectors.joining("\n"));
			XContentParser settingsParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, settingsJSON.getBytes());

			String locationsJSON = locationsFileReader.lines().collect(Collectors.joining("\n"));
			XContentParser locationsParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, locationsJSON.getBytes());

			String speciesJSON = speciesFileReader.lines().collect(Collectors.joining("\n"));
			XContentParser speciesParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, speciesJSON.getBytes());

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
			SanimalData.getInstance().getErrorDisplay().printError("Could not insert a new user into the index!\n" + ExceptionUtils.getStackTrace(e));
		}
		return builder;
	}

	public SettingsData pullRemoteSettings()
	{
		Object settings = fetchFieldForUser("settings");
		if (settings instanceof Map<?, ?>)
		{
			String json = SanimalData.getInstance().getGson().toJson(settings);
			if (json != null)
			{
				return SanimalData.getInstance().getGson().fromJson(json, SettingsData.class);
			}
		}

		return null;
	}

	public List<Species> pullRemoteSpecies()
	{
		Object species = fetchFieldForUser("species");
		if (species instanceof List<?>)
		{
			String json = SanimalData.getInstance().getGson().toJson(species);
			if (json != null)
			{
				return SanimalData.getInstance().getGson().fromJson(json, SPECIES_LIST_TYPE);
			}
		}

		return Collections.emptyList();
	}

	public List<Location> pullRemoteLocations()
	{
		Object locations = fetchFieldForUser("locations");
		if (locations instanceof List<?>)
		{
			((List<?>) locations).forEach(locationMapObj ->
			{
				if (locationMapObj instanceof Map<?, ?>)
				{
					Map<String, Object> locationsMap = (Map<String, Object>) locationMapObj;
					Object position = locationsMap.remove("position");
					if (position instanceof String)
					{
						String[] split = ((String) position).split(", ");
						if (split.length == 2)
						{
							locationsMap.put("latitude", split[0]);
							locationsMap.put("longitude", split[1]);
						}
					}
				}
			});
			String json = SanimalData.getInstance().getGson().toJson(locations);
			if (json != null)
			{
				return SanimalData.getInstance().getGson().fromJson(json, LOCATION_LIST_TYPE);
			}
		}

		return Collections.emptyList();
	}

	private Object fetchFieldForUser(String field)
	{
		RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));

		try
		{
			GetRequest getRequest = new GetRequest();
			List<String> includes = Arrays.asList("username", field);
			List<String> excludes = ListUtils.subtract(Arrays.asList("username", "species", "locations", "settings"), includes);
			getRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.fetchSourceContext(new FetchSourceContext(true, includes.toArray(new String[0]), excludes.toArray(new String[0])));
			GetResponse getResponse = esClient.get(getRequest);
			if (getResponse.isExists() && !getResponse.isSourceEmpty())
			{
				Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
				return sourceAsMap.get(field);
			}
			else
			{
				SanimalData.getInstance().getErrorDisplay().printError("User not found on the DB. This should not be possible.");
			}
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error pulling remote field '" + field + "' for the user '" + SanimalData.getInstance().getUsername() + "' from the ElasticSearch index: \n" + ExceptionUtils.getStackTrace(e));
		}

		try
		{
			esClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}

		return null;
	}

	public void pushLocalSpecies(List<Species> species)
	{
		RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));

		try
		{
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.doc(this.makeSpeciesUpdate(species));

			UpdateResponse updateResponse = esClient.update(updateRequest);

			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error syncing species list, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error updating species list for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}

		try
		{
			esClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	private XContentBuilder makeSpeciesUpdate(List<Species> species)
	{
		XContentBuilder builder = null;
		try
		{
			String speciesJSON = SanimalData.getInstance().getGson().toJson(species);
			builder = XContentFactory.jsonBuilder()
			.startObject()
				.field("species")
				.copyCurrentStructure(XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, speciesJSON.getBytes()))
			.endObject();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not build species list update!\n" + ExceptionUtils.getStackTrace(e));
		}
		return builder;
	}

	public void pushLocalLocations(List<Location> locations)
	{
		RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));

		try
		{
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.doc(this.makeLocationsUpdate(locations));

			UpdateResponse updateResponse = esClient.update(updateRequest);

			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error syncing location list, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error updating location list for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}

		try
		{
			esClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	private Map<String, Object> makeLocationsUpdate(List<Location> locations)
	{
		List<Map<String, Object>> indexedLocations = locations.stream().map(location ->
		{
			Map<String, Object> map = new HashMap<>();
			map.put("name", location.getName());
			map.put("id", location.getId());
			map.put("elevation", location.getElevation());
			map.put("position", location.getLatitude().toString() + ", " + location.getLongitude().toString());
			return map;
		}).collect(Collectors.toList());
		return new HashMap<String, Object>()
		{{
			put("locations", indexedLocations);
		}};
	}

	public void pushLocalSettings(SettingsData settingsData)
	{
		RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_SCHEME)));

		try
		{
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest
					.index(INDEX_SANIMAL_USERS)
					.type(INDEX_SANIMAL_USERS_TYPE)
					.id(SanimalData.getInstance().getUsername())
					.doc(this.makeSettingsUpdate(settingsData));

			UpdateResponse updateResponse = esClient.update(updateRequest);

			if (updateResponse.status() != RestStatus.OK)
				SanimalData.getInstance().getErrorDisplay().printError("Error syncing settings, error response was: " + updateResponse.status());
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Error updating settings for the user '" + SanimalData.getInstance().getUsername() + "'\n" + ExceptionUtils.getStackTrace(e));
		}

		try
		{
			esClient.close();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not close ElasticSearch connection: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	private XContentBuilder makeSettingsUpdate(SettingsData settingsData)
	{
		XContentBuilder builder = null;
		try
		{
			String settingsJSON = SanimalData.getInstance().getGson().toJson(settingsData);
			builder = XContentFactory.jsonBuilder()
					.startObject()
					.field("settings")
					.copyCurrentStructure(XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, settingsJSON.getBytes()))
					.endObject();
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Could not build settings update!\n" + ExceptionUtils.getStackTrace(e));
		}
		return builder;
	}
}
