package org.bodytrack.client;

import org.bodytrack.client.InstanceController.InstanceProducer;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Wrapper for common operations on a GWT
 * {@link com.google.gwt.canvas.client.Canvas Canvas} object.
 *
 * <p>This class is instance-controlled for efficiency: under this
 * system, only one DirectShapeRenderer is created per underlying canvas.</p>
 */
public final class GrapherCanvas {

	/**
	 * The default color, which classes should set as the stroke color
	 * if wishing to &quot;clean up after themselves&quot; when done
	 * changing colors and drawing.
	 */
	public static final CssColor DEFAULT_COLOR = ColorUtils.BLACK;

	/**
	 * The default alpha value, which classes should <em>always</em>
	 * set as the alpha after changing alpha on a Canvas.
	 */
	public static final double DEFAULT_ALPHA = 1.0;

	private final Canvas surface;
	private final Element nativeCanvasElement;

	private static InstanceController<Canvas, GrapherCanvas> instances;

	static {
		instances = new InstanceController<Canvas, GrapherCanvas>(
			new InstanceProducer<Canvas, GrapherCanvas>() {
				@Override
				public GrapherCanvas newInstance(Canvas param) {
					// The constructor will throw the NullPointerException
					// if param is null
					return new GrapherCanvas(param);
				}
			});
	}

	/**
	 * Creates a new <tt>Canvas</tt> that draws on the specified surface.
	 *
	 * @param s
	 * 	The surface on which the new {@link GrapherCanvas} will draw
	 * @throws NullPointerException
	 * 	If s is <code>null</code>
	 * @throws IllegalArgumentException
	 * 	If s has no native HTML canvas element inside its DOM tree
	 */
	private GrapherCanvas(Canvas s) {
		if (s == null)
			throw new NullPointerException("Can't draw on a null surface");

		surface = s;
		nativeCanvasElement = findCanvasElement(surface.getElement());
		if (nativeCanvasElement == null)
			throw new IllegalArgumentException(
				"No native canvas element available");
	}

	/**
	 * Finds the first canvas element in the DOM tree rooted at e.
	 *
	 * @param e
	 * 	The root of the search tree
	 * @return
	 * 	The first canvas element found in the subtree rooted
	 * 	at e, or <code>null</code> if there is no canvas element
	 * 	in the subtree rooted at e
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
	 * 	The {@link gwt.g2d.client.graphics.Surface Surface}
	 * 	on which the new Canvas will draw
	 * @return
	 * 	A Canvas with a pointer to s and to an associated
	 * 	{@link gwt.g2d.client.graphics.DirectShapeRenderer
	 * 	DirectShapeRenderer}
	 */
	public static GrapherCanvas buildCanvas(Canvas s) {
		return instances.newInstance(s);
	}

	/**
	 * Returns the native HTML canvas element found in the DOM tree
	 * rooted at the surface passed in to this object's constructor.
	 *
	 * @return
	 * 	The native canvas element
	 */
	public Element getNativeCanvasElement() {
		return nativeCanvasElement;
	}

	// --------------------------------------------------------------
	// Wrappers for Surface methods
	// --------------------------------------------------------------

	/**
	 * Equivalent to <code>getSurface().getWidth()</code>
	 *
	 * @return
	 * 	The width of this canvas
	 */
	public int getWidth() {
		return surface.getCoordinateSpaceWidth();
	}

	/**
	 * Equivalent to <code>getSurface().getHeight()</code>
	 *
	 * @return
	 * 	The height of this canvas
	 */
	public int getHeight() {
		return surface.getCoordinateSpaceHeight();
	}

	/**
	 * Equivalent to <code>getSurface().setStrokeStyle(color)</code>
	 *
	 * @param color
	 * 	The color that will be used to stroke future drawing on the surface
	 * @return
	 * 	The Surface used for the setStrokeStyle call
	 */
	public Canvas setStrokeStyle(FillStrokeStyle strokeStyle) {
		surface.getContext2d().setStrokeStyle(strokeStyle);
		return surface;
	}
	
	public FillStrokeStyle getStrokeStyle(){
		return surface.getContext2d().getStrokeStyle();
	}

	/**
	 * Equivalent to <code>getSurface().setFillStyle(color)</code>
	 *
	 * @param color
	 * 	The color that will be used to fill future drawing on the surface
	 * @return
	 * 	The Surface used for the setFillStyle call
	 */
	public Canvas setFillStyle(FillStrokeStyle fillStyle) {
		surface.getContext2d().setFillStyle(fillStyle);
		return surface;
	}
	
	public FillStrokeStyle getFillStyle(){
		return surface.getContext2d().getFillStyle();
	}

	/**
	 * Equivalent to <code>getSurface().getLineWidth()</code>
	 */
	public double getLineWidth() {
		return surface.getContext2d().getLineWidth();
	}

	/**
	 * Equivalent to <code>getSurface().setLineWidth(width)</code>
	 *
	 * @param width
	 * 	The new width for lines on the surface
	 * @return
	 * 	The Surface used for the setLineWidth call
	 */
	public Canvas setLineWidth(double width) {
		surface.getContext2d().setLineWidth(width);
		return surface;
	}

	/**
	 * Equivalent to <code>getSurface().getGlobalAlpha()</code>
	 */
	public double getGlobalAlpha() {
		return surface.getContext2d().getGlobalAlpha();
	}

