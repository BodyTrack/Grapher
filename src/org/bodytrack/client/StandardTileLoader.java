package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StandardTileLoader implements TileLoader {
	/**
	 * We never re-request a URL after MAX_REQUESTS_PER_URL or more failures
	 * in a row.
	 */
	private static final int MAX_REQUESTS_PER_URL = 5;

	private final Map<TileDescription, GrapherTile> descriptions;
	private final Set<TileDescription> pendingDescriptions;
	private final Map<String, Integer> pendingUrls;
	private final List<GrapherTile> pendingData;

	private final JavaScriptObject datasource;
	private final GraphAxis timeAxis;

	private final Set<EventListener> eventListeners;
	private final Alertable<GrapherTile> loadTileAlertable;

	public StandardTileLoader(final JavaScriptObject datasource, final GraphAxis timeAxis) {
		this.datasource = datasource;
		this.timeAxis = timeAxis;

		descriptions = new HashMap<TileDescription, GrapherTile>();
		pendingDescriptions = new HashSet<TileDescription>();
		pendingUrls = new HashMap<String, Integer>();
		pendingData = new ArrayList<GrapherTile>();

		eventListeners = new HashSet<EventListener>();
		loadTileAlertable = new LoadTileAlertable();
	}

	@Override
	public final void addEventListener(final EventListener listener) {
		if (listener == null)
			throw new NullPointerException();

		eventListeners.add(listener);
	}

	@Override
	public final void removeEventListener(final EventListener listener) {
		if (listener == null)
			throw new NullPointerException();

		eventListeners.remove(listener);
	}

	/**
	 * Checks for and performs a fetch for data from the server if
	 * necessary
	 */
	@Override
	public final void checkForFetch(final int level) {
		final double tileWidth = getTileWidth(level);
		final long minOffset = computeMinOffset(tileWidth);
		final long maxOffset = computeMaxOffset(tileWidth);

		// No danger of multiple fetches of the same tile, because
		// fetchFromServer ensures that it doesn't attempt to fetch a
		// previously-fetched tile
		for (long offset = minOffset; offset <= maxOffset; offset++) {
			fetchFromServer(level, offset);
		}
	}

	private long computeMinOffset(final double tileWidth) {
		return (long)(timeAxis.getMin() / tileWidth);
	}

	private long computeMaxOffset(final double tileWidth) {
		return (long)(timeAxis.getMax() / tileWidth);
	}

	/**
	 * Possibly fetches the specified tile from the server
	 *
	 * <p>Note that this checks the {@link #pendingDescriptions} and
	 * {@link #descriptions} instance variables to determine if this tile
	 * has already been requested.  If so, does not request anything from
	 * the server.</p>
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
	 * Returns a sorted list of all best resolution tiles available
	 *
	 * @return
	 * 	A sorted list of all the best resolution tiles currently available
	 */
	@Override
	public final List<GrapherTile> getBestResolutionTiles(final int level) {
		final List<GrapherTile> best = new ArrayList<GrapherTile>();

		// When minTime and maxTime are used in calculations, they are
		// used to make the calculations scale-independent
		final double minTime = timeAxis.getMin();
		final double maxTime = timeAxis.getMax();
		final double timespan = maxTime - minTime;

		double maxCoveredTime = minTime;

		while (maxCoveredTime <= maxTime) {
			final GrapherTile bestAtCurrTime =
				getBestResolutionTileAt(maxCoveredTime + timespan * 1e-3, level);
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
	 * 	The best-resolution (lowest-level) tile which has min value
	 * 	less than or equal to time, and max value greater than or
	 * 	equal to time, or <code>null</code> if no such tile exists
	 */
	public final GrapherTile getBestResolutionTileAt(final double time,
			final int bestLevel) {
		GrapherTile bestTile = null;
		TileDescription bestDesc = null;

		for (final Entry<TileDescription, GrapherTile> ent: descriptions.entrySet()) {
			final TileDescription desc = ent.getKey();
			final GrapherTile tile = ent.getValue();

			if (desc.getMinTime() > time || desc.getMaxTime() < time) {
				continue;
			}

			if (bestTile == null) {
				bestTile = tile;
				bestDesc = desc;
			} else {
				final int dlevel = Math.abs(desc.getLevel() - bestLevel);
				final int dbest = Math.abs(bestDesc.getLevel() - bestLevel);
				if (dlevel < dbest) {
					bestTile = tile;
					bestDesc = desc;
				} else if (dlevel == dbest) {
					if (desc.getLevel() < bestDesc.getLevel()) {
						bestTile = tile;
						bestDesc = desc;
					}
				}
			}
		}

		return bestTile;
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
		loadTileNative(datasource, level, (double)offset, loadTileAlertable);
	}

	/**
	 * Retrieves a tile from the specified <code>level</code> and <code>offset</code>.
	 *
	 * <p>Sends a tile retrieved from url to the {@link Alertable#onSuccess(Object)}
	 * or {@link Alertable#onFailure(Object)} callback whenever that tile
	 * arrives.</p>
	 *
	 * @param datasource
	 * 	The datasource from which to retrieve the data
	 * @param level
	 * 	The level of the tile we are retrieving
	 * @param offset
	 * 	The offset of the tile we are retrieving, except of type <code>double</code>.
	 * 	This is required because GWT wraps the <code>long</code> type in a way that
	 * 	makes longs inaccessible to JavaScript
	 * @param callback
	 * 	An {@link Alertable<GrapherTile>} that is passed the loaded tile whenever the
	 * 	tile arrives from the server
	 */
	private native void loadTileNative(final JavaScriptObject datasource,
			final int level,
			final double offset,
			final Alertable<GrapherTile> callback) /*-{
		datasource(level, offset,
			function (tile) {
				var successTile = @org.bodytrack.client.GrapherTile::new(IDLcom/google/gwt/core/client/JavaScriptObject;)(level, offset, tile);

				// The following method is generic in Java, but changing
				// the parameter specification to Object seems to work, if
				// only because of type erasure
				callback.@org.bodytrack.client.Alertable::onSuccess(Ljava/lang/Object;)(successTile);
			},
			function () {
				var failureTile = @org.bodytrack.client.GrapherTile::new(IDLcom/google/gwt/core/client/JavaScriptObject;)(level, offset, null);

				// Again, replacing a Java generic with Object seems to work
				callback.@org.bodytrack.client.Alertable::onFailure(Ljava/lang/Object;)(failureTile);
			});
	}-*/;

	private final class LoadTileAlertable implements Alertable<GrapherTile> {
		/**
		 * Alerts the plot that the new tile has loaded
		 *
		 * @param tile
		 * 	The {@link TileDescription} representing the tile that loaded
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
		 * Tries to re-request the failed tile
		 *
		 * @param tile
		 * 	The {@link TileDescription} representing the tile that failed to load
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
