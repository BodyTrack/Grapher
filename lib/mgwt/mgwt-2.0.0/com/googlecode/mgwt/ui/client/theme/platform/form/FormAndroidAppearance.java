package com.googlecode.mgwt.ui.client.theme.platform.form;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;

import com.googlecode.mgwt.ui.client.widget.form.FormAbstractAppearance;

public class FormAndroidAppearance extends FormAbstractAppearance {

  static {
    Resources.INSTANCE.css().ensureInjected();
  }

  interface Css extends FormCss {}

  interface Resources extends ClientBundle {

    Resources INSTANCE = GWT.create(Resources.class);

    @Source({"com/googlecode/mgwt/ui/client/widget/form/form.css", "form-android.css"})
    Css css();
  }

  @Override
  public FormCss css() {
    return Resources.INSTANCE.css();
  }
}
