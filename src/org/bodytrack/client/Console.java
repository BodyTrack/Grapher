package org.bodytrack.client;

public class Console {
	
	public static native void log(String message)/*-{
		console.log(message);
	}-*/;
	
	public static native void debug(String message)/*-{
		console.debug(message);
	}-*/;
	
	public static native void error(String message)/*-{
		console.error(message);
	}-*/;
	
	public static native void warn(String message)/*-{
		console.warn(message);
	}-*/;
	
	public static void log(int message){
		log(message + "");
	}
	
	public static void debug(int message){
		debug(message + "");
	}
	
	public static void error(int message){
		error(message + "");
	}
	
	public static void warn(int message){
		warn(message + "");
	}
	
	public static void log(double message){
		log(message + "");
	}
	
	public static void debug(double message){
		debug(message + "");
	}
	
	public static void error(double message){
		error(message + "");
	}
	
	public static void warn(double message){
		warn(message + "");
	}

}
