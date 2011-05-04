package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import org.bodytrack.client.Continuation.EmptyContinuation;
import org.bodytrack.client.WebDownloader.DownloadAlertable;
import org.bodytrack.client.WebDownloader.DownloadSuccessAlertable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/**
 * A widget that allows the user to switch between, add,
 * and modify views.  This is usually shown above the grapher itself,
 * and handles buttons and popup windows so that the user interface
 * for views is all out of this class.
 */
public class ViewSwitchWidget extends HorizontalPanel {
	/**
	 * The ID attribute of this element.  Note that this only
	 * works properly if there is only one <tt>ViewSwitchWidget</tt>
	 * per page.
	 */
	private static final String WIDGET_ID = "viewSwitchWidget";

	/**
	 * The name of the CSS class on the popups that come from
	 * this widget.
	 */
	private static final String POPUP_CLASS_NAME = "savedViewPopup";

	/**
	 * The CSS class name that is added to the anchor for the current view
	 */
	private static final String CURRENT_VIEW_CLASS = "currentViewName";

	/**
	 * The maximum number of view names that will be visible to
	 * the user at any time, in any popup from this widget.
	 */
	private static final int MAX_VISIBLE_VIEW_NAMES = 10;

	private final GraphWidget graphWidget;
	private final int userId;
	private final ChannelManager channels;
	private String currentView;

	private final ViewSwitchClickHandler clickHandler;
	private final PushButton saveView;
	private final PushButton restoreView;

	/**
	 * Creates a new <tt>ViewSwitchWidget</tt>.
	 *
	 * @param graphWidget
	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
	 * 		which the channels are drawn
	 * @param userId
	 * 		the ID of the current user
	 * @param mgr
	 * 		the {@link org.bodytrack.client.ChannelManager ChannelManager}
	 * 		that keeps the current set of channels
	 * @throws NullPointerException
	 * 		if widget or mgr is <tt>null</tt>
	 */
	public ViewSwitchWidget(GraphWidget graphWidget, int userId,
			ChannelManager mgr) {
		if (graphWidget == null || mgr == null)
			throw new NullPointerException(
				"Cannot use null widget or ChannelManager");

		this.graphWidget = graphWidget;
		this.userId = userId;
		channels = mgr;
		currentView = null;

		clickHandler = new ViewSwitchClickHandler();
		saveView = new PushButton("Save", clickHandler);
		restoreView = new PushButton("Restore", clickHandler);

		getElement().setId(WIDGET_ID);

		add(saveView);
		add(restoreView);
	}

	/**
	 * Returns the name of the current view, or <tt>null</tt> if there
	 * is no current view.
	 *
	 * @return
	 * 		the name of the current view (<tt>null</tt> represents no view)
	 */
	public String getCurrentViewName() {
		return currentView;
	}

	// TODO: Document
	ChannelManager getChannelManager() {
		return channels;
	}

	/**
	 * Generates the current view as a <tt>SavableView</tt>.
	 *
	 * @return
	 * 		the current view as a <tt>SavableView</tt>
	 */
	public SavableView getCurrentSavableView() {
		return SavableView.buildView(channels, currentView);
	}

	/**
	 * Navigates to the view specified by viewName.
	 *
	 * <p>It is expected that viewName references a valid view.  If this
	 * is not the case, this method does absolutely nothing.</p>
	 *
	 * @param viewName
	 * 		the name of the view to use to replace the current view
	 * @param succ
	 * 		the success continuation that is called with a parameter
	 * 		of <tt>null</tt> after the view is successfully replaced.
	 * 		If this parameter is <tt>null</tt>, it is ignored
	 * @throws NullPointerException
	 * 		if viewName is <tt>null</tt>
	 */
	public void navigateToView(String viewName, Continuation<Object> succ) {
		if (viewName == null)
			throw new NullPointerException("Can't navigate to null view");

		new ViewRestorePopup().replaceCurrentView(viewName,
			ViewRestorePopup.ViewClickHandler.FULL_VIEW, succ);
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
			if (sourceObject == saveView)
				showPopup(new ViewSavePopup(), saveView);
			else if (sourceObject == restoreView)
				showPopup(new ViewRestorePopup(), restoreView);
		}

