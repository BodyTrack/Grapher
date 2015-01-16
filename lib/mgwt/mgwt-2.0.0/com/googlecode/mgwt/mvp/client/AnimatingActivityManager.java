/*
 * Copyright 2010 Daniel Kurka
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
package com.googlecode.mgwt.mvp.client;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;

import com.googlecode.mgwt.ui.client.widget.animation.AnimatableDisplay;
import com.googlecode.mgwt.ui.client.widget.animation.Animation;
import com.googlecode.mgwt.ui.client.widget.animation.AnimationEndCallback;

/**
 * This is a fork of @link {@link ActivityManager} that has the same features,
 * but also adds animations to the lifecycle of Activities.
 *
 * It can be used as a replacement for {@link ActivityManager}, but requires an
 * instance of {@link AnimationMapper} to work properly
 *
 * @author Daniel Kurka
 */
public class AnimatingActivityManager implements PlaceChangeEvent.Handler, PlaceChangeRequestEvent.Handler {

	/**
	 * Wraps our real display to prevent an Activity from taking it over if it
	 * is not the currentActivity.
	 */
	private class ProtectedDisplay implements AcceptsOneWidget {
		private final Activity activity;

		public ProtectedDisplay(Activity activity) {
			this.activity = activity;
		}

		public void setWidget(IsWidget view) {
			if (this.activity == AnimatingActivityManager.this.currentActivity) {
				startingNext = false;
				showWidget(view, currentIsFirst);
			} else {
				if (this.activity == AnimatingActivityManager.this.lastActivity) {
					showWidget(view, currentIsFirst);
				}
			}
		}
	}

