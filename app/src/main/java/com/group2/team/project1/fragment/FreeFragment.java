package com.group2.team.project1.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.group2.team.project1.FreeItem;
import com.group2.team.project1.MainActivity;
import com.group2.team.project1.R;
import com.group2.team.project1.adapter.FreeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// Fragment class for C tab (Free)
public class FreeFragment extends Fragment {

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
                    Bitmap bitmap = ((MainActivity) getActivity()).getBitmap();
                    try {
                        FileOutputStream fos = getActivity().openFileOutput(time + "", Activity.MODE_PRIVATE);
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
                    View view = inflater.inflate(R.layout.dialog_free_photo, null);
                    final AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomDialog).create();
                    dialog.setView(view);
                    dialog.show();

                    ImageButton buttonCamera = (ImageButton) view.findViewById(R.id.free_imageButton_camera), buttonGallery = (ImageButton) view.findViewById(R.id.free_imageButton_gallery);
                    buttonCamera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File file = null;
                            try {
                                file = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (file != null) {
                                Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.group2.team.project1", file);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                                    getActivity().startActivityForResult(intent, MainActivity.REQUEST_CAMERA_FROM_FREE);
                            }
                        }
                    });
                    buttonGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
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

        ((MainActivity) getActivity()).setCurrentPath(image.getAbsolutePath());
        return image;
    }

    public void setButtonPhoto() {
        if (buttonPhoto != null)
            buttonPhoto.setText(R.string.free_remove_photo);
    }
}