		/**
		 * Utility method to show a <tt>PopupPanel</tt> aligned such that
		 * the top left of the popup is at the bottom left of the button.
		 *
		 * @param popup
		 * 		the non-<tt>null</tt> panel to show aligned with alignButton
		 * @param alignButton
		 * 		the button to use to align popup
		 */
		private void showPopup(final PopupPanel popup,
				final PushButton alignButton) {
			popup.setPopupPositionAndShow(new PositionCallback() {
				@Override
				public void setPosition(int offsetWidth,
						int offsetHeight) {
					int left = alignButton.getAbsoluteLeft();
					int top = alignButton.getAbsoluteTop()
						+ alignButton.getOffsetHeight();
					popup.setPopupPosition(left, top);
				}
			});
		}
	}

	/**
	 * A popup window that lets the user save the current view to
	 * the server.
	 *
	 * <p>This window is designed to pop up once, save the current
	 * view, and then be garbage collected.  If the user clicks the
	 * save button multiple times, a new <tt>ViewSavePopup</tt>
	 * should be created every time.</p>
	 */
	private class ViewSavePopup extends PopupPanel {
		private final List<String> viewNames;

		private final VerticalPanel content;
		// We know that the top panel of content will be a HorizontalPanel
		// containing the saveName box on the left and the save button
		// on the right.  The bottom panel of content will be devoted
		// to viewNamesList

		private final TextBox saveName;
		private final PushButton save;
		private final ListBox viewNamesControl;

		/**
		 * Creates a new <tt>ViewSavePopup</tt> object but does not show it.
		 */
		public ViewSavePopup() {
			super(true, true);

			viewNames = new ArrayList<String>();
			saveName = new TextBox();
			save = new PushButton("Save", new ClickHandler() {
				/**
				 * Called whenever save is clicked.
				 */
				@Override
				public void onClick(ClickEvent event) {
					if (event.getSource() != save)
						// We should never enter this
						return;

					String viewName = saveName.getText();
					if (viewName.isEmpty())
						return;
					// TODO: Enforce rules on valid view names

					SavableView view = SavableView.buildView(channels,
						viewName);

					// Send data to the server
					RequestBuilder builder = new RequestBuilder(
						RequestBuilder.POST, getViewWriteUrl());
					builder.setHeader("Content-type",
						"application/x-www-form-urlencoded");
					boolean shouldChangeCurrentView = true;
					try {
						builder.sendRequest(URL.encode("name=" +
							viewName + "&data=" +
							new JSONObject(view).toString()),
							new EmptyRequestCallback());
						// TODO: Check the return value
					} catch (RequestException e) {
						shouldChangeCurrentView = false;
					}

					// Update the current view name, as long as there
					// is no error
					if (shouldChangeCurrentView)
						currentView = viewName;

					// Hide the popup
					ViewSavePopup.this.hide();
				}
			});

			viewNamesControl = new ListBox();
			viewNamesControl.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// Just set the content of saveName to the selected value,
					// and highlight it and give it the focus
					int selectedIndex = viewNamesControl.getSelectedIndex();
					String selectedValue =
						viewNamesControl.getValue(selectedIndex);

					saveName.setText(selectedValue);
					saveName.selectAll();
					saveName.setFocus(true);
				}
			});

			// Add all the controls to content - see the
			// comments next to the declaration of content
			// for the layout
			HorizontalPanel topMatter = new HorizontalPanel();
			topMatter.add(saveName);
			topMatter.add(save);

			content = new VerticalPanel();
			content.add(topMatter);
			// We will add viewNamesList whenever we create it

			setWidget(content);
			addStyleName(POPUP_CLASS_NAME);

			retrieveViewNames(getViewNamesUrl(), viewNames,
				new Alertable<Object>() {
					@Override
					public void onFailure(Object message) {	}

					@Override
					public void onSuccess(Object message) { showViewNames(); }
				});
		}

		/**
		 * Puts together the URL at which we can write data back to the
		 * server, adding or changing a view.
		 *
		 * @return
		 * 		the URL used to set views on the server
		 */
		private String getViewWriteUrl() {
			return "/users/" + userId + "/views/set.json";
		}

		/**
		 * Fills and shows the viewNamesControl widget.
		 *
		 * <p>Uses the viewNames private variable to get the list of
		 * current views, and then adds all those names to
		 * viewNamesControl.</p>
		 */
		private void showViewNames() {
			int numViews = viewNames.size();
			if (numViews == 0)
				return;

			for (String name: viewNames)
				viewNamesControl.addItem(name);

			if (numViews == 1)
				viewNamesControl.setVisibleItemCount(2);
			else
				viewNamesControl.setVisibleItemCount(
					Math.min(numViews, MAX_VISIBLE_VIEW_NAMES));

			content.add(viewNamesControl);
		}

		/**
		 * Does exactly the same thing that setVisible does for any other
		 * popup, along with handling highlighting in the text box
		 * for this popup window.
		 */
		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);

			if (! visible)
				return;

			// Funny ordering because saveName.selectAll() only
			// works when saveName is attached to the document
			// and not hidden
			if (getCurrentViewName() != null) {
				saveName.setText(getCurrentViewName());
				saveName.selectAll();
			}

			saveName.setFocus(true);
		}
	}

	/**
	 * Returns the URL that should be used to retrieve the set of view
	 * names.
	 *
	 * @return
	 * 		the URL from which we can get the list of view names
	 */
	private String getViewNamesUrl() {
		return "/users/" + userId + "/views.json";
	}

	/**
	 * Fills the viewNames parameter with a list of view names retrieved
	 * from url.
	 *
	 * @param url
	 * 		the URL at which we can get the view names
	 * @param viewNames
	 * 		the list that will be filled with the view names
	 * @param callback
	 * 		an object that will be alerted whenever results come back.
	 * 		If the asynchronous request succeeds, calls
	 * 		<code>callback.onSuccess(null)</code>.  However, if the
	 * 		request fails, calls <code>callback.onFailure(null)</code>.
	 */
	// TODO: Add more useful parameters to the callback calls?
	private static void retrieveViewNames(String url,
			final List<String> viewNames,
			final Alertable<Object> callback) {
		WebDownloader.doGet(url, new DownloadAlertable() {
			@Override
			public void onSuccess(String response) {
				fillViewNames(viewNames, response);
				callback.onSuccess(null);
			}

			@Override
			public void onFailure(Request failed) {
				callback.onFailure(null);
			}
		});
	}

	/**
	 * Fills the viewNames list with the set of view names.
	 *
	 * <p>This is intended only as a helper method for
	 * {@link #retrieveViewNames(String, List, Alertable)}.</p>
	 *
	 * <p>As does the
	 * {@link GrapherTile#retrieveTile(String, int, int, List, Alertable)}
	 * method, this method uses JavaScript eval, which represents a
	 * security risk.  Only pass trusted content to this method.</p>
	 *
	 * @param viewNames
	 * 		the list to be filled
	 * @param responseBody
	 * 		the body of the response to our request for view
	 * 		names
	 */
	private static void fillViewNames(List<String> viewNames,
			String responseBody) {
		JSONValue response = JSONParser.parseLenient(responseBody);
		JSONArray views = response.isArray();
		if (views == null)
			return;

		for (int i = 0; i < views.size(); i++) {
			JSONValue viewValue = views.get(i);
			JSONObject viewObject = viewValue.isObject();
			if (viewObject != null) {
				// Success - this should always happen
				JSONValue nameValue = viewObject.get("name");
				if (nameValue == null)
					continue;
				JSONString nameString = nameValue.isString();
				if (nameString == null)
					continue;
				viewNames.add(nameString.stringValue());
			} else {
				// Also accept an array of strings coming from
				// the server, even though this isn't part of
				// the API
				JSONString viewString = viewValue.isString();
				if (viewString != null)
					viewNames.add(viewString.stringValue());
			}
		}
	}

	/**
	 * A class that represents an empty callback on a request.  Does
	 * nothing, regardless of circumstances, so is useful for
	 * situations in which we don't care about the server's return value.
	 */
	private static class EmptyRequestCallback implements RequestCallback {
		@Override
		public void onError(Request request, Throwable exception) { }

		@Override
		public void onResponseReceived(Request request,
				Response response) { }
	}

	/**
	 * Gives the user a way to restore to a particular view, selected
	 * from a list.
	 */
	private class ViewRestorePopup extends PopupPanel {
		private final List<String> viewNames;

		private final ScrollPanel content;
		// content will contain nothing until the set of view names
		// loads

		private final Grid viewNamesControl;

		/**
		 * Creates a new <tt>ViewRestorePopup</tt> object but does not show it.
		 */
		public ViewRestorePopup() {
			super(true, true);

			viewNames = new ArrayList<String>();
			viewNamesControl = new Grid(10, 3);
			content = new ScrollPanel();
			// TODO: Set a max height on content

			setWidget(content);
			addStyleName(POPUP_CLASS_NAME);

			retrieveViewNames(getViewNamesUrl(), viewNames,
				new Alertable<Object>() {
					@Override
					public void onFailure(Object message) {	}

					@Override
					public void onSuccess(Object message) { showViewNames(); }
				});
		}

		/**
		 * Just a front-end for the {@link ViewClickHandler#replaceCurrentView()}
		 * method.
		 *
		 * @param viewName
		 * 		the name of the view to navigate to
		 * @param action
		 * 		the action to take, as defined by the constants in
		 * 		the {@link ViewClickHandler} class
		 * @param succ
		 * 		the success continuation that is called with a parameter
		 * 		of <tt>null</tt> after the view is successfully replaced.
		 * 		If this parameter is <tt>null</tt>, it is ignored
		 * @throws NullPointerException
		 * 		if viewName is <tt>null</tt>
		 */
		public void replaceCurrentView(String viewName, int action,
				Continuation<Object> succ) {
			if (viewName == null)
				throw new NullPointerException("Can't navigate to null view");

			new ViewClickHandler(viewName, action).replaceCurrentView(succ);
		}

		/**
		 * Fills and shows the viewNamesControl widget.
		 *
		 * <p>Uses the viewNames private variable to get the list of
		 * current views, and then adds all those names to
		 * viewNamesControl.  There is one exception, however: the current
		 * view name does not show up in the list of possible views.</p>
		 */
		private void showViewNames() {
			int numViews = viewNames.size();
			if (numViews == 0)
				return;

			for (int row = 0; row < numViews; row++) {
				String name = viewNames.get(row);

				Anchor nameAnchor = new Anchor(name);
				nameAnchor.addClickHandler(new ViewClickHandler(name,
					ViewClickHandler.FULL_VIEW));
				if (name.equals(currentView))
					nameAnchor.addStyleName(CURRENT_VIEW_CLASS);

				Anchor channelsAnchor = new Anchor("Channels Only");
				channelsAnchor.addClickHandler(new ViewClickHandler(name,
					ViewClickHandler.CHANNELS_ONLY));

				Anchor timeAnchor = new Anchor("Time Only");
				timeAnchor.addClickHandler(new ViewClickHandler(name,
					ViewClickHandler.TIME_ONLY));

				viewNamesControl.setWidget(row, 0, nameAnchor);
				viewNamesControl.setWidget(row, 1, channelsAnchor);
				viewNamesControl.setWidget(row, 2, timeAnchor);
			}

			viewNamesControl.resizeRows(numViews);
			content.add(viewNamesControl);
		}

		/**
		 * Handles clicks on the anchor with text equal to the view name.
		 *
		 * <p>Objects of this class are immutable.  Then again, they
		 * don't carry much state that could be mutated.</p>
		 *
		 * <p>The constants CHANNELS_ONLY, TIME_ONLY, and FULL_VIEW are
		 * the only acceptable values for the action this class is
		 * supposed to take.  I would use an enum to implement this
		 * behavior, but I would have to put the enum code inside the
		 * <tt>ViewSwitchWidget</tt> class, since you can't put an enum
		 * inside a non-static private class.</p>
		 */
		private class ViewClickHandler implements ClickHandler {
			public static final int CHANNELS_ONLY = 1;
			public static final int TIME_ONLY = 2;
			public static final int FULL_VIEW = 3;
			// Notice that FULL_VIEW == CHANNELS_ONLY | TIME_ONLY

			private final String viewName;
			private final int action;

			/**
			 * Creates a new ViewClickHandler.
			 *
			 * @param viewName
			 * 		the name of the view that should be used if this
			 * 		is clicked
			 * @param action
			 * 		the action to take on click, which should be one of
			 * 		{@link #CHANNELS_ONLY}, {@link #TIME_ONLY}, and
			 * 		{@link #FULL_VIEW}
			 * @throws NullPointerException
			 * 		if viewName is <tt>null</tt>
			 */
			public ViewClickHandler(String viewName, int action) {
				if (viewName == null)
					throw new NullPointerException("Can't use null view name");
				this.viewName = viewName;
				this.action = action;
			}

			/**
			 * Called whenever the user clicks on a link with this handler
			 * attached.
			 *
			 * <p>This method completely ignores event, instead blindly
			 * performing the actions specified in the constructor.  In fact,
			 * all this does is call {@link #replaceCurrentView()}</p>
			 *
			 * @see #replaceCurrentView()
			 */
			@Override
			public void onClick(ClickEvent event) {
				replaceCurrentView(new EmptyContinuation<Object>());
			}

			/**
			 * Performs the actions specified in the constructor.
			 *
			 * <p>This method is available publicly so that a caller does
			 * not have to go though the {@link #onClick(ClickEvent)}
			 * event in order to replace the current view.</p>
			 *
			 * @param succ
			 * 		the success continuation that is called with a parameter
			 * 		of <tt>null</tt> after the view is successfully replaced.
			 * 		If this parameter is <tt>null</tt>, it is ignored
			 */
			public void replaceCurrentView(final Continuation<Object> succ) {
				WebDownloader.doGet(getViewUrl(),
					WebDownloader.convertToDownloadAlertable(
						new DownloadSuccessAlertable() {
							@Override
							public void onSuccess(String response) {
								substituteView(response, succ);
							}
						}));
			}

			/**
			 * Returns the URL to use to get the view information.
			 *
			 * @return
			 * 		the URL to use to get the view information
			 */
			private String getViewUrl() {
				return "/users/" + userId + "/views/get.json?name=" + viewName;
			}

			/**
			 * Substitutes the view specified in responseText in for
			 * the current view, based on the action specified to the
			 * constructor for this object.
			 *
			 * <p>If the action is equal to FULL_VIEW, this also updates
			 * the currentView field of the parent
			 * {@link ViewSwitchWidget}.</p>
			 *
			 * <p>Also, if succ is not equal to <tt>null</tt>, after
			 * this successfully builds the view it runs
			 * <code>succ.call(null)</code>, which allows a caller
			 * to receive some notification of the event.</p>
			 *
			 * @param responseText
			 * 		the text we received from the server, giving the
			 * 		information from the saved view
			 * @param succ
			 * 		the success continuation that is called with a parameter
			 * 		of <tt>null</tt> after the view is successfully replaced.
			 * 		If this parameter is <tt>null</tt>, it is ignored
			 */
			private void substituteView(String responseText,
					Continuation<Object> succ) {
				SavableView view = SavableView.buildView(responseText);

				ChannelManager newChannels = view.getDataPlots(graphWidget);
				switch (this.action) {
				case TIME_ONLY:
					restoreTime(newChannels);
					break;
				case CHANNELS_ONLY:
					restoreChannels(newChannels);
					break;
				case FULL_VIEW:
					channels.replaceChannels(newChannels);
					currentView = view.getName();
					break;
				}

				ViewRestorePopup.this.hide();

				if (succ != null)
					succ.call(null);
			}

			/**
			 * Restores just the time component of the
			 * {@link org.bodytrack.client.ChannelManager ChannelManager}
			 * newChannels.
			 *
			 * <p>The rules for restoring times are somewhat complicated: if
			 * there is exactly one X-axis on both the current set of
			 * channels and newChannels, we just reset the times on
			 * the current X-axis to the times from newChannels.  If
			 * there is exactly one X-axis on the current set of channels
			 * but more than one on newChannels, we just use the time from
			 * the first channel in newChannels.  If there is more than
			 * one X-axis on the current set of channels, we step through
			 * the axes in newChannels and sequentially assign channels,
			 * repeating the last axis in newChannels if necessary.</p>
			 *
			 * @param newChannels
			 * 		the set of channels which we should use as a basis
			 * 		for shifting our X-axes
			 */
			private void restoreTime(ChannelManager newChannels) {
				int currCount = channels.getXAxes().size();
				int newCount = newChannels.getXAxes().size();

				if (currCount == 0 || newCount == 0)
					return;

				if (currCount == 1) {
					// In either case, we use the first new axis as the
					// new set of bounds for currX
					GraphAxis currX =
						CollectionUtil.getFirst(channels.getXAxes());
					GraphAxis newX =
						CollectionUtil.getFirst(newChannels.getXAxes());
					replaceBounds(currX, newX);
				} else {
					GraphAxis newX = null;
					// We won't actually use null, since there is at
					// least one X-axis in newChannels

					int currIndex = 0;
					List<GraphAxis> currAxes =
						new ArrayList<GraphAxis>(channels.getXAxes());

					for (GraphAxis temp: newChannels.getXAxes()) {
						newX = temp; // Need newX to persist after the loop
						if (currIndex >= currCount) break;
						replaceBounds(currAxes.get(currIndex), newX);
						currIndex++;
					}

					// Repeat the last new axis down the remaining
					// current axes
					for (; currIndex < currCount; currIndex++)
						replaceBounds(currAxes.get(currIndex), newX);
				}
			}

			/**
			 * Replaces the bounds of currX with the bounds of newX.
			 *
			 * <p>It is expected, but not checked explicitly, that currX
			 * and newX are not <tt>null</tt>.  If either is <tt>null</tt>,
			 * a {@link java.lang.NullPointerException NullPointerException}
			 * will be thrown.  However, it is not a problem that we don't
			 * check, since this is an internal method, allowing us to control
			 * all calls to it.</p>
			 *
			 * @param currX
			 * 		the axis to change
			 * @param newX
			 * 		the axis with the bounds to use for currX
			 */
			// TODO: Move this to the GraphAxis class
			private void replaceBounds(GraphAxis currX, GraphAxis newX) {
				double oldMin = currX.getMin();
				double oldMax = currX.getMax();
				double newMin = newX.getMin();
				double newMax = newX.getMax();

				// Zoom in place to the right factor
				currX.zoom((newMax - newMin) / (oldMax - oldMin),
					(oldMin + oldMax) / 2);

				// Now translate
				oldMin = currX.getMin();
				currX.uncheckedDrag(newMin - oldMin);
			}

			/**
			 * Restores the channels from newChannels into the channels
			 * instance variable.
			 *
			 * @param newChannels
			 * 		a {@link org.bodytrack.client.ChannelManager
			 * 		ChannelManager} containing the channels that we
			 * 		should use to replace the channels in the channels
			 * 		instance variable
			 */
			private void restoreChannels(ChannelManager newChannels) {
				if (channels.getXAxes().size() == 0) {
					// If we don't have any channels now, we do a full
					// replacement, including any X-axes from newChannels
					channels.replaceChannels(newChannels);
					return;
				}

				GraphAxis xAxis = CollectionUtil.getFirst(channels.getXAxes());
				channels.clear();

				for (DataPlot newPlot: newChannels.getDataPlots()) {
					newPlot.setXAxis(xAxis);
					channels.addChannel(newPlot);
				}
			}
		}
	}
}
