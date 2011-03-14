package org.bodytrack.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.bodytrack.client.ChannelManager.StringPair;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A panel that offers a way to show the current channel names available to
 * the user.
 */
public class ChannelNamesWidget extends VerticalPanel {
	private final Map<String, DisclosurePanel> devices;
	private final Set<StringPair> channels;
	private final Map<CheckBox, StringPair> checkBoxes;
	private final Map<CheckBox, HandlerRegistration> handlerRegs;
	private final OnCheckHandler valueChangeHandler;

	/**
	 * Creates a new ChannelNamesWidget.
	 *
	 * @param channelNames
	 * 		the map from device names to associated channel names
	 * @throws NullPointerException
	 * 		if channelNames or any of the strings inside it is <tt>null</tt>
	 */
	public ChannelNamesWidget(SortedMap<String, List<String>> channelNames,
			ChannelChangedListener listener) {
		if (channelNames == null)
			throw new NullPointerException(
				"Null map of channel names not allowed");

		devices = new HashMap<String, DisclosurePanel>();
		channels = new HashSet<StringPair>();
		checkBoxes = new HashMap<CheckBox, StringPair>();
		handlerRegs = new HashMap<CheckBox, HandlerRegistration>();
		valueChangeHandler = new OnCheckHandler(listener);

		for (Map.Entry<String, List<String>> ent: channelNames.entrySet())
			add(buildSingleDevicePanel(
				ent.getKey(), ent.getValue(), valueChangeHandler));
	}

	/**
	 * Builds and returns a new
	 * {@link com.google.gwt.user.client.ui.DisclosurePanel DisclosurePanel}
	 * with a series of check boxes that get shown when the user opens
	 * the panel.
	 *
	 * <p>This adds the appropriate check boxes and handlers to the
	 * devices, channels, checkBoxes, and handlerRegs private variables, as
	 * well.</p>
	 *
	 * <p>All the check boxes are initially unchecked, and in the order
	 * in which they appear in the channelNames parameter.  The returned
	 * disclosure panel is initially closed.</p>
	 *
	 * @param deviceName
	 * 		the device name that will show even when the panel is closed
	 * @param channelNames
	 * 		the device names that will show as check boxes when the panel
	 * 		is opened
	 * @param handler
	 * 		a {@link com.google.gwt.event.logical.shared.ValueChangeHandler
	 * 		ValueChangeHandler} that will be called whenever
	 * 		a check box is checked or unchecked
	 * @return
	 * 		a <tt>DisclosurePanel</tt> showing deviceName at all times, and
	 * 		the elements of channelNames when opened
	 */
	private DisclosurePanel buildSingleDevicePanel(String deviceName,
			List<String> channelNames, ValueChangeHandler<Boolean> handler) {
		DisclosurePanel result = new DisclosurePanel(deviceName);

		for (String name: channelNames)
			addChannelToDisclosurePanel(result, deviceName, name, handler);

		devices.put(deviceName, result);

		return result;
	}

	/**
	 * Adds a single channel to a panel.
	 *
	 * <p>This adds the appropriate check boxes and handlers to the
	 * channels, checkBoxes, and handlerRegs private variables, as well.</p>
	 *
	 * @param panel
	 * 		the panel to which to add a <tt>CheckBox</tt> with the specified
	 * 		channel name
	 * @param deviceName
	 * 		the name of the device represented by panel
	 * @param channelName
	 * 		the name of the channel to add
	 * @param handler
	 * 		the <tt>ValueChangeHandler</tt> that should handle
	 * 		events on the new check box for the new channel
	 */
	private void addChannelToDisclosurePanel(DisclosurePanel panel,
			String deviceName, String channelName,
			ValueChangeHandler<Boolean> handler) {
		CheckBox box = new CheckBox(channelName);
		HandlerRegistration reg = box.addValueChangeHandler(handler);

		StringPair channelNamePair =
			new StringPair(deviceName, channelName);
		channels.add(channelNamePair);
		checkBoxes.put(box, channelNamePair);
		handlerRegs.put(box, reg);

		panel.add(box);
	}

	/**
	 * Adds the specified channel to the set of channels to show
	 * on this widget.
	 *
	 * @param deviceName
	 * 		the name of the device for the channel to add
	 * @param channelName
	 * 		the name of the channel itself
	 * @return
	 * 		<tt>true</tt> if and only if the specified channel was
	 * 		added, which occurs if and only if the specified channel
	 * 		was not previously part of this widget
	 */
	public boolean addChannel(String deviceName, String channelName) {
		StringPair channel = new StringPair(deviceName, channelName);
		if (channels.contains(channel))
			return false;

		if (devices.containsKey(deviceName)) {
			// Need to add to the existing panel
			DisclosurePanel panel = devices.get(deviceName);

			addChannelToDisclosurePanel(panel, deviceName, channelName,
				valueChangeHandler);
		} else {
			// Need to make a new panel
			List<String> channelNames = new ArrayList<String>();
			add(buildSingleDevicePanel(deviceName, channelNames,
				valueChangeHandler));
		}

		return true;
	}

	/**
	 * A handler that will call the appropriate method on its
	 * {@link org.bodytrack.client.ChannelChangedListener
	 * ChannelChangedListener} whenever a check box is checked or unchecked.
	 */
	private class OnCheckHandler implements ValueChangeHandler<Boolean> {
		private final ChannelChangedListener listener;

		/**
		 * Creates a new <tt>OnCheckHandler</tt>.
		 *
		 * @param listener
		 * 		the <tt>ChannelChangedListener</tt> that will get
		 * 		called whenever this object's
		 * 		{@link #onValueChange(ValueChangeEvent) onValueChange}
		 * 		method is called
		 */
		public OnCheckHandler(ChannelChangedListener listener) {
			this.listener = listener;
		}

		/**
		 * Called whenever a check box is checked or unchecked.
		 */
		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			Object source = event.getSource();

			// source should always be a CheckBox
			if (! (source instanceof CheckBox))
				return;

			CheckBox sourceBox = (CheckBox) source;
			String deviceName = checkBoxes.get(sourceBox).getFirst();
			String channelName = checkBoxes.get(sourceBox).getSecond();

			if (event.getValue())
				// User just checked a button
				listener.channelAdded(deviceName, channelName);
			else
				// User just unchecked a button
				listener.channelRemoved(deviceName, channelName);
		}
	}
}
