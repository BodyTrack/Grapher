package org.bodytrack.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public enum ChartType {

   DOT("dot"),
   LOLLIPOP("lollipop"),
   PLOT("plot"),
   PHOTO("photo"),
   ZEO("zeo");

   private static final Map<String, ChartType> NAME_TO_TYPE_MAP;

   public static final ChartType DEFAULT_CHART_TYPE = PLOT;

   static {
      final Map<String, ChartType> nameToTypeMap = new HashMap<String, ChartType>(ChartType.values().length);
      for (final ChartType chartType : ChartType.values()) {
         nameToTypeMap.put(chartType.getName(), chartType);
      }
      NAME_TO_TYPE_MAP = Collections.unmodifiableMap(nameToTypeMap);
   }

   /**
    * Returns the <code>ChartType</code> associated with the given <code>name</code> (case insensitive), or
    * {@link #DEFAULT_CHART_TYPE} if no such type exists. Guaranteed to never return <code>null</code>.
    */
   public static ChartType findByName(final String name) {
      if (name != null) {
         final String lowercaseName = name.toLowerCase();
         if (NAME_TO_TYPE_MAP.containsKey(lowercaseName)) {
            return NAME_TO_TYPE_MAP.get(lowercaseName);
         }
      }
      return DEFAULT_CHART_TYPE;
   }

   private final String name;

   private ChartType(final String name) {
      this.name = name.toLowerCase();  // force all names to be lowercase so that we can do a case-insensitive search in findByName()
   }

   public String getName() {
      return name;
   }

   @Override
   public String toString() {
      return getName();
   }
}
