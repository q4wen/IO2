package ca.uwaterloo.io;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class RegisterActivity extends AppCompatActivity {

    EditText username, password;
    Button registerBtn;
    TextView msgBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        msgBox = findViewById(R.id.register_msg);

        registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                registerBtn.setEnabled(false);

                if (username.getText().toString().isEmpty()
                        || password.getText().toString().isEmpty()) {
                    registerBtn.setEnabled(true);
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                String url ="http://www.google.com";
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                finish();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        msgBox.setVisibility(View.VISIBLE);
                    }
                });
                queue.add(stringRequest);

                registerBtn.setEnabled(true);
            }
        });
    }
}
