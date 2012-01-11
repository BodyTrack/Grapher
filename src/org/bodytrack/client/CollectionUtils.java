package org.bodytrack.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CollectionUtils {
	private CollectionUtils() {
		throw new UnsupportedOperationException();
	}

	public static <T extends Comparable<T>> void insertInOrder(final List<T> lst,
			final T elem) {
		if (lst == null || elem == null)
			throw new NullPointerException();

		if (lst.size() == 0) {
			lst.add(elem);
			return;
		}

		final int idx = Collections.binarySearch(lst, elem);

		if (idx < 0) {
			// Collections.binarySearch returns (-(insertion point) - 1) if
			// the item is not found
			lst.add(-idx - 1, elem);
		} else {
			lst.add(idx, elem);
		}
	}

	/**
	 * Returns the first item in lst
	 *
	 * @param <T>
	 * 	The type of object that this method should return
	 * @param lst
	 * 	A collection of objects
	 * @return
	 * 	The first item returned by the iterator for lst, or
	 * 	<code>null</code> if lst is empty
	 */
	public static <T> T getFirst(final Collection<T> lst) {
		for (final T value: lst)
			return value;
		return null;
	}
}
