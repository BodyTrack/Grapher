package org.bodytrack.client;

/**
 * A specification for a class that is notified whenever a channel
 * should be added to, or removed from, the grapher or some other
 * location.
 *
 * <p>Note that we will use the terminology &quot;backing object&quot;
 * to mean the object from which channels are added and removed as
 * the {@link #channelAdded(String, String)} and
 * {@link #channelRemoved(String, String)} methods are called.</p>
 */
public interface ChannelChangedListener {
	/**
	 * Called whenever a channel should be added to the backing object.
	 *
	 * <p>This method should do nothing if the specified channel is
	 * already part of the backing object.  As a result, this method
	 * is idempotent i.e. repeated calls to this method have no more
	 * effect than a single call, as long as there is no intervening
	 * call to {@link #channelRemoved(String, String)}.</p>
	 *
	 * @param deviceName
	 * 		the device name for the channel that should be added
	 * @param channelName
	 * 		the channel name for the channel that should be added
	 */
	void channelAdded(String deviceName, String channelName);

	/**
	 * Called whenever a channel should be removed from the backing object.
	 *
	 * <p>This method should do nothing unless the specified channel is
	 * already part of the backing object.  As a result, this method
	 * is idempotent i.e. repeated calls to this method have no more
	 * effect than a single call, as long as there is no intervening
	 * call to {@link #channelAdded(String, String)}.</p>
	 *
	 * @param deviceName
	 * 		the device name for the channel that should be removed
	 * @param channelName
	 * 		the channel name for the channel that should be removed
	 */
	void channelRemoved(String deviceName, String channelName);
}
