package org.bodytrack.client;

public class Vector2 {
	private double x, y;
	
	public static final Vector2 ZERO = new Vector2(0,0);
	
	public Vector2(double x,double y){
		this.x = x;
		this.y = y;
	}
	
	public Vector2(Vector2 v){
		this.x = v.x;
		this.y = v.y;
	}
	
	public double dot(Vector2 v){
		return x * v.x + y * v.y;	
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public Vector2 scale(double value){
		return new Vector2(x * value, y * value);
	}
	
	public Vector2 add(Vector2 v){
		return new Vector2(x + v.x,y + v.y);
	}
	
	public Vector2 subtract(Vector2 v){
		return new Vector2(x - v.x, y- v.y);
	}

	public double distanceSquared(Vector2 v) {
		return Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2);
	}
	
	public double distance(Vector2 v){
		return Math.sqrt(distanceSquared(v));
	}
	
	public double magnitudeSquared(){
		return Math.pow(x, 2) + Math.pow(y,2);
	}
	
	public double magnitude(){
		return Math.sqrt(magnitudeSquared());
	}
	
	public int getIntX(){
		return (int) x;
	}
	
	public int getIntY(){
		return (int) y;
	}
	
	public boolean isInside(Vector2 topLeft, Vector2 bottomRight){
		return x >= topLeft.x && x <= bottomRight.x && y >= topLeft.y && y <= bottomRight.y;
	}

}
