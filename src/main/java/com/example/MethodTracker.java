package com.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class MethodTracker {
    
  private final Map<String, Set<String>> knownMethods = new HashMap<>();

  public List<String> findNewMethods(String className, Collection<String> currentMethods) {
    Set<String> previous = knownMethods.get(className);
    if (previous == null) {
      previous = new HashSet<>();
    }

    List<String> added = new ArrayList<>();
    for (String method : new TreeSet<>(currentMethods)) { 
      if (!previous.contains(method)) {
        added.add(method);
      }
    }
    knownMethods.put(className, new HashSet<>(currentMethods));
    return added;
  }
}