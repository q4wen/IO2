package ca.uwaterloo.io;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import ca.uwaterloo.io.measure.MeasurementActivity;
import ca.uwaterloo.io.model.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    View loginBtn, registerBtn;
    TextView msgBox;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        msgBox = findViewById(R.id.login_msg);
        username =findViewById(R.id.username);
        password = findViewById(R.id.password);

        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.setIsOffline(false);
                login();
            }
        });

        registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                //finish();
                startActivity(intent);
            }
        });

        View offline = findViewById(R.id.offline);
        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User.setIsOffline(true);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private final String STUB = "stub";
    private final String USER_NAME = "username";
    private final String PASSWORD = "password";
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(STUB, MODE_PRIVATE);
        String saved_username = sharedPreferences.getString(USER_NAME, null);
        String saved_password = sharedPreferences.getString(PASSWORD, null);

        if (saved_username != null && saved_password != null) {
            username.setText(saved_username);
            password.setText(saved_password);
        }
    }

    private void login() {
        loginBtn.setEnabled(false);

        final String usernameStr = username.getText().toString();
        String passwordStr = password.getText().toString();

        if (usernameStr.isEmpty()
                || passwordStr.isEmpty()) {
            loginBtn.setEnabled(true);
            return;
        }
        Map<String,String> params = new HashMap<>();
        params.put("username", usernameStr);
        params.put("password",passwordStr);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constant.url + "/users/authenticate";
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String token = response.get("token").toString();
                            User.setAuthentication(token);
                            User.setUsername(usernameStr);
                        } catch (Exception e) {
                            Log.e("Error while login", e.toString());
                        }

                        getSharedPreferences(STUB, MODE_PRIVATE)
                                .edit()
                                .putString(USER_NAME, username.getText().toString())
                                .putString(PASSWORD, password.getText().toString())
                                .apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        msgBox.setVisibility(View.VISIBLE);
                }
        });
        queue.add(stringRequest);

        loginBtn.setEnabled(true);
    }
}
