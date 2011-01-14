package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

import gwt.g2d.client.graphics.Color;

/**
 * A class containing utilities related to colors.
 *
 * <p>This class contains constants for all the basic CSS colors.
 * The colors are as defined in the CSS3 color module, at
 * <a href="http://www.w3.org/TR/css3-color/">w3.org</a>.</p>
 */
// TODO: Add all named CSS colors, not just the basic colors
public class ColorUtils {
	// --------------------------------------------------------------
	// The CSS basic colors
	// --------------------------------------------------------------

	/**
	 * The color black, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color BLACK = new Color(0x00, 0x00, 0x00);

	/**
	 * The color silver, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color SILVER = new Color(0xC0, 0xC0, 0xC0);

	/**
	 * The color gray, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color GRAY = new Color(0x80, 0x80, 0x80);

	/**
	 * The color white, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color WHITE = new Color(0xFF, 0xFF, 0xFF);

	/**
	 * The color maroon, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color MAROON = new Color(0x80, 0x00, 0x00);

	/**
	 * The color red, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color RED = new Color(0xFF, 0x00, 0x00);

	/**
	 * The color purple, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color PURPLE = new Color(0x80, 0x00, 0x80);

	/**
	 * The color fuchsia, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color FUCHSIA = new Color(0xFF, 0x00, 0xFF);

	/**
	 * The color green, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color GREEN = new Color(0x00, 0x80, 0x00);

	/**
	 * The color lime, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color LIME = new Color(0x00, 0xFF, 0x00);

	/**
	 * The color olive, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color OLIVE = new Color(0x80, 0x80, 0x00);

	/**
	 * The color yellow, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color YELLOW = new Color(0xFF, 0xFF, 0x00);

	/**
	 * The color navy, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color NAVY = new Color(0x00, 0x00, 0x80);

	/**
	 * The color blue, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color BLUE = new Color(0x00, 0x00, 0xFF);

	/**
	 * The color teal, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color TEAL = new Color(0x00, 0x80, 0x80);

	/**
	 * The color aqua, as a {@link gwt.g2d.client.graphics.Color Color}
	 * object.
	 */
	public static final Color AQUA = new Color(0x00, 0xFF, 0xFF);

	// These maps are used for converting between basic names and colors
	// The names are perfectly valid to use in CSS styles unchanged
	private static final Map<String, Color> basicNameToColor =
		new HashMap<String, Color>();
	private static final Map<Color, String> basicColorToName =
		new HashMap<Color, String>();

	static {
		// We always use lower-case names

		basicNameToColor.put("black", BLACK);
		basicNameToColor.put("silver", SILVER);
		basicNameToColor.put("gray", GRAY);
		basicNameToColor.put("white", WHITE);
		basicNameToColor.put("maroon", MAROON);
		basicNameToColor.put("red", RED);
		basicNameToColor.put("purple", PURPLE);
		basicNameToColor.put("fuchsia", FUCHSIA);
		basicNameToColor.put("green", GREEN);
		basicNameToColor.put("lime", LIME);
		basicNameToColor.put("olive", OLIVE);
		basicNameToColor.put("yellow", YELLOW);
		basicNameToColor.put("navy", NAVY);
		basicNameToColor.put("blue", BLUE);
		basicNameToColor.put("teal", TEAL);
		basicNameToColor.put("aqua", AQUA);

		// Since the mapping is one-to-one, we can make the reverse
		// map very easily
		for (Map.Entry<String, Color> entry: basicNameToColor.entrySet())
			basicColorToName.put(entry.getValue(), entry.getKey());
	}

	public static boolean haveColorName(Color c) {
		return basicColorToName.containsKey(c);
	}

	public static String getColorName(Color c) {
		if (haveColorName(c))
			return basicColorToName.get(c);

		return c.getColorCode();
	}

	public static boolean haveColorObject(String name) {
		return basicNameToColor.containsKey(name.toLowerCase());
	}

	public static Color getColor(String name) {
		if (haveColorObject(name))
			return basicNameToColor.get(name);

		return null;
	}
}
