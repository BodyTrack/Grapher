package org.bodytrack.client;

import gwt.g2d.client.math.Vector2;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Plot {

   /** Sets the {@link PlotContainer} which contains this <code>Plot</code>. */
   void registerPlotContainer(PlotContainer plotContainer);

   /**
    * Unregisters the given {@link PlotContainer} from this <code>Plot</code> if and only if the given
    * {@link PlotContainer} is not <code>null</code> and is currently registered with this <code>Plot</code>. That
    * is, if this <code>Plot</code> is already associated with a {@link PlotContainer} other than the given one,
    * then nothing happens.
    */
   void unregisterPlotContainer(PlotContainer plotContainer);

   /**
    * Paints this Plot on the stored {@link PlotContainer}.
    *
    * @see #registerPlotContainer(PlotContainer)
    */
   void paint(Canvas canvas, int newPaintEventId);

   /**
    * Returns the X-Axis for this Plot.
    *
    * @return the X-axis for this Plot
    */
   GraphAxis getXAxis();

   /**
    * Returns the Y-Axis for this Plot.
    *
    * @return the Y-axis for this Plot
    */
   GraphAxis getYAxis();

   /**
    * Stops highlighting this Plot.
    *
    * <p>Note that this does not affect the highlighting status on the axes associated with this Plot.</p>
    */
   void unhighlight();

   /**
    * Tells whether or not this Plot is highlighted.
    *
    * <p>If {@link #setHighlightedPoint(PlottablePoint)} has been called since the constructor and since the last call to
    * {@link #unhighlight()}, returns <code>true</code>.  Otherwise, returns <code>false</code>.</p>
    *
    * @return <code>true</code> if and only if this Plot is highlighted
    */
   boolean isHighlighted();

   /**
    * Highlights this Plot if and only if it contains a point within some threshold defined by the implementation.
    *
    *
    * @param pos
    * 		the position at which the mouse is hovering, and from which
    * 		we want to derive our highlighting
    * @return
    * 		<code>true</code> if and only if this highlights the axes
    */
   boolean highlightIfNear(final Vector2 pos);

   /**
    * Returns the highlighted point maintained by this {@link Plot}.
    *
    * @return
    * 		the highlighted point this {@link Plot} keeps, or
    * 		<code>null</code> if there is no highlighted point
    */
   PlottablePoint getHighlightedPoint();

   void setHighlightedPoint(final PlottablePoint highlightedPoint);

   void onClick(final Vector2 pos);
}