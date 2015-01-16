package com.googlecode.mgwt.ui.client.util.impl;

import com.google.gwt.dom.client.Element;

public interface CssUtilImpl {

	public void translate(Element el, int x, int y);

	public void setDelay(Element el, int milliseconds);

	public void setOpacity(Element el, double opacity);

	public void setDuration(Element el, int time);

	public void rotate(Element element, int degree);

	public boolean hasTransform();

	public boolean hasTransistionEndEvent();

	public boolean has3d();

	public String getTransformProperty();

	public int[] getPositionFromTransForm(Element element);

	public int getTopPositionFromCssPosition(Element element);

	public int getLeftPositionFromCssPosition(Element element);

	public void resetTransform(Element element);

	public void setTransistionProperty(Element element, String string);

	public void setTransFormOrigin(Element element, int x, int y);

	public void setTransistionTimingFunction(Element element, String string);

	public void setTranslateAndZoom(Element element, int x, int y, double scale);

  public void translatePercent(Element el, double x, double y);

}
