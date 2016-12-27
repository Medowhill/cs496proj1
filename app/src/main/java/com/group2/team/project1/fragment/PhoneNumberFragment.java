package com.group2.team.project1.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.group2.team.project1.R;

// Fragment class for A tab (Phone book)
public class PhoneNumberFragment extends Fragment {
    public static PhoneNumberFragment newInstance() {
        return new PhoneNumberFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phone, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.textView_phone);
        textView.setText("Phone Number Fragment");
        return rootView;
    }
}