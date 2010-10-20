package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

/**
 * Wrapper for a G2D Surface/DirectShapeRenderer pair.
 *
 * <p>This is intended to collect many of the important methods of the
 * {@link gwt.g2d.client.graphics.Surface Surface} and its associated
 * {@link gwt.g2d.client.graphics.DirectShapeRenderer DirectShapeRenderer}
 * in one class.  Note that not all methods of the two classes are
 * represented here.  However, the important methods of the two classes are
 * wrapped here, and calls to {@link #getSurface()} and to
 * {@link #getRenderer()}, which simply return references to those two
 * objects, allow calls to the other methods.</p>
 *
 * <p>This class is instance-controlled for efficiency: under this
 * system, only one DirectShapeRenderer is created per Surface.</p>
 */
public class Canvas {
	private Surface surface;
	private DirectShapeRenderer renderer;

	private static Map<Surface, Canvas> instances;

	static {
		instances = new HashMap<Surface, Canvas>();
	}

	private Canvas() { }

	/**
	 * Factory method to create a new Canvas object.
	 *
	 * @param s
	 * 		the {@link gwt.g2d.client.graphics.Surface Surface}
	 * 		on which the new Canvas will draw
	 * @return
	 * 		a Canvas with a pointer to s and to an associated
	 * 		{@link gwt.g2d.client.graphics.DirectShapeRenderer
	 * 		DirectShapeRenderer}
	 */
	public static Canvas buildCanvas(Surface s) {
		if (s == null)
			throw new NullPointerException("Surface cannot be null");

		// We have already made a Canvas that points to s
		if (instances.containsKey(s))
			return instances.get(s);

		Canvas result = new Canvas();

		result.surface = s;

		result.renderer = new DirectShapeRenderer(result.surface);

		return result;
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
