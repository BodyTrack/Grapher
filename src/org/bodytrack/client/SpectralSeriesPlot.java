package org.bodytrack.client;

import java.util.List;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.JavaScriptObject;

public class SpectralSeriesPlot extends BaseSeriesPlot {

    private static final int MAX_COLOR_VALUE = 255;

    private final SpectralPlotRenderer renderer = new SpectralPlotRenderer();

    public SpectralSeriesPlot(final JavaScriptObject datasource,
            final JavaScriptObject nativeXAxis,
            final JavaScriptObject nativeYAxis,
            final boolean localDisplay) {
        super(datasource, nativeXAxis, nativeYAxis, localDisplay);
    }

    @Override
    protected SeriesPlotRenderer getRenderer() {
        return renderer;
    }

    @Override
    public boolean highlightIfNear(final Vector2 pos) {
        return false;
    }

    private final static class SpectralPlotRenderer implements SeriesPlotRenderer {

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

            final List<Integer> values = tile.getDFT();

            // Don't even consider drawing the points that won't show up
            final int startIdx = (int)Math.floor(Math.max(0, yAxis.getMin()));
            final int endIdx = (int)Math.ceil(Math.min(values.size() - 1, yAxis.getMax()));

            // Actually draw the points
            drawing.beginClippedPath();
            for (int i = startIdx; i <= endIdx; i++) {
                final int v = values.get(i);
                final double y = yAxis.project2D(v).getY();
                final CssColor color = getColor(v, numSteps - 1);

                drawing.setStrokeStyle(color);
                drawing.drawLineSegment(minX, y, maxX, y);
            }
            drawing.strokeClippedPath();
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
}
