package model.elasticsearch;

import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.cyverse.ImageCollection;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.SettingsData;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.xcontent.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class used to create JSON requests from model classes for managing schemas
 */
public class ElasticSearchSchemaManager
{
	/**
	 * Helper function which returns the JSON required to create the user's index mapping
	 *
	 * @return An XContentBuilder which can be used to create JSON in Java
	 */
	XContentBuilder makeSanimalUsersIndexMapping(String indexType) throws IOException
	{
		// Well, it's the builder design pattern. RIP me
		return XContentFactory.jsonBuilder()
		.startObject()
			.startObject(indexType)
				.startObject("properties")
					.startObject("username")
						.field("type", "keyword")
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
	 * Helper function which returns the JSON required to create the metadata index mapping
	 *
	 * @return An XContentBuilder which can be used to create JSON in Java
	 */
	XContentBuilder makeSanimalMetadataIndexMapping(String indexType) throws IOException
	{
		// Well, it's the builder design pattern. RIP me
		return XContentFactory.jsonBuilder()
		.startObject()
			.startObject(indexType)
				.startObject("properties")
					.startObject("storageType")
						.field("type", "keyword")
					.endObject()
					.startObject("storagePath")
						.field("type", "keyword")
					.endObject()
					.startObject("collectionID")
						.field("type", "keyword")
					.endObject()
					.startObject("imageMetadata")
						.field("type", "object")
						.startObject("properties")
							.startObject("dateTaken")
								.field("type", "date")
								.field("format", "date_time")
							.endObject()
							.startObject("yearTaken")
								.field("type", "integer")
							.endObject()
							.startObject("monthTaken")
								.field("type", "integer")
							.endObject()
							.startObject("hourTaken")
								.field("type", "integer")
							.endObject()
							.startObject("dayOfYearTaken")
								.field("type", "integer")
							.endObject()
							.startObject("dayOfWeekTaken")
								.field("type", "integer")
							.endObject()
							.startObject("location")
								.field("type", "object")
								.startObject("properties")
									.startObject("name")
										.field("type", "keyword")
									.endObject()
									.startObject("id")
										.field("type", "keyword")
									.endObject()
									.startObject("elevation")
										.field("type", "double")
									.endObject()
									.startObject("position")
										.field("type", "geo_point")
									.endObject()
								.endObject()
							.endObject()
							.startObject("speciesEntries")
								.field("type", "nested")
								.startObject("properties")
									.startObject("species")
										.field("type", "object")
										.startObject("properties")
											.startObject("commonName")
												.field("type", "keyword")
											.endObject()
											.startObject("scientificName")
												.field("type", "keyword")
											.endObject()
										.endObject()
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
	 * Helper function which returns the JSON required to create the collections index mapping
	 *
	 * @return An XContentBuilder which can be used to create JSON in Java
	 */
	XContentBuilder makeSanimalCollectionsIndexMapping(String indexType) throws IOException
	{
		// Well, it's the builder design pattern. RIP me
		return XContentFactory.jsonBuilder()
		.startObject()
			.startObject(indexType)
				.startObject("properties")
					.startObject("name")
						.field("type", "keyword")
					.endObject()
					.startObject("organization")
						.field("type", "keyword")
					.endObject()
					.startObject("contactInfo")
						.field("type", "keyword")
					.endObject()
					.startObject("description")
						.field("type", "text")
					.endObject()
					.startObject("id")
						.field("type", "keyword")
					.endObject()
					.startObject("permissions")
						.field("type", "nested")
						.startObject("properties")
							.startObject("username")
								.field("type", "keyword")
							.endObject()
							.startObject("read")
								.field("type", "boolean")
							.endObject()
							.startObject("upload")
								.field("type", "boolean")
							.endObject()
							.startObject("owner")
								.field("type", "boolean")
							.endObject()
						.endObject()
					.endObject()
					.startObject("uploads")
						.field("type", "nested")
						.startObject("properties")
							.startObject("uploadUser")
								.field("type", "keyword")
							.endObject()
							.startObject("uploadDate")
								.field("type", "date")
								.field("format", "date_time")
							.endObject()
							.startObject("imagesWithSpecies")
								.field("type", "integer")
							.endObject()
							.startObject("imageCount")
								.field("type", "integer")
							.endObject()
							.startObject("editComments")
								.field("type", "text")
							.endObject()
							.startObject("uploadIRODSPath")
								.field("type", "keyword")
							.endObject()
						.endObject()
					.endObject()
				.endObject()
			.endObject()
		.endObject();
	}

	/**
	 * Given a username, this function returns the JSON representing a default user with that username
	 *
	 * @param username The username of the user to be added into an index
	 * @return A JSON blob containing all default values ready to setup a user's account
	 */
	XContentBuilder makeCreateUser(String username)
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
			SanimalData.getInstance().getErrorDisplay().notify("Could not insert a new user into the index!\n" + ExceptionUtils.getStackTrace(e));
		}
		return builder;
	}

