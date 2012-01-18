package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DataPointListener extends JavaScriptObject {
	public static enum TriggerAction {
		HIGHLIGHT("highlight"),
		CLICK("click"),
		MOUSEOVER("mouseover");

		private final String actionName;

		private TriggerAction(String actionName) {
			this.actionName = actionName;
		}

		public String getActionName() {
			return actionName;
		}
	}

	protected DataPointListener() { }

	/**
	 * The dispatcher helper function that determines which native call to make
	 *
	 * @param point
	 * 	The point (may be <code>null</code>) about which the page needs to
	 * 	be informed
	 * @param action
	 * 	The non-<code>null</code> action that triggered this call
	 * @throws NullPointerException
	 * 	If action is <code>null</code>
	 */
	public void handleDataPointUpdate(final PlottablePoint point,
			final TriggerAction action) {
		if (action == null)
			throw new NullPointerException();

		if (point == null)
			handleNoDataPointUpdate(action.getActionName());
		else
			handleDataPointUpdate(point.getDate(),
					point.getValue(),
					point.getDateAsString(),
					point.getValueAsString(),
					point.getComment(),
					action.getActionName());
	}

	private native void handleDataPointUpdate(final double date,
			final double value,
			final String dateStr,
			final String valueStr,
			final String comment,
			final String actionName) /*-{
		this({
				"date": date,
				"value": value,
				"dateString": dateStr,
				"valueString": valueStr,
				"comment": comment
			},
			{
				"actionName": actionName
			});
	}-*/;

	private native void handleNoDataPointUpdate(final String actionName) /*-{
		this(null,
			{
				"actionName": actionName
			});
	}-*/;
}
