package com.googlecode.mgwt.ui.client.widget.dialog.panel;

import com.google.gwt.uibinder.client.UiFactory;

import com.googlecode.mgwt.ui.client.widget.button.ButtonBase;

public class DialogButton extends ButtonBase {
  private DialogPanelAppearance appearance;

  public DialogButton(DialogPanelAppearance appearance, String text) {
    super(appearance);
    this.appearance = appearance;
    setElement(appearance.uiBinder().createAndBindUi(this));
    setText(text);
  }

  public void setCancel(boolean cancel) {
    removeStyleNames();
    if (cancel) {
      addStyleName(appearance.css().cancelbutton());
    }
  }

  public void setOK(boolean ok) {
    removeStyleNames();
    if (ok) {
      addStyleName(appearance.css().okbutton());
    }
  }

  @UiFactory
  public DialogPanelAppearance getAppearance() {
	  return appearance;
  }

  private void removeStyleNames() {
    removeStyleName(appearance.css().cancelbutton());
    removeStyleName(appearance.css().okbutton());
  }
}
