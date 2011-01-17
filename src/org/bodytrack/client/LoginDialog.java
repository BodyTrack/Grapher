package org.bodytrack.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginDialog extends Composite {

	private static LoginDialogUiBinder uiBinder = GWT
			.create(LoginDialogUiBinder.class);

	interface LoginDialogUiBinder extends UiBinder<Widget, LoginDialog> {
	}

	public LoginDialog() {
		initWidget(uiBinder.createAndBindUi(this));
		// Enter key in username skips to password
		username.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == '\r') password.setFocus(true);
			}});
		// Enter key in password submits
		password.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == '\r') submit();
			}});
	}

	private LoginWidget loginWidget;
	
	@UiField
	Button login;
	
	@UiField
	Button cancel;
	
	@UiField
	TextBox username;
	
	@UiField
	PasswordTextBox password;

	public void clear() {
		username.setText("");
		password.setText("");
	}
	
	public void execute(LoginWidget w) {
		loginWidget = w;
		username.setFocus(true);
	}
	
	@UiHandler("login")
	void loginClick(ClickEvent e) {
		submit();
	}
	
	void submit() {
		loginWidget.login(username.getText(), password.getText());
		clear();
	}

	@UiHandler("cancel")
	void cancelClick(ClickEvent e) {
		loginWidget.loginCanceled();
		clear();
	}

}
