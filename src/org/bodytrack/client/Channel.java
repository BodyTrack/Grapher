package org.bodytrack.client;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * <p>
 * <code>Channel</code> represents a device channel.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class Channel {

   private final String deviceName;
   private final String channelName;
   private final ChartType chartType;
   private final JSONObject channelSpecs;

   /**
    * Constructs a <code>Channel</code> with the given <code>deviceName</code> and <code>channelName</code>.  The
    * <code>Channel</code> will have <code>null</code> channel specs, but use the given <code>chartType</code>.  If the
    * given <code>chartType</code> is <code>null</code> or a matching {@link ChartType} cannot be found, the default
    * {@link ChartType#DEFAULT_CHART_TYPE} is used.
    */
   public Channel(final String deviceName, final String channelName, final String chartType) {
      this(deviceName, channelName, null, ChartType.findByName(chartType));
   }

   /**
    * Constructs a <code>Channel</code> with the given <code>deviceName</code> and <code>channelName</code>.  The
    * <code>Channel</code> will have the given <code>channelSpecs</code>.  This constructor will attempt to obtain the
    * {@link ChartType} from the channel specs.  If the channel specs are <code>null</code>, or a chart type is not
    * specified, or the chart type is not recognized, then the <code>Channel</code> will be created with the default
    * {@link ChartType#DEFAULT_CHART_TYPE}.
    */
   public Channel(final String deviceName, final String channelName, final JSONObject channelSpecs) {
      this(deviceName, channelName, channelSpecs, null);
   }

   /**
    * Constructs a <code>Channel</code> with the given <code>deviceName</code> and <code>channelName</code>.  The
    * <code>Channel</code> will have the given <code>channelSpecs</code> and {@link ChartType}.  Since either, both, or
    * neither of the <code>channelSpecs</code> and {@link ChartType} may be <code>null</code>, this constructor does
    * the following:
    * <table border="1" cellpadding="3" cellspacing="0">
    *    <tr>
    *       <th><code>channelSpecs</code></th>
    *       <th><code>ChartType</code></th>
    *       <th>Constructor Behavior</th>
    *    </tr>
    *    <tr>
    *       <td><code>null</code></td>
    *       <td><code>null</code></td>
    *       <td>Channel specs will be <code>null</code>, chart type is {@link ChartType#DEFAULT_CHART_TYPE}</td>
    *    </tr>
    *    <tr>
    *       <td><code>null</code></td>
    *       <td>non-<code>null</code></td>
    *       <td>Channel specs will be <code>null</code>, chart type is as given</td>
    *    </tr>
    *    <tr>
    *       <td>non-<code>null</code></td>
    *       <td><code>null</code></td>
    *       <td>
    *          Channel specs will be non-<code>null</code>, constructor tries to obtain chart type from channel specs.
    *          If chart type cannot be determined from channel specs, it defaults to {@link ChartType#DEFAULT_CHART_TYPE}
    *       </td>
    *    </tr>
    *    <tr>
    *       <td>non-<code>null</code></td>
    *       <td>non-<code>null</code></td>
    *       <td>
    *          Channel specs will be non-<code>null</code>, constructor uses the specified {@link ChartType} (regardless
    *          of what chart type value might be specified in the channel specs).
    *       </td>
    *    </tr>
    * </table>
    */
   public Channel(final String deviceName, final String channelName, final JSONObject channelSpecs, final ChartType chartType) {
      this.deviceName = deviceName;
      this.channelName = channelName;

      if (deviceName == null || channelName == null) {
         throw new IllegalArgumentException("The deviceName and channelName must both be non-null");
      }

      // If both the chartType and channelSpecs are BOTH null, then just default to using the default ChartType and a
      // null channelSpecs.
      if (chartType == null && channelSpecs == null) {
         this.chartType = ChartType.DEFAULT_CHART_TYPE;
         this.channelSpecs = null;
      } else {
         // if they're not BOTH null, then check whether only one is, or both are not (I realize these cases could be
         // compbined and simplified, but I chose to explicitly define them here for clarity).
         if (chartType == null) {
            // if the chartType is null, then channelSpecs can't be, so try to get it from the channelSpecs
            this.channelSpecs = channelSpecs;
            this.chartType = ChartType.findByName(getStringValue("type"));
         } else if (channelSpecs == null) {
            // if the channelSpecs is null, then chartType can't be (because it would have been handled by an above
            // case), so just use the given chartType
            this.channelSpecs = null;
            this.chartType = chartType;
         } else {
            // gives precedence to the specified chart type, regardless of what might be specified in the channel specs
            this.channelSpecs = channelSpecs;
            this.chartType = chartType;
         }
      }
   }

   /** Returns the device name.  Guaranteed to be non-<code>null</code>. */
   public String getDeviceName() {
      return deviceName;
   }

   /** Returns the channel name.  Guaranteed to be non-<code>null</code>. */
   public String getChannelName() {
      return channelName;
   }

   /** Returns the {@link ChartType}.  Guaranteed to be non-<code>null</code>. */
   public ChartType getChartType() {
      return chartType;
   }

   public boolean hasChannelSpecs() {
      return channelSpecs != null;
   }

   public JSONObject getChannelSpecs() {
      return channelSpecs;
   }

   public String getStringValue(final String key) {
      if (key != null && channelSpecs != null && channelSpecs.containsKey(key)) {
         final JSONValue jsonValue = channelSpecs.get(key);
         if (jsonValue != null) {
            final JSONString val = jsonValue.isString();
            if (val != null) {
               return val.stringValue();
            }
         }
      }
      return null;
   }

   public Double getNumberValue(final String key) {
      if (key != null && channelSpecs != null && channelSpecs.containsKey(key)) {
         final JSONValue jsonValue = channelSpecs.get(key);
         if (jsonValue != null) {
            final JSONNumber val = jsonValue.isNumber();
            if (val != null) {
               return val.doubleValue();
            }
         }
      }
      return null;
   }

   public Boolean getBooleanValue(final String key) {
      if (key != null && channelSpecs != null && channelSpecs.containsKey(key)) {
         final JSONValue jsonValue = channelSpecs.get(key);
         if (jsonValue != null) {
            final JSONBoolean val = jsonValue.isBoolean();
            if (val != null) {
               return val.booleanValue();
            }
         }
      }
      return null;
   }
}
