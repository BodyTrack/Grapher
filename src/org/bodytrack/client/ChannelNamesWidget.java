package org.bodytrack.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A panel that offers a way to show the current channel names available to
 * the user.
 */
public class ChannelNamesWidget extends VerticalPanel {
	/**
	 * Creates a new ChannelNamesWidget.
	 *
	 * @param channelNames
	 * 		the map from device names to associated channel names
	 * @throws NullPointerException
	 * 		if channelNames is <tt>null</tt>
	 */
	public ChannelNamesWidget(Map<String, List<String>> channelNames) {
		if (channelNames == null)
			throw new NullPointerException(
				"Null map of channel names not allowed");

		for (Map.Entry<String, List<String>> ent: channelNames.entrySet())
			add(buildSingleDevicePanel(ent.getKey(), ent.getValue()));
	}

	/**
	 * Builds and returns a new
	 * {@link com.google.gwt.user.client.ui.DisclosurePanel DisclosurePanel}
	 * with a series of check boxes that get shown when the user opens
	 * the panel.
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
	 * @return
	 * 		a <tt>DisclosurePanel</tt> showing deviceName at all times, and
	 * 		the elements of channelNames when opened
	 */
	private static DisclosurePanel buildSingleDevicePanel(String deviceName,
			List<String> channelNames) {
		DisclosurePanel result = new DisclosurePanel(deviceName);

		for (String name: channelNames) {
			CheckBox box = new CheckBox(name);
			result.add(box);
		}

		return result;
	}
}