	/**
	 * Helper function to create a species update JSON blob given a list of species as the replacement
	 *
	 * @param species A list of species to replace the existing list
	 * @return A JSON blob ready to be executed by an update request
	 */
	XContentBuilder makeSpeciesUpdate(List<Species> species) throws IOException
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
	 * Helper function that takes a list of locations and turns them into a map of string->object format ready to
	 * by converted into JSON by the elasticsearch client.
	 *
	 * @param locations The list of locations to be converted
	 * @return A map of key value pairs to be converted into JSON
	 */
	XContentBuilder makeLocationsUpdate(List<Location> locations) throws IOException
	{
		// Begin locations updates with an array (since we have a list of locations)
		XContentBuilder locationsJSON = XContentFactory.jsonBuilder()
		.startObject()
			.startArray("locations");

		// Iterate over all locations passed in, for each create an object and assign proper fields
		for (Location location : locations)
		{
			locationsJSON
			.startObject()
				.field("name", location.getName())
				.field("id", location.getId())
				.field("elevation", location.getElevation())
				.field("position", location.getLatitude() + ", " + location.getLongitude())
			.endObject();
		}

		// Finish the locations JSON array and return it
		locationsJSON
			.endArray()
		.endObject();
		return locationsJSON;
	}

	/**
	 * Given a settings object, this function will create a JSON blob contain all settings info
	 *
	 * @param settingsData The data to be stored in the JSON blob
	 * @return A content builder ready to be exported as JSON
	 */
	XContentBuilder makeSettingsUpdate(SettingsData settingsData) throws IOException
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
	 * Utility function used to create a JSON request body which creates a collection
	 *
	 * @param imageCollection The image collection to create the request for
	 * @return A JSON builder formatted to create a collection
	 * @throws IOException If the JSON is improperly formatted
	 */
	XContentBuilder makeCreateCollection(ImageCollection imageCollection) throws IOException
	{
		// Convert the collection to JSON
		String collectionJSON = SanimalData.getInstance().getGson().toJson(imageCollection);

		// Read this JSON directly and return it. Simple as that
		return XContentFactory.jsonBuilder()
			.copyCurrentStructure(XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, collectionJSON.getBytes()));
	}

	/**
	 * Utility function used to create a JSON request body which updates a collection's settings
	 *
	 * @param imageCollection The collection to update
	 * @return A JSON blob formatted to update the collection
	 * @throws IOException If something in the JSON went wrong
	 */
	XContentBuilder makeCollectionUpdate(ImageCollection imageCollection) throws IOException
	{
		// Convert the collection's permission data to JSON. Store the JSON and convert it to a content factory object
		// We can't do the entire document at once because we don't want to overwrite uploads
		String permissionsJSON = SanimalData.getInstance().getGson().toJson(imageCollection.getPermissions());
		return XContentFactory.jsonBuilder()
		.startObject()
			// Setup all the basic fields
			.field("contactInfo", imageCollection.getContactInfo())
			.field("description", imageCollection.getDescription())
			.field("id", imageCollection.getID().toString())
			.field("name", imageCollection.getName())
			.field("organization", imageCollection.getOrganization())
			// Permissions are pulled permission list
			.field("permissions")
			.copyCurrentStructure(XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, permissionsJSON.getBytes()))
		.endObject();
	}

	/**
	 * Utility function used to convert an image entry to its JSON representation
	 *
	 * @param imageEntry The image to conver to its JSON representation
	 * @param collectionID The ID of the collection that the image belongs to
	 * @param basePath The base path on CyVerse that the image belongs to
	 * @param localDirAbsolutePath The local directory absolute path of the image
	 * @return A map of key->value pairs used later in creating JSON
	 */
	Tuple<String, XContentBuilder> imageToJSONMap(ImageEntry imageEntry, String collectionID, String basePath, String localDirAbsolutePath) throws IOException
	{
		// Compute the final path the image will have once uploaded on the datastore. It should look something like:
		// /iplant/home/user/.../Uploads/imgxyz.jpg
		return this.imageToJSONMap(imageEntry, collectionID, basePath + StringUtils.substringAfter(imageEntry.getFile().getAbsolutePath(), localDirAbsolutePath));
	}

	/**
	 * Utility function used to convert an image entry to its JSON representation
	 *
	 * @param imageEntry The image to conver to its JSON representation
	 * @param collectionID The ID of the collection that the image belongs to
	 * @param fileAbsolutePath The absolute path of the file on CyVerse
	 * @return A map of key->value pairs used later in creating JSON
	 */
	Tuple<String, XContentBuilder> imageToJSONMap(ImageEntry imageEntry, String collectionID, String fileAbsolutePath) throws IOException
	{
		// On windows paths have \ as a path separator vs unix /. Make sure that we always use /
		String fixedAbsolutePath = fileAbsolutePath.replace('\\', '/');
		// Start the image metadata JSON with basic fields
		XContentBuilder imageJSON = XContentFactory.jsonBuilder()
		.startObject()
			.field("storageType", "CyVerse Datastore")
			.field("storagePath", fixedAbsolutePath)
			.field("collectionID", collectionID)
			.startObject("imageMetadata")
				.field("dateTaken", imageEntry.getDateTaken().atZone(ZoneId.systemDefault()).format(SanimalMetadataFields.INDEX_DATE_TIME_FORMAT))
				.field("yearTaken", imageEntry.getDateTaken().getYear())
				.field("monthTaken", imageEntry.getDateTaken().getMonthValue())
				.field("hourTaken", imageEntry.getDateTaken().getHour())
				.field("dayOfYearTaken", imageEntry.getDateTaken().getDayOfYear())
				.field("dayOfWeekTaken", imageEntry.	getDateTaken().getDayOfWeek().getValue())
				.startObject("location")
					.field("elevation", imageEntry.getLocationTaken().getElevation())
					.field("id", imageEntry.getLocationTaken().getId())
					.field("name", imageEntry.getLocationTaken().getName())
					.field("position", imageEntry.getLocationTaken().getLatitude() + ", " + imageEntry.getLocationTaken().getLongitude())
				.endObject()
				.startArray("speciesEntries");

		// For each species entry we write out one JSON object
		for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
		{
			imageJSON
					.startObject()
					.startObject("species")
						.field("commonName", speciesEntry.getSpecies().getCommonName())
						.field("scientificName", speciesEntry.getSpecies().getScientificName())
					.endObject()
					.field("count", speciesEntry.getCount())
					.endObject();
		}

		// Finalize the JSON object
		imageJSON
				.endArray()
			.endObject()
		.endObject();

		// We return two fields, one is the absolute path of the image file, and the other is the JSON representing the image metadata
		return Tuple.tuple(fixedAbsolutePath, imageJSON);
	}
}
