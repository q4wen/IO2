package ca.uwaterloo.io.measure;

import androidx.appcompat.app.AppCompatActivity;
import ca.uwaterloo.io.R;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class MeasurementActivity extends AppCompatActivity {

    Button connectBtn, uploadBtn;
    TextView textBox, spo2Text, bpmText;

    CMS50EW cms50EW;
    private LineChart mChart;

    int state = 0;

    private Handler mHandler = new Handler();
    Runnable mUpdateClockTask = new Runnable() {
        public void run() {
            spo2Text.setText(Integer.toString(cms50EW.spo2));
            bpmText.setText(Integer.toString(cms50EW.bpm));
            addEntry(cms50EW.data);
            mHandler.postDelayed(mUpdateClockTask, 500);
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
                        mChart.getLineData().clearValues();
                        cms50EW.start();
                        mHandler.removeCallbacks(mUpdateClockTask);
                        mHandler.postDelayed(mUpdateClockTask, 100);

                        btn.setText("Stop");
                        state = 2;
                        break;
                    default:
                        cms50EW.stop();
                        mHandler.removeCallbacks(mUpdateClockTask);

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
                    return;
                }
                RequestQueue queue = Volley.newRequestQueue(MeasurementActivity.this);
                String url ="http://www.google.com";
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue.add(stringRequest);

                uploadBtn.setEnabled(true);
            }
        });

        initializeGraph();
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

    int count = 0;
    private void addEntry(int i) {
        LineData data = mChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = new LineDataSet(null, "Dynamic Data");
                set.setDrawValues(false);
                data.addDataSet(set);
            }

//            if (set.getEntryCount() > 5) {
//                set.removeFirst();
//                mChart.notifyDataSetChanged();
//            }

            data.addEntry(new Entry(set.getEntryCount(), i), 0);

            mChart.notifyDataSetChanged();

            mChart.setVisibleXRange(0, 10);
            mChart.moveViewToX(set.getEntryCount() - 10);
        }
    }
}
