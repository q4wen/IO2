package ca.uwaterloo.io;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import ca.uwaterloo.io.dashboard.DashboardFragment;
import ca.uwaterloo.io.measure.MeasureFragment;
import ca.uwaterloo.io.setting.SettingFragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment logFragment;
    private Fragment dashBoardFragment;
    private Fragment settingFragment;
    private Fragment measureFragment;
    private FragmentManager fragmentManager;

    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_measure:
                    fragmentManager.beginTransaction().replace(R.id.content_frame, measureFragment).commit();
                    return true;
                case R.id.navigation_dashboard:
                    fragmentManager.beginTransaction().replace(R.id.content_frame, dashBoardFragment).commit();
                    return true;
                case R.id.navigation_setting:
                    fragmentManager.beginTransaction().replace(R.id.content_frame, settingFragment).commit();
                    return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_measure:
                    fragmentManager.beginTransaction().replace(R.id.content_frame, measureFragment).commit();
                    return true;
                case R.id.navigation_dashboard:
                    fragmentManager.beginTransaction().replace(R.id.content_frame, dashBoardFragment).commit();
                    return true;
                case R.id.navigation_setting:
                    fragmentManager.beginTransaction().replace(R.id.content_frame, settingFragment).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        //navigation.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //logFragment = new LogFragment();
        dashBoardFragment = new DashboardFragment();
        measureFragment = new MeasureFragment();
        settingFragment = new SettingFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, dashBoardFragment).commit();
    }
}
