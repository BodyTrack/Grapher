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
package com.googlecode.mgwt.dom.client.recognizer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Propagate events from a source. There is an issue on mobile webkit which gets
 * confused about events if an alert is shown from an event handler, see:
 * http://
 * blog.daniel-kurka.de/2012/05/mobile-webkit-alert-dialog-breaks-touch.html
 *
 * This class provides a workaround by propagating events with a
 * ScheduledCommand
 *
 * @author Daniel Kurka
 *
 */
public class EventPropagatorMobileImpl implements EventPropagator {

	private static class SCommand implements ScheduledCommand {
		private final HasHandlers source;
		private final GwtEvent<?> event;

		public SCommand(HasHandlers source, GwtEvent<?> event) {
			this.source = source;
			this.event = event;
		}

		@Override
		public void execute() {
			source.fireEvent(event);
		}
	}

	@Override
	public void fireEvent(final HasHandlers source, final GwtEvent<?> event) {
		// see issue 135
		// http://code.google.com/p/mgwt/issues/detail?id=135
		Scheduler.get().scheduleDeferred(new SCommand(source, event));
	}
}
