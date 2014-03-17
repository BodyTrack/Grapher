package org.bodytrack.client;

import com.google.gwt.core.client.JsArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * <code>DataSeriesPlotRenderer</code> is the default {@link SeriesPlotRenderer} for {@link DataSeriesPlot}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DataSeriesPlotRenderer extends BaseSeriesPlotRenderer {

   public DataSeriesPlotRenderer(final StyleDescription styleDescription) {
      super(styleDescription);
   }

   @Override
   protected List<SeriesPlotRenderingStrategy> buildSeriesPlotRenderingStrategies(final JsArray<StyleDescription.StyleType> styleTypes,
                                                                                  final Double highlightLineWidth) {
      final List<SeriesPlotRenderingStrategy> renderingStrategies = new ArrayList<SeriesPlotRenderingStrategy>();

      if (styleTypes != null) {

         // loop over all the style types
         for (int i = 0; i < styleTypes.length(); i++) {
            final StyleDescription.StyleType styleType = styleTypes.get(i);

            // don't bother creating a rendering strategy if the style type shouldn't be shown
            if (styleType != null && styleType.willShow()) {
               final SeriesPlotRenderingStrategy renderingStrategy = createPlotRenderingStrategy(styleType, highlightLineWidth);
               if (renderingStrategy != null) {
                  renderingStrategies.add(renderingStrategy);
               }
            }
         }
      }

      return renderingStrategies;
   }

   @Override
   protected List<DataPointRenderingStrategy> buildPointRenderingStrategies(final JsArray<StyleDescription.StyleType> styleTypes,
                                                                        final Double highlightLineWidth) {
      final List<DataPointRenderingStrategy> renderingStrategies = new ArrayList<DataPointRenderingStrategy>();

      if (styleTypes != null) {

         // loop over all the style types
         for (int i = 0; i < styleTypes.length(); i++) {
            final StyleDescription.StyleType styleType = styleTypes.get(i);

            // don't bother creating a rendering strategy if the style type shouldn't be shown
            if (styleType != null && styleType.willShow()) {
               final DataPointRenderingStrategy renderingStrategy = createPointRenderingStrategy(styleType, highlightLineWidth);
               if (renderingStrategy != null) {
                  renderingStrategies.add(renderingStrategy);
               }
            }
         }
      }

      return renderingStrategies;
   }

   private SeriesPlotRenderingStrategy createPlotRenderingStrategy(final StyleDescription.StyleType styleType,
                                                                   final Double highlightLineWidth) {

      if (styleType != null) {
         final Type type = Type.findByName(styleType.getType());
         return type.createRenderingStrategy(styleType, highlightLineWidth);
      }

      return null;
   }

   private DataPointRenderingStrategy createPointRenderingStrategy(final StyleDescription.StyleType styleType,
                                                               final Double highlightLineWidth) {

      final SeriesPlotRenderingStrategy renderingStrategy = createPlotRenderingStrategy(styleType, highlightLineWidth);

      if (renderingStrategy != null && renderingStrategy instanceof DataPointRenderingStrategy) {
         return (DataPointRenderingStrategy)renderingStrategy;
      }

      return null;
   }

   /** Enumeration defining the various types of data series plot style types. */
   private static enum Type {

      POINT("point", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new CircleRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      CIRCLE("circle", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new CircleRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      LOLLIPOP("lollipop", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new LollipopRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      CROSS("cross", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new CrossRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      PLUS("plus", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new PlusRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      SQUARE("square", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new SquareRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      LINE("line", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new LineRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      VALUE("value", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new ValueRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      ZEO("zeo", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new ZeoRenderingStrategy(styleType, highlightLineWidth);
         }
      }),
      STRIP("strip", new SeriesPlotRenderingStrategyFactory() {
         @Override
         public SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                   final Double highlightLineWidth) {
            return new StripRenderingStrategy(styleType, highlightLineWidth);
         }
      });

      private interface SeriesPlotRenderingStrategyFactory extends Serializable {
         SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                            final Double highlightLineWidth);
      }

      private static final Map<String, Type> NAME_TO_TYPE_MAP;

      public static final Type DEFAULT_DATA_SERIES_PLOT_TYPE = LINE;

      static {
         final Map<String, Type> nameToTypeMap = new HashMap<String, Type>(Type.values().length);
         for (final Type dataSeriesPlotType : Type.values()) {
            nameToTypeMap.put(dataSeriesPlotType.getName(), dataSeriesPlotType);
         }
         NAME_TO_TYPE_MAP = Collections.unmodifiableMap(nameToTypeMap);
      }

      /**
       * Returns the <code>Type</code> associated with the given <code>name</code> (case insensitive), or
       * {@link #DEFAULT_DATA_SERIES_PLOT_TYPE} if no such type exists. Guaranteed to never return <code>null</code>.
       */
      public static Type findByName(final String name) {
         if (name != null) {
            final String lowercaseName = name.toLowerCase();
            if (NAME_TO_TYPE_MAP.containsKey(lowercaseName)) {
               return NAME_TO_TYPE_MAP.get(lowercaseName);
            }
         }
         return DEFAULT_DATA_SERIES_PLOT_TYPE;
      }

      private final String name;
      private final SeriesPlotRenderingStrategyFactory seriesPlotRenderingStrategyFactory;

      private Type(final String name, final SeriesPlotRenderingStrategyFactory seriesPlotRenderingStrategyFactory) {
         this.name = name.toLowerCase();  // force all names to be lowercase so that we can do a case-insensitive search in findByName()
         this.seriesPlotRenderingStrategyFactory = seriesPlotRenderingStrategyFactory;
      }

      public String getName() {
         return name;
      }

      public SeriesPlotRenderingStrategy createRenderingStrategy(final StyleDescription.StyleType styleType,
                                                                 final Double highlightLineWidth) {
         return seriesPlotRenderingStrategyFactory.create(styleType, highlightLineWidth);
      }

      @Override
      public String toString() {
         return getName();
      }
   }
}
