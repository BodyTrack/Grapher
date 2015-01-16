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
package com.googlecode.mgwt.dom.client.event.animation;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link AnimationEndEvent} events:
 *
 * @author Daniel Kurka
 */
public interface AnimationEndHandler extends EventHandler {
	/**
	 * Called when a animation end event is fired.
	 *
	 * @param event the {@link AnimationEndEvent} that was fired
	 */
	void onAnimationEnd(AnimationEndEvent event);
}
