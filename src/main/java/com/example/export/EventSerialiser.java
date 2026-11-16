package com.example.export;

import java.util.List;
import com.example.TelemetryEvent;

public interface EventSerialiser {
  String serialise(SessionInfo session, List<TelemetryEvent> events);
}

