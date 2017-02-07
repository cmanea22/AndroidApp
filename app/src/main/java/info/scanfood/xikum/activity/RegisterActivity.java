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

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFirstName;
    private EditText inputLastName;
    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFirstName = (EditText) findViewById(R.id.first_name);
        inputLastName = (EditText) findViewById(R.id.last_name);
        inputUsername = (EditText) findViewById(R.id.username);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String firstName = inputFirstName.getText().toString().trim();

                String lastName = inputLastName.getText().toString().trim();

                String username = inputUsername.getText().toString().trim();

                String email = inputEmail.getText().toString().trim();

                String password = inputPassword.getText().toString().trim();

                if (!firstName.isEmpty() && !lastName.isEmpty() && !username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    registerUser(firstName, lastName, username, email, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your information!", Toast.LENGTH_LONG).show();


                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);

                startActivity(myIntent);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String first_name, final String last_name, final String username, final String email, final String password) {

        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    boolean status = jObj.getBoolean("status");
                    boolean data = jObj.optBoolean("data");

                    if (status == true && data == true) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
/*
                       JSONObject user = jObj.getJSONObject("data");
                        String first_name = user.getString("first_name");
                        String last_name = user.getString("last_name");
                        String username = user.getString("username");
                        String email = user.getString("email");
                        String access_token = user.getString("access_token");
                        String role = user.getString("role");

                        System.out.println("AAAAAAAAAAAA "+first_name);

                        // Inserting row in users table
                        db.addUser(first_name, last_name, username, email, access_token, role);*/

                        Toast.makeText(getApplicationContext(), "User successfully registered. Please login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent( RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("first_name", first_name);
                params.put("last_name", last_name);
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);

                System.out.println(params);
                return params;

            }

        };

        // Adding request to request queue
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
