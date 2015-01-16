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

import com.google.gwt.user.client.Timer;

/**
 * Execute code with a GWT timer
 *
 * @author Daniel Kurka
 *
 */
public class TimerExecturGwtTimerImpl implements TimerExecutor {

	private static class InternalTimer extends Timer {

		private final CodeToRun codeToRun;

		public InternalTimer(CodeToRun codeToRun) {
			this.codeToRun = codeToRun;
		}

		@Override
		public void run() {
			codeToRun.onExecution();
		}
	}

	@Override
	public void execute(final CodeToRun codeToRun, int time) {
		new InternalTimer(codeToRun).schedule(time);
	}
}
