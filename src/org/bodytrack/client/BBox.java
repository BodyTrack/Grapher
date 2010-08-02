package org.bodytrack.client;

import gwt.g2d.client.math.Vector2;

public class BBox {
	Vector2 min, max;
	BBox(Vector2 min, Vector2 max) {
		this.min = min; this.max = max;
	}
	public boolean contains(double x, double y) {
		return min.getX() <= x && x < max.getX() && min.getY() <= y && y < max.getY();
	}
}
