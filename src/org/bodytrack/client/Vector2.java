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
	
	public int getIntX(){
		return (int) x;
	}
	
	public int getIntY(){
		return (int) y;
	}

}