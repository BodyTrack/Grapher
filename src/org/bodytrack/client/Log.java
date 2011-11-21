package org.bodytrack.client;

/**
 * <p>
 * <code>Log</code> is a stupidly simple class to enable logging to the browser's JavaScript console.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class Log {

   public static native boolean debug(final String s) /*-{
      console.log(s);
   }-*/;

   private Log() {
      // private to prevent instantiation
   }
}
