package org.bodytrack.client;

import gwt.g2d.client.math.Vector2;

/**
 * Provides a way for a class to ensure that it is drawing within
 * previously specified bounds, without dealing directly with checking
 * bounds.
 *
 * <p>Objects of this class are immutable, and thus can be shared
 * freely, and are thread-safe as well (although, since JavaScript is
 * single-threaded, this is not much use).</p>
 *
 * <p>There is some inconsistency in this class: some methods are
 * all-or-nothing, drawing nothing if part of a structure is out of
 * bounds, and some methods will draw part of a structure if the
 * rest is out of bounds.  Each method documents its way of handling
 * a partially out of bounds structure, and all methods act exactly
 * the same as their DirectShapeRenderer counterparts if all parts of
 * a structure are in bounds.</p>
 */
public final class BoundedDrawingBox {
	private Canvas canvas;
	private double xMin;
	private double yMin;
	private double xMax;
	private double yMax;

	// General tolerance for double equality
	private static final Double TOLERANCE = 1e-6;

	/**
	 * Creates a new BoundedDrawingBox.
	 *
	 * @param canvas
	 * 		the {@link org.bodytrack.client.Canvas Canvas} on which this
	 * 		will draw
	 * @param minX
	 * 		the minimum X-value at which this object should draw a point
	 * @param minY
	 * 		the minimum Y-value at which this object should draw a point
	 * @param maxX
	 * 		the maximum X-value at which this object should draw a point
	 * @param maxY
	 * 		the maximum Y-value at which this object should draw a point
	 * @throws NullPointerException
	 * 		if canvas is <tt>null</tt>
	 * @throws IllegalArgumentException
	 * 		if minX is greater than maxX, or minY is greater than maxY
	 */
	public BoundedDrawingBox(Canvas canvas, double minX, double minY,
			double maxX, double maxY) {
		if (canvas == null)
			throw new NullPointerException("Cannot use null Canvas");

		if (minX > maxX || minY > maxY)
			throw new IllegalArgumentException(
				"Illegal bounds: min value greater than max");

		this.canvas = canvas;
		this.xMin = minX;
		this.yMin = minY;
		this.xMax = maxX;
		this.yMax = maxY;
	}

	/**
	 * Returns the Canvas used to construct this object.
	 *
	 * @return
	 * 		the Canvas used to construct this object
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Draws a circle with the specified values and radius,
	 * if and only if all parts of the circle are in bounds.
	 *
	 * @param x
	 * 		the X-value at the center of the circle
	 * @param y
	 * 		the Y-value at the center of the circle
	 * @param radius
	 * 		the radius of the circle
	 */
	public void drawCircle(double x, double y, double radius) {
		if (inBounds(x - radius, y - radius)
				&& inBounds(x - radius, y + radius)
				&& inBounds(x + radius, y - radius)
				&& inBounds(x + radius, y + radius))
			getCanvas().getRenderer().drawCircle(x, y, radius);
	}

	/**
	 * Draws a circle with the specified values and radius,
	 * if and only if the center the circle is in bounds.
	 *
	 * <p>Unlike {@link #drawCircle(double, double, double)},
	 * this only checks whether the circle's center is in
	 * bounds, not whether the whole circle is in bounds.  This
	 * method is much more appropriate for small circles, in
	 * which the edges and center are very close together.</p>
	 *
	 * @param x
	 * 		the X-value at the center of the circle
	 * @param y
	 * 		the Y-value at the center of the circle
	 * @param radius
	 * 		the radius of the circle
	 */
	public void drawDot(double x, double y, double radius) {
		if (inBounds(x, y))
			getCanvas().getRenderer().drawCircle(x, y, radius);
	}

