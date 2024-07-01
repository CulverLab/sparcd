package model.util;

import com.google.gson.*;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;
import java.time.*;

public class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d:M:uuuu H:m:s:n");

  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type srcType,
      JsonSerializationContext context) {

    JsonObject jret = new JsonObject();

    JsonObject jcur = new JsonObject();
    jcur.addProperty("year", localDateTime.getYear());
    jcur.addProperty("month", localDateTime.getMonthValue());
    jcur.addProperty("day", localDateTime.getDayOfMonth());
    jret.add("date", jcur);

    jcur = new JsonObject();
    jcur.addProperty("hour", localDateTime.getHour());
    jcur.addProperty("minute", localDateTime.getMinute());
    jcur.addProperty("second", localDateTime.getSecond());
    jcur.addProperty("nano", localDateTime.getNano());
    jret.add("time", jcur);

    return jret;
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

    String dateStr = String.format("%s:%s:%s %s:%s:%s:%s", date.get("day"), date.get("month"), date.get("year"),
                                  time.get("hour"), time.get("minute"), time.get("second"), time.get("nano"));

    return LocalDateTime.parse(dateStr, formatter);
  }
}
