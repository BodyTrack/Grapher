package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A set of methods to wrap native JavaScript objects and make them easier to
 * work with in Java.
 */
public final class Dynamic extends JavaScriptObject {
	protected Dynamic() { }

	public native <T> T get(String fieldName) /*-{
		return this[fieldName];
	}-*/;

	public native void set(String fieldName, Object value) /*-{
		this[fieldName] = value;
	}-*/;

	public native <T> T call(String functionName, Object... args) /*-{
		return this[functionName].call(this, args);
	}-*/;
}
