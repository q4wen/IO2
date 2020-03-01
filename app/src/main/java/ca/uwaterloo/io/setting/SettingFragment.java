package ca.uwaterloo.io.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.uwaterloo.io.LoginActivity;
import ca.uwaterloo.io.MainActivity;
import ca.uwaterloo.io.R;

public class SettingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        View logOutBtn = v.findViewById(R.id.logout);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
        return v;
    }
}
