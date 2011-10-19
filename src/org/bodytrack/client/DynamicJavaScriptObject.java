package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

public final class DynamicJavaScriptObject extends JavaScriptObject {
	protected DynamicJavaScriptObject() { }

	public native <T> T getField(String fieldName) /*-{
		return this[fieldName];
	}-*/;
}
