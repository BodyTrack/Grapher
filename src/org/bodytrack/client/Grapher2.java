package org.bodytrack.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Grapher2 implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		InfoPublisher.setUpWrappers();
	}

	// TODO: Remove the dependence on this method
	public static int getAxisMargin() {
		return 10;
	}
}
