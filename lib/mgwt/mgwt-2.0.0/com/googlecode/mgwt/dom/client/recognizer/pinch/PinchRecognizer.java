/*
 * Copyright 2012 Daniel Kurka
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.dom.client.recognizer.pinch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.shared.HasHandlers;

import com.googlecode.mgwt.dom.client.event.touch.TouchCopy;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.dom.client.recognizer.EventPropagator;

/**
 * A PinchRecognizer tracks two finger on a screen that perform a zooming / pinching action
 *
 * @author Daniel Kurka
 *
 */
public class PinchRecognizer implements TouchHandler {

  private static EventPropagator DEFAULT_EVENT_PROPAGATOR;

  private final HasHandlers source;

  private EventPropagator eventPropagator;

  private enum State {
    READY, INVALID, ONE_FINGER, TWO_FINGER;
  }

  private State state;

  private TouchCopy touchStart1;
  private TouchCopy touchStart2;

  private int touchCount;
  private double distance;

  private final OffsetProvider offsetProvider;

  /**
   * Construct a {@link PinchRecognizer}
   *
   * @param source the source to fire events on
   * @param offsetProvider the offset provider
   */
  public PinchRecognizer(HasHandlers source, OffsetProvider offsetProvider) {

    if (source == null) {
      throw new IllegalArgumentException("source can not be null");
    }
    if (offsetProvider == null) {
      throw new IllegalArgumentException("offsetProvider can not be null");
    }

    this.source = source;
    this.offsetProvider = offsetProvider;
    state = State.READY;
  }

  @Override
  public void onTouchStart(TouchStartEvent event) {
    touchCount++;
    switch (state) {
      case READY:
        touchStart1 = TouchCopy.copy(event.getTouches().get(0));
        state = State.ONE_FINGER;
        break;
      case ONE_FINGER:
        touchStart2 = TouchCopy.copy(event.getTouches().get(1));
        distance = (int) Math.sqrt(Math.pow(touchStart1.getPageX() - touchStart2.getPageX(), 2) + Math.pow(touchStart1.getPageY() - touchStart2.getPageY(), 2));
        state = State.TWO_FINGER;
        break;

      default:
        state = State.INVALID;
        break;
    }
  }

  @Override
  public void onTouchMove(TouchMoveEvent event) {
    switch (state) {
      case TWO_FINGER:

        Touch touch1 = event.getTouches().get(0);
        Touch touch2 = event.getTouches().get(1);

        int left = offsetProvider.getLeft();
        int top = offsetProvider.getTop();

        int x1 = touch1.getPageX() - left;
        int y1 = touch1.getPageY() - top;
        int x2 = touch2.getPageX() - left;
        int y2 = touch2.getPageY() - top;

        double newDistance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        getEventPropagator().fireEvent(source, new PinchEvent(x, y, distance / newDistance));
        distance = newDistance;

        break;

      default:
        state = State.INVALID;
        break;
    }
  }

  @Override
  public void onTouchEnd(TouchEndEvent event) {
    touchCount--;
    if (touchCount <= 0) {
      reset();
    } else {
      if (state == State.TWO_FINGER) {
        state = State.ONE_FINGER;
      } else {
        if (touchCount == 2) {
          state = State.TWO_FINGER;
        }
      }
    }
  }

  @Override
  public void onTouchCancel(TouchCancelEvent event) {
    touchCount--;
    if (touchCount <= 0) {
      reset();
    } else {
      if (state == State.TWO_FINGER) {
        state = State.ONE_FINGER;
      } else {
        if (touchCount == 2) {
          state = State.TWO_FINGER;
        }
      }
    }
  }

  private void reset() {
    touchCount = 0;
    state = State.READY;
  }

  // Visible for testing
  EventPropagator getEventPropagator() {
    if (eventPropagator == null) {
      if (DEFAULT_EVENT_PROPAGATOR == null) {
        DEFAULT_EVENT_PROPAGATOR = GWT.create(EventPropagator.class);
      }
      eventPropagator = DEFAULT_EVENT_PROPAGATOR;
    }
    return eventPropagator;
  }
}
