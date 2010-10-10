package org.bodytrack.client;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

/**
 * Wrapper for a G2D Surface/DirectShapeRenderer pair.
 *
 * This is intended to collect many of the important methods of the
 * {@link gwt.g2d.client.graphics.Surface Surface} and its associated
 * {@link gwt.g2d.client.graphics.DirectShapeRenderer DirectShapeRenderer}
 * in one class.  Note that not all methods of the two classes are
 * represented here.  However, the important methods of the two classes are
 * wrapped here, and calls to {@link #getSurface()} and to
 * {@link #getRenderer()}, which simply return references to those two
 * objects, allow calls to the other methods.
 */
public class Canvas {
	private Surface surface;
	private DirectShapeRenderer renderer;

	public Canvas(Surface s) {
		if (s == null)
			throw new NullPointerException("Surface cannot be null");

		surface = s;

		renderer = new DirectShapeRenderer(surface);
	}

	/**
	 * Returns the Surface passed in to this object's constructor.
	 *
	 * @return
	 * 		the surface
	 */
	public Surface getSurface() {
		return surface;
	}

	/**
	 * Returns the DirectShapeRenderer derived from the Surface passed
	 * in to this object's constructor.
	 *
	 * @return
	 * 		the renderer
	 */
	public DirectShapeRenderer getRenderer() {
		return renderer;
	}
}
