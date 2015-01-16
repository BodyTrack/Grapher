/*
 * Copyright 2014 Daniel Kurka
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
package com.googlecode.mgwt.ui.client.widget.panel.flex;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Panel;

public class FlexPanelDefaultAppearance implements FlexPanelAppearance {
  static {
    Resources.INSTANCE.css().ensureInjected();
  }

  interface Resources extends ClientBundle {

    Resources INSTANCE = GWT.create(Resources.class);

    @Source({"flex.css"})
    FlexPanelCss css();
  }

  @UiTemplate("FlexPanelBaseAppearance.ui.xml")
  interface Binder extends UiBinder<Panel, FlexPanel> {
  }

  private static final Binder UI_BINDER = GWT.create(Binder.class);

  @Override
  public UiBinder<Panel, FlexPanel> uiBinder() {
    return UI_BINDER;
  }

  @Override
  public FlexPanelCss css() {
    return Resources.INSTANCE.css();
  }
}
