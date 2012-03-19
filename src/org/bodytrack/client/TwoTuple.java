package org.bodytrack.client;

public final class TwoTuple<T1, T2> {
	private final T1 item1;
	private final T2 item2;

	public TwoTuple(T1 item1, T2 item2) {
		this.item1 = item1;
		this.item2 = item2;
	}

	public T1 getItem1() {
		return item1;
	}

	public T2 getItem2() {
		return item2;
	}

	// Convenience method to create a new TwoTuple by taking advantage of
	// the compiler's type inference on generic methods, rather than
	// specifying the types upon object creation
	public static <T1, T2> TwoTuple<T1, T2> create(T1 item1, T2 item2) {
		return new TwoTuple<T1, T2>(item1, item2);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + item1 + ", " + item2 + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item1 == null) ? 0 : item1.hashCode());
		result = prime * result + ((item2 == null) ? 0 : item2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TwoTuple<?, ?>))
			return false;
		TwoTuple<?, ?> other = (TwoTuple<?, ?>) obj;
		if (item1 == null) {
			if (other.item1 != null) {
				return false;
			}
		} else if (!item1.equals(other.item1)) {
			return false;
		}
		if (item2 == null) {
			if (other.item2 != null) {
				return false;
			}
		} else if (!item2.equals(other.item2)) {
			return false;
		}
		return true;
	}
}
