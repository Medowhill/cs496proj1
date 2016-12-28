package com.group2.team.project1.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.group2.team.project1.ActivityResultEvent;
import com.group2.team.project1.EventBus;
import com.group2.team.project1.FreeItem;
import com.group2.team.project1.R;
import com.group2.team.project1.adapter.FreeAdapter;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Fragment class for C tab (Free)
public class FreeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    final public static int REQUEST_CAMERA = 1, REQUEST_GALLERY = 2, REQUEST_SHARE = 0;
    final public static int PERMISSION_REQUEST_STORAGE = 1, PERMISSION_REQUEST_LOCATION = 2, PERMISSION_REQUEST_GALLERY = 3;
    final private static int PHOTO_WIDTH_MAX = 1440, PHOTO_HEIGHT_MAX = 1440;
    final private static String FILE_NAME = "FreeFragmentDataSave";

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton buttonSave, buttonPhoto;
    private FloatingActionButton fabSearch, fabCancel;
    private LinearLayout layoutMemo, layoutSearch;
    private TextView textViewDate, textViewInclude;
    private Animation animation;

    private GoogleApiClient googleApiClient;
    private ArrayList<FreeItem> items, savedItems;
    private boolean photo = false, searching = false;
    private int position;
    private Bitmap bitmap;
    private String currentPath, address;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private File fileToDelete;

    public FreeFragment() {
        items = new ArrayList<>();
        savedItems = new ArrayList<>();
    }

    public static FreeFragment newInstance() {
        return new FreeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        address = getString(R.string.free_unknown_location);

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
                    FreeItem item = new FreeItem(object.getLong("date"), object.getString("content"), object.getString("address"), object.getBoolean("photo"));
                    items.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(getContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("cs496", "connected");
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        else
            startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("cs496", "sus");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("cs496", "fail");
    }

    @Override
    public void onLocationChanged(final Location location) {
        new Thread() {
            @Override
            public void run() {
                address = getAddress(location);
            }
        }.start();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        animation = AnimationUtils.loadAnimation(getContext(), R.anim.free_recyclerview_save);
        View rootView = inflater.inflate(R.layout.fragment_free, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.free_recyclerView);
        editText = (EditText) rootView.findViewById(R.id.free_editText);
        buttonSave = (ImageButton) rootView.findViewById(R.id.free_button_save);
        buttonPhoto = (ImageButton) rootView.findViewById(R.id.free_button_photo);
        fabSearch = (FloatingActionButton) rootView.findViewById(R.id.free_fab_search);
        fabCancel = (FloatingActionButton) rootView.findViewById(R.id.free_fab_cancel);
        layoutMemo = (LinearLayout) rootView.findViewById(R.id.free_linearLayout_memo);
        layoutSearch = (LinearLayout) rootView.findViewById(R.id.free_linearLayout_search);
        textViewDate = (TextView) rootView.findViewById(R.id.free_textView_date);
        textViewInclude = (TextView) rootView.findViewById(R.id.free_textView_include);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new FreeAdapter(getContext(), this, items));

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

        if (editText.getText().toString().length() == 0)
            buttonSave.setEnabled(false);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time = Calendar.getInstance().getTime().getTime();
                FreeItem item = new FreeItem(time, editText.getText().toString(), address, photo);
                if (photo) {
                    try {
                        FileOutputStream fos = getActivity().openFileOutput(time + "", Activity.MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bitmap.recycle();
                    buttonPhoto.setBackgroundResource(R.drawable.photo_add);
                    photo = false;
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
                if (!photo) {
                    View view = inflater.inflate(R.layout.dialog_free_photo, null);
                    final AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.TransparentDialog).create();
                    dialog.setView(view);
                    dialog.show();

                    ImageButton buttonCamera = (ImageButton) view.findViewById(R.id.free_imageButton_camera), buttonGallery = (ImageButton) view.findViewById(R.id.free_imageButton_gallery);
                    buttonCamera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File file = null;
                            try {
                                file = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                            if (file != null) {
                                Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.group2.team.project1", file);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                                    ActivityCompat.startActivityForResult(getActivity(), intent, REQUEST_CAMERA, null);
                            }
                        }
                    });
                    buttonGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_GALLERY);
                            else
                                getPhotoFromGallery();
                        }
                    });
                } else {
                    bitmap.recycle();
                    buttonPhoto.setBackgroundResource(R.drawable.photo_add);
                    photo = false;
                }
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.dialog_free_search, null);
                final CalendarView calendarView1 = (CalendarView) view.findViewById(R.id.free_calendarView1), calendarView2 = (CalendarView) view.findViewById(R.id.free_calendarView2);
                final EditText editText = (EditText) view.findViewById(R.id.free_editText_search);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(view);
                builder.setPositiveButton(R.string.free_search_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long day = 24L * 3600 * 1000;
                        long date1 = calendarView1.getDate() / day * day, date2 = calendarView2.getDate() / day * day + day;
                        savedItems.addAll(items);
                        items.clear();
                        for (FreeItem item : savedItems) {
                            long date = item.getDate();
                            if (date1 <= date && date < date2 && item.getContent().contains(editText.getText().toString()))
                                items.add(item);
                        }
                        recyclerView.getAdapter().notifyDataSetChanged();
                        fabCancel.setVisibility(View.VISIBLE);
                        layoutMemo.setVisibility(View.GONE);
                        layoutSearch.setVisibility(View.VISIBLE);
                        textViewDate.setText("From " + format.format(new Date(date1)) + " to " + format.format(new Date(date2)));
                        textViewInclude.setText("Including \"" + editText.getText().toString() + "\"");
                        searching = true;
                    }
                });
                builder.setNegativeButton(R.string.free_search_negative, null);
                builder.show();

                calendarView1.setDate(calendarView2.getDate() - 30L * 24 * 3600 * 1000);
                calendarView1.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        if (calendarView1.getDate() > calendarView2.getDate()) {
                            Toast.makeText(getContext(), "Invalid date", Toast.LENGTH_LONG).show();
                            calendarView1.setDate(calendarView2.getDate());
                        }
                    }
                });
                calendarView2.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        if (calendarView1.getDate() > calendarView2.getDate()) {
                            Toast.makeText(getContext(), "Invalid date", Toast.LENGTH_LONG).show();
                            calendarView2.setDate(calendarView1.getDate());
                        } else if (calendarView2.getDate() > Calendar.getInstance().getTime().getTime()) {
                            Toast.makeText(getContext(), "Invalid date", Toast.LENGTH_LONG).show();
                            calendarView2.setDate(Calendar.getInstance().getTime().getTime());
                        }
                    }
                });
            }
        });

        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabCancel.setVisibility(View.INVISIBLE);
                layoutSearch.setVisibility(View.GONE);
                layoutMemo.setVisibility(View.VISIBLE);
                items.clear();
                items.addAll(savedItems);
                savedItems.clear();
                recyclerView.getAdapter().notifyDataSetChanged();
                searching = false;
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getInstance().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread() {
            @Override
            public void run() {
                googleApiClient.connect();
            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getInstance().unregister(this);
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
                object.put("address", item.getAddress());
                object.put("photo", item.isPhoto());
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        writeFile(array.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    sharePhotoAfterGetPermission(position);
                break;
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLocationUpdates();
                break;
            case PERMISSION_REQUEST_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getPhotoFromGallery();
                break;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull ActivityResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap tmp = BitmapFactory.decodeFile(currentPath);
                    if (tmp == null) {
                        Toast.makeText(getContext(), "Fail to load the photo", Toast.LENGTH_LONG).show();
                        return;
                    }
                    new File(currentPath).delete();
                    setPhoto(tmp);
                    setButtonPhoto();
                }
                break;
            case REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Uri imageUri = data.getData();
                        InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        setPhoto(BitmapFactory.decodeStream(imageStream));
                        setButtonPhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_SHARE:
                Log.i("cs496", "after share");
                boolean res = fileToDelete.delete();
                Log.i("cs496", res + "");
                break;
        }
    }

    private void getPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        ActivityCompat.startActivityForResult(getActivity(), intent, REQUEST_GALLERY, null);
    }

    private void setPhoto(Bitmap tmp) {
        int width = tmp.getWidth();
        int height = tmp.getHeight();

        if (width > PHOTO_WIDTH_MAX || height > PHOTO_HEIGHT_MAX) {
            float scaleFactor = Math.min(1.f * PHOTO_WIDTH_MAX / width, 1.f * PHOTO_HEIGHT_MAX / height);
            bitmap = Bitmap.createScaledBitmap(tmp, (int) (width * scaleFactor), (int) (height * scaleFactor), false);
            tmp.recycle();
        } else
            bitmap = tmp;
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
            FileOutputStream stream = getActivity().openFileOutput(FILE_NAME, Activity.MODE_PRIVATE);
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

        currentPath = image.getAbsolutePath();
        return image;
    }

    public void setButtonPhoto() {
        if (buttonPhoto != null)
            buttonPhoto.setBackgroundResource(R.drawable.photo_remove);
        photo = true;
    }

    public void handleDropdownClick(final int position) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        adapter.add(getString(R.string.free_edit));
        adapter.add(getString(R.string.free_delete));
        adapter.add(getString(R.string.free_share_text));
        if (items.get(position).isPhoto())
            adapter.add(getString(R.string.free_share_photo));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ListDialog);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (searching) {
                            Toast.makeText(getContext(), "You cannot edit the memo in the search mode.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        FreeItem item = items.remove(position);
                        recyclerView.getAdapter().notifyDataSetChanged();

                        editText.setText(item.getContent());
                        if (item.isPhoto()) {
                            if (bitmap != null && !bitmap.isRecycled())
                                bitmap.recycle();
                            try {
                                FileInputStream fis = getContext().openFileInput(item.getDate() + "");
                                byte[] arr = new byte[fis.available()];
                                fis.read(arr);
                                bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            new File(getContext().getFilesDir() + File.separator + item.getDate()).delete();
                            buttonPhoto.setBackgroundResource(R.drawable.photo_remove);
                            photo = true;
                        } else {
                            buttonPhoto.setBackgroundResource(R.drawable.photo_add);
                            photo = false;
                        }
                        break;
                    case 1:
                        FreeItem itm = items.remove(position);
                        if (itm.isPhoto())
                            new File(getContext().getFilesDir() + File.separator + itm.getDate()).delete();
                        if (searching)
                            savedItems.remove(itm);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                    case 2:
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, items.get(position).getContent());
                        getActivity().startActivity(Intent.createChooser(intent, getString(R.string.free_share_text)));
                        break;
                    case 3:
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            FreeFragment.this.position = position;
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                        } else
                            sharePhotoAfterGetPermission(position);
                        break;
                }
            }
        });
        builder.show();
    }

    public void sharePhotoAfterGetPermission(int position) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("image/jpeg");

        byte[] arr = null;
        try {
            FileInputStream fis = getContext().openFileInput(items.get(position).getDate() + "");
            arr = new byte[fis.available()];
            fis.read(arr);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (arr == null)
            return;

        fileToDelete = new File(Environment.getExternalStorageDirectory() + File.separator + "tmp" + Calendar.getInstance().getTime().getTime());
        try {
            fileToDelete.createNewFile();
            FileOutputStream fos = new FileOutputStream(fileToDelete);
            fos.write(arr);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToDelete));
        getActivity().startActivityForResult(Intent.createChooser(intent, getString(R.string.free_share_photo)), REQUEST_SHARE);
    }

    private String getAddress(Location location) {
        String nowAddress = getString(R.string.free_unknown_location);
        Geocoder geocoder = new Geocoder(getContext(), Locale.KOREA);
        List<Address> addresses;
        try {
            if (geocoder != null) {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    String currentLocationAddress = addresses.get(0).getAddressLine(0).toString();
                    nowAddress = currentLocationAddress;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return nowAddress;
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, new LocationRequest(), this);
    }
}