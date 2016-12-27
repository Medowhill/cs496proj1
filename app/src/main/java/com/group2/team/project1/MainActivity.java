package com.group2.team.project1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import com.group2.team.project1.fragment.FreeFragment;
import com.group2.team.project1.fragment.GalleryFragment;
import com.group2.team.project1.fragment.PhoneNumberFragment;

public class MainActivity extends AppCompatActivity {

    final public static int REQUEST_CAMERA_FROM_FREE = 1, REQUEST_GALLERY_FROM_FREE = 2, PERMISSION_REQUEST_FROM_FREE = 3;
    final private static int PHOTO_WIDTH_MAX = 1440, PHOTO_HEIGHT_MAX = 1440;

    final public static int REQUEST_CONTACT_ADD = 3;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private String currentPath;
    private Bitmap bitmap;
    private int position;

    Intent intent = null;

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
        switch (requestCode) {
            case REQUEST_CAMERA_FROM_FREE:
                if (resultCode == RESULT_OK) {
                    setPhoto(BitmapFactory.decodeFile(currentPath));

                    Fragment fragment = mSectionsPagerAdapter.fragments[2];
                    if (fragment != null)
                        ((FreeFragment) fragment).setButtonPhoto();
                    else
                        Toast.makeText(getApplicationContext(), "Fail to load the photo! Please go to A tab and come back to C tab.", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_GALLERY_FROM_FREE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri imageUri = data.getData();
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        setPhoto(BitmapFactory.decodeStream(imageStream));

                        Fragment fragment = mSectionsPagerAdapter.fragments[2];
                        if (fragment != null)
                            ((FreeFragment) fragment).setButtonPhoto();
                        else
                            Toast.makeText(getApplicationContext(), "Fail to load the photo! Please go to A tab and come back to C tab.", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CONTACT_ADD:
                if(resultCode == RESULT_OK){
                    Toast.makeText(getApplicationContext(), "add tried", Toast.LENGTH_LONG).show();
                    Fragment fragment = mSectionsPagerAdapter.fragments[0];

                    if(fragment != null){
                        ((PhoneNumberFragment)fragment).addData(data.getBundleExtra("data"));
                    }
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_FROM_FREE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Fragment fragment = mSectionsPagerAdapter.fragments[2];
                if (fragment != null) {
                    ((FreeFragment) fragment).sharePhotoAfterGetPermission(position);
                }
            }
        }
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

    public void handleDropdownClick(final int position) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
        final Fragment fragment = mSectionsPagerAdapter.fragments[2];
        adapter.add(getString(R.string.free_edit));
        adapter.add(getString(R.string.free_delete));
        adapter.add(getString(R.string.free_share_text));
        if (fragment != null && ((FreeFragment) fragment).hasPhoto(position))
            adapter.add(getString(R.string.free_share_photo));

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.ListDialog);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (fragment != null) {
                    FreeFragment freeFragment = ((FreeFragment) fragment);
                    switch (which) {
                        case 0:
                            freeFragment.editItem(position);
                            break;
                        case 1:
                            freeFragment.deleteItem(position);
                            break;
                        case 2:
                            freeFragment.shareText(position);
                            break;
                        case 3:
                            MainActivity.this.position = position;
                            freeFragment.sharePhoto(position);
                            break;
                    }
                }
            }
        });
        builder.show();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void recycleBitmap() {
        if (bitmap != null && !bitmap.isRecycled())
            bitmap.recycle();
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

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
