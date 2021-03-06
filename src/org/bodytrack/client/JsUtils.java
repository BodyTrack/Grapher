package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public final class JsUtils {
	private JsUtils() {
		throw new UnsupportedOperationException();
	}

	public static boolean contains(final JsArrayString lst, final String test) {
		if (lst == null)
			throw new NullPointerException();
		if (test == null)
			return containsNull(lst);

		for (int i = 0; i < lst.length(); i++) {
			if (test.equals(lst.get(i)))
				return true;
		}

		return false;
	}

	public static boolean containsNull(final JsArrayString lst) {
		if (lst == null)
			throw new NullPointerException();

		for (int i = 0; i < lst.length(); i++) {
			if (lst.get(i) == null)
				return true;
		}

		return false;
	}

	public static native JavaScriptObject callFunction(final JavaScriptObject fun,
			final JavaScriptObject... args) /*-{
		fun.apply(null, args);
	}-*/;

	public static native JavaScriptObject convertBoolean(final boolean b) /*-{
		if (b) {
			return true;
		}
		return false;
	}-*/;
}
