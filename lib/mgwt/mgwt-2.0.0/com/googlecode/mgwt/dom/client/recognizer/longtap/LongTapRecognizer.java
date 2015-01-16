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
package com.googlecode.mgwt.dom.client.recognizer.longtap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.shared.HasHandlers;

import com.googlecode.mgwt.collection.shared.CollectionFactory;
import com.googlecode.mgwt.collection.shared.LightArray;
import com.googlecode.mgwt.dom.client.event.touch.TouchCopy;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.dom.client.recognizer.EventPropagator;
import com.googlecode.mgwt.dom.client.recognizer.TimerExecturGwtTimerImpl;
import com.googlecode.mgwt.dom.client.recognizer.TimerExecutor;
import com.googlecode.mgwt.dom.client.recognizer.TimerExecutor.CodeToRun;

/**
 * This class can recognize long taps
 *
 * @author Daniel Kurka
 */
public class LongTapRecognizer implements TouchHandler {

  public static final int DEFAULT_WAIT_TIME_IN_MS = 1500;
  public static final int DEFAULT_MAX_DISTANCE = 15;

  protected enum State {
    INVALID, READY, FINGERS_DOWN, FINGERS_UP, WAITING
  };

  protected State state;
  private final HasHandlers source;
  private final int numberOfFingers;
  private final int time;

  private LightArray<TouchCopy> startPositions;
  private int touchCount;
  private final int distance;

  private TimerExecutor timerExecutor;

  private EventPropagator eventPropagator;

  private static EventPropagator DEFAULT_EVENT_PROPAGATOR;

  public LongTapRecognizer(HasHandlers source) {
    this(source, 1);
  }

  public LongTapRecognizer(HasHandlers source, int numberOfFingers) {
    this(source, numberOfFingers, DEFAULT_WAIT_TIME_IN_MS);
  }

  /**
   * Construct a LongTapRecognizer
   *
   * @param source the source on which to fire events on
   * @param numberOfFingers the number of fingers that should be detected
   * @param time the time the fingers need to touch
   */
  public LongTapRecognizer(HasHandlers source, int numberOfFingers, int time) {
    this(source, numberOfFingers, time, DEFAULT_MAX_DISTANCE);
  }

  /**
   * Construct a LongTapRecognizer
   *
   * @param source the source on which to fire events on
   * @param numberOfFingers the number of fingers that should be detected
   * @param time the time the fingers need to touch
   * @param maxDistance the maximum distance each finger is allowed to move
   */
  public LongTapRecognizer(HasHandlers source, int numberOfFingers, int time, int maxDistance) {

    if (source == null) {
      throw new IllegalArgumentException("source can not be null");
    }
    if (numberOfFingers < 1) {
      throw new IllegalArgumentException("numberOfFingers > 0");
    }

    if (time < 200) {
      throw new IllegalArgumentException("time > 200");
    }

    if (maxDistance < 0) {
      throw new IllegalArgumentException("maxDistance > 0");
    }

    this.source = source;
    this.numberOfFingers = numberOfFingers;
    this.time = time;
    this.distance = maxDistance;

    state = State.READY;
    startPositions = CollectionFactory.constructArray();
    touchCount = 0;
  }

  @Override
  public void onTouchStart(TouchStartEvent event) {

    JsArray<Touch> touches = event.getTouches();
    touchCount++;

    switch (state) {
      case INVALID:
        break;
      case READY:
        startPositions.push(TouchCopy.copy((touches.get(touchCount - 1))));
        state = State.FINGERS_DOWN;
        break;
      case FINGERS_DOWN:
        startPositions.push(TouchCopy.copy(touches.get(touchCount - 1)));
        break;
      case FINGERS_UP:
      default:
        state = State.INVALID;
        break;
    }

    if (touchCount == numberOfFingers) {
      state = State.WAITING;
      getTimerExecutor().execute(new CodeToRun() {

        @Override
        public void onExecution() {
          if (state != State.WAITING) {
            // something else happened forget it
            return;
          }

          getEventPropagator().fireEvent(source, new LongTapEvent(source, numberOfFingers, time, startPositions));
          reset();

        }
      }, time);
    }

    if (touchCount > numberOfFingers) {
      state = State.INVALID;
    }
  }

  @Override
  public void onTouchMove(TouchMoveEvent event) {
    switch (state) {
      case WAITING:
      case FINGERS_DOWN:
      case FINGERS_UP:
        // compare positions
        JsArray<Touch> currentTouches = event.getTouches();
        for (int i = 0; i < currentTouches.length(); i++) {
          Touch currentTouch = currentTouches.get(i);
          for (int j = 0; j < startPositions.length(); j++) {
            TouchCopy startTouch = startPositions.get(j);
            if (currentTouch.getIdentifier() == startTouch.getIdentifier()) {
              if (Math.abs(currentTouch.getPageX() - startTouch.getPageX()) > distance || Math.abs(currentTouch.getPageY() - startTouch.getPageY()) > distance) {
                state = State.INVALID;
                break;
              }
            }
            if (state == State.INVALID) {
              break;
            }
          }
        }

        break;

      default:
        state = State.INVALID;
        break;
    }
  }

  @Override
  public void onTouchEnd(TouchEndEvent event) {
    int currentTouches = event.getTouches().length();
    switch (state) {
      case WAITING:
        state = State.INVALID;
        break;

      case FINGERS_DOWN:
        state = State.FINGERS_UP;
        break;
      case FINGERS_UP:
        // are we ready?
        if (currentTouches == 0 && touchCount == numberOfFingers) {
          // fire and reset

          reset();
        }
        break;

      case INVALID:
      default:
        if (currentTouches == 0)
          reset();
        break;
    }
  }

  @Override
  public void onTouchCancel(TouchCancelEvent event) {
    state = State.INVALID;
    int currentTouches = event.getTouches().length();
    if (currentTouches == 0) {
      reset();
    }
  }

  protected void reset() {
    state = State.READY;
    touchCount = 0;
  }

  // Visible for testing
  TimerExecutor getTimerExecutor() {
    if (timerExecutor == null) {
      timerExecutor = new TimerExecturGwtTimerImpl();
    }
    return timerExecutor;
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
