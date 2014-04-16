package com.urbanlaunchpad.newmarket;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class IniconfigActivity extends Activity implements View.OnClickListener {

	public static final int REQUEST_ACCOUNT_PICKER = 1;
	public static final int REQUEST_PERMISSIONS = 2;
	public static final String FUSION_TABLE_SCOPE = "https://www.googleapis.com/auth/fusiontables";
	public static final String API_KEY = "AIzaSyDaKmbcfkO82DeRHgJA4Mwwt1mBJ9_Hrx0";
	
	// Global instance of the HTTP transport.
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	// Global instance of the JSON factory.

	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	public static String username = "";
	// by default.
	public static GoogleAccountCredential credential;
	public static Fusiontables fusiontables;
	public static SharedPreferences prefs;
	TextView usernameField;
	ImageView cont;
	AutoCompleteTextView input;
	String jsonsurveystring;
	JSONObject jsurv = null;
	@SuppressLint("HandlerLeak")
	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == EVENT_TYPE.GOT_USERNAME.ordinal()) {
				// have input a name. update it on interface
				usernameField.setText(username);
				cont.setVisibility(View.VISIBLE);
				findViewById(R.id.bcontinue).setClickable(true);
			} else if (msg.what == EVENT_TYPE.PARSED_CORRECTLY.ordinal()) {
				RelativeLayout navBar = (RelativeLayout) findViewById(R.id.iniconfig_navbar);
				navBar.removeViewAt(0);
				
				// got survey!
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.survey_parsed, Toast.LENGTH_SHORT);
				toast.show();
			} else if (msg.what == EVENT_TYPE.PARSED_INCORRECTLY.ordinal()) {
				RelativeLayout navBar = (RelativeLayout) findViewById(R.id.iniconfig_navbar);
				navBar.removeViewAt(0);
				findViewById(R.id.bcontinue).setClickable(true);

				// got bad/no survey!
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.no_survey_obtained, Toast.LENGTH_SHORT);
				toast.show();
				jsurv = null;
			}  else {
				Log.e("Survey Parser", "Error parsing survey");
			}
		}
	};
	AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iniconfig);

		prefs = this.getSharedPreferences("org.urbanlaunchpad.flocktracker",
				Context.MODE_PRIVATE);
		// initialize fields
		usernameField = (TextView) findViewById(R.id.usernameText);

		// initialize dialog for inputting project name
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.select_project);

		input = new AutoCompleteTextView(this);

		if (prefs.contains("lastProject")) {
			// Create the adapter and set it to the AutoCompleteTextView
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1,
					new String[] { prefs.getString("lastProject", "") });
			input.setThreshold(1);
			input.setAdapter(adapter);
			input.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
				}
			});
		}
		alert.setView(input);

		// set listener for ok when user inputs project name
		alert.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		alert.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						dialog.dismiss();
					}
				});

		alertDialog = alert.create();

		// set listeners for rows and disable continue button
		View loginButtonView = findViewById(R.id.login_button);
		cont = (ImageView) findViewById(R.id.bcontinue);
		cont.setVisibility(View.GONE);
		loginButtonView.setOnClickListener(this);
		cont.setOnClickListener(this);

		// get credential with scopes
		credential = GoogleAccountCredential.usingOAuth2(this,
				Arrays.asList(FUSION_TABLE_SCOPE, DriveScopes.DRIVE));

	}

	@Override
	public void onClick(View view) {
		Integer id = view.getId();

		if (id == R.id.login_button) {
			// Google credentials
			startActivityForResult(credential.newChooseAccountIntent(),
					REQUEST_ACCOUNT_PICKER);
		}  else if (id == R.id.bcontinue) {
			if (jsurv == null) {
				Toast toast = Toast.makeText(getApplicationContext(),
						R.string.invalid_user_project, Toast.LENGTH_SHORT);
				toast.show();
				return;
			}

			// Go to runs activity
			 Intent i = new Intent(getApplicationContext(),
			 RunActivity.class);
			 i.putExtra("jsonsurvey", jsurv.toString());
			 i.putExtra("username", username);
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
						JSON_FACTORY, credential)
						.setApplicationName("UXMexico").build();

				// update our username field
				messageHandler.sendEmptyMessage(EVENT_TYPE.GOT_USERNAME
						.ordinal());
			}
			break;
		case REQUEST_PERMISSIONS:
			if (resultCode == RESULT_OK) {
				// TODO Activate continue button.
			} else {
				startActivityForResult(credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}

	// Username selection helper functions

	private enum EVENT_TYPE {
		GOT_USERNAME, PARSED_CORRECTLY, PARSED_INCORRECTLY, INPUT_NAME
	}

}
