package org.bodytrack.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * A widget that allows the user to switch between, add,
 * and modify views.
 */
public class ViewSwitchWidget extends HorizontalPanel {
	private final ChannelManager channels;
	private final ViewSwitchClickHandler clickHandler;
	private final Button saveView;
	private final Button restoreView;

	/**
	 * Creates a new <tt>ViewSwitchWidget</tt>.
	 *
	 * @param mgr
	 * 		the {@link org.bodytrack.client.ChannelManager ChannelManager}
	 * 		that keeps the current set of channels
	 * @throws NullPointerException
	 * 		if mgr is <tt>null</tt>
	 */
	public ViewSwitchWidget(ChannelManager mgr) {
		if (mgr == null)
			throw new NullPointerException(
				"Cannot use null ChannelManager");

		channels = mgr;
		clickHandler = new ViewSwitchClickHandler();
		saveView = new Button("Save", clickHandler);
		restoreView = new Button("Restore", clickHandler);
	}

	/**
	 * The class that handles clicks for the saveView and restoreView
	 * buttons.
	 */
	private class ViewSwitchClickHandler implements ClickHandler {
		/**
		 * Handles clicks for the saveView and restoreView buttons.
		 *
		 * <p>If event is <tt>null</tt> or came from a button that
		 * is not part of this widget, does nothing.  Otherwise,
		 * opens the correct popup to save or restore the current
		 * view.</p>
		 *
		 * @param event
		 * 		the click event containing information about the
		 * 		user's click on the saveView or restoreView button
		 */
		@Override
		public void onClick(ClickEvent event) {
			if (event == null)
				return;

			Object sourceObject = event.getSource();
			if (sourceObject == saveView) {
				// TODO: Some save action here
			} else if (sourceObject == restoreView) {
				// TODO: Some restore action here
			}
		}
	}
}
