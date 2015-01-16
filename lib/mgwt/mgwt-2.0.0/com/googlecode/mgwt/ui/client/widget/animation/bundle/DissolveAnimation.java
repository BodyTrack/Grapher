package com.googlecode.mgwt.ui.client.widget.animation.bundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import com.googlecode.mgwt.ui.client.widget.animation.Animation;

public class DissolveAnimation extends AnimationBase {

  interface Bundle extends ClientBundle {
    @Source({"dissolve.css"})
    CSS css();
  }

  public interface CSS extends Animation.AnimationCss, CssResource {
    @Override
    String in();

    @Override
    String out();

    @Override
    String reverse();
  }

  private static final Bundle bundle = GWT.create(Bundle.class);

  static {
    bundle.css().ensureInjected();
  }

  public DissolveAnimation(boolean reversed) {
    super("mgwt-dissolve", reversed);
  }

  @Override
  public CSS css() {
    return bundle.css();
  }
}