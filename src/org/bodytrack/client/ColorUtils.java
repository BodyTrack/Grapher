package org.bodytrack.client;

import com.google.gwt.canvas.dom.client.CssColor;
/**
 * A class containing utilities related to colors.
 *
 * <p>This class contains constants for all the basic CSS colors.
 * The colors are as defined in the CSS3 color module, at
 * <a href="http://www.w3.org/TR/css3-color/">
 * http://www.w3.org/TR/css3-color/</a>.
 * </p>
 */
public final class ColorUtils {
	// --------------------------------------------------------------
	// The CSS basic colors
	// --------------------------------------------------------------

	public static final CssColor BLACK = CssColor.make("black");
	public static final CssColor SILVER = CssColor.make("silver");
	public static final CssColor GRAY = CssColor.make("gray");
	public static final CssColor WHITE = CssColor.make("white");
	public static final CssColor MAROON = CssColor.make("maroon");
	public static final CssColor RED = CssColor.make("red");
	public static final CssColor PURPLE = CssColor.make("purple");
	public static final CssColor FUCHSIA = CssColor.make("fuchsia");
	public static final CssColor GREEN = CssColor.make("green");
	public static final CssColor LIME = CssColor.make("lime");
	public static final CssColor OLIVE = CssColor.make("olive");
	public static final CssColor YELLOW = CssColor.make("yellow");
	public static final CssColor NAVY = CssColor.make("navy");
	public static final CssColor BLUE = CssColor.make("blue");
	public static final CssColor TEAL = CssColor.make("teal");
	public static final CssColor AQUA = CssColor.make("aqua");
	
	//extended colors
	public static final CssColor DARK_GRAY = CssColor.make("darkgray");
	
	private ColorUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * A smarter version of {@link #getNamedColor(String)}.
	 *
	 * <p>This first tries to use {@link #getNamedColor(String)}, and, if
	 * that fails, tries to parse name for some common color code styles.
	 * If that also fails, returns <code>null</code>.</p>
	 *
	 * @param name
	 * 	The name of the color to check
	 * @return
	 * 	A color for name if possible, <code>null</code> otherwise
	 * @throws NullPointerException
	 * 	If name is <code>null</code>
	 */
	public static CssColor buildColor(String name) {
		/*Color namedColor = getNamedColor(name);
		if (namedColor != null)
			return namedColor;

		name = name.toUpperCase();

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

		return null;*/
		return CssColor.make(name);
	}

	/**
	 * Parses a color code of the form &quot;#RGB&quot; or &quot;#RRGGBB&quot;
	 *
	 * @param code
	 * 	The non-<code>null</code> upper-case color code to parse
	 * @return
	 * 	A color representing the color a browser would handle code as,
	 * 	or <code>null</code> if we can't parse it
	 */
	/*private static Color parseHexColor(String code) {
		if (code.startsWith("#"))
			code = code.substring(1);
		else
			return null; // We only deal with colors of the form "#RRGGBB"

		if (code.length() == 3) {
			// Case where "#ABC" is interpreted to mean "#AABBCC"
			char r = code.charAt(0);
			char g = code.charAt(1);
			char b = code.charAt(2);

			try {
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
		} else if (code.length() == 6) {
			// Normal case
			String r = code.substring(0, 2);
			String g = code.substring(2, 4);
			String b = code.substring(4);

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
	 * @param code
	 * 	The non-<code>null</code> upper-case color code to parse
	 * @return
	 * 	A color representing the color a browser would handle code as,
	 * 	or <code>null</code> if we can't parse it
	 */
	/*private static Color parseRgbaColor(String code) {
		// Pull off the rgba
		if (code.startsWith("RGBA"))
			code = code.substring(4);
		else
			return null; // We only deal with colors of the form RGBA(...)

		// Pull off the parens
		if (code.startsWith("(") && code.endsWith(")"))
			code = code.substring(1, code.length() - 1);
		else
			return null;

		// Now for the real parsing
		String[] parts = code.split(",");
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
	 * @param code
	 * 	The non-<code>null</code> upper-case color code to parse
	 * @return
	 * 	A color representing the color a browser would handle code as,
	 * 	or <code>null</code> if we can't parse it
	 */
	/*private static Color parseRgbColor(String code) {
		// Pull off the rgb
		if (code.startsWith("RGB"))
			code = code.substring(3);
		else
			return null; // We only deal with colors of the form RGB(...)

		// Pull off the parens
		if (code.startsWith("(") && code.endsWith(")"))
			code = code.substring(1, code.length() - 1);
		else
			return null;

		// Now for the real parsing
		String[] parts = code.split(",");
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
	}*/

	public static CssColor getColor(String name, CssColor defaultColor) {
		/*if (name == null)
			return defaultColor;
		Color trueColor = buildColor(name);
		return (trueColor != null) ? trueColor : defaultColor;*/
		return CssColor.make(name);
	}
}
