package org.bodytrack.client;

import gwt.g2d.client.math.Vector2;

public class Basis {
	public Vector2 x;
	public Vector2 y;

	Basis(Vector2 x, Vector2 y) {
		this.x = x;
		this.y = y;
	}

	public static final Basis xRightYUp =
		new Basis(new Vector2(1, 0), new Vector2(0, -1));
	public static final Basis xDownYRight =
		new Basis(new Vector2(0, 1), new Vector2(1, 0));
}