	/**
	 * Equivalent to <code>getSurface().setGlobalAlpha(alpha)</code>
	 *
	 * @param alpha
	 * 	The new alpha for the canvas
	 * @return
	 * 	The Surface used for the setGlobalAlpha call
	 */
	public Canvas setGlobalAlpha(double alpha) {
		surface.getContext2d().setGlobalAlpha(alpha);
		return surface;
	}

	/**
	 * Equivalent to <code>getSurface().getTextAlign()</code>
	 *
	 * @return
	 * 	The text alignment for this canvas
	 */
	public TextAlign getTextAlign() {
		return TextAlign.valueOf(surface.getContext2d().getTextAlign().toUpperCase());
	}

	/**
	 * Equivalent to <code>getSurface().setTextAlign(textAlign)</code>
	 *
	 * @param textAlign
	 * 	The new alignment to use for text on the canvas
	 * @return
	 * 	The Surface used for the setTextAlign call
	 */
	public Canvas setTextAlign(TextAlign textAlign) {
		surface.getContext2d().setTextAlign(textAlign);
		return surface;
	}

	/**
	 * Equivalent to <code>getSurface().getTextBaseline()</code>
	 */
	public TextBaseline getTextBaseline() {
		return TextBaseline.valueOf(surface.getContext2d().getTextBaseline().toUpperCase());
	}
	
	public TextMetrics measureText(String text){
		return surface.getContext2d().measureText(text);
	}

	/**
	 * Equivalent to <code>getSurface().setTextBaseline(textBaseline)</code>
	 *
	 * @param textBaseline
	 * 	The new baseline to use for text on the canvas
	 * @return
	 * 	The Surface used for the setTextBaseline call
	 */
	public Canvas setTextBaseline(TextBaseline textBaseline) {
		surface.getContext2d().setTextBaseline(textBaseline);
		return surface;
	}

	/**
	 * Equivalent to <code>getSurface().clear()</code>
	 */
	public Canvas clear() {
		surface.getContext2d().clearRect(0, 0, surface.getCoordinateSpaceWidth(), surface.getCoordinateSpaceHeight());
		return surface;
	}

	public Canvas save() {
		surface.getContext2d().save();
		return surface;
	}

	public Canvas restore() {
		surface.getContext2d().restore();
		return surface;
	}

	public Canvas strokeRectangle(double x,
			double y, double width, double height) {
		surface.getContext2d().strokeRect(x, y, width, height);
		return surface;
	}

	public Canvas fillRectangle(double x,
			double y, double width, double height) {
		surface.getContext2d().fillRect(x, y, width, height);
		return surface;
	}

	public GrapherCanvas beginPath() {
		surface.getContext2d().beginPath();
		return this;
	}

	public Canvas stroke() {
		surface.getContext2d().stroke();
		return surface;
	}

	public Canvas fill() {
		surface.getContext2d().fill();
		return surface;
	}

	public Canvas drawLineSegment(Vector2 start, Vector2 end) {
		return drawLineSegment(start.getX(),start.getY(),end.getX(),end.getY());
	}
	
	public Canvas drawLineSegment(double x1, double y1, double x2, double y2) {
		surface.getContext2d().moveTo(x1, y1);
		surface.getContext2d().lineTo(x2, y2);
		return surface;
	}

	public Canvas fillText(String label, Vector2 v) {
		return fillText(label,v.getX(),v.getY());	
	}
	
	public Canvas fillText(String text, double x, double y) {
		surface.getContext2d().fillText(text, x, y);
		return surface;
	}

	public void drawCircle(double x, double y, double radius) {
		arc(x,y,radius,0,Math.PI * 2,true);
	}

	public void setSize(int width, int height) {
		final CanvasElement nativeElement = surface.getCanvasElement();
		nativeElement.setWidth(width);
		nativeElement.setHeight(height);
	}

	public GrapherCanvas moveTo(double x, double y) {
		surface.getContext2d().moveTo(x,y);
		return this;
	}
	
	public GrapherCanvas moveTo(Vector2 v){
		return moveTo(v.getX(),v.getY());
	}

	public GrapherCanvas lineTo(double x, double y) {
		surface.getContext2d().lineTo(x,y);
		return this;
	}
	
	public GrapherCanvas lineTo(Vector2 v){
		return lineTo(v.getX(), v.getY());
	}

	public void arc(double x, double y, double radius, double startAngle, double endAngle,
			boolean anticlockwise) {
		surface.getContext2d().arc(x,y,radius,startAngle,endAngle,anticlockwise);
	}

	public void rect(double x, double y, double w, double h) {
		surface.getContext2d().rect(x, y, w, h);
	}

	public void closePath() {
		surface.getContext2d().closePath();
	}

	public void clip() {
		surface.getContext2d().clip();
	}

	public String getFont() {
		return surface.getContext2d().getFont();
	}

	public void setFont(String font) {
		surface.getContext2d().setFont(font);
	}
	
	public void drawImage(ImageElement image, double x, double y){
		surface.getContext2d().drawImage(image,x,y);
	}
	
	public void drawImage(ImageElement image, double x, double y, double w, double h){
		surface.getContext2d().drawImage(image,x,y,w,h);
	}
}
