package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

public final class Dynamic extends JavaScriptObject {
	protected Dynamic() { }

	public native <T> T get(String fieldName) /*-{
		return this[fieldName];
	}-*/;

	public native <T> T call(String functionName, Object... args) /*-{
		return this[functionName].call(this, args);
	}-*/;
}
