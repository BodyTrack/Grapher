package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class DataPointListener extends JavaScriptObject {
   protected DataPointListener() {
   }

   public final void handleDataPointUpdate(final PlottablePoint point) {
      if (point == null) {
         handleNoDataPointUpdate();
      } else {
         handleDataPointUpdate(point.getDate(),
                               point.getValue(),
                               point.getDateAsString(),
                               point.getValueAsString(),
                               point.getComment());
      }
   }

   private native void handleDataPointUpdate(final double date,
                                             final double value,
                                             final String dateStr,
                                             final String valueStr,
                                             final String comment) /*-{
      this({
              "date":date,
              "value":value,
              "dateString":dateStr,
              "valueString":valueStr,
              "comment":comment
           });
   }-*/;

   private native void handleNoDataPointUpdate() /*-{
      this(null);
   }-*/;
}
