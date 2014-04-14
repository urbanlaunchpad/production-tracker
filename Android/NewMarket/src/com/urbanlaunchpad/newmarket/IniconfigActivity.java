package org.urbanlaunchpad.flocktracker;

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
    public static final String API_KEY = "AIzaSyB4Nn1k2sML-0aBN2Fk3qOXLF-4zlaNwmg";
    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static String username = "";
    // by default.
    public static GoogleAccountCredential credential;
    public static Fusiontables fusiontables;
    public static SharedPreferences prefs;
    private static int LOADING_ID = -1;
    TextView usernameField;
    TextView projectNameField;
    ImageView cont;
    AutoCompleteTextView input;
    String projectName = "";
    String jsonsurveystring;
    JSONObject jsurv = null;
    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == EVENT_TYPE.GOT_USERNAME.ordinal()) {
                // have input a name. update it on interface
                usernameField.setText(username);
            } else if (msg.what == EVENT_TYPE.GOT_PROJECT_NAME.ordinal()) {
                // have input a project name. update it on interface
                projectNameField.setText(projectName);
            } else if (msg.what == EVENT_TYPE.PARSED_CORRECTLY.ordinal()) {
                RelativeLayout navBar = (RelativeLayout) findViewById(R.id.iniconfig_navbar);
                navBar.removeViewAt(0);
                findViewById(R.id.bcontinue).setClickable(true);

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
            } else if (msg.what == EVENT_TYPE.INPUT_NAME.ordinal()) {
                input.setText(projectName);
                // want to display alert to get project name
                alertDialog.show();
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                Log.e("Survey Parser", "Error parsing survey");
            }
        }
    };
    AlertDialog alertDialog;
    private boolean debison = false; // If true, a test project will be loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniconfig);

        prefs = this.getSharedPreferences("org.urbanlaunchpad.flocktracker",
            Context.MODE_PRIVATE);

        // initialize fields
        usernameField = (TextView) findViewById(R.id.usernameText);
        projectNameField = (TextView) findViewById(R.id.projectNameText);

        // initialize dialog for inputting project name
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.select_project);

        input = new AutoCompleteTextView(this);

        if (prefs.contains("lastProject")) {
            // Create the adapter and set it to the AutoCompleteTextView
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new String[]{prefs.getString("lastProject", "")});
            input.setThreshold(1);
            input.setAdapter(adapter);
            input.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                    onPositiveButtonClicked();
                }
            });
        }
        alert.setView(input);

        // set listener for ok when user inputs project name
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                onPositiveButtonClicked();
            }
        });

        alert.setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    dialog.dismiss();
                }
            }
        );

        alertDialog = alert.create();

        // set listeners for rows and disable continue button
        View projectNameSelectRow = findViewById(R.id.projectNameRow);
        View usernameSelectRow = findViewById(R.id.usernameRow);
        cont = (ImageView) findViewById(R.id.bcontinue);
        usernameSelectRow.setOnClickListener(this);
        projectNameSelectRow.setOnClickListener(this);
        cont.setOnClickListener(this);

        // Debug mode, passes project without internet connection.
        DebuggingIsOn(debison);

        // get credential with scopes
        credential = GoogleAccountCredential.usingOAuth2(this,
            Arrays.asList(FUSION_TABLE_SCOPE, DriveScopes.DRIVE));

    }

    public void onPositiveButtonClicked() {
        // save the project name
        projectName = input.getText().toString().trim();
        prefs.edit().putString("lastProject", projectName).commit();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        alertDialog.dismiss();

        if (!projectName.isEmpty()) {
            // update our interface with project name
            messageHandler.sendEmptyMessage(EVENT_TYPE.GOT_PROJECT_NAME
                .ordinal());

            parseSurvey();
        }
    }

    @Override
    public void onClick(View view) {
        Integer id = view.getId();

        if (id == R.id.usernameRow) {
            // Google credentials
            startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
        } else if (id == R.id.projectNameRow) {
            if (username.isEmpty()) {
                Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.select_user_first, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }


            // Show the popup dialog to get the project name
            messageHandler.sendEmptyMessage(EVENT_TYPE.INPUT_NAME.ordinal());
        } else if (id == R.id.bcontinue) {
            if (jsurv == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.invalid_user_project, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }


            // Go to survey
            Intent i = new Intent(getApplicationContext(), SurveyorActivity.class);
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
            + " WHERE table_id = '" + tableId + "'"
        );
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

    /*
     * Survey getting helper functions
     */

    public void parseSurvey() {
        ProgressBar loading = new ProgressBar(this);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        loading.setLayoutParams(params);
        RelativeLayout navBar = (RelativeLayout) findViewById(R.id.iniconfig_navbar);
        navBar.addView(loading, 0);
        findViewById(R.id.bcontinue).setClickable(false);
        // get and parse survey
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (!getSurvey(projectName)) {
                        return;
                    }

                    try {
                        jsurv = new JSONObject(jsonsurveystring);
                        messageHandler
                            .sendEmptyMessage(EVENT_TYPE.PARSED_CORRECTLY
                                .ordinal());
                    } catch (JSONException e) {
                        Log.e("JSON Parser",
                            "Error parsing data " + e.toString());
                        messageHandler
                            .sendEmptyMessage(EVENT_TYPE.PARSED_INCORRECTLY
                                .ordinal());
                    }
                } catch (ClientProtocolException e1) {
                    e1.printStackTrace();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_PERMISSIONS);
                } catch (IOException e1) {
                    if (projectName.equals(prefs.getString("lastProject", ""))) {
                        try {
                            jsurv = new JSONObject(prefs.getString(
                                "jsonsurveystring", ""));
                            messageHandler
                                .sendEmptyMessage(EVENT_TYPE.PARSED_CORRECTLY
                                    .ordinal());
                        } catch (JSONException e) {
                            Log.e("JSON Parser",
                                "Error parsing data " + e.toString());
                            messageHandler
                                .sendEmptyMessage(EVENT_TYPE.PARSED_INCORRECTLY
                                    .ordinal());
                        }
                    }

                    e1.printStackTrace();
                }
            }
        }).start();
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
                    parseSurvey();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    /*
     * Username selection helper functions
     */

    private void DebuggingIsOn(boolean deb) {
        // Debug mode, passes project without internet connection.
        // Stuff for debugging without internet connection.
        if (deb) {
            projectName = "My fake project is back";
            username = "fakeuser123@youdontknowwhere.us";
            try {
                jsurv = new JSONObject(
                    "{\"Survey\":{\"Chapters\":[{\"Chapter\":\"SITUACIÓN DEL LOTE Y VIVIENDA\"," +
                    "\"Questions\":[{\"id\":\"q1\",\"Question\":\"¿Cuál es el uso de su lote?\",\"Kind\":\"OL\"," +
                    "\"Other\":true,\"Answers\":[{\"Answer\":\"Habitacional\"},{\"Answer\":\"Mixto\"}]}," +
                    "{\"id\":\"q2\",\"Question\":\"¿Cuál es la superficie del lote?\",\"Kind\":\"LP\"," +
                    "\"LoopLimit\":\"q6\"},{\"id\":\"q3\",\"Question\":\"¿Cuál es la superficie construida de su " +
                    "lote?\",\"Kind\":\"ON\"},{\"id\":\"q4\",\"Question\":\"¿Cuántas viviendas est·n construidas en " +
                    "este predio?\",\"Kind\":\"ON\"},{\"id\":\"q5\",\"Question\":\"¿Cuántas familias comparten esta " +
                    "vivienda?\",\"Kind\":\"ON\"},{\"id\":\"q6\",\"Question\":\"Este predio es:\",\"Kind\":\"MC\"," +
                    "\"Other\":true,\"Jump\":\"q6d\",\"Answers\":[{\"Answer\":\"Propio\"},{\"Answer\":\"Rentado 6b\"," +
                    "\"Jump\":\"q6b\"},{\"Answer\":\"Prestado 6d\"},{\"Answer\":\"Compartido 6d\"}," +
                    "{\"Answer\":\"Lo cuida 6d\"},{\"Answer\":\"Secesión de derechos 6d\"}," +
                    "{\"Answer\":\"Otra tenencia 6d\"}]},{\"id\":\"q6a\",\"Question\":\"¿A través de quién " +
                    "adquirió/rentó/ocupó el lote? 6d\",\"Kind\":\"MC\",\"Other\":true,\"Jump\":\"q6d\"," +
                    "\"Answers\":[{\"Answer\":\"Fraccionador 6d\"},{\"Answer\":\"Lider 6d\"}," +
                    "{\"Answer\":\"Comunitario 6d\"},{\"Answer\":\"Ejidatario o comunero 6d\"}," +
                    "{\"Answer\":\"Funcionario 6d\"}]},{\"id\":\"q6b\",\"Question\":\"Si renta, " +
                    "¿Cuánto paga mensualmente? 7\",\"Kind\":\"OT\"},{\"id\":\"q6d\"," +
                    "\"Question\":\"¿Qué documentos de posesión y/o propiedad tiene?\",\"Kind\":\"OT\"}," +
                    "{\"id\":\"q7\",\"Question\":\"¿Cuánto tiempo lleva viviendo aquí?\",\"Kind\":\"ON\"}," +
                    "{\"id\":\"q8\",\"Question\":\"¿Está el predio en algún proceso de regularización?\"," +
                    "\"Kind\":\"MC\",\"Other\":true,\"Answers\":[{\"Answer\":\"Sí\"},{\"Answer\":\"No\"}]}," +
                    "{\"id\":\"q9\",\"Question\":\"¿Sabe que adquiriÛ un lote en zona no apta para vivienda?\"," +
                    "\"Kind\":\"MC\",\"Other\":true,\"Answers\":[{\"Answer\":\"SÌ\"},{\"Answer\":\"No\"}]}]}," +
                    "{\"Chapter\":\"POBREZA DEL TIEMPO LIBRE\",\"Questions\":[{\"id\":\"q29\"," +
                    "\"Question\":\"¿Cuántos trabajos tiene?\",\"Kind\":\"ON\"},{\"id\":\"q30\"," +
                    "\"Question\":\"¿Cuántos días a la semana trabaja?\",\"Kind\":\"ON\"},{\"id\":\"q31\"," +
                    "\"Question\":\"¿Trabaja los fines de semana?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"SÌ\"},{\"Answer\":\"No\"}]},{\"id\":\"q32\"," +
                    "\"Question\":\"¿A qué hora entra a trabajar?\",\"Kind\":\"ON\"},{\"id\":\"q33\"," +
                    "\"Question\":\"¿A qué hora sale de trabajar?\",\"Kind\":\"ON\"},{\"id\":\"q34\"," +
                    "\"Question\":\"¿Cuántas horas al día trabaja?\",\"Kind\":\"ON\"},{\"id\":\"q35\"," +
                    "\"Question\":\"Cuando tiene tiempo libre, ¿qué hace?\",\"Kind\":\"CB\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"Convive con la familia\"},{\"Answer\":\"Trata de descansar o " +
                    "dormir\"},{\"Answer\":\"Hace cualquier cosa dentro de su casa\"}," +
                    "{\"Answer\":\"Hace alguna actividad recreativa\"},{\"Answer\":\"Ir al cine\"}," +
                    "{\"Answer\":\"Ir al teatro\"},{\"Answer\":\"Ir al museo\"},{\"Answer\":\"Ir al parque\"}," +
                    "{\"Answer\":\"Ir a convivir con familiares cercanos\"},{\"Answer\":\"Ir a convivir con " +
                    "amigos\"},{\"Answer\":\"Leer un libro\"},{\"Answer\":\"Navegar en internet\"}," +
                    "{\"Answer\":\"Ver televisión\"}]}]},{\"Chapter\":\"ORGANIZACIÓN VECINAL\"," +
                    "\"Questions\":[{\"id\":\"q36\",\"Question\":\"Como comunidad, " +
                    "¿Cómo es que resuelven sus carencias, como?\",\"Kind\":\"MC\",\"Other\":true,\"Jump\":\"q36a\"," +
                    "\"Answers\":[{\"Answer\":\"Agua 36a\"},{\"Answer\":\"Pavimentación 36a\"}," +
                    "{\"Answer\":\"Electricidad 36a\"},{\"Answer\":\"Regularización de la tenencia de la tierra " +
                    "36a\"}]},{\"id\":\"q36a\",\"Question\":\"¿Quién se encarga?\",\"Kind\":\"OT\"},{\"id\":\"q37\"," +
                    "\"Question\":\"¿Dejan que se incorporen al asentamiento nuevos vecinos?\",\"Kind\":\"MC\"," +
                    "\"Other\":false,\"Jump\":\"q37a\",\"Answers\":[{\"Answer\":\"Si 37a\"}," +
                    "{\"Answer\":\"No 37a\"}]},{\"id\":\"q37a\",\"Question\":\"¿Por qué?\",\"Kind\":\"OT\"}," +
                    "{\"id\":\"q38\",\"Question\":\"¿En este asentamiento tiene algún familiar?\",\"Kind\":\"MC\"," +
                    "\"Other\":false,\"Answers\":[{\"Answer\":\"Si\"},{\"Answer\":\"No\"}]},{\"id\":\"q39\"," +
                    "\"Question\":\"¿Alguien se encarga de organizarlos?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Jump\":\"q39a\",\"Answers\":[{\"Answer\":\"Si 39a\"},{\"Answer\":\"No\"}]},{\"id\":\"q39a\"," +
                    "\"Question\":\"¿Quién?\",\"Kind\":\"OT\"},{\"id\":\"q40\"," +
                    "\"Question\":\"Para lograr sus demandas de servicios como agua, luz, pavimentación, " +
                    "vivienda. ¿A dónde acuden?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"Coordinación Territorial\"},{\"Answer\":\"La Delegación\"}," +
                    "{\"Answer\":\"GDF\"},{\"Answer\":\"Partido Político\"}]},{\"id\":\"q41\"," +
                    "\"Question\":\"¿Les han resuelto sus demandas?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Jump\":\"q41a\",\"Answers\":[{\"Answer\":\"Si 41a\"},{\"Answer\":\"No 41b\"}]}," +
                    "{\"id\":\"q41a\",\"Question\":\"¿Cuáles?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"Agua\"},{\"Answer\":\"Pavimentación\"},{\"Answer\":\"Electricidad\"}," +
                    "{\"Answer\":\"Regularización y tenencia de la tierra\"}]},{\"id\":\"q41b\"," +
                    "\"Question\":\"¿Por qué?\",\"Kind\":\"OT\"},{\"id\":\"q42\",\"Question\":\"¿Se han visto en la " +
                    "necesidad de que ustedes mismos las resuelvan?\",\"Kind\":\"MC\"," +
                    "\"Answers\":[{\"Answer\":\"Sí\"},{\"Answer\":\"No\"}]},{\"id\":\"q43\"," +
                    "\"Question\":\"¿Cuentan con una organización en la colonia?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Jump\":\"q44\",\"Answers\":[{\"Answer\":\"Sí\"},{\"Answer\":\"No 44\"}]},{\"id\":\"q43a\"," +
                    "\"Question\":\"¿Cuales son las funciones del líder vecinal respecto a la colonia?\"," +
                    "\"Kind\":\"OT\"},{\"id\":\"q43b\",\"Question\":\"¿Que logros han conseguido a través de la " +
                    "organización?\",\"Kind\":\"MC\",\"Other\":false,\"Answers\":[{\"Answer\":\"Servicios Urbanos " +
                    "(pavimentación, agua, luz, etc.)\"},{\"Answer\":\"Regularización de la tenencia de la tierra\"}," +
                    "{\"Answer\":\"Servicios sociales (educación-salud)\"}]},{\"id\":\"q43c\"," +
                    "\"Question\":\"¿Con que frecuencia tienen reuniones?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"Semanal\"},{\"Answer\":\"Mensual\"}]},{\"id\":\"q44\"," +
                    "\"Question\":\"Usted, ¿Qué tan seguro se siente en este lugar?\",\"Kind\":\"MC\"," +
                    "\"Other\":false,\"Jump\":\"q45\",\"Answers\":[{\"Answer\":\"Seguro 45\"}," +
                    "{\"Answer\":\"Medio seguro\"},{\"Answer\":\"Inseguro\"}]},{\"id\":\"q45\"," +
                    "\"Question\":\"¿Cómo percibes la seguridad en este lugar?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Jump\":\"q46a\",\"Answers\":[{\"Answer\":\"Buena 46a\"},{\"Answer\":\"Regular\"}," +
                    "{\"Answer\":\"Pésima\"}]},{\"id\":\"q46\",\"Question\":\"¿Por qué regular o pésima?\"," +
                    "\"Kind\":\"MC\",\"Other\":false,\"Jump\":\"q47\",\"Answers\":[{\"Answer\":\"Por ausencia de " +
                    "pratullas 47\"},{\"Answer\":\"Por ausencia de policías o pie o en bicicleta 47\"}," +
                    "{\"Answer\":\"Ausencia de Policias en motocicleta 47\"}]},{\"id\":\"q46a\"," +
                    "\"Question\":\"¿Por qué buena?\",\"Kind\":\"MC\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"Por que hay patrullas\"},{\"Answer\":\"Por la presencia de policias a" +
                    " pie o en bicicleta\"},{\"Answer\":\"Presencia de policia en bicicleta\"}]},{\"id\":\"q47\"," +
                    "\"Question\":\"¿Se organizan para que ustedes mismos vigilen su calle?\",\"Kind\":\"MC\"," +
                    "\"Other\":false,\"Jump\":\"q48\",\"Answers\":[{\"Answer\":\"Sí\"},{\"Answer\":\"No 48\"}]}," +
                    "{\"id\":\"q47a\",\"Question\":\"¿Cómo se organizan?\",\"Kind\":\"MC\",\"Other\":true," +
                    "\"Answers\":[{\"Answer\":\"Hacen guardias\"},{\"Answer\":\"Hacen rondines\"}," +
                    "{\"Answer\":\"Ponen alarmas\"}]}]},{\"Chapter\":\"RIESGOS Y AMENAZAS\"," +
                    "\"Questions\":[{\"id\":\"q48\",\"Question\":\"¿Que tipo de riesgos tienen en esta zona?\"," +
                    "\"Kind\":\"MC\",\"Other\":true,\"Answers\":[{\"Answer\":\"Inundaciones\"}," +
                    "{\"Answer\":\"Deslizamiento de tierra\"},{\"Answer\":\"Incendios forestales\"}," +
                    "{\"Answer\":\"Desalojos\"},{\"Answer\":\"Otros\"},{\"Answer\":\"Ninguno\"}]},{\"id\":\"q49\"," +
                    "\"Question\":\"¿A que riesgo se siente más vulnerable?\",\"Kind\":\"MC\",\"Other\":true," +
                    "\"Answers\":[{\"Answer\":\"Inundaciones\"},{\"Answer\":\"Deslizamiento de tierra\"}," +
                    "{\"Answer\":\"Incendios forestales\"},{\"Answer\":\"Desalojos\"}," +
                    "{\"Answer\":\"Infraestructura física\"},{\"Answer\":\"Otros\"},{\"Answer\":\"Ninguno\"}]}," +
                    "{\"id\":\"q50\",\"Question\":\"¿Que tipo de obras se han llevado a cabo para mejorar este lugar " +
                    "contra los riesgos?\",\"Kind\":\"MC\",\"Other\":true,\"Answers\":[{\"Answer\":\"Poner muros de " +
                    "contención\"},{\"Answer\":\"Costaleras\"},{\"Answer\":\"Lonas antiderrumbes\"}]}," +
                    "{\"id\":\"q51\",\"Question\":\"¿Como las han logrado?\",\"Kind\":\"CB\",\"Other\":false," +
                    "\"Answers\":[{\"Answer\":\"Acciones individuales y familiares\"}," +
                    "{\"Answer\":\"Obras vecinales/comunitarias\"},{\"Answer\":\"Obras del Gobierno " +
                    "delegacional\"}]}]}],\"TableID\":\"11lGsm8B2SNNGmEsTmuGVrAy1gcJF9TQBo3G1Vw0\"}," +
                    "\"TrackerAlarm\":{\"Questions\":[{\"id\":\"q1\",\"Question\":\"Camión\",\"Kind\":\"MC\"," +
                    "\"Other\":true,\"Answers\":[{\"Answer\":\"Ruta 56\"},{\"Answer\":\"Ruta 15\"}]},{\"id\":\"q2\"," +
                    "\"Question\":\"Número de pasajeros.\",\"Kind\":\"ON\",\"Other\":true}]," +
                    "\"TableID\":\"1Q2mr8ni5LTxtZRRi3PNSYxAYS8HWikWqlfoIUK4\"}}"
                );
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            messageHandler.sendEmptyMessage(EVENT_TYPE.PARSED_CORRECTLY
                .ordinal());
            messageHandler.sendEmptyMessage(EVENT_TYPE.GOT_USERNAME.ordinal());
            messageHandler.sendEmptyMessage(EVENT_TYPE.GOT_PROJECT_NAME
                .ordinal());
        }
    }

    private enum EVENT_TYPE {
        GOT_USERNAME, GOT_PROJECT_NAME, PARSED_CORRECTLY, PARSED_INCORRECTLY, INPUT_NAME
    }

}
