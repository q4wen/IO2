package ca.uwaterloo.io.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.viewpager.widget.ViewPager;
import ca.uwaterloo.io.Constant;
import ca.uwaterloo.io.CustomJsonRequest;
import ca.uwaterloo.io.R;
import ca.uwaterloo.io.model.Bpm;
import ca.uwaterloo.io.model.User;
import ca.uwaterloo.io.network.GetDataService;
import ca.uwaterloo.io.network.RetrofitClientInstance;

public class DashboardFragment extends Fragment {

    private TextView graphTitle, spo2Val, bpmVal;

    String state = "spo2";

    LineChart lineChart;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!User.getIsOffline()) {
            loadSpo2();
            loadBpm();
        }
    }

    private void loadData(final String type) {
        //Offline mode
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String listStr = sharedPreferences.getString(type, "");
//        if (listStr == null || listStr.isEmpty()) {
//            return;
//        }
//
//        lineChart.clear();
//        ArrayList<Entry> values = new ArrayList<>();
//        String[] arr = listStr.split(",");
//        for (int i = 0; i < arr.length; i++) {
//            int value = Integer.parseInt(arr[i]);
//            values.add(new Entry(i, value));
//            if (i == arr.length - 1) {
//                if (type.equals("bpm")) {
//                    bpmVal.setText(arr[i]);
//                } else {
//                    spo2Val.setText(arr[i]);
//                }
//            }
//        }
//
//        LineDataSet set = new LineDataSet(values, "");
//        set.setDrawFilled(true);
//
//        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(set);
//        LineData data = new LineData(dataSets);
//        lineChart.setData(data);


        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        String url = Constant.url + "/" + type + "/" + User.getUsername();
        Map<String, String> params = new HashMap<>();
        params.put("username", User.getUsername());

        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Entry> values = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject data = (JSONObject) response.get(i);
                                String dataStr = data.get(type).toString();

                                values.add(new Entry(i, Integer.parseInt(dataStr)));

                                if (i == response.length() - 1) {
                                    if (type.equals("bpm")) {
                                        bpmVal.setText(dataStr);
                                    } else {
                                        spo2Val.setText(dataStr);
                                    }
                                }


                            }
                            LineDataSet set = new LineDataSet(values, "");
                            set.setDrawFilled(true);

                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set);
                            LineData data = new LineData(dataSets);

                            if (type.equals(state)) {
                                lineChart.clear();
                                lineChart.setData(data);
                            }

                        } catch (Exception e) {
                            Log.e("loading data", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("loading data", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + User.getAuthentication());
                params.put("Cache-Control", "no-cache");
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", User.getUsername());
                return params;
            };
        };
        queue.add(stringRequest);
    }

    private void loadSpo2() {
        loadData("spo2");
        graphTitle.setText("SpO2 History");
    }

    private void loadBpm() {
        loadData("bpm");
        graphTitle.setText("BPM History");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        lineChart = v.findViewById(R.id.chart);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);

        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getXAxis().setDrawLabels(false);

//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return "abc";
//            }
//        });

        graphTitle = v.findViewById(R.id.graph_title);
        bpmVal = v.findViewById(R.id.bpm);
        spo2Val = v.findViewById(R.id.spo2);


        View spo2Btn = v.findViewById(R.id.spo2Btn);
        spo2Btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                state = "spo2";
                loadSpo2();
            }
        });

        View bpmBtn = v.findViewById(R.id.bpmBtn);
        bpmBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                state = "bpm";
                loadBpm();
            }
        });
        return v;
    }
}