	/**
	 * Draws a line segment from (x1, y1) to (x2, y2), or at least as
	 * much of the line segment as is in bounds.
	 *
	 * <p>Note that this method will draw a partial line segment if part
	 * of the line segment is out of bounds.</p>
	 *
	 * @param x1
	 * 		the X-coordinate of the first point (out of the two connected
	 * 		by the line segment we are to draw)
	 * @param y1
	 * 		the Y-coordinate of the first point (out of the two connected
	 * 		by the line segment we are to draw)
	 * @param x2
	 * 		the X-coordinate of the second point (out of the two connected
	 * 		by the line segment we are to draw)
	 * @param y2
	 * 	 	the Y-coordinate of the second point (out of the two connected
	 * 		by the line segment we are to draw)
	 */
	public void drawLineSegment(double x1, double y1, double x2, double y2) {
		if (inBounds(x1, y1) && inBounds(x2, y2)) {
			// If everything is in bounds, we just draw the line
			getCanvas().getRenderer().drawLineSegment(x1, y1, x2, y2);
			return;
		}

		// Now we know at least one of the endpoints is out of bounds

		if (inBounds(x1, y1)) {
			// First point in bounds, second point out of bounds

			Vector2 secondEndpoint = getSecondEndpoint(x1, y1, x2, y2);

			// Now we have a pair (x1, y1), (x2, y2) where both points are
			// in bounds
			getCanvas().getRenderer().drawLineSegment(x1, y1,
				secondEndpoint.getX(), secondEndpoint.getY());

			return;
		}

		// Since a line segment is non-directional, we can reuse code
		// by drawing the same line the opposite direction, as long as
		// (x2, y2) is in bounds
		if (inBounds(x2, y2)) {
			drawLineSegment(x2, y2, x1, y1);
			return;
		}

		// Now we know both points are out of bounds

		if (x1 == x2) {
			// Vertical line
			getCanvas().getRenderer().drawLineSegment(x1, yMin, x1, yMax);
			return;
		}

		double slope = (y2 - y1) / (x2 - x1);

		if (slope == 0) {
			// Optimization: don't do anything extra if the slope is 0

			getCanvas().getRenderer().drawLineSegment(xMin, y1, xMax, y2);
			return;
		}

		if (x1 > x2) {
			// We ensure that x1 < x2, which allows a consistent
			// set of values.  To do this, we swap (x1, y1) and
			// (x2, y2) if x1 > x2

			double temp = x1;
			x1 = x2;
			x2 = temp;

			temp = y1;
			y1 = x2;
			y2 = temp;
		}

		Vector2 topIntercept = getIntersect(
			new Vector2(x1, y1),
			slope,
			new Vector2(0.0, yMax),
			0.0);
		Vector2 rightIntercept = getIntersect(
			new Vector2(x1, y1),
			slope,
			new Vector2(xMax, 0.0),
			Double.POSITIVE_INFINITY);
		Vector2 bottomIntercept = getIntersect(
			new Vector2(x1, y1),
			slope,
			new Vector2(0.0, yMin),
			0.0);
		Vector2 leftIntercept = getIntersect(
			new Vector2(x1, y1),
			slope,
			new Vector2(xMin, 0.0),
			Double.POSITIVE_INFINITY);

		Vector2 in, out;

		if (slope > 0) {
			// Line can come in through bottom or left side, and exit
			// through right or top side (in geometry, not
			// on the screen)

			in = inBounds(bottomIntercept)
				? bottomIntercept : leftIntercept;
			out = inBounds(topIntercept)
				? topIntercept : rightIntercept;
		} else {
			// Line can come in through top or left side, and exit
			// through right or bottom side (in geometry, not
			// on the screen)

			in = inBounds(topIntercept)
				? topIntercept : leftIntercept;
			out = inBounds(bottomIntercept)
				? bottomIntercept : bottomIntercept;
		}

		// Now we have the line to draw across the box
		getCanvas().getRenderer().drawLineSegment(
			in.getX(), in.getY(),
			out.getX(), out.getY());
	}

	/**
	 * Calculates the correct (x2, y2) to use, given that (x1, y1) is
	 * in bounds and (x2, y2) is out of bounds.
	 *
	 * @param x1
	 * 		the X-coordinate of the first point
	 * @param y1
	 * 		the Y-coordinate of the first point
	 * @param x2
	 * 		the X-coordinate of the second point
	 * @param y2
	 * 		the Y-coordinate of the second point
	 * @return
	 * 		a Vector2 with the new (x2, y2) to use, on the line between
	 * 		(x1, y1) and (x2, y2)
	 */
	private Vector2 getSecondEndpoint(double x1, double y1, double x2,
			double y2) {
		if (! (inBounds(x1, y1) && ! inBounds(x2, y2)))
			throw new IllegalArgumentException(
				"Invalid endpoints: this is designed to calculate "
				+ "an endpoint with the first point in bounds");

		double slope = (y2 - y1) / (x2 - x1);

		if (doubleEquals(x1, x2) || Double.isInfinite(slope)) {
			// Vertical line: we know x2 is safe because x1 is
			// safe, so we just set y2
			// The second part of this clause tests for overflow, even if
			// x1 and x2 are not quite equal

			x2 = x1;

			if (y2 < yMin)
				y2 = yMin;
			else
				y2 = yMax;
		} else {
			// Non-vertical line: we can use slope in this case

			if (doubleEquals(slope, 0.0)) {
				// Horizontal line: y2 is safe, so just set x2
				y2 = y1;

				if (x2 < xMin)
					x2 = xMin;
				else
					x2 = xMax;
			} else if (slope > 0) {
				// Upward-sloping line: we know (x2, y2) must be
				// separated from (x1, y1) by either the right or
				// top edge (actually, this is correct for
				// geometry, but, for computer drawing, the separation
				// is really at the right or bottom edge)

				Vector2 rightIntercept = getIntersect(
					new Vector2(x1, y1),
					slope,
					new Vector2(xMax, 0.0),
					Double.POSITIVE_INFINITY);

				Vector2 topIntercept = getIntersect(
					new Vector2(x1, y1),
					slope,
					new Vector2(0.0, yMax),
					0.0);

				if (inBounds(rightIntercept)) {
					// Line goes off right edge
					x2 = rightIntercept.getX();
					y2 = rightIntercept.getY();
				} else {
					// Line goes off top edge
					x2 = topIntercept.getX();
					y2 = topIntercept.getY();
				}
			} else {
				// Downward-sloping line: we know (x2, y2) must be
				// separated from (x1, y1) by either the left or
				// bottom edge (actually, this is correct for
				// geometry, but, for computer drawing, the separation
				// is really at the left or top edge)

				Vector2 leftIntercept = getIntersect(
					new Vector2(x1, y1),
					slope,
					new Vector2(xMin, 0.0),
					Double.POSITIVE_INFINITY);

				Vector2 bottomIntercept = getIntersect(
					new Vector2(x1, y1),
					slope,
					new Vector2(0.0, yMin),
					0.0);

				if (inBounds(leftIntercept)) {
					// Line goes off left edge
					x2 = leftIntercept.getX();
					y2 = leftIntercept.getY();
				} else {
					// Line goes off bottom edge
					x2 = bottomIntercept.getX();
					y2 = bottomIntercept.getY();
				}
			}
		}

		return new Vector2(x2, y2);
	}

