package org.bodytrack.client;

import java.util.Set;

/**
 * A set of utility methods for the collection classes.
 */
public class CollectionUtil {
	/**
	 * Returns the first item in s.
	 *
	 * @param <T>
	 * 		the type of object we should return
	 * @param s
	 * 		a nonempty set of objects
	 * @return
	 * 		the first item returned by the iterator
	 * 		for s
	 */
	public static <T> T getFirst(Set<T> s) {
		for (T value: s)
			return value;
		return null;
	}
}
