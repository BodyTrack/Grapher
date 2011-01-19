package org.bodytrack.client.Junkyard;

import org.bodytrack.client.LoginWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class BodyTrackWid extends Composite {

	private static BodyTrackWidUiBinder uiBinder = GWT
			.create(BodyTrackWidUiBinder.class);

	interface BodyTrackWidUiBinder extends UiBinder<Widget, BodyTrackWid> {
	}

	@UiField
	LoginWidget loginWidget;
	
	@UiField
	TabLayoutPanel tabLayoutPanel;
	
	public BodyTrackWid() {
		initWidget(uiBinder.createAndBindUi(this));
		tabLayoutPanel.add(new Label("foo"), "foo");
		tabLayoutPanel.add(new HTMLPanel("bar"), "bar");
	}

}
