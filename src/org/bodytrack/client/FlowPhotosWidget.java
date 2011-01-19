package org.bodytrack.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FlowPhotosWidget extends FlowPanel {
	
	private double currentBeginTime;
	private double currentEndTime;
	
	FlowPhotosWidget(double beginTime, double endTime) {
		requestRange(beginTime,endTime);
		currentBeginTime=beginTime;
		currentEndTime=endTime;
	}
	
	static class Photo extends JavaScriptObject {
		protected Photo() {}
		public final native int id()                 /*-{ return this.id;              }-*/;
		public final native double timestamp()       /*-{ return this.begin_d;         }-*/;
		public final native void logme()             /*-{ console.log(this);           }-*/;
		public final native JsArrayString tagArray() /*-{ return this.tags;            }-*/;
		public final native String tags()            /*-{ return this.tags.join(", "); }-*/;
		public final String thumbURL300() { return "/users/" + G.user_id + "/logphotos/"+id()+".300.jpg"; }
	}
	
	static class PhotoWidget extends VerticalPanel {
		private TextBox textBox;
		private Photo photo;
		static ChangeHandler changeHandler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				
				TextBox changed = (TextBox) event.getSource();
				GWT.log("onChange " + changed.getText());
			}};
		
		PhotoWidget(Photo p) {
			final PhotoWidget photoView = this;
			photo=p;
			Image image = new Image(photo.thumbURL300());
			image.setStyleName("photoViewImage");
			image.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					photoView.onMouseOverPhoto();
				}});
			image.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					photoView.onMouseOutPhoto();
				}});
			this.add(image);
			Date d = new Date((long) Math.round(photo.timestamp() * 1000.0));
			Label label = new Label(d.toLocaleString());
			label.setStyleName("photoViewDate");
			this.add(label);
			textBox = new TextBox();
			textBox.setText(photo.tags());
			textBox.setStyleName("photoViewTags");
			textBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					photoView.setTags(photoView.textBox.getText());
				};
			});
			this.add(textBox);
		}
		
		PopupPanel popup;
		
		protected void onMouseOverPhoto() {
			if (popup == null) {
				popup = new PopupPanel();
				popup.add(new Image(photo.thumbURL300()));
				this.add(popup);
			}
			popup.setPopupPosition(this.getAbsoluteLeft(),this.getAbsoluteTop()-300);
			popup.show();
		}
		protected void onMouseOutPhoto() {
			popup.hide();
		}

		public void setTags(String[] tags) {
			GWT.log("setTags " + tags);			
			textBox.setText(StringUtil.join(tags, ", "));
			final String url = "/" + G.user_id + "/tags/" + photo.id() + "/set?tags=" + StringUtil.join(tags, ",");
			GWT.log("about to hit " + url);
			// TODO: this should be .POST
			RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
			try {
				b.sendRequest(null, new RequestCallback() {
					@Override
					public void onError(Request request,
							Throwable e) {
						GWT.log("PVW setTags " + url + ": error from server");
						e.printStackTrace();
					}

					@Override
					public void onResponseReceived(Request request,
							Response response) {
						GWT.log("PVW setTags " + url + ": status " + response.getStatusCode());
					}
				});
			} catch (RequestException e) {
				GWT.log("PVW setTags " + url + ": caught exception");
				e.printStackTrace();
			}		

		}
		
		public void setTags(String tags) {
			setTags(tags.split(",\\s*"));
		}
	}
	
	native JsArray<Photo> buildPhotoArray(String json) /*-{ return eval(json); }-*/;
	
	ArrayList<Photo> photos = new ArrayList<Photo>();
	
	void receiveTile(String json) {
		JsArray<Photo> newpix = buildPhotoArray(json); 
		int ni=0, i=0;
		GWT.log("before receiveTile, we have " + photos.size() + " pix");
		GWT.log("have " + newpix.length() + " new pix");
		while (ni < newpix.length()) {
			if (i == photos.size() || newpix.get(ni).timestamp() < photos.get(i).timestamp()) {
				PhotoWidget pv = new PhotoWidget(newpix.get(ni));
				pv.setStyleName("photoViewFlow");
				this.insert(pv, i);
				photos.add(i++, newpix.get(ni++));
			} else if (newpix.get(ni).id() == photos.get(i).id()) {
				// skip
			} else {
				i++;
			}
		}
		GWT.log("after receiveTile, we have " + photos.size() + " pix");
	}
	
	
	void requestRange(double beginTime, double endTime) {
		double timeLength = endTime-beginTime;
		int level = (int) Math.floor(TileDescription.computeLevel(timeLength));
		TileDescription beginTile = TileDescription.tileAt(level, beginTime);
		TileDescription endTile = TileDescription.tileAt(level, endTime);
		for (int offset = beginTile.getOffset(); offset <= endTile.getOffset(); offset++) {
			final String url = "/photos/1/" + level + "." + offset + ".json";
			GWT.log("PVW requesting " + url);

			RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
			try {
				b.sendRequest(null, new RequestCallback() {
					@Override
					public void onError(Request request,
							Throwable e) {
						GWT.log("PVW " + url + ": error from server");
						e.printStackTrace();
					}

					@Override
					public void onResponseReceived(Request request,
							Response response) {
						if (response.getStatusCode() == 200) {
							GWT.log("PVW " + url + ": status 200; " + response.getText());
							receiveTile(response.getText());
						} else {
							GWT.log("PVW " + url + ": status " + response.getStatusCode());
						}
					}
				});
			} catch (RequestException e) {
				GWT.log("PVW " + url + ": caught exception");
				e.printStackTrace();
			}		
		}
	}
		
	
}
	
	
