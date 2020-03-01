package ca.uwaterloo.io.measure;

import androidx.appcompat.app.AppCompatActivity;
import ca.uwaterloo.io.R;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Random;

public class MeasurementActivity extends AppCompatActivity {

    Button connectBtn, startBtn, stopBtn;
    TextView textBox, spo2Text, bpmText;

    CMS50EW cms50EW;
    private LineChart mChart;

    int state = 0;

    private Handler mHandler = new Handler();
    Runnable mUpdateClockTask = new Runnable() {
        public void run() {
            //String text = "Spo2: " + cms50EW.spo2 + " BPM: " + cms50EW.bpm;
            spo2Text.setText(Integer.toString(cms50EW.spo2));
            bpmText.setText(Integer.toString(cms50EW.bpm));
            addEntry(cms50EW.data);
            mHandler.postDelayed(mUpdateClockTask, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textBox = findViewById(R.id.textBox);
        connectBtn = findViewById(R.id.connect);
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
                        btn.setText("Start");
                        if (cms50EW.connect()) {
                            state = 1;
                        }
                        break;
                    case 1:
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