	private static final Activity NULL_ACTIVITY = new AbstractActivity() {
		@Override
		public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
			panel.setWidget(null);
		}
	};

	private final ActivityMapper mapper;

	private final EventBus eventBus;
	private final ResettableEventBus stopperedEventBus;

	private Activity currentActivity = NULL_ACTIVITY;
	private Place currentPlace;
	private boolean currentIsFirst = false;

	private Activity lastActivity = NULL_ACTIVITY;

	private AnimatableDisplay display;

	private boolean startingNext = false;

	private HandlerRegistration handlerRegistration;

	private final AnimationMapper animationMapper;
	private boolean listeningForAnimationEnd = false;
	private boolean ignorePlaceChange = false;
	private boolean fireAnimationEvents;
	private LinkedList<PlaceChangeEvent> placeChangeStack = new LinkedList<PlaceChangeEvent>();

	/**
	 * Create an ActivityManager. Next call {@link #setDisplay}.
	 *
	 * @param mapper finds the {@link Activity} for a given
	 *            {@link com.google.gwt.place.shared.Place}
	 * @param eventBus source of {@link PlaceChangeEvent} and
	 *            {@link PlaceChangeRequestEvent} events.
	 * @param animationMapper a
	 *            {@link com.googlecode.mgwt.mvp.client.AnimationMapper} object.
	 */
	public AnimatingActivityManager(ActivityMapper mapper, AnimationMapper animationMapper, EventBus eventBus) {
		this.mapper = mapper;
		this.animationMapper = animationMapper;
		this.eventBus = eventBus;
		this.stopperedEventBus = new ResettableEventBus(eventBus);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Deactivate the current activity, find the next one from our
	 * ActivityMapper, and start it.
	 * <p>
	 * The current activity's widget will be hidden immediately, which can cause
	 * flicker if the next activity provides its widget asynchronously. That can
	 * be minimized by decent caching. Perenially slow activities might mitigate
	 * this by providing a widget immediately, with some kind of "loading"
	 * treatment.
	 *
	 * @see com.google.gwt.place.shared.PlaceChangeEvent.Handler#onPlaceChange(PlaceChangeEvent)
	 */
	public void onPlaceChange(PlaceChangeEvent event) {
		if (ignorePlaceChange) {
			//remember the place change event to be executed after the current one is done
			placeChangeStack.add(event);
			return;
		}

		Activity nextActivity = getNextActivity(event);
		Animation animation = getAnimation(event);

		Throwable caughtOnStop = null;
		Throwable caughtOnStart = null;

		if (nextActivity == null) {
			nextActivity = NULL_ACTIVITY;
		}

		if (currentActivity.equals(nextActivity)) {
			return;
		}

		if (startingNext) {
			// The place changed again before the new current activity showed
			// its
			// widget
			currentActivity.onCancel();
			currentActivity = NULL_ACTIVITY;
			startingNext = false;
		} else {

			/*
			 * Kill off the activity's handlers, so it doesn't have to worry
			 * about them accidentally firing as a side effect of its tear down
			 */
			stopperedEventBus.removeHandlers();

			try {
				currentActivity.onStop();
			} catch (Throwable t) {
				caughtOnStop = t;
			} finally {
				/*
				 * And kill them off again in case it was naughty and added new
				 * ones during onstop
				 */
				stopperedEventBus.removeHandlers();
			}
		}

		currentActivity = nextActivity;
		currentPlace = event.getNewPlace();

		if (animation != null) {
			currentIsFirst = !currentIsFirst;
		}

		startingNext = true;

		/*
		 * Now start the thing. Wrap the actual display with a per-call instance
		 * that protects the display from canceled or stopped activities, and
		 * which maintains our startingNext state.
		 */
		try {
			currentActivity.start(new ProtectedDisplay(currentActivity), stopperedEventBus);
		} catch (Throwable t) {
			caughtOnStart = t;
		}

		if (caughtOnStart != null || caughtOnStop != null) {
			Set<Throwable> causes = new LinkedHashSet<Throwable>();
			if (caughtOnStop != null) {
				causes.add(caughtOnStop);
			}
			if (caughtOnStart != null) {
				causes.add(caughtOnStart);
			}

			throw new UmbrellaException(causes);
		}

		// animate
		animate(animation);
	}

	public void setFireAnimationEvents(boolean fireAnimationEvents) {
		this.fireAnimationEvents = fireAnimationEvents;
	}

	public boolean isFireAnimationEvents() {
		return fireAnimationEvents;
	}

	private void onAnimationEnd() {
		if (listeningForAnimationEnd) {

			if (fireAnimationEvents) {
				eventBus.fireEvent(new MGWTAnimationEndEvent());
			}

			if (!placeChangeStack.isEmpty()) {
				//execute deffered so that animation end will fire!
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						ignorePlaceChange = false;
						PlaceChangeEvent poll = placeChangeStack.poll();
						onPlaceChange(poll);
					}
				});

			} else {
				ignorePlaceChange = false;
			}
		}
	}

	private void animate(Animation animation) {

		listeningForAnimationEnd = true;
		ignorePlaceChange = true;

		if (fireAnimationEvents) {
			eventBus.fireEvent(new MGWTAnimationStartEvent());
		}

		if (animation == null) {
			AnimatingActivityManager.this.onAnimationEnd();
			return;
		}

		display.animate(animation, currentIsFirst, new AnimationEndCallback() {

			@Override
			public void onAnimationEnd() {
				AnimatingActivityManager.this.onAnimationEnd();

			}
		});
	}

	private Animation getAnimation(PlaceChangeEvent event) {
		Place newPlace = event.getNewPlace();
		return animationMapper.getAnimation(currentPlace, newPlace);
	}

	public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
		if (!currentActivity.equals(NULL_ACTIVITY)) {
			event.setWarning(currentActivity.mayStop());
		}
	}

	/**
	 * Sets the display for the receiver, and has the side effect of starting or
	 * stopping its monitoring the event bus for place change events.
	 * <p>
	 * If you are disposing of an ActivityManager, it is important to call
	 * setDisplay(null) to get it to deregister from the event bus, so that it
	 * can be garbage collected.
	 *
	 * @param display an instance of AcceptsOneWidget
	 */
	public void setDisplay(AnimatableDisplay display) {
		boolean wasActive = (null != this.display);
		boolean willBeActive = (null != display);
		this.display = display;

		if (wasActive != willBeActive) {
			updateHandlers(willBeActive);
		}

	}

	private Activity getNextActivity(PlaceChangeEvent event) {
		if (display == null) {
			/*
			 * Display may have been nulled during PlaceChangeEvent dispatch.
			 * Don't bother the mapper, just return a null to ensure we shut
			 * down the current activity
			 */
			return null;
		}
		return mapper.getActivity(event.getNewPlace());
	}

	private void showWidget(IsWidget view, boolean first) {
		if (display != null) {
			if (first) {
				display.setFirstWidget(view);
			} else {
				display.setSecondWidget(view);
			}
		}
	}

	private void updateHandlers(boolean activate) {
		if (activate) {
			final com.google.web.bindery.event.shared.HandlerRegistration placeReg = eventBus.addHandler(PlaceChangeEvent.TYPE, this);
			final com.google.web.bindery.event.shared.HandlerRegistration placeRequestReg = eventBus.addHandler(PlaceChangeRequestEvent.TYPE, this);

			this.handlerRegistration = new HandlerRegistration() {
				public void removeHandler() {
					placeReg.removeHandler();
					placeRequestReg.removeHandler();
				}
			};
		} else {
			if (handlerRegistration != null) {
				handlerRegistration.removeHandler();
				handlerRegistration = null;
			}
		}
	}
}
