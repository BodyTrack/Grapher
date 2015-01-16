package com.googlecode.mgwt.ui.client.util.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;

import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeEvent;
import com.googlecode.mgwt.dom.client.event.orientation.OrientationChangeEvent.ORIENTATION;
import com.googlecode.mgwt.ui.client.util.OrientationHandler;
import com.googlecode.mgwt.ui.client.widget.main.MainResourceHolder;
import com.googlecode.mgwt.ui.client.widget.main.MainResourceAppearance.UtilCss;

public abstract class BaseOrientationHandler implements OrientationHandler {
	private static native JavaScriptObject setupOrientation0(
  		BaseOrientationHandler handler)/*-{
  	var func = $entry(function() {
  		handler.@com.googlecode.mgwt.ui.client.util.impl.BaseOrientationHandler::onorientationChange(I)($wnd.orientation);
  	});
  	$doc.body.onorientationchange = func;
  	return func;
  }-*/;

  private static native void destroyOrientation(JavaScriptObject o)/*-{
  	$doc.body.onorientationchange = null;
  	$doc.removeEventListener("orientationChanged", o);
  }-*/;

  // update styles on body
  	private static void setClasses(ORIENTATION o) {

  	  UtilCss utilCss = MainResourceHolder.getUtilCss();
  		switch (o) {

  		case PORTRAIT:
  			Document.get().getBody().addClassName(utilCss.portrait());
  			Document.get().getBody().removeClassName(utilCss.landscape());
  			break;
  		case LANDSCAPE:
  			Document.get().getBody().addClassName(utilCss.landscape());
  			Document.get().getBody().removeClassName(utilCss.portrait());
  			break;

  		default:
  			break;
  		}
  	}

  protected static ORIENTATION currentOrientation;
	protected static boolean orientationInitialized;
	protected JavaScriptObject nativeJsFunction;

	private EventBus manager;

  @Override
	public final void maybeSetupOrientation(EventBus manager) {
		this.manager = manager;
		if (orientationInitialized)
			return;
		if (!GWT.isClient()) {
			return;
		}
		doSetupOrientation();
		orientationInitialized = true;
		setClasses(getOrientation());
	}

	protected abstract void doSetupOrientation();

	protected void setupNativeBrowerOrientationHandler() {
		nativeJsFunction = setupOrientation0(this);
		Window.addCloseHandler(new CloseHandler<Window>() {

			@Override
			public void onClose(CloseEvent<Window> event) {
				destroyOrientation(nativeJsFunction);
			}
		});
	}

	protected static native int getOrientation0()/*-{
		if (typeof ($wnd.orientation) == 'undefined') {
			return 0;
		}
		return $wnd.orientation;
	}-*/;

  protected static ORIENTATION getBrowserOrientation() {
    int orientation = getOrientation0();

    ORIENTATION o;
    switch (orientation) {
      case 0:
      case 180:
        o = ORIENTATION.PORTRAIT;
        break;

      case 90:
      case -90:
        o = ORIENTATION.LANDSCAPE;
        break;

      default:
        throw new IllegalStateException("this should not happen!?");
    }
    return o;
  }

  void fireOrientationChangedEvent(ORIENTATION orientation) {
  	setClasses(orientation);
  	manager.fireEvent(new OrientationChangeEvent(orientation));
  }

  private void onorientationChange(int orientation) {
  	ORIENTATION o;
  	switch (orientation) {
  	case 0:
  	case 180:
  		o = ORIENTATION.PORTRAIT;
  		break;
  	case 90:
  	case -90:
  		o = ORIENTATION.LANDSCAPE;
  		break;
  	default:
  		o = ORIENTATION.PORTRAIT;
  		break;
  	}
  	currentOrientation = o;
  	fireOrientationChangedEvent(o);
  }
}
