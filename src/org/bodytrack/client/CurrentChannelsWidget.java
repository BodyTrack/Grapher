package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

import org.bodytrack.client.ChannelManager.StringPair;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A widget that shows the names of the current panels in a flowing layout.
 */
public class CurrentChannelsWidget extends FlowPanel
		implements ChannelChangedListener {

	// The real job of this class is to make sure that
	// channelMgr and channels stay in sync, and that everything
	// in channels has been added to this widget
	private final ChannelManager channelMgr;
	private final Map<StringPair, ChannelLink> channels;

	/**
	 * Creates a new <tt>CurrentChannelsWidget</tt>.
	 *
	 * <p>After this widget is constructed, it is <em>required</em>
	 * that this be added to the set of listeners on mgr.</p>
	 */
	public CurrentChannelsWidget(ChannelManager mgr) {
		channelMgr = mgr;
		channels = new HashMap<StringPair, ChannelLink>();
	}

	/**
	 * Called whenever the
	 * {@link org.bodytrack.client.ChannelManager ChannelManager}
	 * held by this object has a channel added.
	 *
	 * @param deviceName
	 * 		the nickname of the device from which the new channel came
	 * @param channelName
	 * 		the name of the channel on the device
	 * @throws NullPointerException
	 * 		if either deviceName or channelName is <tt>null</tt>
	 */
	@Override
	public void channelAdded(String deviceName, String channelName) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Can't add channel with a null parameter");

		StringPair chan = new StringPair(deviceName, channelName);

		if (channels.containsKey(chan))
			return;

		channels.put(chan, new ChannelLink(chan));
	}

	/**
	 * Called whenever the
	 * {@link org.bodytrack.client.ChannelManager ChannelManager}
	 * held by this object has a channel removed.
	 */
	@Override
	public void channelRemoved(String deviceName, String channelName) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Can't remove channel with a null parameter");

		StringPair chan = new StringPair(deviceName, channelName);

		if (channels.containsKey(chan)) {
			ChannelLink visibleRepr = channels.remove(chan);
			remove(visibleRepr);
		}
	}

	/**
	 * A representation of a single channel's link, which should be
	 * added to this <tt>CurrentChannelsWidget</tt> to allow the user
	 * to remove a single channel.
	 *
	 * <p>Objects of this class are immutable, and thus no external
	 * synchronization or special care is required.</p>
	 */
	private class ChannelLink extends HorizontalPanel {
		/**
		 * The HTML that shows on the remove link.
		 */
		private static final String REMOVE_HTML =
			"<span style=\"border: 1px solid blue\">X</span>";
		// TODO: Set the HTML for remove to include a CSS class e.g.
		// <span class="hyperlink-remove">X</span>
		// Perhaps have this CSS put a box around the X, to make the
		// look clear

		private final StringPair name;
		private final Label link;
		private final Anchor remove;

		/**
		 * Creates a new <tt>ChannelLink</tt> to make visible to
		 * the user.
		 *
		 * @param name
		 * 		the name of the device and channel from which this
		 * 		channel came, represented as a (deviceName, channelName)
		 * 		pair
		 * @throws NullPointerException
		 * 		if name is <tt>null</tt>
		 */
		public ChannelLink(StringPair name) {
			if (name == null)
				throw new NullPointerException(
					"Null channel name not allowed");

			this.name = name;
			link = new Label(name.toDisplayString());
			remove = new Anchor(REMOVE_HTML, true);
			remove.addClickHandler(new RemoveHandler());

			add(link);
			add(remove);
		}

		/**
		 * Class that allows removal of a channel whenever the user clicks
		 * the remove anchor.
		 */
		private class RemoveHandler implements ClickHandler {
			/**
			 * Called whenever the user requests to remove the channel.
			 */
			@Override
			public void onClick(ClickEvent event) {
				channelMgr.removeChannel(name.getFirst(), name.getSecond());
			}
		}
	}
}