	/**
	 * Finds the intercept between the two lines.
	 *
	 * <p>If the two lines are parallel, returns <tt>null</tt> to
	 * signal parallel lines.  A calling method should be prepared
	 * for a <tt>null</tt> return, for this reason.</p>
	 *
	 * <p>Note that this method does not take into account whether or
	 * not these lines are in bounds, or if the intercept point is in
	 * bounds.</p>
	 *
	 * @param point1
	 * 		any point on the first line
	 * @param slope1
	 * 		the slope of the first line.  May be Double.POSITIVE_INFINITY
	 * 		or Double.NEGATIVE_INFINITY to signal a vertical line.
	 * @param point2
	 * 		any point on the second line
	 * @param slope2
	 * 	 	the slope of the second line.  May be Double.POSITIVE_INFINITY
	 * 		or Double.NEGATIVE_INFINITY to signal a vertical line.
	 * @return
	 * 		the unique point of intersection between the two lines, or
	 * 		<tt>null</tt> if the lines are parallel.
	 */
	private Vector2 getIntersect(Vector2 point1, double slope1,
			Vector2 point2, double slope2) {
		if (slope1 == slope2)
			return null;

		// Check if the first line is vertical
		if (Double.isInfinite(slope1)) {

			// Check if both lines are vertical
			if (Double.isInfinite(slope2))
				return null;

			// Already know the X-value of the intercept
			double x = point1.getX();

			// By math, (y - y2) = m2 (x - x2)
			// implies that y = m2 (x - x2) + y2
			double y = slope2 * (x - point2.getX()) + point2.getY();

			return new Vector2(x, y);
		} else if (Double.isInfinite(slope2))
			// Same intercept if we switch the lines
			return getIntersect(point2, slope2, point1, slope1);

		// Now for the general case, with neither line vertical

		// By math, (y - y1) = m1 (x - x1) and (y - y2) = m2 (x - x2)
		// implies that x = (1 / (m1 - m2)) (m1 x1 - m2 x2 + y2 - y1)
		// and that y = m1 (x - x1) + y1
		double x = (1 / (slope1 - slope2))
			* (slope1 * point1.getX() - slope2 * point2.getX()
				+ point2.getY() - point1.getY());
		double y = slope1 * (x - point1.getX()) + point1.getY();

		return new Vector2(x, y);
	}

	/**
	 * Returns <tt>true</tt> if and only if the point (x, y) is
	 * contained in the rectangle determined by (minX, minY) and
	 * (maxX, maxY).
	 *
	 * @param x
	 * 		the X-value of the point to check
	 * @param y
	 * 		the Y-value of the point to check
	 * @return
	 * 		<tt>true</tt> if and only if (x, y) is in bounds
	 */
	private boolean inBounds(double x, double y) {
		return x >= xMin && x <= xMax && y >= yMin && y <= yMax;
	}

	/**
	 * Exactly the same as calling
	 * {@code inBounds(point.getX(), point.getY())}.
	 *
	 * @param point
	 * 		the (x, y) point to check for bounds
	 * @return
	 * 		<tt>true</tt> if and only if (x, y) is in bounds
	 */
	private boolean inBounds(Vector2 point) {
		return inBounds(point.getX(), point.getY());
	}

	/**
	 * Tells if a and b are equal, within TOLERANCE.
	 *
	 * <p>Note that this does not really define an equality
	 * relationship, since it is not transitive.  It is possible
	 * to define doubles a, b, and c such that
	 * {@code doubleEquals(a, b)} and {@code doubleEquals(b, c)} are
	 * both true, but {@code doubleEquals(a, c)} is false.  Thus,
	 * this method should be used with care.</p>
	 *
	 * @param a
	 * 		some double value to be compared
	 * @param b
	 * 		some other double value to be compared
	 * @return
	 * 		<tt>true</tt> if and only if a and b are within
	 * 		TOLERANCE of each other
	 * @see #TOLERANCE
	 */
	private static boolean doubleEquals(double a, double b) {
		return Math.abs(a - b) < TOLERANCE;
	}
}
