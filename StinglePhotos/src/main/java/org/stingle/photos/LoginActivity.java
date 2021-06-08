package org.stingle.photos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.stingle.photos.AsyncTasks.LoginAsyncTask;
import org.stingle.photos.Auth.LoginManager;
import org.stingle.photos.Util.Helpers;

public class LoginActivity extends AppCompatActivity {



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Helpers.setLocale(this);
		Helpers.blockScreenshotsIfEnabled(this);

		setContentView(R.layout.activity_login);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		actionBar.setTitle(getString(R.string.sign_in));

		findViewById(R.id.login).setOnClickListener(login());
		findViewById(R.id.register).setOnClickListener(gotoSignUp());
		findViewById(R.id.forgot_password).setOnClickListener(gotoForgotPassword());
		((EditText) findViewById(R.id.password)).setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_GO) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				doLogin();
				return true;
			}
			return false;
		});

		String apiServerUrl = StinglePhotosApplication.getCrypto().getApiServerUrl();
                if (apiServerUrl == "") {
                        apiServerUrl = getString(R.string.api_server_url);
                }
                ((EditText)findViewById(R.id.api_server)).setText(apiServerUrl);
	}

	private OnClickListener login() {
		return v -> doLogin();
	}

	private OnClickListener gotoSignUp() {
		return v -> {
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, SignUpActivity.class);
			startActivity(intent);
			finish();
		};
	}
	private OnClickListener gotoForgotPassword() {
		return v -> {
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, ForgotPasswordActivity.class);
			startActivity(intent);
		};
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		LoginManager.disableLockTimer(this);
	}
	
	private void doLogin() {
		String email = ((EditText) findViewById(R.id.email)).getText().toString();
		String password = ((EditText) findViewById(R.id.password)).getText().toString();
		String apiServerUrl = ((EditText) findViewById(R.id.api_server)).getText().toString();

		if(!Helpers.isValidEmail(email)){
			Helpers.showAlertDialog(this, getString(R.string.error), getString(R.string.invalid_email));
			return;
		}

		if(password.length() < Integer.valueOf(getString(R.string.min_pass_length))){
			Helpers.showAlertDialog(this, getString(R.string.error), String.format(getString(R.string.password_short), getString(R.string.min_pass_length)));
			return;
		}

		StinglePhotosApplication.getCrypto().saveApiServerUrl(apiServerUrl);

		(new LoginAsyncTask(this, email, password)).execute();

	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}
}
