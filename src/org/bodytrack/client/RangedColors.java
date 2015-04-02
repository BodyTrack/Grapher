package org.bodytrack.client;

import com.google.gwt.canvas.dom.client.CssColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class RangedColors {
   private final Double[] rangeBounds;
   private final CssColor[] colors;

   RangedColors(final String colorRangesStr) {
      final List<Double> rangeBoundsList = new ArrayList<Double>();
      final List<String> colorsList = new ArrayList<String>();

      // parse the color ranges string into colors and range bounds
      if (colorRangesStr != null) {
         final String[] parts = colorRangesStr.split(";");
         Double previousValue = Double.NEGATIVE_INFINITY;
         for (int i = 0; i < parts.length; i++) {
            // parse even numbered elements as colors, and odd ones as range bounds
            final String part = parts[i].trim();
            if (i % 2 == 0) {
               colorsList.add(part);
            } else {
               final Double value = parseDouble(part);
               // abort if the value can't be parsed as a double or the series is not strictly increasing
               if (value == null || Double.compare(previousValue, value) >= 0) {
                  Log.debug("org.bodytrack.client.StripRenderingStrategy: Aborting color range parsing because " +
                            "value [" + value + "] either cannot be parsed as a double, or is not strictly greater " +
                            "than the previous value [" + previousValue + "]");
                  break;
               }
               rangeBoundsList.add(value);
               previousValue = value;
            }
         }
      }

      rangeBounds = rangeBoundsList.toArray(new Double[rangeBoundsList.size()]);
      colors = new CssColor[colorsList.size()];

      // Populate the colors array.  Yes, I know I should be able to just do colorsList.toArray(new CssColor[colorsList.size()]);
      // above to create and populate the array.  That's exactly what we used to do, but it stopped working when we
      // upgraded GWT.  Perhaps related to this issue: https://code.google.com/p/google-web-toolkit/issues/detail?id=6263
      // Regardless, the following works...
      for (int i = 0; i < colors.length; i++) {
         colors[i] = parseColor(colorsList.get(i));
      }

      Log.debug("Range Bounds");
      for (int i = 0; i < rangeBounds.length; i++) {
         Log.debug("   [" + i + "] = [" + rangeBounds[i] + "]");
      }
      Log.debug("Colors");
      for (int i = 0; i < colors.length; i++) {
         Log.debug("   [" + i + "] = [" + colors[i] + "]");
      }
   }

   CssColor getColorForValue(final double value) {
      int index = Arrays.binarySearch(rangeBounds, value);
      if (index < 0) {
         index = -1 * index - 1;
      }

      if (index >= 0 && index < colors.length) {
         return colors[index];
      }

      return null;
   }

   private Double parseDouble(final String doubleStr) {
      try {
         return Double.parseDouble(doubleStr);
      }
      catch (NumberFormatException e) {
         Log.debug("org.bodytrack.client.StripRenderingStrategy.parseDouble(): Invalid double value [" + doubleStr + "]");
      }

      return null;
   }

   private CssColor parseColor(final String colorString) {
      if (colorString != null && colorString.length() > 0 && !"null".equalsIgnoreCase(colorString)) {
         try {
            return CssColor.make(colorString);
         }
         catch (final Exception ignored) {
            Log.debug("org.bodytrack.client.StripRenderingStrategy.parseColor(): Invalid color [" + colorString + "]");
         }
      }
      return null;
   }
}
