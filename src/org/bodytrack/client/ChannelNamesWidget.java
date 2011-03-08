package org.bodytrack.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public ChannelNamesWidget(Map<String, List<String>> channelNames,
			ChannelChangedListener listener) {
		if (channelNames == null)
			throw new NullPointerException(
				"Null map of channel names not allowed");

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
	 * <p>This also adds the appropriate check boxes and handlers to the
	 * checkBoxes and handlerRegs private variables, as well.</p>
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
	 * 		ValueChangeHandler<Boolean>} that will be called whenever a check
	 * 		box is checked or unchecked
	 * @return
	 * 		a <tt>DisclosurePanel</tt> showing deviceName at all times, and
	 * 		the elements of channelNames when opened
	 */
	private DisclosurePanel buildSingleDevicePanel(String deviceName,
			List<String> channelNames, ValueChangeHandler<Boolean> handler) {
		DisclosurePanel result = new DisclosurePanel(deviceName);

		for (String name: channelNames) {
			CheckBox box = new CheckBox(name);
			HandlerRegistration reg = box.addValueChangeHandler(handler);
			checkBoxes.put(box, new StringPair(deviceName, name));
			handlerRegs.put(box, reg);

			result.add(box);
		}

		return result;
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

	/**
	 * A simple class to hold an immutable pair of non-<tt>null</tt> strings.
	 */
	private static class StringPair {
		private String s1;
		private String s2;

		/**
		 * Initializes a new <tt>StringPair</tt>.
		 *
		 * @param s1
		 * 		the first string to hold
		 * @param s2
		 * 		the second string to hold
		 */
		public StringPair(String s1, String s2) {
			if (s1 == null || s2 == null)
				throw new NullPointerException(
					"Cannot use null string with the StringPair class");

			this.s1 = s1;
			this.s2 = s2;
		}

		/**
		 * Returns the first string this holds.
		 *
		 * @return
		 * 		the value of s1 passed to the constructor when this
		 * 		<tt>StringPair</tt> was created
		 */
		public String getFirst() {
			return s1;
		}

		/**
		 * Returns the second string this holds.
		 *
		 * @return
		 * 		the value of s2 passed to the constructor when this
		 * 		<tt>StringPair</tt> was created
		 */
		public String getSecond() {
			return s2;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + s1.hashCode();
			result = prime * result + s2.hashCode();
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (! (obj instanceof StringPair))
				return false;
			StringPair other = (StringPair) obj;
			// We know that s1 and s2 are never null
			return s1.equals(other.s1) && s2.equals(other.s2);
		}
	}
}
