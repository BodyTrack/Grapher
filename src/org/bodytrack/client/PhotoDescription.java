package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Represents a single photo and all its parameters.
 *
 * <p>An overlay type for a generic photo description.  This data
 * type can be loaded from JSON using the
 * {@link #buildDescription(String)} method.</p>
 *
 * <p>Note that this class is final because GWT requires that all method
 * calls on an overlay type be resolved at compile time.  We had the choice
 * between making the class final and making all public instance methods
 * final.  We opted for the former option.</p>
 */
public final class PhotoDescription extends JavaScriptObject {
	// Overlay types always have protected, zero-arg constructors
	// with empty bodies
	protected PhotoDescription() { }

	/**
	 * Returns a single PhotoDescription, given a JSON string containing the data.
	 *
	 * <h2 style="color: red">WARNING:</h2>
	 *
	 * <p>Note that the JSON string is assumed to be trusted.  This method uses
	 * the JavaScript eval() function, <strong>which could allow arbitrary
	 * code to execute on a user's browser</strong> if this string was not
	 * completely generated by BodyTrack servers over a secure connection.
	 * This could allow an attacker to view all of a user's data, simply by
	 * filling in code to request all valid data tiles and then to send
	 * those tiles to the attacker's machine.  As such, data from insecure
	 * connections, and especially from cross-site requests, should not be
	 * passed in as the data parameter here.</p>
	 *
	 * @param json
	 * 		a JSON string containing data for a single photo
	 * @return
	 * 		a PhotoDescription object with the same data as is found in json
	 */
	public static native PhotoDescription buildDescription(String json) /*-{
		eval("var desc = " + json);
		return desc;
	}-*/;

	/**
	 * Returns the value of the &quot;id&quot; field on this photo
	 * description, or a negative number if the field is not present.
	 *
	 * @return
	 * 		the value of the &quot;id&quot; field on this photo
	 * 		description, or a negative number if the field is not present
	 */
	public native int getId() /*-{
		if (!this.id)
			return -1;

		return this.id;
	}-*/;

	/**
	 * Returns the value of the &quot;begin_d&quot; field on this
	 * photo description, or a negative number less than or equal to
	 * the value -1e300 if this field is not present.
	 *
	 * @return
	 * 		the value of the &quot;begin_d&quot; field on this
	 * 		photo description, or a negative number less than or equal to
	 * 		the value -1e300 if this field is not present
	 */
	public native double getBeginDate() /*-{
		if (!this.begin_d)
			return -1e308;

		return this.begin_d;
	}-*/;

	/**
	 * Returns the count field from this photo, or 1 if there is
	 * no count tag.
	 *
	 * @return
	 * 		the count field from this photo if such a field exists,
	 * 		or 1 otherwise
	 * @return
	 */
	public native int getCount() /*-{
		if (!this.count) {
			return 1;
		}

		return this.count;
	}-*/;

	/**
	 * Returns the tags for this <tt>PhotoDescription</tt>.
	 *
	 * @return
	 * 		the tags for this <tt>PhotoDescription</tt>
	 */
	public final native JsArrayString getTags() /*-{
		return this.tags;
	}-*/;

	/**
	 * Returns <tt>true</tt> if and only if this photo is tagged with
	 * the &quot;nsfw&quot; tag as true.
	 *
	 * @return
	 * 		the value of the &quot;nsfw&quot; flag, or <tt>false</tt>
	 * 		if that flag is not present
	 */
	public native boolean isNsfw() /*-{
		return !!(this.nsfw);
	}-*/;
}
