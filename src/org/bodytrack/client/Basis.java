package org.bodytrack.client;

public class Basis {
	public final Vector2 x;
	public final Vector2 y;

	Basis(Vector2 x, Vector2 y) {
		if (x == null || y == null)
			throw new NullPointerException(
				"Null basis vector not allowed");

		this.x = x;
		this.y = y;
	}

	/**
	 * The basis used by Y-axes.
	 */
	public static final Basis xRightYUp =
		new Basis(new Vector2(1, 0), new Vector2(0, -1));

	/**
	 * The basis used by X-axes.
	 */
	public static final Basis xDownYRight =
		new Basis(new Vector2(0, 1), new Vector2(1, 0));

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (! (other instanceof Basis))
			return false;

		Basis otherBasis = (Basis) other;

		// This only works as is because we forbid x and y from being null
		// (the constructor enforces this invariant)
		return this.x.equals(otherBasis.x) && this.y.equals(otherBasis.y);
	}

	@Override
	public int hashCode() {
		return x.hashCode() ^ y.hashCode();
	}
}
