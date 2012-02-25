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

   void checkForFetch(final int correctLevel);

   List<GrapherTile> getBestResolutionTiles(final int currentLevel);

   GrapherTile getBestResolutionTileAt(final double time, final int bestLevel);
}