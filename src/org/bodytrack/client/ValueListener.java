package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class ValueListener extends JavaScriptObject {
   protected ValueListener() {
   }

   public final native void handleValueUpdate(final String newValue) /*-{
      this(newValue);
   }-*/;
}
