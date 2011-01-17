package org.bodytrack.client;

import com.google.gwt.user.client.ui.VerticalPanel;

public class BodyTrackWidget extends VerticalPanel {
	LoginWidget loginWidget;
	FlowPhotosWidget flowPhotosWidget;
	
	BodyTrackWidget() {
		loginWidget = new LoginWidget();
		this.add(loginWidget);
		// TODO: select between different widgets
		//flowPhotosWidget = new FlowPhotosWidget(1293861600-86400*15, 1293861600);
		//this.add(flowPhotosWidget);
	}
}
