package com.group2.team.project1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.group2.team.project1.fragment.FreeFragment;
import com.group2.team.project1.fragment.GalleryFragment;
import com.group2.team.project1.fragment.PhoneNumberFragment;

public class MainActivity extends AppCompatActivity {

    final public static int REQUEST_CONTACT_ADD = 3;
    final public static int REQUEST_CONTACT_MODIFY = 4;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

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
            case FreeFragment.REQUEST_CAMERA:
                EventBus.getInstance().post(ActivityResultEvent.create(requestCode, resultCode, data));
                break;
            case FreeFragment.REQUEST_GALLERY:
                EventBus.getInstance().post(ActivityResultEvent.create(requestCode, resultCode, data));
                break;
            case REQUEST_CONTACT_ADD:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "add tried", Toast.LENGTH_LONG).show();
                    Fragment fragment = mSectionsPagerAdapter.fragments[0];

                    if (fragment != null) {
                        ((PhoneNumberFragment) fragment).addData(data.getBundleExtra("data"));
                    }
                }
                break;
            case REQUEST_CONTACT_MODIFY:
                if(resultCode == RESULT_OK){
                    Fragment fragment = mSectionsPagerAdapter.fragments[0];

                    if(fragment != null){
                        ((PhoneNumberFragment)fragment).addData(data.getBundleExtra("data"));
                    }
                }
        }
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
