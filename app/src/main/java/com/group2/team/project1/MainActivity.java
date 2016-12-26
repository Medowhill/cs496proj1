package com.group2.team.project1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.group2.team.project1.R;
import com.group2.team.project1.adapter.GalleryAdapter;

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

    private String currentPath;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("cs496test", requestCode + "," + resultCode);
        if (requestCode == REQUEST_CAMERA_FROM_FREE) {
            if (resultCode == RESULT_OK) {
                setPic();
                Fragment fragment = mSectionsPagerAdapter.fragments[2];
                Log.i("cs496test", fragment + "");
                if (fragment != null)
                    ((FreeFragment) fragment).setButtonPhoto();
            }
        }
    }

    private void setPic() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Get the dimensions of the View
        int targetW = size.x / 4;
        int targetH = size.y / 6;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        bitmap = BitmapFactory.decodeFile(currentPath, bmOptions);
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
            return new GalleryFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

            final GalleryAdapter adapter = new GalleryAdapter(getContext());
            for (int i = 1; i < 10; i++) {
                adapter.add(getResources().getIdentifier("t" + i, "drawable", getActivity().getPackageName()));
            }

            final ImageView iv1 = (ImageView) rootView.findViewById(R.id.imageView1);

            Gallery g = (Gallery) rootView.findViewById(R.id.gallery1);
            g.setAdapter(adapter);
            g.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    iv1.setImageResource(adapter.get(position));
                }
            });
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
        private Animation animation;

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
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            animation = AnimationUtils.loadAnimation(getContext(), R.anim.free_recyclerview_save);
            View rootView = inflater.inflate(R.layout.fragment_free, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.free_recyclerView);
            editText = (EditText) rootView.findViewById(R.id.free_editText);
            buttonSave = (Button) rootView.findViewById(R.id.free_button_save);
            buttonPhoto = (Button) rootView.findViewById(R.id.free_button_photo);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new FreeAdapter(getContext(), items));

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
                    if (s.toString().length() == 0)
                        buttonSave.setEnabled(false);
                    else
                        buttonSave.setEnabled(true);
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
                    recyclerView.startAnimation(animation);
                }
            });

            buttonPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonPhoto.getText().toString().equals(getString(R.string.free_add_photo))) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = null;
                        try {
                            file = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // Continue only if the File was successfully created
                        if (file != null) {
                            Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.group2.team.project1", file);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                                getActivity().startActivityForResult(intent, REQUEST_CAMERA_FROM_FREE);
                        }
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

        private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);

            ((MainActivity) getActivity()).currentPath = image.getAbsolutePath();
            return image;
        }

        void setButtonPhoto() {
            if (buttonPhoto != null)
                buttonPhoto.setText(R.string.free_remove_photo);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment[] fragments = new Fragment[3];

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Log.i("cs496test", position + "");
            switch (position) {
                case 0:
                    return fragments[0] = PhoneNumberFragment.newInstance();
                case 1:
                    return fragments[1] = GalleryFragment.newInstance();
                case 2:
                    return fragments[2] = FreeFragment.newInstance();
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
