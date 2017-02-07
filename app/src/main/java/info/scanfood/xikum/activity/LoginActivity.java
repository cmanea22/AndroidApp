package info.scanfood.xikum.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.scanfood.xikum.R;
import info.scanfood.xikum.app.AppConfig;
import info.scanfood.xikum.app.AppController;
import info.scanfood.xikum.helper.SQLiteHandler;
import info.scanfood.xikum.helper.SessionManager;

import static java.lang.Boolean.TRUE;

public class LoginActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQL handler
        db = new SQLiteHandler(getApplicationContext());

        // Manage Session
        session = new SessionManager(getApplicationContext());

        // Check if user is logged
        if (session.isLoggedIn()) {
            //Redirect to MainActivity Page
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();


                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),"Please enter your login information!", Toast.LENGTH_LONG).show();


                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent RegisterLink = new Intent(getApplicationContext(),
                RegisterActivity.class);
                startActivity(RegisterLink);
                finish();
            }
        });

    }

    //function to verify login details in mysql db

    private void checkLogin(final String username, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Loading to ...");
        showDialog();



        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
               // Log.e(TAG, "Login is Response: " + response.toString());

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean status = jObj.getBoolean("status");
                    //boolean data = jObj.getBoolean("data");

                    // Check for error json
                    if (status == TRUE) {
                        // user success logged in
                        // login session
                        session.setLogin(true);

                        // Now store the user in SQL
                      //  String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("data");
                        String first_name = user.getString("first_name");
                        String last_name = user.getString("last_name");
                        String username = user.getString("username");
                        String email = user.getString("email");
                        //String id = user.getString("id");
                        String access_token = user.getString("access_token");
                        String role = user.getString("role");
                        Log.e(TAG, "EROARE JSON " +first_name);

                       // String uid = jObj.getString("uid");
                       // String created_at = user.getString("created_at");

                        // Inserting row in users table
                        db.addUser(first_name, last_name, username, email, access_token, role);

                        // Launch main activityemail
                        Intent mainActivityLuncher = new Intent(LoginActivity.this, MainActivity.class);

                        startActivity(mainActivityLuncher);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Wrong information", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Eroare: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
                showDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);

                System.out.println("OUT:" +params);


                return params;
            }

        };


        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
