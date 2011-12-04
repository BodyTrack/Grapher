package org.bodytrack.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.KnownColor;

/**
 * A class containing utilities related to colors.
 *
 * <p>This class contains constants for all the basic CSS colors.
 * The colors are as defined in the CSS3 color module, at
 * <a href="http://www.w3.org/TR/css3-color/">w3.org</a>.</p>
 */
// TODO: Add all named CSS colors, not just the basic colors
// TODO: To do this, define my own list of KnownColor instances, since
// KnownColor already defines all the constants I need
public final class ColorUtils {
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

	public static Color getBasicColor(String name) {
		if (name == null)
			throw new NullPointerException("Can't get color for null name");

		if (haveColorObject(name))
			return basicNameToColor.get(name);

		return null;
	}

	/**
	 * A smarter version of {@link #getBasicColor(String)}.
	 *
	 * <p>This first tries to use {@link #getBasicColor(String)}, and, if
	 * that fails, tries to parse name for some common color code styles.
	 * If that also fails, returns <tt>null</tt>.</p>
	 *
	 * @param name
	 * 		the name of the color to check
	 * @return
	 * 		a color for name if possible, <tt>null</tt> otherwise
	 * @throws NullPointerException
	 * 		if name is <tt>null</tt>
	 */
	public static Color buildColor(String name) {
		Color namedColor = getBasicColor(name);
		if (namedColor != null)
			return namedColor;

		name = name.toUpperCase(); // Important for both method calls

		if (name.startsWith("#"))
			return parseHexColor(name);
		if (name.startsWith("RGBA"))
			return parseRgbaColor(name);
		if (name.startsWith("RGB"))
			return parseRgbColor(name);

		// Chances are, if we have gotten this far, the KnownColor
		// check won't help either
		List<KnownColor> knownColors =
			new ArrayList<KnownColor>(KnownColor.getKnownColors());
		for (KnownColor color: knownColors) {
			if (color.getColorCode().equalsIgnoreCase(name))
				return color;
		}

		return null;
	}

	/**
	 * Parses a color code of the form &quot;#RGB&quot; or &quot;#RRGGBB&quot;
	 *
	 * @param name
	 * 		the non-<tt>null</tt> upper-case color code to parse
	 * @return
	 * 		a color representing the color a browser would handle name as,
	 * 		or <tt>null</tt> if we can't parse it
	 */
	private static Color parseHexColor(String name) {
		if (name.startsWith("#"))
			name = name.substring(1);
		else
			return null; // We only deal with colors of the form "#RRGGBB"

		if (name.length() == 3) {
			// Case where "#ABC" is interpreted to mean "#AABBCC"
			char r = name.charAt(0);
			char g = name.charAt(1);
			char b = name.charAt(2);

			try {
				// TODO: Possibly replace this with a recursive
				// call, after doubling the characters and putting
				// the # sign back on
				int redHalf = Character.digit(r, 16);
				int greenHalf = Character.digit(g, 16);
				int blueHalf = Character.digit(b, 16);

				int red = (redHalf << 4) + redHalf;
				int green = (greenHalf << 4) + greenHalf;
				int blue = (blueHalf << 4) + blueHalf;

				return new Color(red, green, blue);
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (name.length() == 6) {
			// Normal case
			String r = name.substring(0, 2);
			String g = name.substring(2, 4);
			String b = name.substring(4);

			try {
				int red = Integer.parseInt(r, 16);
				int green = Integer.parseInt(g, 16);
				int blue = Integer.parseInt(b, 16);

				return new Color(red, green, blue);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		return null; // Ill-formed color code
	}

	/**
	 * Parses a color code of the form &quot;RGBA(red,green,blue,alpha)&quot;
	 *
	 * <p>This is especially useful, since it parses the default returned value
	 * of {@link gwt.g2d.client.graphics.Color#getColorCode()
	 * Color.getColorCode()}.</p>
	 *
	 * @param name
	 * 		the non-<tt>null</tt> upper-case color code to parse
	 * @return
	 * 		a color representing the color a browser would handle name as,
	 * 		or <tt>null</tt> if we can't parse it
	 */
	private static Color parseRgbaColor(String name) {
		// Pull off the rgba
		if (name.startsWith("RGBA"))
			name = name.substring(4);
		else
			return null; // We only deal with colors of the form RGBA(...)

		// Pull off the parens
		if (name.startsWith("(") && name.endsWith(")"))
			name = name.substring(1, name.length() - 1);
		else
			return null;

		// Now for the real parsing
		String[] parts = name.split(",");
		if (parts.length != 4)
			return null;

		String r = parts[0].trim();
		String g = parts[1].trim();
		String b = parts[2].trim();
		String a = parts[3].trim();

		try {
			int red = Integer.parseInt(r);
			int green = Integer.parseInt(g);
			int blue = Integer.parseInt(b);
			double alpha = Double.parseDouble(a);

			return new Color(red, green, blue, alpha);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Parses a color code of the form &quot;RGB(red,green,blue)&quot;
	 *
	 * @param name
	 * 		the non-<tt>null</tt> upper-case color code to parse
	 * @return
	 * 		a color representing the color a browser would handle name as,
	 * 		or <tt>null</tt> if we can't parse it
	 */
	private static Color parseRgbColor(String name) {
		// Pull off the rgb
		if (name.startsWith("RGB"))
			name = name.substring(3);
		else
			return null; // We only deal with colors of the form RGB(...)

		// Pull off the parens
		if (name.startsWith("(") && name.endsWith(")"))
			name = name.substring(1, name.length() - 1);
		else
			return null;

		// Now for the real parsing
		String[] parts = name.split(",");
		if (parts.length != 3)
			return null;

		String r = parts[0].trim();
		String g = parts[1].trim();
		String b = parts[2].trim();

		try {
			int red = Integer.parseInt(r);
			int green = Integer.parseInt(g);
			int blue = Integer.parseInt(b);

			return new Color(red, green, blue);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Color getColor(String name, Color defaultColor) {
		if (name == null)
			return defaultColor;
		Color trueColor = buildColor(name);
		return (trueColor != null) ? trueColor : defaultColor;
	}
}
