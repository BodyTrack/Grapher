package org.bodytrack.client;

import java.util.Date;

/**
 * <p>
 * {@link #getNext()} produces an <code>int</code> sequence number,
 * starting at 1.  The sequence number resets to 1 after reaching
 * {@link #MIN_THROTTLED}.
 * </p>
 *
 * <p>
 * This segments the set of integers into two regions - one for regular
 * sequence numbers and one for throttled sequence numbers.  Regular
 * sequence numbers go from 1 to {@link #MIN_THROTTLED}, and throttled
 * sequence numbers go from <code>MIN_THROTTLED + 1</code> to
 * {@link Integer#MAX_VALUE}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SequenceNumber {
	private static final SequenceNumber INSTANCE = new SequenceNumber();

	// The number of milliseconds between unique values returned from
	// getNextThrottled
	private static final int THROTTLED_THRESHOLD = 50;

	// Have to divide and then multiple in order to prevent overflow
	private static final int MIN_THROTTLED = (Integer.MAX_VALUE / 4) * 3;

	public static int getNext() {
		if (INSTANCE.id == MIN_THROTTLED) {
			INSTANCE.id = 0;
		}
		return ++INSTANCE.id;
	}

	/**
	 * Similar to {@link #getNext()}, but only returns a new value once every
	 * {@link #THROTTLED_THRESHOLD} milliseconds.
	 *
	 * @return
	 * 	A new value if and only if there has been at least
	 * 	{@link #THROTTLED_THRESHOLD} milliseconds since the last new value
	 */
	public static int getNextThrottled() {
		long now = new Date().getTime();

		if (now - INSTANCE.latestThrottledTimestamp >= THROTTLED_THRESHOLD) {
			INSTANCE.latestThrottledTimestamp = now;

			// Need to return a new ID
			if (INSTANCE.throttledId == Integer.MAX_VALUE) {
				INSTANCE.throttledId = MIN_THROTTLED;
			}
			return ++INSTANCE.throttledId;
		}

		// Return the old ID because THROTTLED_THRESHOLD milliseconds
		// haven't passed
		return INSTANCE.throttledId;
	}

	private int id = 0;
	private int throttledId = MIN_THROTTLED;
	private long latestThrottledTimestamp = 0;

	private SequenceNumber() {
		// private to prevent instantiation
	}
}
