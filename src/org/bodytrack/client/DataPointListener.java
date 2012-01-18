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

	public void handleDataPointUpdate(final PlottablePoint point,
			final TriggerAction action) {
		handleDataPointUpdate(point, action, null);
	}

	/**
	 * Fires the callback with the specified information
	 *
	 * @param point
	 * 	The point (may be <code>null</code>) about which the page needs to
	 * 	be informed
	 * @param action
	 * 	The action that triggered this call
	 * @param info
	 * 	An JavaScript object that, if not null, gets passed in as part of
	 * 	the target argument to the callback
	 * @throws NullPointerException
	 * 	If action is <code>null</code>
	 */
	public void handleDataPointUpdate(final PlottablePoint point,
			final TriggerAction action,
			final JavaScriptObject info) {
		if (action == null)
			throw new NullPointerException();

		if (point == null)
			handleNoDataPointUpdate(action.getActionName(), info);
		else
			handleDataPointUpdate(point.getDate(),
					point.getValue(),
					point.getDateAsString(),
					point.getValueAsString(),
					point.getComment(),
					action.getActionName(),
					info);
	}

	private native void handleDataPointUpdate(final double date,
			final double value,
			final String dateStr,
			final String valueStr,
			final String comment,
			final String actionName,
			final JavaScriptObject info) /*-{
		var pointObj = {
			"date": date,
			"value": value,
			"dateString": dateStr,
			"valueString": valueStr,
			"comment": comment
		};

		if (info == null) {
			this(pointObj,
				{
					"actionName": actionName
				});
		} else {
			this(pointObj,
				{
					"actionName": actionName,
					"info": info
				});
		}
	}-*/;

	private native void handleNoDataPointUpdate(final String actionName,
			final JavaScriptObject info) /*-{
		if (info == null) {
			this(null,
			{
				"actionName": actionName
			});
		} else {
			this(null,
			{
				"actionName": actionName,
				"info": info
			});
		}
	}-*/;
}
