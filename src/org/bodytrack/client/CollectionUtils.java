package org.bodytrack.client;

import java.util.Collection;
import java.util.List;

public final class CollectionUtils {
	private CollectionUtils() {
		throw new UnsupportedOperationException();
	}

	public static <T extends Comparable<T>> int insertInOrder(final List<T> lst,
			final T elem) {
		if (lst == null || elem == null)
			throw new NullPointerException();

		if (lst.size() == 0) {
			lst.add(elem);
			return 0;
		}

		final int idx = CollectionUtils.binarySearch(lst, elem);

		if (idx < 0) {
			// Collections.binarySearch returns (-(insertion point) - 1) if
			// the item is not found
			lst.add(-idx - 1, elem);
			return -idx - 1;
		}

		lst.add(idx, elem);
		return idx;
	}

	// Meant to emulate the Collections.binarySearch method exactly, but does
	// not use equals() at all.
	//
	// After implementing, I checked this code against Joshua Bloch's at
	// http://googleresearch.blogspot.com/2006/06/extra-extra-read-all-about-it-nearly.html
	public static <T1 extends Comparable<T2>, T2> int binarySearch(
			final List<T2> lst, final T1 elem) {
		if (lst == null || elem == null)
			throw new NullPointerException();

		int low = 0;
		int high = lst.size() - 1;

		while (low <= high) {
			final int mid = low + (high - low) / 2;

			final T2 testElem = lst.get(mid);
			final int comparison = elem.compareTo(testElem);

			if (comparison == 0)
				return mid;
			if (comparison > 0) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}

		return -(low + 1);
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
