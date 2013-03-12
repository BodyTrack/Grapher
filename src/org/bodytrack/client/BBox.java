package org.bodytrack.client;

public class BBox {
	Vector2 min, max;
	BBox(Vector2 min, Vector2 max) {
		this.min = min; this.max = max;
	}
	public boolean contains(double x, double y) {
		return min.getX() <= x && x < max.getX() && min.getY() <= y && y < max.getY();
	}
	public boolean contains(Vector2 pos) {
		return contains(pos.getX(), pos.getY());
	}
}
