package com.urbanlaunchpad.newmarket;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.urbanlaunchpad.newmarket.helpers.typefaceHelper;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.Arrays;

public class IniconfigActivity extends Activity implements View.OnClickListener {

	public static final int REQUEST_ACCOUNT_PICKER = 1;
	public static final int REQUEST_PERMISSIONS = 2;
	public static final String FUSION_TABLE_SCOPE = "https://www.googleapis.com/auth/fusiontables";
	public static final String FUSIONTABLES_READONLY_SCOPE = "https://www.googleapis.com/auth/fusiontables.readonly";
	public static final String API_KEY = "AIzaSyDaKmbcfkO82DeRHgJA4Mwwt1mBJ9_Hrx0";
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	// public static final String API_KEY =
	// "AIzaSyCqPamoXPMqdosIdnXSMk0Gl82fdGie0gs";

	// Global instance of the JSON factory.

	public static String username = "";
	// by default.
	public static GoogleAccountCredential credential;
	public static Fusiontables fusiontables;
	public static SharedPreferences prefs;
	ImageView cont;
	AutoCompleteTextView input;
	String jsonsurveystring;
	protected TextView usernameField;
	@SuppressLint("HandlerLeak")
	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == EVENT_TYPE.GOT_USERNAME.ordinal()) {
				// have input a name. update it on interface
				usernameField.setText(username);
				cont.setVisibility(View.VISIBLE);
				findViewById(R.id.bcontinue).setClickable(true);
			} else {
				Log.e("Survey Parser", "Error parsing survey");
			}
		}
	};
	AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_iniconfig);
		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Check the preferences saving.
		prefs = this.getSharedPreferences("com.urbanlaunchpad.newmarket",
				Context.MODE_PRIVATE);
		// Custom typefaces.
		TextView appName = (TextView) typefaceHelper.setCustomTypeface(
				findViewById(R.id.app_name), this);
		TextView version = (TextView) typefaceHelper.setCustomTypeface(
				findViewById(R.id.version), this);
		usernameField = (TextView) typefaceHelper.setCustomTypeface(
				findViewById(R.id.usernameText), this);
		Button loginButtonView = (Button) typefaceHelper.setCustomTypeface(
				findViewById(R.id.login_button), this);
		// TODO handle saved username preferences.
		if (prefs.contains("username")) {
		}

		// set listeners and disable continue button

		cont = (ImageView) findViewById(R.id.bcontinue);
		cont.setVisibility(View.GONE);
		loginButtonView.setOnClickListener(this);
		cont.setOnClickListener(this);

		// get credential with scopes
		credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(
				FUSION_TABLE_SCOPE, FUSIONTABLES_READONLY_SCOPE,
				DriveScopes.DRIVE));
	}

	@Override
	public void onClick(View view) {
		Integer id = view.getId();
		if (id == R.id.login_button) {
			// Google credentials
			startActivityForResult(credential.newChooseAccountIntent(),
					REQUEST_ACCOUNT_PICKER);
		} else if (id == R.id.bcontinue) {
			// Go to runs activity
			Intent i = new Intent(getApplicationContext(), RunsActivity.class);
			startActivity(i);
		}
	}

	public boolean getSurvey(String tableId) throws ClientProtocolException,
			IOException, UserRecoverableAuthIOException {
		String MASTER_TABLE_ID = "1isCCC51fe6nWx27aYWKfZWmk9w2Zj6a4yTyQ5c4";
		Sql sql = fusiontables.query().sql(
				"SELECT survey_json FROM " + MASTER_TABLE_ID
						+ " WHERE table_id = '" + tableId + "'");
		sql.setKey(API_KEY);

		Sqlresponse response = sql.execute();
		if (response == null) {
			return false;
		}

		jsonsurveystring = response.getRows().get(0).get(0).toString();

		// save this for offline use
		prefs.edit().putString("jsonsurveystring", jsonsurveystring).commit();
		Log.v("response", jsonsurveystring);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null
					&& data.getExtras() != null) {
				username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				credential.setSelectedAccountName(username);
				fusiontables = new Fusiontables.Builder(HTTP_TRANSPORT,
						JSON_FACTORY, credential).setApplicationName(
						"NewMarket").build();
				// fusiontables = new Fusiontables.Builder(HTTP_TRANSPORT,
				// JSON_FACTORY, credential).setApplicationName(
				// "NewMarket").build();

				// update our username field
				messageHandler.sendEmptyMessage(EVENT_TYPE.GOT_USERNAME
						.ordinal());
			}
			break;
		}
	}

	// Username selection helper functions

	private enum EVENT_TYPE {
		GOT_USERNAME
	}

}
