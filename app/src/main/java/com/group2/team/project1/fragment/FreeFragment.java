package com.group2.team.project1.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
    private boolean photo = false;
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton buttonSave, buttonPhoto;
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
        buttonSave = (ImageButton) rootView.findViewById(R.id.free_button_save);
        buttonPhoto = (ImageButton) rootView.findViewById(R.id.free_button_photo);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new FreeAdapter((MainActivity) getActivity(), items));

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
                    ((MainActivity) getActivity()).recycleBitmap();
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
                                    getActivity().startActivityForResult(intent, MainActivity.REQUEST_CAMERA_FROM_FREE);
                            }
                        }
                    });
                    buttonGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            dialog.dismiss();
                            getActivity().startActivityForResult(intent, MainActivity.REQUEST_GALLERY_FROM_FREE);
                        }
                    });
                } else {
                    ((MainActivity) getActivity()).recycleBitmap();
                    buttonPhoto.setBackgroundResource(R.drawable.photo_add);
                    photo = false;
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
            buttonPhoto.setBackgroundResource(R.drawable.photo_remove);
        photo = true;
    }

    public void deleteItem(int position) {
        items.remove(position);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void editItem(int position) {
        FreeItem item = items.remove(position);
        recyclerView.getAdapter().notifyDataSetChanged();

        editText.setText(item.getContent());
        if (item.isPhoto()) {
            ((MainActivity) getActivity()).recycleBitmap();
            try {
                FileInputStream fis = getContext().openFileInput(item.getDate() + "");
                byte[] arr = new byte[fis.available()];
                fis.read(arr);
                ((MainActivity) getActivity()).setBitmap(BitmapFactory.decodeByteArray(arr, 0, arr.length));
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            buttonPhoto.setBackgroundResource(R.drawable.photo_remove);
            photo = true;
        } else {
            buttonPhoto.setBackgroundResource(R.drawable.photo_add);
            photo = false;
        }
    }

    public boolean hasPhoto(int position) {
        return items.get(position).isPhoto();
    }

    public void shareText(int position) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, items.get(position).getContent());
        getActivity().startActivity(Intent.createChooser(intent, getString(R.string.free_share_text)));
    }

    public void sharePhoto(int position) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.PERMISSION_REQUEST_FROM_FREE);
        else
            sharePhotoAfterGetPermission(position);
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

        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "tmp" + Calendar.getInstance().getTime().getTime());
        try {
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(arr);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        getActivity().startActivity(Intent.createChooser(intent, getString(R.string.free_share_photo)));
    }
}