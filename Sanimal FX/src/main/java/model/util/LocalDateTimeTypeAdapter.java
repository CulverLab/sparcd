package model.util;

import com.google.gson.*;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;
import java.time.*;

public class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d:M:uuuu H:m:s");

  @Override
  public JsonElement serialize(LocalDateTime zonedDateTime, Type srcType,
      JsonSerializationContext context) {

    return new JsonPrimitive(formatter.format(zonedDateTime));
  }

  @Override
  public LocalDateTime deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    if (json == null || !json.isJsonObject()) {
      return null;
    }

    JsonObject jobj = (JsonObject)json;
    JsonObject date = jobj.getAsJsonObject("date");
    JsonObject time = jobj.getAsJsonObject("time");

    String dateStr = String.format("%s:%s:%s %s:%s:%s", date.get("day"), date.get("month"), date.get("year"),
                                  time.get("hour"), time.get("minute"), time.get("second"));

    return LocalDateTime.parse(dateStr/*json.getAsString()*/, formatter);
  }
}
