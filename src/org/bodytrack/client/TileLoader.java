package org.bodytrack.client;

import java.util.List;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface TileLoader {
	interface EventListener {
		void handleLoadSuccess();
		void handleLoadFailure();
	}

	void addEventListener(EventListener listener);
	void removeEventListener(EventListener listener);

	boolean checkForFetch();
	boolean checkForFetch(final double minTime, final double maxTime,
			final EventListener onload);

	List<GrapherTile> getBestResolutionTiles();
	List<GrapherTile> getBestResolutionTiles(final int currentLevel);
	List<GrapherTile> getBestResolutionTiles(final int currentLevel,
			final double minTime, final double maxTime);

	GrapherTile getBestResolutionTileAt(final double time);
	GrapherTile getBestResolutionTileAt(final double time, final int bestLevel);
}