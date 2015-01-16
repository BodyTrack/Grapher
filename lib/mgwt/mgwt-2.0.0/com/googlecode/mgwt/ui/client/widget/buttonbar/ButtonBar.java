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
package com.googlecode.mgwt.ui.client.widget.buttonbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import com.googlecode.mgwt.ui.client.widget.base.IsSizeable;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper.Alignment;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper.Justification;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper.Orientation;
import com.googlecode.mgwt.ui.client.widget.panel.flex.IsFlexPanel;

import java.util.Iterator;


public class ButtonBar extends Composite implements IsFlexPanel, HasWidgets.ForIsWidget {

  protected static final ButtonBarAppearance DEFAULT_APPEARANCE = GWT
      .create(ButtonBarAppearance.class);

  @UiField
  public FlexPanel container;

  protected ButtonBarAppearance appearance;

  public ButtonBar() {
    this(DEFAULT_APPEARANCE);
  }

  public ButtonBar(ButtonBarAppearance appearance) {
    this.appearance = appearance;
    initWidget(appearance.barBinder().createAndBindUi(this));
  }

  @Override
  public void add(Widget w) {
    if(w instanceof IsSizeable) {
      IsSizeable isBar = (IsSizeable) w;
      isBar.setSmall(true);
    }
    container.add(w);
  }

  @Override
  public void clear() {
    for(int i = 0; i < container.getWidgetCount(); i++) {
      Widget w = container.getWidget(i);
      if(w instanceof IsSizeable) {
        IsSizeable isBar = (IsSizeable) w;
        isBar.setSmall(false);
      }
    }

    container.clear();
  }

  @Override
  public Iterator<Widget> iterator() {
    return container.iterator();
  }

  @Override
  public boolean remove(Widget w) {
    if(w instanceof IsSizeable) {
      IsSizeable isBar = (IsSizeable) w;
      isBar.setSmall(false);
    }
    return container.remove(w);
  }

  @UiFactory
  public ButtonBarAppearance getAppearance() {
	  return appearance;
  }

  @Override
  public void add(IsWidget w) {
    add(w.asWidget());
  }

  @Override
  public boolean remove(IsWidget w) {
    return remove(w.asWidget());
  }

  @Override
  public void add(Widget widget, int flex) {
    container.add(widget, flex);
  }

  @Override
  public void setOrientation(Orientation value) {
    container.setOrientation(value);
  }

  @Override
  public void setAlignment(Alignment value) {
    container.setAlignment(value);
  }

  @Override
  public void setJustification(Justification value) {
    container.setJustification(value);
  }
}
