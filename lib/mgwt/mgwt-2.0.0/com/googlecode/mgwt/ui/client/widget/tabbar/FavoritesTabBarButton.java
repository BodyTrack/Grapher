/*
 * Copyright 2011 Daniel Kurka
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
package com.googlecode.mgwt.ui.client.widget.tabbar;

import com.google.gwt.dom.client.Style.Unit;

import com.googlecode.mgwt.ui.client.widget.tabbar.resources.TabBarImageHolder;

/**
 * A simple favorites tab bar button.
 *
 * @author Daniel Kurka
 *
 */
public class FavoritesTabBarButton extends TabBarButton {

	public FavoritesTabBarButton() {
		this(TabPanel.DEFAULT_APPEARANCE);
	}

  /**
   * Construct a BookmarkTabBarButton with a given css
   *
   * @param css the css to use
   */
	public FavoritesTabBarButton(TabBarAppearance appearance) {
		super(appearance, TabBarImageHolder.get().favorites(),
		    TabBarImageHolder.get().favoritesSelected());
		setText("Favorites");
		text.getStyle().setTop(1, Unit.PX);
	}
}