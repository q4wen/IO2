package ca.uwaterloo.io.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Objects;

import androidx.viewpager.widget.ViewPager;
import ca.uwaterloo.io.R;

public class DashboardFragment extends Fragment {

    private GraphPagerAdapter graphPagerAdapter;
    private ViewPager vp;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        graphPagerAdapter = new GraphPagerAdapter(getContext(), new ArrayList<LineData>());
        vp = view.findViewById(R.id.view_pager);
        vp.setAdapter(graphPagerAdapter);

//        Button yearBtn = view.findViewById(R.id.yearBtn);
//        yearBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<LineData> dataList = new ArrayList<>();
//                for (int i = 0; i < 3; i++)
//                    dataList.add(generateRandomData());
//                vp.setAdapter(new GraphPagerAdapter(getContext(), dataList));
//            }
//        });
//        Button mothBtn = view.findViewById(R.id.monthBtn);
//        mothBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<LineData> dataList = new ArrayList<>();
//                for (int i = 0; i < 3; i++)
//                    dataList.add(generateRandomData());
//                vp.setAdapter(new GraphPagerAdapter(getContext(), dataList));
//            }
//        });
//        Button weekBtn = view.findViewById(R.id.weekBtn);
//        weekBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<LineData> dataList = new ArrayList<>();
//                for (int i = 0; i < 3; i++)
//                    dataList.add(generateRandomData());
//                vp.setAdapter(new GraphPagerAdapter(getContext(), dataList));
//            }
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeUI();
    }

    private void initializeUI() {
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()));
        String url ="http://www.google.com";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<LineData> dataList = new ArrayList<>();

                        for (int i = 0; i < 3; i++) {
                            dataList.add(generateRandomData());
                        }
                        graphPagerAdapter.setData(dataList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        queue.add(stringRequest);
    }

    private LineData generateRandomData() {
        int count = 10, range = 100;
        ArrayList<Entry> values = new ArrayList<>();

        for (int j = 0; j < count; j++) {
            float val = (float) (Math.random() * range);
            values.add(new Entry(j, val));
        }

        LineDataSet set = new LineDataSet(values, "");
        set.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        LineData data = new LineData(dataSets);
        return data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
}
