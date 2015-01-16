/*
 * Copyright 2012 Daniel Kurka
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.dom.client.recognizer.swipe;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * base class for all swipte events
 *
 * @author Daniel Kurka
 *
 * @param <H> Handler type of the event
 */
public abstract class SwipeEvent<H extends EventHandler> extends GwtEvent<H> {

	public enum DIRECTION {
		LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP;
	}

	private final DIRECTION direction;

	/**
	 * Construct a swipe event with a given direction
	 *
	 * @param direction the direction to use
	 */
	public SwipeEvent(DIRECTION direction) {
		this.direction = direction;
	}

	/**
	 * the direction of the event
	 *
	 * @return the direction
	 */
	public DIRECTION getDirection() {
		return direction;
	}
}
