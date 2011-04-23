package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.KnownColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bodytrack.client.ChannelManager.StringPair;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/**
 * A widget that shows the names of the current panels in a flowing layout.
 */
public class CurrentChannelsWidget extends FlowPanel
		implements ChannelChangedListener {
	/**
	 * The CSS ID we will use for this widget.
	 */
	private static final String WIDGET_ID = "currentChannelsWidget";

	/**
	 * The CSS class for the color picker itself.
	 */
	private static final String COLOR_POPUP_CLASS = "colorPopup";

	/**
	 * The CSS class for the colored buttons that are shown on the color
	 * picker.
	 */
	private static final String COLOR_BUTTON_CLASS = "colorPopup-colorButton";

	// The real job of this class is to make sure that
	// channelMgr and channels stay in sync, and that everything
	// in channels has been added to this widget
	private final ChannelManager channelMgr;
	private final Map<StringPair, ChannelLink> channels;

	/**
	 * Creates a new <tt>CurrentChannelsWidget</tt>.
	 *
	 * <p>This widget adds itself to the set of listeners for mgr,
	 * and also adds all channels in mgr to itself initially.  Any
	 * new channels, however, must be added through the
	 * {@link #channelAdded(String, String)} alert.
	 */
	public CurrentChannelsWidget(ChannelManager mgr) {
		channelMgr = mgr;
		channels = new HashMap<StringPair, ChannelLink>();

		for (DataPlot plot: channelMgr.getDataPlots())
			channelAdded(plot.getDeviceName(), plot.getChannelName());

		getElement().setId(WIDGET_ID);
		channelMgr.addChannelListener(this);
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

		ChannelLink visibleRepr = new ChannelLink(chan);

		channels.put(chan, visibleRepr);
		add(visibleRepr);
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
		private static final String REMOVE_HTML = "X";

		private final StringPair name;
		private final HTML link;
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
			link = new HTML(getLinkString(), true);
			link.addDoubleClickHandler(new DoubleClickHandler() {
				// On double-click, a color picker pops up that allows
				// the user to change the color of the channel
				@Override
				public void onDoubleClick(DoubleClickEvent event) {
					final int x = (int) Math.round(event.getClientX());
					final int y = (int) Math.round(event.getClientY());
					final ChannelColorChanger changer =
						new ChannelColorChanger();
					changer.setPopupPositionAndShow(new PositionCallback() {
						@Override
						public void setPosition(int offsetWidth,
								int offsetHeight) {
							changer.setPopupPosition(x, y);
						}
					});
				}
			});

			remove = new Anchor(REMOVE_HTML, true);
			remove.setStyleName("channelRemoveLink");
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					channelMgr.removeChannel(
						ChannelLink.this.name.getFirst(),
						ChannelLink.this.name.getSecond());
				}
			});

			add(link);
			add(remove);

			setStyleName("channelLinkFlow");
		}


		/**
		 * Builds a string representation of the HTML that the
		 * link private variable should use when initialized.
		 *
		 * @return
		 * 		the non-<tt>null</tt> HTML string the link private
		 * 		variable should use as its content
		 */
		private String getLinkString() {
			DataPlot chan = channelMgr.getChannel(name);

			if (chan != null && chan.getColor() != null)
				return "<span style=\"color: "
					+ chan.getColor().getColorCode() + "\">"
					+ name.toDisplayString() + "</span>";

			return name.toDisplayString();
		}

		/**
		 * A color picker that allows changing the color of the
		 * current channel.
		 *
		 * <p>This just uses the name field of the enclosing class to
		 * determine which channel's color to change, so it is important
		 * that this field be immutable, as it is.</p>
		 */
		private class ChannelColorChanger extends PopupPanel {
			private final int numColors;
			private final PushButton[][] colorButtons;

			private final ScrollPanel content;
			private final Grid colorGrid; // array of buttons from colorButtons

			/**
			 * Creates a new <tt>ChannelColorChanger</tt> using all the known
			 * colors.
			 */
			public ChannelColorChanger() {
				super(true, true);

				List<Color> knownColors = getKnownColors();

				numColors = knownColors.size();

				int cols = (int) (Math.floor(Math.sqrt(numColors)));
				int rows = (int) (Math.ceil(((double) numColors) / cols));

				colorButtons = new PushButton[rows][cols];
				colorGrid = new Grid(rows, cols);

				ClickHandler handler = new ColorButtonClickHandler();

				for (int i = 0; i < numColors; i++) {
					int r = i / cols;
					int c = i % cols;

					PushButton btn = new PushButton("", handler);
					btn.addStyleName(COLOR_BUTTON_CLASS);
					btn.getElement().getStyle().setBackgroundColor(
						knownColors.get(i).getColorCode());

					colorButtons[r][c] = btn;
					colorGrid.setWidget(r, c, btn);
				}

				content = new ScrollPanel(colorGrid);
				content.setWidth("16em");
				content.setHeight("10em");
				// TODO: Replace hardcoded strings with CSS, or at least
				// some class-level constants

				this.setWidget(content);
				this.addStyleName(COLOR_POPUP_CLASS);
			}

			/**
			 * Returns a series of known colors.
			 *
			 * @return
			 * 		a set of known colors to use for the color picker.  For
			 * 		now, this is just the return value of a call to
			 * 		{@link gwt.g2d.client.graphics.KnownColor#getKnownColors()
			 * 		KnownColor.getKnownColors}, converted to a list
			 */
			private List<Color> getKnownColors() {
				return new ArrayList<Color>(KnownColor.getKnownColors());
			}

			/**
			 * A class whose sole job is to set the name of the current
			 * channel to the background color of some clicked
			 * {@link com.google.gwt.user.client.ui.PushButton PushButton}.
			 */
			private class ColorButtonClickHandler implements ClickHandler {
				/**
				 * Called whenever a button is clicked.
				 *
				 * <p>Sets the color of the current channel to the same
				 * color that the button has, if this is possible.</p>
				 */
				@Override
				public void onClick(ClickEvent event) {
					Object eventSource = event.getSource();
					if (! (eventSource instanceof PushButton))
						return;

					PushButton source = (PushButton) eventSource;
					String colorName =
						source.getElement().getStyle().getBackgroundColor();
					Color newColor = ColorUtils.buildColor(colorName);
					if (newColor == null)
						return;

					DataPlot plot = channelMgr.getChannel(name);
					if (plot == null)
						return;

					plot.setColor(newColor);
					link.setHTML(getLinkString());
					ChannelColorChanger.this.hide();
				}
			}
		}
	}
}
