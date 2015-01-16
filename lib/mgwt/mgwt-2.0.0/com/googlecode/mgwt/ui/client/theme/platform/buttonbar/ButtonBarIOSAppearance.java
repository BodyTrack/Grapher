package com.googlecode.mgwt.ui.client.theme.platform.buttonbar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;

import com.googlecode.mgwt.ui.client.widget.buttonbar.ButtonBarAbstractAppearance;

public class ButtonBarIOSAppearance extends ButtonBarAbstractAppearance {

  static {
    Resources.INSTANCE.barCss().ensureInjected();
  }

  interface Css extends ButtonBarCss {}

  interface Resources extends ClientBundle {

    Resources INSTANCE = GWT.create(Resources.class);

    @Source({"com/googlecode/mgwt/ui/client/widget/buttonbar/buttonbar.css", "buttonbar-ios.css"})
    Css barCss();
  }

  @Override
  public ButtonBarCss barCss() {
    return Resources.INSTANCE.barCss();
  }
}
