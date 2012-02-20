package org.bodytrack.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;

public class NativeObjectSet<T extends JavaScriptObject> implements Set<T> {

	public interface EqualsHashcodeProvider<U> {
		boolean equals(U obj1, U obj2);
		int hashCode(U obj);
	}

	private final EqualsHashcodeProvider<T> provider;
	private final Set<Wrapper<T>> wrappedSet;

	public NativeObjectSet(final EqualsHashcodeProvider<T> provider) {
		this.provider = provider;
		wrappedSet = new HashSet<Wrapper<T>>();
	}

	@Override
	public boolean add(T e) {
		return wrappedSet.add(new Wrapper<T>(provider, e));
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		wrappedSet.clear();
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof JavaScriptObject))
			return false;

		@SuppressWarnings("unchecked")
		T e = (T)(JavaScriptObject)o;

		return wrappedSet.contains(new Wrapper<T>(provider, e));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return wrappedSet.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return wrappedSet.size();
	}

	@Override
	public Object[] toArray() {
		@SuppressWarnings("unchecked")
		Wrapper<T>[] wrapped = (Wrapper<T>[])wrappedSet.toArray();

		Object[] result = new Object[wrapped.length];
		for (int i = 0; i < wrapped.length; i++)
			result[i] = wrapped[i].getObject();

		return result;
	}

	@Override
	public <U> U[] toArray(U[] a) {
		throw new UnsupportedOperationException();
	}

	private static class Wrapper<U extends JavaScriptObject> {
		private final EqualsHashcodeProvider<U> provider;
		private final U underlyingObject;

		public Wrapper(EqualsHashcodeProvider<U> provider, U underlyingObject) {
			this.provider = provider;
			this.underlyingObject = underlyingObject;
		}

		public U getObject() {
			return underlyingObject;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Wrapper<?>))
				return false;

			Wrapper<?> other = (Wrapper<?>)obj;

			@SuppressWarnings("unchecked")
			U otherObj = (U)other.underlyingObject;

			return provider.equals(underlyingObject, otherObj);
		}

		@Override
		public int hashCode() {
			return provider.hashCode(underlyingObject);
		}
	}
}
