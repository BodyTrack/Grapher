package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class LoginWidget extends HorizontalPanel {
	Label status = new Label();
	ClickableLink action;
	PopupPanel loginPopup = new PopupPanel();
	LoginDialog loginDialog = new LoginDialog();
	
	LoginWidget() {
		final LoginWidget loginWidget = this;
		this.setStyleName("loginWidget");
		this.add(status);
		action = new ClickableLink("", new ClickHandler() {
			@Override public void onClick(ClickEvent event) { loginWidget.action(); } 
			});
		this.add(action);
	
		loginPopup.add(loginDialog);
		
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
			loginPopup.setPopupPosition(10, 10);
			loginPopup.show();
			loginDialog.execute(this);
		}
	}
	
	public boolean isLoggedIn() {
		return G.username != null;
	}
		
	static class LoginReply extends JavaScriptObject {
		protected LoginReply() {}
		public final native String fail()     /*-{ return this.fail;    }-*/;
		public final native int    user_id()  /*-{ return this.user_id; }-*/;
		public final native String username() /*-{ return this.login;   }-*/;
		public final native void   log()      /*-{ console.log(this);   }-*/;

		static native LoginReply fromJSON(String json) /*-{ eval("var ret="+json); return ret; }-*/;
	}
	
	public void refresh() {
		String url="/login_status.json";
		// TODO: retry, give error messages
		RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
		final LoginWidget loginWidget = this;
		try {
			b.sendRequest(null, new RequestCallback() {
				@Override public void onError(Request request, Throwable e) {}
				@Override public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200) {
						LoginReply reply=LoginReply.fromJSON(response.getText());
						reply.log();
						if (reply.fail() == null) loginWidget.updateWidget(reply.username(), reply.user_id());
					}
				}
			});
		} catch (RequestException e) {}
	}
		
	protected void updateWidget(String username, int user_id) {
		G.username=username;
		G.user_id=user_id;
		if (username == null) {
			status.setText("");
			action.setText("Login");
		} else {
			status.setText(username);
			action.setText("Logout");
		}
	}

	public void login(String username, String password) {
		loginPopup.hide();
		String url="/login.json";
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
						reply.log();
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
		loginDialog.clear();
		String url="/logout";
		// TODO: retry, give error messages
		RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
		final LoginWidget loginWidget=this;
		try {
			b.sendRequest(null, new RequestCallback() {
				@Override public void onError(Request request, Throwable e) {
					Window.alert("Error logging out: " + e);
				}
				@Override public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200) {
						loginWidget.updateWidget(null, 0);
					} else {
						Window.alert("Error logging out: " + response.getText());
					}
				}
			});
		} catch (RequestException e) {
			Window.alert("Error logging out: " + e);
		}		
	}

	public void loginCanceled() {
		loginPopup.hide();
	}	
}
