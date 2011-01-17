package org.bodytrack.client;

import org.bodytrack.client.FlowPhotosWidget.Photo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class LoginWidget extends HorizontalPanel {
	Label status = new Label();
	HTML action = new HTML();
	PopupPanel loginPopup = new PopupPanel();
	
	LoginWidget() {
		final LoginWidget loginWidget = this;
		this.setStyleName("loginWidget");
		this.add(status);
		action.addClickHandler(new ClickHandler() {
			@Override public void onClick(ClickEvent event) { loginWidget.action(); } 
			});
		this.add(action);
	
		updateWidget(null, 0);
		refresh();
	}

	private void initializePopup() {
		loginPopup = new PopupPanel();
	}

	public void action() {
		if (isLoggedIn()) {
			logout();
		} else {
			login("rsargent", "bodytrack ftw");
		}
	}
	
	
	private String username;
	private int uid;
		
	public boolean isLoggedIn() {
		return username != null;
	}
	public String getUsername() {
		return username;
	}
	public int getUid() {
		return uid;
	}
		
	static class LoginReply extends JavaScriptObject {
		protected LoginReply() {}
		public final native String fail() /*-{ return this.fail; }-*/;
		public final native int user_id() /*-{ return this.user_id; }-*/;
		public final String username() { return "UID."+user_id(); }
		//public final native void log() /*-{ console.log(this); }-*/;

		static native LoginReply fromJSON(String json) /*-{ eval("var ret="+json); return ret; }-*/;
	}
	
	public void refresh() {
		String url=G.rootUrl() + "/login_status.json";
		// TODO: retry, give error messages
		RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
		final LoginWidget loginWidget = this;
		try {
			b.sendRequest(null, new RequestCallback() {
				@Override public void onError(Request request, Throwable e) {}
				@Override public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200) {
						LoginReply reply=LoginReply.fromJSON(response.getText());
						if (reply.fail() == null) loginWidget.updateWidget(reply.username(), reply.user_id());
					}
				}
			});
		} catch (RequestException e) {}
	}
		
	protected void updateWidget(String username, int user_id) {
		GWT.log("updateWidget " + username + " " + user_id);
		this.username=username;
		this.uid=user_id;
		if (username == null) {
			status.setText("");
			action.setHTML("<a href=\"javascript:\">Login</a>");
		} else {
			status.setText(username);
			action.setHTML("<a href=\"javascript:\">Logout</a>");
		}
	}

	public void login(String username, String password) {
		String url=G.rootUrl() + "/login.json";
		String postData = URL.encodeComponent("login")+"="+URL.encodeComponent(username);
		postData += "&" + URL.encodeComponent("password")+"="+URL.encodeComponent(password);
		RequestBuilder b = new RequestBuilder(RequestBuilder.POST, url);
		b.setHeader("Content-Type", "application/x-www-form-urlencoded");
		final LoginWidget loginWidget = this;
		try {
			b.sendRequest(postData, new RequestCallback() {
				@Override public void onError(Request request, Throwable e) {
					Window.alert("Error logging in: " + e);
				}
				@Override public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200) {
						LoginReply reply=LoginReply.fromJSON(response.getText());
						if (reply.fail() != null) {
							Window.alert("Error logging in: " + reply.fail());
						} else {
							loginWidget.updateWidget(reply.username(), reply.user_id());
						}
					} else {
						Window.alert("Error logging in: " + response.getText());
					}
				}
			});
		} catch (RequestException e) {
			Window.alert("Error logging in: " + e);
		}			
	}
	
	public void logout() {
		String url=G.rootUrl() + "/logout";
		// TODO: retry, give error messages
		RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
		final LoginWidget loginWidget=this;
		try {
			b.sendRequest(null, new RequestCallback() {
				@Override public void onError(Request request, Throwable e) {
					Window.alert("Error logging out: " + e);
				}
				@Override public void onResponseReceived(Request request, Response response) {
					GWT.log("logout response received");
					if (response.getStatusCode() == 200) {
						loginWidget.updateWidget(null, 0);
					} else {
						Window.alert("Error logging out: " + response.getText());
					}
					GWT.log("logout: " + response.getStatusCode() + "; " + response.getText());
				}
			});
		} catch (RequestException e) {
			Window.alert("Error logging out: " + e);
		}		
	}	
}
