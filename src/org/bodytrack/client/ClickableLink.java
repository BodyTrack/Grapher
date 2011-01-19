package org.bodytrack.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class ClickableLink extends HTML {
	ClickableLink(String text, ClickHandler clickHandler) {
		this.setStyleName("clickableLink");
		setText(text);
		this.addClickHandler(clickHandler);
	}
	@Override
	public
	void setText(String name) {
		setHTML("<a href=\"javascript:''\">" + name + "</a>");
	}
}
