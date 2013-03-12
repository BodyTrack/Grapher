package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.canvas.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a way for a class to ensure that it is drawing within
 * previously specified bounds, without directly checking bounds
 *
 * <p>Objects of this class are immutable, and thus can be shared freely.</p>
 *
 * <p>All methods act exactly the same as their DirectShapeRenderer
 * counterparts if all parts of a structure are in bounds.</p>
 *
 * <p>Also note that none of these methods calls
 * {@link org.bodytrack.client.GrapherCanvas#beginPath() beginPath()} or
 * {@link org.bodytrack.client.GrapherCanvas#stroke() stroke()} on the
 * underlying {@link org.bodytrack.client.GrapherCanvas Canvas} object: it
 * is up the the caller to call these methods.</p>
 */
public final class BoundedDrawingBox {
	private final GrapherCanvas canvas;
	private final double xMin;
	private final double yMin;
	private final double xMax;
	private final double yMax;

	private static final double TWO_PI = 2 * Math.PI;

	// General tolerance for double equality
	private static final double TOLERANCE = 1e-6;

	/**
	 * Creates a new BoundedDrawingBox.
	 *
	 * @param canvas
	 * 		the {@link org.bodytrack.client.GrapherCanvas Canvas} on which this
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
	public BoundedDrawingBox(GrapherCanvas canvas, double minX, double minY,
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
	 * Returns the Canvas used to construct this object
	 *
	 * @return
	 * 	The Canvas used to construct this object
	 */
	public GrapherCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Returns a {@link Vector2} representing the top left corner of the
	 * region this encloses
	 *
	 * @return
	 * 	A {@link Vector2} with components (minX, minY)
	 */
	public Vector2 getTopLeft() {
		return new Vector2(xMin, yMin);
	}

	/**
	 * Returns a {@link Vector2} representing the bottom right corner of the
	 * region this encloses
	 *
	 * @return
	 * 	A {@link Vector2} with components (maxX, maxY)
	 */
	public Vector2 getBottomRight() {
		return new Vector2(xMax, yMax);
	}

	/**
	 * Returns the width of the <code>BoundedDrawingBox</code>
	 */
	public double getWidth() {
		return xMax - xMin;
	}

	/**
	 * Returns the height of the <code>BoundedDrawingBox</code>
	 */
	public double getHeight() {
		return yMax - yMin;
	}

   /**
    * Draws a circle with the specified values and radius,
    * if and only if the center of the circle is in bounds.
    * This method merely draws the shape--it is up to the
    * caller to call {@link #strokeClippedPath} and/or
    * {@link #fillClippedPath()} afterwards.
    *
    * <p>This method checks whether the circle's center is in
    * bounds, not whether the whole circle is in bounds.</p>
    *
    * @param x
    * 		the X-value at the center of the circle
    * @param y
    * 		the Y-value at the center of the circle
    * @param radius
    * 		the radius of the circle
    */
   public void drawCircle(final double x,
                          final double y,
                          final double radius) {
      if (contains(x, y)) {
         final Context ctx1 = canvas.getContext();
         ctx1.moveTo(x + radius, y);
         ctx1.arc(x, y, radius, 0, TWO_PI, false);
      }
   }

   public void fillCircle(final double x, final double y, final double radius) {
		canvas.drawCircle(x, y, radius);

		// TODO: This and other BoundedDrawingBox methods should not stroke
		// or fill directly
		// Need to move the fill() call to the rendering strategy
		canvas.fill();
	}

   /**
    * Draws a square with the specified values and radius,
    * if and only if the center of the square is in bounds.
    * This method merely draws the shape--it is up to the
    * caller to call {@link #strokeClippedPath} and/or
    * {@link #fillClippedPath()} afterwards.
    *
    * <p>This method checks whether the square's center is in
    * bounds, not whether the whole square is in bounds.</p>
    *
    * @param x
    * 		the X-value at the center of the square
    * @param y
    * 		the Y-value at the center of the square
    * @param radius
    * 		the radius of the square (i.e. half the length of a side)
    */
   public void drawSquare(final double x,
                          final double y,
                          final double radius) {
      if (contains(x, y)) {
         final Context ctx = canvas.getContext();
         ctx.moveTo(x - radius, y - radius);
         ctx.rect(x - radius, y - radius, 2 * radius, 2 * radius);
      }
   }

	public void fillText(final String text, final double x, final double y) {
		// TODO: Same issue as fillCircle
		canvas.fillText(text, x, y);
	}

	public void setStrokeStyle(final Color color) {
		canvas.setStrokeStyle(color);
	}

	public void setFillStyle(final Color color) {
		canvas.setFillStyle(color);
	}

	/**
	 * Sets up clipping on the canvas, then calls beginPath
	 *
	 * <p>This replaces any other call to beginPath on the canvas.</p>
	 *
	 * @see #strokeClippedPath()
	 */
	public void beginClippedPath() {
		Context ctx = canvas.getContext();

		ctx.save();
		// Set up the path for clipping
		ctx.beginPath();
		ctx.rect(xMin, yMin, getWidth(), getHeight());
		ctx.closePath();
		ctx.clip();

		ctx.beginPath();
	}

	/**
	 * Draws a line segment from (x1, y1) to (x2, y2), or at least as
	 * much of the line segment as is in bounds
	 *
	 * <p>Note that this method will draw a partial line segment if part
	 * of the line segment is out of bounds.  More importantly, note
	 * that this method is <em>only</em> valid between a call to
	 * {@link #beginClippedPath()} and a call to
	 * {@link #strokeClippedPath()}.  Otherwise, undefined behavior
	 * may result.</p>
	 *
	 * @param x1
	 * 	The X-coordinate of the first point (out of the two connected
	 * 	by the line segment we are to draw)
	 * @param y1
	 * 	The Y-coordinate of the first point (out of the two connected
	 * 	by the line segment we are to draw)
	 * @param x2
	 * 	The X-coordinate of the second point (out of the two connected
	 * 	by the line segment we are to draw)
	 * @param y2
	 * 	The Y-coordinate of the second point (out of the two connected
	 * 	by the line segment we are to draw)
	 */
	public void drawLineSegment(double x1, double y1, double x2, double y2) {
		Context ctx = canvas.getContext();

		ctx.moveTo(x1, y1);
		ctx.lineTo(x2, y2);
	}

	/**
	 * Strokes the path and removes clipping from the canvas
	 *
	 * <p>This replaces any other call to stroke on the canvas.  Note
	 * that at no time may we ever have more calls to this method
	 * than to {@link #beginClippedPath()}.  If this is so,
	 * undefined behavior will result.</p>
	 *
	 * @see #beginClippedPath()
	 */
	public void strokeClippedPath() {
		Context ctx = canvas.getContext();

		ctx.stroke();
		ctx.restore();
	}

	/**
	 * Fills the path and removes clipping from the canvas
	 *
	 * <p>This replaces any other call to fill on the canvas.  Note
	 * that at no time may we ever have more calls to this method
	 * than to {@link #beginClippedPath()}.  If this is so,
	 * undefined behavior will result.</p>
	 *
	 * @see #beginClippedPath()
	 * @see #strokeClippedPath
	 */
	public void fillClippedPath() {
		Context ctx = canvas.getContext();

		ctx.fill();
		ctx.restore();
	}

	/**
	 * Draws a line segment from (x1, y1) to (x2, y2), or at least as
	 * much of the line segment as is in bounds
	 *
	 * <p>Note that this method will draw a partial line segment if part
	 * of the line segment is out of bounds.</p>
	 *
	 * <p>This is exactly the old version of
	 * {@link #drawLineSegment(double, double, double, double)}, from before
	 * the discovery of the clipping API in the browser's context objects.
	 * This is included mainly as a way to save the old code.  Note that this
	 * should <em>not</em> be called between calls to
	 * {@link #beginClippedPath()} and {@link #strokeClippedPath()}, but
	 * rather between regular beginPath and stroke calls.</p>
	 *
	 * @param x1
	 * 	The X-coordinate of the first point (out of the two connected
	 * 	by the line segment we are to draw)
	 * @param y1
	 * 	The Y-coordinate of the first point (out of the two connected
	 * 	by the line segment we are to draw)
	 * @param x2
	 * 	The X-coordinate of the second point (out of the two connected
	 * 	by the line segment we are to draw)
	 * @param y2
	 * 	The Y-coordinate of the second point (out of the two connected
	 * 	by the line segment we are to draw)
	 */
	public void drawLineSegmentSoftwareClip(double x1, double y1, double x2,
			double y2) {
		if (contains(x1, y1) && contains(x2, y2)) {
			// If everything is in bounds, we just draw the line
			getCanvas().drawLineSegment(x1, y1, x2, y2);
			return;
		}

		// Now we know at least one of the endpoints is out of bounds

		if (contains(x1, y1)) {
			// First point in bounds, second point out of bounds

			Vector2 secondEndpoint = getSecondEndpoint(x1, y1, x2, y2);

			// Now we have a pair (x1, y1), (x2, y2) where both points are
			// in bounds
			getCanvas().drawLineSegment(x1, y1,
					secondEndpoint.getX(), secondEndpoint.getY());

			return;
		}

		// Since a line segment is non-directional, we can reuse code
		// by drawing the same line the opposite direction, as long as
		// (x2, y2) is in bounds
		if (contains(x2, y2)) {
			drawLineSegment(x2, y2, x1, y1);
			return;
		}

		// Now we know both points are out of bounds

		drawLineOutOfBounds(x1, y1, x2, y2);
	}

	/**
	 * Calculates the correct (x2, y2) to use, given that (x1, y1) is
	 * in bounds and (x2, y2) is out of bounds
	 *
	 * @param x1
	 * 	The X-coordinate of the first point
	 * @param y1
	 * 	The Y-coordinate of the first point
	 * @param x2
	 * 	The X-coordinate of the second point
	 * @param y2
	 * 	The Y-coordinate of the second point
	 * @return
	 * 	A Vector2 with the new (x2, y2) to use, on the line between
	 * 	(x1, y1) and (x2, y2)
	 * @throws IllegalArgumentException
	 * 	Unless (x1, y1) is in bounds and (x2, y2) is out of bounds
	 */
	private Vector2 getSecondEndpoint(double x1, double y1, double x2,
			double y2) {
		if (! (contains(x1, y1) && ! contains(x2, y2)))
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
			} else
				return chooseIntercept(x1, y1, x2, y2);
		}

		return new Vector2(x2, y2);
	}

	/**
	 * Finds the correct second point to use when (x1, y1) is in bounds,
	 * (x2, y2) is out of bounds, and the line between them is neither
	 * horizontal nor vertical
	 *
	 * <p>This assumes that the aforementioned preconditions hold, and
	 * will not work properly otherwise.  Since this is a private method,
	 * we do not bother to check the preconditions and instead rely upon
	 * the calling methods (all of which are a part of this class) to
	 * check the preconditions.</p>
	 *
	 * @param x1
	 * 	The X-coordinate of the point that is in bounds
	 * @param y1
	 * 	The Y-coordinate of the point that is in bounds
	 * @param x2
	 * 	The X-coordinate of the point that is out of bounds
	 * @param y2
	 * 	The Y-coordinate of the point that is out of bounds
	 * @return
	 * 	The point at which the line between the two points
	 * 	crosses a boundary line
	 */
	private Vector2 chooseIntercept(double x1, double y1, double x2, double y2) {
		double slope = (y2 - y1) / (x2 - x1);

		Vector2 rightIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(xMax, 0.0), Double.POSITIVE_INFINITY);
		Vector2 topIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(0.0, yMax), 0.0);
		Vector2 leftIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(xMin, 0.0), Double.POSITIVE_INFINITY);
		Vector2 bottomIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(0.0, yMin), 0.0);

		// Now 4 cases on the sign of slope and the relative values of
		// x2 and x1.  All comments we make use directions from an abstract
		// Cartesian plane from geometry, not the way the lines are actually
		// drawn on the screen.

		if (slope > 0 && x2 > x1) {
			// Upward-sloping line, goes off right or top side
			return contains(rightIntercept) ? rightIntercept : topIntercept;
		}
		if (slope > 0) {
			// Upward-sloping line, goes off left or bottom side
			return contains(leftIntercept) ? leftIntercept : bottomIntercept;
		}
		if (x2 > x1) {
			// Downward-sloping line, goes off right or bottom side
			return contains(rightIntercept) ? rightIntercept : bottomIntercept;
		}

		// Downward-sloping line, goes off left or top side
		return contains(leftIntercept) ? leftIntercept : topIntercept;
	}

	/**
	 * Draws whichever line segment is necessary, given that points
	 * (x1, y1) and (x2, y2) are both out of bounds
	 *
	 * @param x1
	 * 	The X-coordinate of the first point
	 * @param y1
	 * 	The Y-coordinate of the first point
	 * @param x2
	 * 	The X-coordinate of the second point
	 * @param y2
	 * 	The Y-coordinate of the second point
	 * @throws IllegalArgumentException
	 * 	If either (x1, y1) or (x2, y2) is in bounds
	 */
	private void drawLineOutOfBounds(double x1, double y1, double x2, double y2) {
		if (contains(x1, y1) || contains(x2, y2))
			throw new IllegalArgumentException(
				"At least one point is in bounds");

		// See if we really should draw the line
		if (! lineCouldCrossBounds(x1, y1, x2, y2))
			return;

		double slope = (y2 - y1) / (x2 - x1);		
		if (doubleEquals(x1, x2) || Double.isInfinite(slope)) {
			// Vertical line
			getCanvas().drawLineSegment(x1, yMin, x1, yMax);
			return;
		}

		if (doubleEquals(slope, 0.0)) {
			// Optimization: don't do anything extra if the slope is 0

			getCanvas().drawLineSegment(xMin, y1, xMax, y2);
			return;
		}

		Vector2 topIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(0.0, yMax), 0.0);
		Vector2 rightIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(xMax, 0.0), Double.POSITIVE_INFINITY);
		Vector2 bottomIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(0.0, yMin), 0.0);
		Vector2 leftIntercept = getIntersection(
				new Vector2(x1, y1), slope,
				new Vector2(xMin, 0.0), Double.POSITIVE_INFINITY);

		List<Vector2> inBoundsIntercepts = getInBoundsPoints(
				topIntercept, rightIntercept, leftIntercept,
				bottomIntercept);

		// Don't draw a line unless there are two points to draw
		if (inBoundsIntercepts.size() < 2)
			return;
		else if (inBoundsIntercepts.size() == 3) {
			// The line goes through a corner
			// We eliminate one of the two points which are
			// closest together

			double distSq01 = inBoundsIntercepts.get(0).distanceSquared(
					inBoundsIntercepts.get(1));
			double distSq02 = inBoundsIntercepts.get(0).distanceSquared(
					inBoundsIntercepts.get(2));
			double distSq12 = inBoundsIntercepts.get(1).distanceSquared(
					inBoundsIntercepts.get(2));

			// Always remove the unique point that is involved in
			// the two smallest of the three distances
			if (distSq01 < distSq02) {
				if (distSq02 < distSq12)
					inBoundsIntercepts.remove(0);
				else
					inBoundsIntercepts.remove(1);
			} else {
				if (distSq02 < distSq12)
					inBoundsIntercepts.remove(2);
				else
					inBoundsIntercepts.remove(1);
			}
		} else if (inBoundsIntercepts.size() == 4) {
			// The line goes through both corners
			// We know that all four of our intercepts are in bounds,
			// so we can use them directly.  The top and bottom
			// intercepts are guaranteed to be in bounds

			inBoundsIntercepts.clear();
			inBoundsIntercepts.add(topIntercept);
			inBoundsIntercepts.add(bottomIntercept);
		}

		// Now we have the line to draw across the box
		getCanvas().drawLineSegment(
				inBoundsIntercepts.get(0),
				inBoundsIntercepts.get(1));
	}

	/**
	 * Returns <code>true</code> if the line from (x1, y1) to (x2, y2)
	 * could cross through this bounding box
	 *
	 * <p>In practical terms, this means returning <tt>false</tt> if
	 * both points are on the same side of one of the boundaries, and
	 * <code>true</code> otherwise.  This method is just intended as a
	 * very quick way to rule out a lot of possible lines without having
	 * to do more complicated (and definitive) checks.</p>
	 *
	 * @param x1
	 * 	The X-coordinate of the first point
	 * @param y1
	 * 	The Y-coordinate of the first point
	 * @param x2
	 * 	The X-coordinate of the second point
	 * @param y2
	 * 	The Y-coordinate of the second point
	 * @return
	 * 	<code>true</code> if the line from (x1, y1) to
	 * 	(x2, y2) could cross through this bounding box
	 */
	private boolean lineCouldCrossBounds(double x1, double y1, double x2,
			double y2) {
		if (x1 < xMin && x2 < xMin)
			return false;

		if (y1 < yMin && y2 < yMin)
			return false;

		if (x1 > xMax && x2 > xMax)
			return false;

		if (y1 > yMax && y2 > yMax)
			return false;

		return false;
	}

	/**
	 * Finds the intercept between the two lines
	 *
	 * <p>If the two lines are parallel, returns <code>null</code> to
	 * signal parallel lines.  A calling method should be prepared
	 * for a <code>null</code> return, for this reason.</p>
	 *
	 * <p>Note that this method does not take into account whether or
	 * not these lines are in bounds, or if the intercept point is in
	 * bounds.</p>
	 *
	 * @param point1
	 * 	Any point on the first line
	 * @param slope1
	 * 	The slope of the first line.  May be Double.POSITIVE_INFINITY
	 * 	or Double.NEGATIVE_INFINITY to signal a vertical line
	 * @param point2
	 * 	Any point on the second line
	 * @param slope2
	 * 	The slope of the second line.  May be Double.POSITIVE_INFINITY
	 * 	or Double.NEGATIVE_INFINITY to signal a vertical line
	 * @return
	 * 	The unique point of intersection between the two lines, or
	 * 	<code>null</code> if the lines are parallel
	 */
	private Vector2 getIntersection(Vector2 point1, double slope1,
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
			return getIntersection(point2, slope2, point1, slope1);

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
	 * Returns <code>true</code> if and only if the point (x, y) is
	 * contained in the rectangle determined by (minX, minY) and
	 * (maxX, maxY)
	 *
	 * @param x
	 * 	The X-value of the point to check
	 * @param y
	 * 	The Y-value of the point to check
	 * @return
	 * 	<code>true</code> if and only if (x, y) is in bounds
	 */
	public boolean contains(double x, double y) {
		return x >= xMin && x <= xMax && y >= yMin && y <= yMax;
	}

	/**
	 * Exactly the same as calling
	 * {@code contains(point.getX(), point.getY())}
	 *
	 * @param point
	 * 	The (x, y) point to check for bounds
	 * @return
	 * 	<code>true</code> if and only if (x, y) is in bounds.  This returns
	 * 	<code>false</code> if point is <code>null</code>
	 */
	public boolean contains(Vector2 point) {
		if (point == null)
			return false;

		return contains(point.getX(), point.getY());
	}

	/**
	 * Returns <code>true</code> iff any part of the rectangle
	 * described by (x, y, width, height) is part of this drawing box
	 *
	 * @param x
	 * 	The X-coordinate of the top left corner of the rectangle
	 * @param y
	 * 	The Y-coordinate of the top left corner of the rectangle
	 * @param width
	 * 	The width of the rectangle
	 * @param height
	 * 	The width of the rectangle
	 * @return
	 * 	<code>true</code> iff any part of the rectangle described
	 * 	by (x, y, width, height) is part of this drawing box
	 */
	public boolean containsRectanglePart(final double x,
			final double y,
			final double width,
			final double height) {
		return (x + width >= xMin)
			&& (x < xMax)
			&& (y + height >= yMin)
			&& (y < yMax);
	}

	/**
	 * Tells if a and b are equal, within {@link #TOLERANCE}
	 *
	 * <p>Note that this does not really define an equality
	 * relationship, since it is not transitive.  It is possible
	 * to define doubles a, b, and c such that
	 * {@code doubleEquals(a, b)} and {@code doubleEquals(b, c)} are
	 * both true, but {@code doubleEquals(a, c)} is false.  Thus,
	 * this method should be used with care.</p>
	 *
	 * @param a
	 * 	Some double value to be compared
	 * @param b
	 * 	Some other double value to be compared
	 * @return
	 * 	<code>true</code> if and only if a and b are within
	 * 	{@link #TOLERANCE} of each other
	 * @see #TOLERANCE
	 */
	private static boolean doubleEquals(double a, double b) {
		return Math.abs((a - b) / a) < TOLERANCE;
	}

	/**
	 * Performs a filter operation on points, keeping only those points
	 * which are in bounds
	 *
	 * @param points
	 * 	The point on which we will filter
	 * @return
	 * 	A list of points that are in bounds.  The returned list includes
	 * 	all in-bounds points (as determined by {@link #contains(Vector2)}),
	 * 	in the same order in which they were passed to this method
	 */
	private List<Vector2> getInBoundsPoints(Vector2... points) {
		List<Vector2> result = new ArrayList<Vector2>();

		for (Vector2 point: points)
			if (contains(point))
				result.add(point);

		return result;
	}
}
