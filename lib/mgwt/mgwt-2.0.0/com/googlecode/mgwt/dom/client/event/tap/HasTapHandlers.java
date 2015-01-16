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
package com.googlecode.mgwt.dom.client.event.tap;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A widget that implements this interface provides registration for
 * {@link TapHandler} instances.
 *
 * @author Daniel Kurka
 */
public interface HasTapHandlers {
	/**
	 * Adds a {@link TapHandler} handler.
	 *
	 * @param handler the simple touch handler
	 * @return {@link HandlerRegistration} used to remove this handler
	 */
	HandlerRegistration addTapHandler(TapHandler handler);
}
