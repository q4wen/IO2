package ca.uwaterloo.io.measure;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import ca.uwaterloo.io.Constant;
import ca.uwaterloo.io.R;
import ca.uwaterloo.io.model.User;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MeasurementActivity extends AppCompatActivity {

    Button connectBtn, uploadBtn;
    TextView textBox, spo2Text, bpmText;

    CMS50EW cms50EW;
    private LineChart mChart;

    CountDownLatch responseWaiter;
    int state = 0;
    boolean hasError;
    int httpCounter;

    int count = 0;
    private Handler mHandler = new Handler();
    Runnable mUpdateClockTask = new Runnable() {
        public void run() {
            spo2Text.setText(Integer.toString(cms50EW.spo2));
            bpmText.setText(Integer.toString(cms50EW.bpm));
            addEntry(cms50EW.data);
            mHandler.postDelayed(mUpdateClockTask, 500);

            if (cms50EW.spo2 != 0) count++;
            if (count >= 100) {
                cms50EW.stop();
                textBox.setText("Measurement complete.");
                textBox.setVisibility(View.VISIBLE);
                uploadBtn.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textBox = findViewById(R.id.textBox);
        connectBtn = findViewById(R.id.connect);
        uploadBtn = findViewById(R.id.upload);
        spo2Text = findViewById(R.id.spo2);
        bpmText = findViewById(R.id.bpm);

        cms50EW = new CMS50EW(this);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                btn.setEnabled(false);

                switch (state) {
                    case 0:
                        if (cms50EW.connect()) {
                            state = 1;
                            btn.setText("Start");
                        }
                        break;
                    case 1:
                        textBox.setVisibility(View.INVISIBLE);
                        mChart.getLineData().clearValues();
                        cms50EW.start();
                        mHandler.removeCallbacks(mUpdateClockTask);
                        mHandler.postDelayed(mUpdateClockTask, 100);

                        btn.setText("Stop");
                        state = 2;
                        uploadBtn.setEnabled(false);
                        break;
                    default:
                        cms50EW.stop();
                        mHandler.removeCallbacks(mUpdateClockTask);
                        uploadBtn.setEnabled(true);

                        btn.setText("Start");
                        state = 1;
                        break;
                }

                btn.setEnabled(true);
            }

        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadBtn.setEnabled(false);

                if (cms50EW.bpm == 0 || cms50EW.spo2 == 0) {
                    //return;
                }

                hasError = false;
                httpCounter = 2;

                responseWaiter = new CountDownLatch(2);
                upload("bpm");
                upload("spo2");




//                Map<String,String> params = new HashMap<>();
//                params.put("username", User.getUsername());
//                params.put("bpm", Integer.toString(cms50EW.bpm));
//
//                RequestQueue queue = Volley.newRequestQueue(MeasurementActivity.this);
//                String url = Constant.url + "/bpm";
//                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                    }
//                }){
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String,String> params = new HashMap<>();
//                        params.put("Content-Type","application/json");
//                        params.put("Authorization","Bearer " + User.getAuthentication());
//                        params.put("Cache-Control", "no-cache");
//                        return params;
//                    }
//                };
//                queue.add(stringRequest);

                uploadBtn.setEnabled(true);
            }
        });

        initializeGraph();
    }

    private void showDialog() {
        final String message = hasError ? "Upload fail" : "Upload sucess";
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasurementActivity.this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void upload(String type) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MeasurementActivity.this);
        String bpmStr = preferences.getString(type, "");
        SharedPreferences.Editor editor = preferences.edit();

        int data = type.equals("bpm") ? cms50EW.bpm : cms50EW.spo2;

        if (bpmStr == null || bpmStr.isEmpty()) {
            editor.putString(type, Integer.toString(data));
            editor.apply();
        } else {
            bpmStr += "," + data;
            editor.putString(type, bpmStr);
            editor.apply();
        }

        Map<String,String> params = new HashMap<>();
        params.put("username", User.getUsername());
        params.put(type, Integer.toString(data));

        RequestQueue queue = Volley.newRequestQueue(MeasurementActivity.this);
        String url = Constant.url + "/" + type;
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        httpCounter--;
                        if (httpCounter <= 0) {
                            showDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                httpCounter--;
                hasError = true;
                if (httpCounter <= 0) {
                    showDialog();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/json");
                params.put("Authorization","Bearer " + User.getAuthentication());
                params.put("Cache-Control", "no-cache");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        cms50EW.exit();
        mHandler.removeCallbacks(mUpdateClockTask);
    }

    private void initializeGraph() {
        mChart = findViewById(R.id.chart);

        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getAxisRight().setDrawLabels(false);
        mChart.getXAxis().setDrawLabels(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getLegend().setEnabled(false);
        Description description = new Description();
        description.setText("");
        mChart.setDescription(description);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        YAxis yAxis = mChart.getAxis(YAxis.AxisDependency.LEFT);
        yAxis.setAxisMaximum(120f);
        yAxis.setAxisMinimum(-20f);

        // add empty data
        mChart.setData(data);
    }

    private void addEntry(int i) {
        LineData data = mChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = new LineDataSet(null, "Dynamic Data");
                set.setDrawValues(false);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), i), 0);

            mChart.notifyDataSetChanged();

            mChart.setVisibleXRange(0, 10);
            mChart.moveViewToX(set.getEntryCount() - 10);
        }
    }
}
