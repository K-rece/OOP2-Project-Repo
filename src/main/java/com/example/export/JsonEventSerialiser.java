package com.example.export;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import com.example.TelemetryEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonEventSerialiser implements EventSerialiser {

  static final String SCHEMA_VERSION = "1.0";
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  @Override
  public String serialize(List<TelemetryEvent> events) {

    List<TelemetryEvent> ordered = new ArrayList<>(events);
    ordered.sort(Comparator.comparingLong(TelemetryEvent::getTimestamp));

    JsonArray eventArray = new JsonArray();
    int sequence = 1;
  
    for (TelemetryEvent event : ordered) {
      eventArray.add(toJson(event, sequence++));
    }


    //converts events into text
    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", SCHEMA_VERSION);
    root.addProperty("eventCount", ordered.size());
    root.add("events", eventArray);
    return gson.toJson(root);
  }

  //sends the converted text into a json file
  private JsonObject toJson(TelemetryEvent event, int sequence) {

    JsonObject obj = new JsonObject();
    obj.addProperty("sequence", sequence);
    obj.addProperty("type", event.getEventType().name());
    obj.addProperty("timestamp", Instant.ofEpochMilli(event.getTimestamp()).toString());
    obj.add("data", gson.toJsonTree(new TreeMap<>(event.getData())));
    return obj;
  }
}