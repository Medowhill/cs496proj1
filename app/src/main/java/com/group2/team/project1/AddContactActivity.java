package com.group2.team.project1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by q on 2016-12-27.
 */

public class AddContactActivity extends Activity {
    private EditText mNameEt;
    private EditText mPhonenumberEt;
    private int modifyPosition = -1;
    private ImageView mProfile;
    private Uri mPhotoURI = null;
    String mCurrentPhotoPath = null;


    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_SEARCH = 2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcontact);

        mNameEt = (EditText) findViewById(R.id.name_add_edit);
        mPhonenumberEt = (EditText) findViewById(R.id.phonenumber_add_edit);
        Intent gotIntent = getIntent();
        if (gotIntent != null) {
            Bundle gotBundle = gotIntent.getBundleExtra("data");
            if (gotBundle != null) {
                mNameEt.setText(gotBundle.getString("name"));
                mPhonenumberEt.setText(gotBundle.getString("phoneNumber"));
                modifyPosition = gotBundle.getInt("position");
                if (!gotBundle.getString("photoDir").equals("")) {
                    //mCurrentPhotoPath = gotBundle.getString("photoDir");
                    mPhotoURI = Uri.parse(gotBundle.getString("photoDir"));
                    //Log.i("cs496: ocCreate", mCurrentPhotoPath);
                }

            }
        }

        mProfile = (ImageView) findViewById(R.id.pic_add);
        if (mPhotoURI == null) {//(mCurrentPhotoPath == null) {
            mProfile.setImageResource(R.drawable.ic_face_black_48dp);
        } else {
            setPic();
        }

    }

    private void setPic() {

        int targetW = 150; //mProfile.getWidth();
        int targetH = 150; //mProfile.getHeight();
        try {

            InputStream is = this.getContentResolver().openInputStream(mPhotoURI);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            //BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            //MediaStore.Images.Media.getBitmap(this.getContentResolver(), mPhotoURI);
            BitmapFactory.decodeStream(is, null, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            is = this.getContentResolver().openInputStream(mPhotoURI);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, bmOptions);

            //Log.i("cs496", bitmap.getWidth() + "," + bitmap.getHeight());
            mProfile.setImageBitmap(bitmap);

            /*ExifInterface exif = new ExifInterface(mPhotoURI.getPath());
            String[] proj = {MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = getContentResolver().query(mPhotoURI, proj, null, null, null);
            int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);
            cursor.moveToFirst();
            int orientation = cursor.getInt(colIndex);
            String ori = cursor.getString(colIndex);
            Log.i("cs496", mPhotoURI.toString());
            //int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch(orientation){
                case ExifInterface.ORIENTATION_NORMAL:
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    mProfile.setRotation(90f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    mProfile.setRotation(180f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    mProfile.setRotation(270f);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    mProfile.setScaleX(-1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    mProfile.setScaleY(-1);
                    break;

            }*/


        } catch (Exception e) {
            mProfile.setImageResource(R.drawable.ic_face_black_48dp);
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addCommitButton: {
                //Contact newContact = new Contact();
                String mName = mNameEt.getText().toString();
                String mPhoneNumber = mPhonenumberEt.getText().toString();

                Bundle newBundle = new Bundle();

                newBundle.putString("name", mName);
                newBundle.putString("phoneNumber", mPhoneNumber);
                //Log.i("cs496: add", mPhotoURI.toString());
                if (mPhotoURI != null) { //(mCurrentPhotoPath != null) {
                    //newBundle.putString("photoDir", mCurrentPhotoPath);
                    newBundle.putString("photoDir", mPhotoURI.toString());
                } else {
                    newBundle.putString("photoDir", "");
                }

                newBundle.putInt("position", modifyPosition);


                Intent newIntent = new Intent(this, MainActivity.class);
                newIntent.putExtra("data", newBundle);
                Log.i("cs496", "onClick");
                setResult(RESULT_OK, newIntent);
                finish();
                break;
            }
            case R.id.addCancelButton: {
                finish();
                break;
            }
            case R.id.new_pic_add: {
                Intent newIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (newIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (photoFile != null) {
                        mPhotoURI = FileProvider.getUriForFile(this, "com.group2.team.project1", photoFile);
                        Log.i("cs496", mPhotoURI.toString());
                        newIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                        startActivityForResult(newIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                break;
            }
            case R.id.pic_add: {
                Intent newIntent = new Intent(Intent.ACTION_GET_CONTENT);
                newIntent.setType("image/*");
                if (newIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(newIntent, REQUEST_IMAGE_SEARCH);
                }

            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
        } else if (requestCode == REQUEST_IMAGE_SEARCH && resultCode == RESULT_OK) {
            mPhotoURI = data.getData();
            setPic();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
