package org.bodytrack.client;

import java.util.HashSet;
import java.util.Set;

public abstract class BasePlotContainer implements PlotContainer, Resizable {
	
	protected final Set<Plot> containedPlots = new HashSet<Plot>();
	
	public void paint() {
		paint(SequenceNumber.getNext());
	}
	
	/**
    * Adds the given {@link Plot} to the collection of plots to be drawn.
    *
    * Note that a plot can only be added once to this PlotContainer's internal collection.
    *
    * @throws NullPointerException if plot is <code>null</code>
    */
   public void addPlot(final Plot plot) {
      if (plot == null) {
         throw new NullPointerException("Cannot add null plot");
      }
      containedPlots.add(plot);
      plot.registerPlotContainer(this);

      paint(SequenceNumber.getNext());
   }

   /**
    * Removes the given {@link Plot} from the collection of plots to be drawn.
    *
    * <p>Does nothing if plot is <code>null</code> or not contained by this {@link SeriesPlotContainer}<p>
    */
   public void removePlot(final Plot plot) {
      if (plot == null) {
         return;
      }
      containedPlots.remove(plot);
      plot.unregisterPlotContainer(this);

      paint(SequenceNumber.getNext());
   }
}
