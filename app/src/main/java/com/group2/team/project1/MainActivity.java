package com.group2.team.project1;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.group2.team.project1.adapter.FreeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final private static int REQUEST_CAMERA_FROM_FREE = 1;

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
    private Bitmap bitmap;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("cs496test", "result" + requestCode + "," + resultCode);
        if (resultCode == RESULT_OK) {
            //      if (requestCode == REQUEST_CAMERA_FROM_FREE) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            //      }
        }
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
        private ArrayList<FreeItem> items;

        private RecyclerView recyclerView;
        private EditText editText;
        private Button buttonSave, buttonPhoto;

        public FreeFragment() {
            items = new ArrayList<>();
        }

        public static FreeFragment newInstance() {
            return new FreeFragment();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String data = readFile();
            Log.i("cs496test", data);
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

            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    FreeItem item = new FreeItem(object.getLong("date"), object.getString("content"), object.getBoolean("photo"));
                    items.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View rootView = inflater.inflate(R.layout.fragment_free, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.free_recyclerView);
            editText = (EditText) rootView.findViewById(R.id.free_editText);
            buttonSave = (Button) rootView.findViewById(R.id.free_button_save);
            buttonPhoto = (Button) rootView.findViewById(R.id.free_button_photo);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new FreeAdapter(getContext(), items));
            Log.i("cs496test", items.toString());

            editText.addTextChangedListener(new TextWatcher() {
                String previousString = "";

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    previousString = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (editText.getLineCount() > 5) {
                        editText.setText(previousString);
                        editText.setSelection(editText.length());
                    }
                }
            });

            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean photo = buttonPhoto.getText().toString().equals(getString(R.string.free_remove_photo));
                    long time = Calendar.getInstance().getTime().getTime();
                    FreeItem item = new FreeItem(time, editText.getText().toString(), photo);
                    if (photo) {
                        Bitmap bitmap = ((MainActivity) getActivity()).bitmap;
                        try {
                            FileOutputStream fos = getActivity().openFileOutput(time + "", MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bitmap.recycle();
                        buttonPhoto.setText(R.string.free_add_photo);
                    }
                    editText.setText("");
                    items.add(0, item);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            });

            buttonPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonPhoto.getText().toString().equals(getString(R.string.free_add_photo))) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA_FROM_FREE);
                        buttonPhoto.setText(R.string.free_remove_photo);
                    } else {
                        buttonPhoto.setText(R.string.free_add_photo);
                    }
                }
            });
            return rootView;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            JSONArray array = new JSONArray();
            for (FreeItem item : items) {
                JSONObject object = new JSONObject();
                try {
                    object.put("date", item.getDate());
                    object.put("content", item.getContent());
                    object.put("photo", item.isPhoto());
                    array.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            writeFile(array.toString());
            Log.i("cs496test", array.toString());
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
