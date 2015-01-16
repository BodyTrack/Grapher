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

import com.google.gwt.event.shared.GwtEvent;

import com.googlecode.mgwt.dom.client.event.touch.TouchCopy;

/**
 * A {@link SwipeStartEvent} is fired when the user moves his finger over a
 * certain amount on the display
 *
 * @author Daniel Kurka
 */
public class SwipeStartEvent extends SwipeEvent<SwipeStartHandler> {

	private final static GwtEvent.Type<SwipeStartHandler> TYPE = new Type<SwipeStartHandler>();
	private final int distance;
	private final TouchCopy touch;

	public static GwtEvent.Type<SwipeStartHandler> getType() {
		return TYPE;
	}

	/**
	 * Construct a {@link SwipeStartEvent}
	 *
	 * @param distance the distance the finger already moved
	 * @param j
	 * @param i
	 * @param direction the direction of the finger
	 */
	public SwipeStartEvent(TouchCopy touch, int distance, SwipeEvent.DIRECTION direction) {
		super(direction);
		this.touch = touch;
		this.distance = distance;
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SwipeStartHandler> getAssociatedType() {
		return TYPE;
	}

	/**
	 * The distance the finger moved before the event occured
	 *
	 * @return the distance in px
	 */
	public int getDistance() {
		return distance;
	}

	@Override
	protected void dispatch(SwipeStartHandler handler) {
		handler.onSwipeStart(this);
	}

	public TouchCopy getTouch() {
		return touch;
	}
}
