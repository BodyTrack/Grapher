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
	boolean checkForFetch(final double xMin, final double xMax);

	List<GrapherTile> getBestResolutionTiles();
	GrapherTile getBestResolutionTileAt(final double time);
}