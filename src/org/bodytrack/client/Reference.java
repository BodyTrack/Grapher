package org.bodytrack.client;

/**
 * A class that wraps an object to simulate passing that object by reference
 *
 * <p>In non-GWT code, use AtomicReference, which has nice thread safety
 * properties.  However, since GWT compiles Java into JavaScript, which is a
 * single-threaded language, there is no need for atomicity in an
 * implementation of pass by reference semantics.</p>
 *
 * @param <T>
 * 	The type of the object this wraps
 */
public class Reference<T> {
	private T obj;

	public Reference(T obj) {
		this.obj = obj;
	}

	public T get() {
		return obj;
	}

	public void set(T obj) {
		this.obj = obj;
	}

	@Override
	public int hashCode() {
		return (obj == null) ? 0 : obj.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof Reference<?>))
			return false;
		Reference<?> other = (Reference<?>)o;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return (obj == null) ? "" : obj.toString();
	}
}
