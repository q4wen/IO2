package ca.uwaterloo.io;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
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

public class RegisterActivity extends AppCompatActivity {

    EditText username, password, firstName, lastName;
    Button registerBtn;
    TextView msgBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        msgBox = findViewById(R.id.register_msg);

        registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                registerBtn.setEnabled(false);

                if (username.getText().toString().isEmpty()
                        || password.getText().toString().isEmpty()
                        || firstName.getText().toString().isEmpty()
                        || lastName.getText().toString().isEmpty()) {
                    registerBtn.setEnabled(true);
                    return;
                }
                Map<String,String> params = new HashMap<>();
                params.put("username",username.getText().toString());
                params.put("password",password.getText().toString());
                params.put("firstName",firstName.getText().toString());
                params.put("lastName",lastName.getText().toString());

                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                String url = Constant.url + "/users/register";
                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                finish();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error when register", error.toString());
                        msgBox.setVisibility(View.VISIBLE);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String,String> params = new HashMap<>();
                        params.put("Content-Type","application/json");
                        return params;
                    }
                };
                queue.add(stringRequest);

                registerBtn.setEnabled(true);
            }
        });
    }
}
