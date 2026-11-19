package com.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Ubayd Razack
 * Remembers each class's methods from its last successful compile, so the methods added since then
 * can be reported.
 */
public class MethodTracker {
    
  /** Each class name mapped to the method signatures seen at its last compile. */
  private final Map<String, Set<String>> knownMethods = new HashMap<>();

  /** Creates a tracker that has not seen any classes yet. */
  public MethodTracker() {
  }

  /**
   * Compares a class's current methods with the ones seen last time, and remembers the current
   * ones for next time.
   *
   * @param className the class that was compiled
   * @param currentMethods the class's method signatures now
   * @return the methods not present last time, in alphabetical order (empty if none)
   */
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