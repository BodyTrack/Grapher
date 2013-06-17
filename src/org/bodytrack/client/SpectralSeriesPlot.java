package org.bodytrack.client;

import java.util.List;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.JavaScriptObject;

public class SpectralSeriesPlot extends BaseSeriesPlot {

    private static final int FETCH_LEVEL = -2; // Tiles of about 1 minute each
    private static final int MAX_COLOR_VALUE = 255;

    private final SpectralPlotRenderer renderer = new SpectralPlotRenderer();

    public SpectralSeriesPlot(final JavaScriptObject datasource,
            final JavaScriptObject nativeXAxis,
            final JavaScriptObject nativeYAxis,
            final boolean localDisplay) {
        super(datasource, nativeXAxis, nativeYAxis, localDisplay);
    }

    @Override
    protected TileLoader buildTileLoader(final JavaScriptObject datasource,
                                         final GraphAxis xAxis,
                                         final boolean localDisplay) {
        final TileLoader wrappedLoader = super.buildTileLoader(datasource, xAxis, localDisplay);
        return new SpectralTileLoader(wrappedLoader, xAxis, FETCH_LEVEL);
    }

    @Override
    protected SeriesPlotRenderer getRenderer() {
        return renderer;
    }

    @Override
    public boolean highlightIfNear(final Vector2 pos) {
        return false;
    }

    private static final class SpectralPlotRenderer implements SeriesPlotRenderer {

        @Override
        public void render(final GrapherCanvas canvas,
                final BoundedDrawingBox drawing,
                final Iterable<GrapherTile> tiles,
                final GraphAxis xAxis,
                final GraphAxis yAxis,
                final PlottablePoint highlightedPoint) {
            // We ignore any potentially highlighted point
            for (final GrapherTile tile: tiles)
                render(drawing, tile.getSpectralTile(), xAxis, yAxis);
        }

        // Renders a single tile of data
        public void render(final BoundedDrawingBox drawing,
                final SpectralTile tile,
                final GraphAxis xAxis,
                final GraphAxis yAxis) {
            final TileDescription desc = tile.getDescription();
            final int numSteps = tile.getNumSteps();

            final double minX = xAxis.project2D(desc.getMinTime()).getX();
            final double maxX = xAxis.project2D(desc.getMaxTime()).getX();

            final List<List<Integer>> allValues = tile.getDFT();
            final double width = (maxX - minX) / allValues.size();

            for (int windowID = 0; windowID < allValues.size(); windowID++) {
                final List<Integer> window = allValues.get(windowID);
                final double startX = minX + width * windowID;

                // Don't even consider drawing the points that won't show up
                final int startIdx = (int)Math.floor(Math.max(0, yAxis.getMin()));
                final int endIdx = (int)Math.ceil(Math.min(window.size() - 1, yAxis.getMax()));

                // Actually draw the points
                final GrapherCanvas canvas = drawing.getCanvas();
                for (int i = startIdx; i <= endIdx; i++) {
                    final double topY = yAxis.project2D(i + 1).getY();
                    final double botY = yAxis.project2D(i).getY();
                    final CssColor color = getColor(window.get(i), numSteps - 1);

                    drawing.setFillStyle(color);
                    // Add an extra 0.5 pixels around the edge so there are no
                    // gaps between neighboring rectangles
                    canvas.fillRectangle(startX - 0.5, topY - 0.5, width + 1, botY - topY + 1);
                }
            }
        }

        // TODO: Start allowing the style to set the colormap
        private CssColor getColor(final int value, final int maxValue) {
            // Zero is blue, then we range up to red for max value
            assert (value >= 0);
            assert (value <= maxValue);

            final double proportion = ((double)value) / maxValue;
            return CssColor.make((int)Math.round(proportion * MAX_COLOR_VALUE),
                    0, // No green is ever used
                    (int)Math.round((1 - proportion) * MAX_COLOR_VALUE));
        }

        @Override
        public void setStyleDescription(final StyleDescription styleDescription) {
            // TODO: Stop ignoring styles
        }
    }


    private static final class SpectralTileLoader implements TileLoader {

        private static final int MAX_WIDTH_RATIO = 256;
        private final TileLoader loader;
        private final GraphAxis xAxis;
        private final int level;

        public SpectralTileLoader(final TileLoader loader,
                                  final GraphAxis xAxis,
                                  final int level) {
            this.loader = loader;
            this.xAxis = xAxis;
            this.level = level;
        }

        @Override
        public void addEventListener(final EventListener listener) {
            loader.addEventListener(listener);
        }

        @Override
        public void removeEventListener(final EventListener listener) {
            loader.removeEventListener(listener);
        }

        /**
         * Checks for fetch at the correct level.
         *
         * <p>
         * This is the only method that this class does not forward to the wrapped loader.
         * Instead of fetching the right-size tile for the X-axis, this fetches at the
         * required level.
         * </p>
         */
        @Override
        public boolean checkForFetch() {
            final double tileWidth = Math.pow(2, level) * GrapherTile.TILE_WIDTH;

            final double minTime = xAxis.getMin();
            final double maxTime = xAxis.getMax() + tileWidth;

            if (maxTime - minTime >= MAX_WIDTH_RATIO * tileWidth)
                return loader.checkForFetch();

            // Fetch at tile boundaries, taking advantage of the fact that StandardTileLoader
            // fetches the biggest tile whose width is no bigger than the specified window
            boolean anyFetched = false;
            for (double time = minTime; time <= maxTime; time += tileWidth)
                anyFetched |= loader.checkForFetch(time, MathEx.nextDouble(time + tileWidth), null);

            return anyFetched;
        }

        @Override
        public boolean checkForFetch(final double minTime, final double maxTime, final EventListener onload) {
            return loader.checkForFetch(minTime, maxTime, onload);
        }

        @Override
        public List<GrapherTile> getBestResolutionTiles() {
            return loader.getBestResolutionTiles();
        }

        @Override
        public List<GrapherTile> getBestResolutionTiles(final double minTime,
                                                        final double maxTime,
                                                        final int currentLevel) {
            return loader.getBestResolutionTiles(minTime, maxTime, currentLevel);
        }

        @Override
        public List<GrapherTile> getBestResolutionTiles(final double minTime, final double maxTime) {
            return loader.getBestResolutionTiles(minTime, maxTime);
        }

        @Override
        public GrapherTile getBestResolutionTileAt(final double time) {
            return loader.getBestResolutionTileAt(time);
        }

        @Override
        public GrapherTile getBestResolutionTileAt(final double time, final int bestLevel) {
            return loader.getBestResolutionTileAt(time, bestLevel);
        }
    }

}
