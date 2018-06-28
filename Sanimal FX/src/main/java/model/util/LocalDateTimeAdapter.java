package model.util;

import com.google.gson.*;
import model.constant.SanimalMetadataFields;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class used to serialize and deserialize a local date time object
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime>
{
	/**
	 * Serialize is called when a local date time needs to be converted into JSON
	 *
	 * @param localDateTime The time to convert into JSON
	 * @param type ignored
	 * @param jsonSerializationContext ignored
	 * @return The date passed into the first parameter as a JSON primitive
	 */
	@Override
	public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext)
	{
		return new JsonPrimitive(localDateTime.atZone(ZoneId.systemDefault()).format(SanimalMetadataFields.INDEX_DATE_TIME_FORMAT));
	}

	/**
	 * Deserialize is called when a local date needs to be read from a JSON blob
	 *
	 * @param jsonElement The JSON element to read the date from
	 * @param type ignored
	 * @param jsonDeserializationContext ignored
	 * @return The local date time represented by the JSON
	 * @throws JsonParseException If the JSON is incorrectly formatted
	 */
	@Override
	public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
	{
		return ZonedDateTime.parse(jsonElement.getAsString(), SanimalMetadataFields.INDEX_DATE_TIME_FORMAT).toLocalDateTime();
	}
}
