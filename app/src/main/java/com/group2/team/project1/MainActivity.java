package com.group2.team.project1;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    // Fragment class for A tab (Phone book)
    public static class PhoneNumberFragment extends Fragment {
        public static PhoneNumberFragment newInstance() {
            PhoneNumberFragment fragment = new PhoneNumberFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_phone, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.textView_phone);
            textView.setText("Phone Number Fragment");
            return rootView;
        }
    }

    // Fragment class for B tab (Gallery)
    public static class GalleryFragment extends Fragment {

        public static GalleryFragment newInstance() {
            GalleryFragment fragment = new GalleryFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.textView_gallery);
            textView.setText("Gallery Fragment");
            return rootView;
        }
    }

    // Fragment class for C tab (Free)
    public static class FreeFragment extends Fragment {

        final private String FILE_NAME = "FreeFragmentDataSave";
        private FloatingActionButton fab;
        private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        public static FreeFragment newInstance() {
            return new FreeFragment();
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_free, container, false);
            fab = (FloatingActionButton) rootView.findViewById(R.id.free_fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View dialogView = inflater.inflate(R.layout.dialog_free, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setView(dialogView);
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final EditText editText = (EditText) dialogView.findViewById(R.id.free_dialog_editText);
                            final CalendarView calendarView = (CalendarView) dialogView.findViewById(R.id.free_dialog_calendarView);

                            String data = readFile();
                            JSONArray array = null;
                            if (data.length() == 0) {
                                array = new JSONArray();
                            } else {
                                try {
                                    array = new JSONArray(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (array != null) {
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("date", calendarView.getDate());
                                    object.put("content", editText.getText().toString());
                                    array.put(object);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                writeFile(array.toString());
                            }
                        }
                    });
                    builder.setCancelable(true);
                    builder.show();
                }
            });
            return rootView;
        }

        private String readFile() {
            try {
                FileInputStream stream = getActivity().openFileInput(FILE_NAME);
                byte[] arr = new byte[stream.available()];
                stream.read(arr);
                stream.close();
                return new String(arr);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        private void writeFile(String data) {
            try {
                FileOutputStream stream = getActivity().openFileOutput(FILE_NAME, MODE_PRIVATE);
                byte[] arr = data.getBytes();
                stream.write(arr);
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        MainActivity activity;

        public SectionsPagerAdapter(FragmentManager fm, MainActivity activity) {
            super(fm);
            this.activity = activity;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return PhoneNumberFragment.newInstance();
                case 1:
                    return GalleryFragment.newInstance();
                case 2:
                    return FreeFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "A";
                case 1:
                    return "B";
                case 2:
                    return "C";
            }
            return null;
        }
    }
}
