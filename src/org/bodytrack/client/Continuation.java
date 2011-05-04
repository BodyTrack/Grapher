package org.bodytrack.client;

/**
 * A basic specification of a continuation.
 *
 * @param <T>
 * 		the type of object that will be passed to this continuation
 */
// TODO: Probably should convert Alertable to ContinuationPair,
// since it defines a success and a failure continuation.  However,
// Alertable does work with event notifications, so that might not
// be the best strategy.
public interface Continuation<T> {
	/**
	 * Calls the continuation.
	 *
	 * @param result
	 * 		the result of some earlier computation, which is passed
	 * 		to the continuation to use
	 */
	public void call(T result);

	/**
	 * A do-nothing implementation of the {@link Continuation} interface.
	 *
	 * @param <U>
	 * 		the type parameter that is referred to as T in the
	 * 		{@link Continuation} interface
	 */
	public class EmptyContinuation<U> implements Continuation<U> {
		@Override
		public void call(U result) { }
	}
}
