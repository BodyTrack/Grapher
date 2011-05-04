package org.bodytrack.client;

/**
 * A basic specification of a continuation.
 *
 * @param <T>
 * 		the type of object that will be passed to this continuation
 */
// TODO: Probably should convert Alertable to ContinuationPair,
// since it defines a success and a failure continuation
public interface Continuation<T> {
	/**
	 * Calls the continuation.
	 *
	 * @param result
	 * 		the result of some earlier computation, which is passed
	 * 		to the continuation to use
	 */
	public void call(T result);
}
