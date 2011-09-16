package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

import org.bodytrack.client.InstanceController.InstanceProducer;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import gwt.g2d.client.graphics.Color;
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
// TODO: Use the colors out of ColorUtils
public final class Canvas {
	/*
	 * All the following colors should have exactly the same values as
	 * their CSS counterparts by the same (lowercase) name.
	 */

	public static final Color BLACK = new Color(0x00, 0x00, 0x00);
	public static final Color DARK_GRAY = new Color(0xA9, 0xA9, 0xA9);
	public static final Color GRAY = new Color(0x80, 0x80, 0x80);
	public static final Color RED = new Color(0xFF, 0x00, 0x00);
	public static final Color GREEN = new Color(0x00, 0x80, 0x00);
	public static final Color BLUE = new Color(0x00, 0x00, 0xFF);
	public static final Color YELLOW = new Color(0xFF, 0xFF, 0x00);

	private static final Map<Color, String> colorsToNames =
		new HashMap<Color, String>();

	static {
		colorsToNames.put(BLACK, "black");
		colorsToNames.put(DARK_GRAY, "darkgray");
		colorsToNames.put(GRAY, "gray");
		colorsToNames.put(RED, "red");
		colorsToNames.put(GREEN, "green");
		colorsToNames.put(BLUE, "blue");
		colorsToNames.put(YELLOW, "yellow");
	}

	/**
	 * The default color, which classes should set as the stroke color
	 * if wishing to &quot;clean up after themselves&quot; when done
	 * changing colors and drawing.
	 */
	public static final Color DEFAULT_COLOR = BLACK;

	/**
	 * The default alpha value, which classes should <em>always</em>
	 * set as the alpha after changing alpha on a Canvas.
	 */
	public static final double DEFAULT_ALPHA = 1.0;

	private final Surface surface;
	private final DirectShapeRenderer renderer;
	private final Element nativeCanvasElement;

	private static InstanceController<Surface, Canvas> instances;

	static {
		instances = new InstanceController<Surface, Canvas>(
			new InstanceProducer<Surface, Canvas>() {
				@Override
				public Canvas newInstance(Surface param) {
					// The constructor will throw the NullPointerException
					// if param is null
					return new Canvas(param);
				}
			});
	}

	/**
	 * Creates a new <tt>Canvas</tt> that draws on the specified surface.
	 *
	 * @param s
	 * 		the surface on which the new <tt>Canvas</tt> will draw
	 * @throws NullPointerException
	 * 		if s is <tt>null</tt>
	 * @throws IllegalArgumentException
	 * 		if s has no native HTML canvas element inside its DOM tree
	 */
	private Canvas(Surface s) {
		if (s == null)
			throw new NullPointerException("Can't draw on a null surface");

		surface = s;
		renderer = new DirectShapeRenderer(surface);
		nativeCanvasElement = findCanvasElement(surface.getElement());
		if (nativeCanvasElement == null)
			throw new IllegalArgumentException(
				"No native canvas element available");
	}

	/**
	 * Finds the first canvas element in the DOM tree rooted at e.
	 *
	 * @param e
	 * 		the root of the search tree
	 * @return
	 * 		the first canvas element found in the subtree rooted
	 * 		at e, or <tt>null</tt> if there is no canvas element
	 * 		in the subtree rooted at e
	 */
	private Element findCanvasElement(Element e) {
		if (e == null)
			return null;

		// This is kind of a heuristic that allows us to ignore
		// namespaces in the DOM
		if (e.getTagName().toLowerCase().contains("canvas"))
			return e;

		int childCount = DOM.getChildCount(e);
		for (int i = 0; i < childCount; i++) {
			Element child = DOM.getChild(e, i);
			if (child == null)
				continue; // Should never happen
			Element canvas = findCanvasElement(child);
			if (canvas != null)
				return canvas;
		}

		return null;
	}

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
		return instances.newInstance(s);
	}

	/**
	 * Converts color to a human-readable string if color is one
	 * of the constants defined by this class, and converts color
	 * to a color code otherwise.
	 *
	 * <p>Regardless, returns a string that can be used by a
	 * browser as a color specification.</p>
	 *
	 * @param color
	 * 		the Color object we want to examine
	 * @return
	 * 		a code representing color, hopefully in a human-readable
	 * 		form
	 */
	public static String friendlyName(Color color) {
		if (colorsToNames.containsKey(color))
			return colorsToNames.get(color);

		return color.getColorCode();
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

	/**
	 * Returns the native HTML canvas element found in the DOM tree
	 * rooted at the surface passed in to this object's constructor.
	 *
	 * @return
	 * 		the native canvas element
	 */
	public Element getNativeCanvasElement() {
		return nativeCanvasElement;
	}

	// --------------------------------------------------------------
	// Copies of Surface methods
	// --------------------------------------------------------------

	/**
	 * Is exactly equivalent to a call to getSurface().setStrokeStyle(color)
	 *
	 * @param color
	 * 		the color that will be used to stroke future drawing on the
	 * 		surface
	 * @return
	 * 		the Surface used for the setStrokeStyle call
	 */
	public Surface setStrokeStyle(Color color) {
		return surface.setStrokeStyle(color);
	}

	/**
	 * Is exactly equivalent to a call to getSurface().setFillStyle(color)
	 *
	 * @param color
	 * 		the color that will be used to fill future drawing on the
	 * 		surface
	 * @return
	 * 		the Surface used for the setFillStyle call
	 */
	public Surface setFillStyle(Color color) {
		return surface.setFillStyle(color);
	}

	/**
	 * Is exactly equivalent to a call to getSurface().save()
	 *
	 * @return
	 * 		the Surface used for the save call
	 */
	public Surface save() {
		return surface.save();
	}

	/**
	 * Is exactly equivalent to a call to getSurface().restore()
	 *
	 * @return
	 * 		the Surface used for the restore call
	 */
	public Surface restore() {
		return surface.restore();
	}

	/**
	 * Is exactly equivalent to a call to getSurface().measureText().
	 *
	 * @param text
	 * 		some non-{@literal null} string to measure
	 * @return
	 * 		the number of pixels that text would take up if it were drawn
	 * 		on this canvas
	 */
	public double measureText(String text) {
		return surface.measureText(text);
	}

	// --------------------------------------------------------------
	// Copies of DirectShapeRenderer methods
	// --------------------------------------------------------------

	/**
	 * Is exactly equivalent to a call to getRenderer().beginPath()
	 *
	 * @return
	 * 		the DirectShapeRenderer used for the beginPath call
	 */
	public DirectShapeRenderer beginPath() {
		return renderer.beginPath();
	}

	/**
	 * Is exactly equivalent to a call to getRenderer().closePath()
	 *
	 * @return
	 * 		the DirectShapeRenderer used for the closePath call
	 */
	public DirectShapeRenderer closePath() {
		return renderer.closePath();
	}

	/**
	 * Is exactly equivalent to a call to getRenderer().stroke()
	 *
	 * @return
	 * 		the DirectShapeRenderer used for the stroke call
	 */
	public DirectShapeRenderer stroke() {
		return renderer.stroke();
	}

	/**
	 * Is exactly equivalent to a call to getRenderer().fill()
	 *
	 * @return
	 * 		the DirectShapeRenderer used for the fill call
	 */
	public DirectShapeRenderer fill() {
		return renderer.fill();
	}
}
