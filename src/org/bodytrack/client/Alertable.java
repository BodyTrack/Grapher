package org.bodytrack.client;

/**
 * A very generic interface to alert an object whenever events occur.
 *
 * @param <T>
 * 		the types of messages that will be sent to objects whenever
 * 		events occur
 */
public interface Alertable<T> {
	/**
	 * Called whenever a successful action occurs.
	 *
	 * @param message
	 * 		some message that gives detail on the success
	 */
	void onSuccess(T message);

	/**
	 * Called whenever a failure occurs.
	 *
	 * @param message
	 * 		some message that gives detail on the failure
	 */
	void onFailure(T message);
}
