package com.group2.team.project1.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;

import com.group2.team.project1.R;
import com.group2.team.project1.adapter.GalleryAdapter;

// Fragment class for B tab (Gallery)
public class GalleryFragment extends Fragment {

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        final GalleryAdapter adapter = new GalleryAdapter(getContext());
        for (int i = 1; i < 21; i++) {
            adapter.add(getResources().getIdentifier("t" + i, "drawable", getActivity().getPackageName()));
        }

        final ImageView iv1 = (ImageView) rootView.findViewById(R.id.imageView1);

        Gallery g = (Gallery) rootView.findViewById(R.id.gallery1);
        g.setAdapter(adapter);
        g.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                iv1.setImageResource(adapter.get(position));
            }
        });
        return rootView;
    }
}