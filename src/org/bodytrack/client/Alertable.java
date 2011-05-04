package org.bodytrack.client;

/**
 * A very generic interface to alert an object whenever events occur.
 *
 * @param <T>
 * 		the types of messages that will be sent to objects whenever
 * 		events occur
 */
// TODO: There are at least four interfaces (this interface,
// Continuation, WebDownloader.DownloadAlertable, and
// WebDownloader.DownloadSuccessAlertable) that all try to allow CPS
// to work in Java, since Java doesn't have lambdas or even delegates.
// I need to cut the number of interfaces down to one or two.  Probably
// one for the success/failure case, one for the success case, and maybe
// one for the "alert when I'm done" case
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
