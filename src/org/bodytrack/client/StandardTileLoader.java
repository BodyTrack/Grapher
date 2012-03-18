package org.bodytrack.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardTileLoader implements TileLoader {
	/**
	 * We never re-request a URL with MAX_REQUESTS_PER_URL or more failures
	 * in a row.
	 */
	private static final int MAX_REQUESTS_PER_URL = 5;

	private static final double EPSILON = 1e-10;

	// Values related to getting new values from the server (the
	// data will be pulled in with the checkForFetch call)
	private final Map<TileDescription, GrapherTile> descriptions;
	private final Set<TileDescription> pendingDescriptions;
	private final Map<String, Integer> pendingUrls;
	private final List<GrapherTile> pendingData;

	// Determining whether or not we should retrieve more data from the server
	private final JavaScriptObject datasource;
	private final GraphAxis timeAxis;
	private int currentLevel;
	private long currentMinOffset;
	private long currentMaxOffset;

	private final Set<EventListener> eventListeners;
	private final Alertable<GrapherTile> loadTileAlertable;

	public StandardTileLoader(final JavaScriptObject datasource,
			final GraphAxis timeAxis) {
		this.datasource = datasource;
		this.timeAxis = timeAxis;
		currentLevel = Integer.MIN_VALUE;
		currentMinOffset = Long.MAX_VALUE;
		currentMaxOffset = Long.MIN_VALUE;

		descriptions = new HashMap<TileDescription, GrapherTile>();
		pendingDescriptions = new HashSet<TileDescription>();
		pendingUrls = new HashMap<String, Integer>();
		pendingData = new ArrayList<GrapherTile>();

		eventListeners = new HashSet<EventListener>();
		loadTileAlertable = new LoadTileAlertable();
	}

	@Override
	public final void addEventListener(final EventListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}

		eventListeners.add(listener);
	}

	@Override
	public final void removeEventListener(final EventListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}

		eventListeners.remove(listener);
	}

	/**
	 * Checks for and performs a fetch for data from the server if
	 * necessary.
	 */
	@Override
	public final void checkForFetch(final int correctLevel) {
		final long correctMinOffset = computeMinOffset(correctLevel);
		final long correctMaxOffset = computeMaxOffset(correctLevel);

		if (correctLevel != currentLevel) {
			for (long i = correctMinOffset; i <= correctMaxOffset; i++) {
				fetchFromServer(correctLevel, i);
			}
		} else if (correctMinOffset < currentMinOffset) {
			fetchFromServer(correctLevel, correctMinOffset);
		} else if (correctMaxOffset > currentMaxOffset) {
			fetchFromServer(correctLevel, correctMaxOffset);
		}

		// This way we don't fetch the same data multiple times
		currentLevel = correctLevel;
		currentMinOffset = correctMinOffset;
		currentMaxOffset = correctMaxOffset;
	}

	/**
	 * Returns the offset at which the left edge of the X-axis is operating.
	 *
	 * <p>
	 * Returns the offset of the tile in which the minimum value of the X-axis
	 * is found.
	 * </p>
	 *
	 * @param level
	 * 	The level at which we assume we are operating when calculating offsets
	 * @return
	 * 	The current offset of the timeAxis, based on level and the private
	 * 	variable timeAxis
	 */
	private long computeMinOffset(final int level) {
		final double tileWidth = getTileWidth(level);
		return (long)(timeAxis.getMin() / tileWidth);
	}

	/**
	 * Returns the offset at which the right edge of the X-axis is operating.
	 *
	 * <p>
	 * Returns the offset of the tile in which the maximum value of the X-axis
	 * is found.
	 * </p>
	 *
	 * @param level
	 * 	The level at which we assume we are operating when calculating offsets
	 * @return
	 * 	The current offset of the timeAxis, based on level
	 * 	and the private variable timeAxis
	 */
	private long computeMaxOffset(final int level) {
		final double tileWidth = getTileWidth(level);
		return (long)(timeAxis.getMax() / tileWidth);
	}

	/**
	 * Fetches the specified tile from the server.
	 *
	 * <p>
	 * Note that this checks the pendingDescriptions instance variable to
	 * determine if this tile has already been requested.  If so, does not
	 * request anything from the server.
	 * </p>
	 *
	 * @param level
	 * 	The level of the tile to fetch
	 * @param offset
	 * 	The offset of the tile to fetch
	 */
	private void fetchFromServer(final int level, final long offset) {
		final TileDescription desc = new TileDescription(level, offset);

		// Ensures we don't fetch the same tile twice unnecessarily
		if (pendingDescriptions.contains(desc) || descriptions.containsKey(desc)) {
			return;
		}

		final String tileKey = desc.getTileKey();

		// Make sure we don't fetch this again unnecessarily
		pendingDescriptions.add(desc);
		pendingUrls.put(tileKey, 0);

		loadTile(level, offset);
	}

	/**
	 * Checks to see if we have received data from the server
	 */
	private void checkForNewData() {
		if (pendingData.size() > 0) {
			// Pull all the data out of pendingData
			for (final GrapherTile tile : pendingData) {
				if (tile == null) {
					continue;
				}

				descriptions.put(tile.getDescription(), tile);

				// Make sure we don't still mark this as pending
				pendingDescriptions.remove(tile.getDescription());
			}

			pendingData.clear();
		}
	}

	/**
	 * Returns a sorted list of all best resolution tiles available.
	 *
	 * @return
	 * 	A sorted list of all the best resolution tiles in currentData
	 */
	@Override
	public final List<GrapherTile> getBestResolutionTiles(final int currentLevel) {
		final List<GrapherTile> best = new ArrayList<GrapherTile>();

		// When minTime and maxTime are used in calculations, they are
		// used to make the calculations scale-independent
		final double minTime = timeAxis.getMin();
		final double maxTime = timeAxis.getMax();

		// Making sure that timespan is at least EPSILON ensures that
		// the (timespan * 1e-3) calculation below doesn't become zero
		// and leave this method stuck in an infinite loop
		final double timespan = Math.max(maxTime - minTime, EPSILON);

		double maxCoveredTime = minTime;

		while (maxCoveredTime <= maxTime) {
			final GrapherTile bestAtCurrTime =
				getBestResolutionTileAt(maxCoveredTime + timespan * 1e-3, currentLevel);
			// We need to move a little to the right of the current time
			// so we don't get the same tile twice

			if (bestAtCurrTime == null) {
				maxCoveredTime += timespan * 1e-2;
			} else {
				best.add(bestAtCurrTime);

				maxCoveredTime = bestAtCurrTime.getDescription().getMaxTime();
			}
		}

		return best;
	}

	/**
	 * Returns the best-resolution tile that covers the specified
	 * point.
	 *
	 * @param time
	 * 	The time which must be covered by the tile
	 * @param bestLevel
	 * 	The level to which we want the returned tile to be close
	 * @return
	 * 	The best-resolution (with level closest to bestLevel) tile
	 * 	which has min value less than or equal to time, and max value
	 * 	greater than or	equal to time, or <tt>null</tt> if no such tile
	 * 	exists
	 */
	@Override
	public final GrapherTile getBestResolutionTileAt(final double time,
			final int bestLevel) {
		GrapherTile best = null;
		TileDescription bestDesc = null;

		for (final GrapherTile tile : descriptions.values()) {
			final TileDescription desc = tile.getDescription();

			if (desc.getMinTime() > time || desc.getMaxTime() < time) {
				continue;
			}

			if (best == null) {
				best = tile;
				bestDesc = desc;
			} else if (Math.abs(desc.getLevel() - bestLevel) <
					Math.abs(bestDesc.getLevel() - bestLevel)) {
				best = tile;
				bestDesc = desc;
			} else if (Math.abs(desc.getLevel() - bestLevel) ==
				Math.abs(bestDesc.getLevel() - bestLevel)) {
				if (desc.getLevel() < bestDesc.getLevel()) {
					best = tile;
					bestDesc = desc;
				}
			}
		}

		return best;
	}

	/**
	 * Returns the width of a single tile.
	 *
	 * @param level
	 * 	The level of the tile for which we will find the width
	 * @return
	 * 	The width of a tile at the given level
	 */
	private static double getTileWidth(final int level) {
		return (new TileDescription(level, 0)).getTileWidth();
	}

	/**
	 * Retrieves a tile from the specified <code>level</code> and <code>offset</code>.
	 *
	 * @param level
	 * 	The level of the tile we are retrieving
	 * @param offset
	 * 	The offset of the tile we are retrieving
	 */
	private void loadTile(final int level, final long offset) {
		loadTileNative(datasource, level, (double)offset,
				Long.toString(offset), loadTileAlertable);
	}

	/**
	 * Retrieves a tile from the specified <code>level</code> and <code>offset</code>.
	 *
	 * <p>
	 * Sends a tile retrieved from url to the {@link Alertable#onSuccess(Object)}
	 * or {@link Alertable#onFailure(Object)} callback whenever that tile
	 * arrives.
	 * </p>
	 *
	 * @param theDatasource
	 * 	The theDatasource from which to retrieve the data
	 * @param level
	 * 	The level of the tile we are retrieving
	 * @param offset
	 * 	The offset of the tile we are retrieving
	 * @param offsetString
	 * 	A string representation of offset, without losing any precision
	 * @param callback
	 * 	An {@link Alertable<String>} that is passed the loaded tile whenever
	 * 	the tile arrives
	 */
	private native void loadTileNative(final JavaScriptObject theDatasource,
			final int level,
			final double offset,
			final String offsetString,
			final Alertable<GrapherTile> callback) /*-{
		theDatasource(level,
			offset,
			function (tile) {
				var successTile = @org.bodytrack.client.GrapherTile::new(ILjava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(level, offsetString, tile);

				// The following call is generic in Java, but changing
				// the parameter specification to Object seems to work, if
				// only because of type erasure
				callback.@org.bodytrack.client.Alertable::onSuccess(Ljava/lang/Object;)(successTile);
			},
			function () {
				var failureTile = @org.bodytrack.client.GrapherTile::new(ILjava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(level, offsetString, null);

				// Again, replacing a Java generic with Object seems to work
				callback.@org.bodytrack.client.Alertable::onFailure(Ljava/lang/Object;)(failureTile);
		});
	}-*/;

	private final class LoadTileAlertable implements Alertable<GrapherTile> {
		/**
		 * Called every time a new tile loads.
		 *
		 * @param tile
		 * 	The <tt>GrapherTile</tt> representing the tile that loaded
		 */
		@Override
		public void onSuccess(final GrapherTile tile) {
			final String tileKey = tile.getDescription().getTileKey();

			pendingData.add(tile);

			if (pendingUrls.containsKey(tileKey)) {
				pendingUrls.remove(tileKey);
			}

			checkForNewData();

			// tell listeners that a tile has loaded
			for (final EventListener listener : eventListeners) {
				listener.handleLoadSuccess();
			}
		}

		/**
		 * Called every time a tile load fails.
		 *
		 * <p>Tries to re-request the tile.</p>
		 *
		 * @param tile
		 * 	The <tt>GrapherTile</tt> representing the tile that failed to load
		 */
		@Override
		public void onFailure(final GrapherTile tile) {
			final TileDescription desc = tile.getDescription();
			final String tileKey = desc.getTileKey();

			if (pendingUrls.containsKey(tileKey)) {
				final int oldValue = pendingUrls.get(tileKey);
				if (oldValue > MAX_REQUESTS_PER_URL) {
					return;
				}

				pendingUrls.remove(tileKey);
				pendingUrls.put(tileKey, oldValue + 1);
			} else {
				pendingUrls.put(tileKey, 1);
			}

			loadTile(desc.getLevel(), desc.getOffset());

			// tell listeners that a tile failed to load
			for (final EventListener listener : eventListeners) {
				listener.handleLoadFailure();
			}
		}
	}
}
