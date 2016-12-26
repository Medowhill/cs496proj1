package com.group2.team.project1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.group2.team.project1.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by q on 2016-12-26.
 */


public class GalleryAdapter extends BaseAdapter {

    private List<Integer> galleryIda = new ArrayList<Integer>();
    private final Context mContext;
    private LayoutInflater inflater;

    public GalleryAdapter(Context c) {
        mContext = c;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return galleryIda.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gallery_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
        imageView.setImageResource(galleryIda.get(position));

        return convertView;
    }

    public void add(int i) {
        galleryIda.add(i);
    }

    public int get(int position) {
        return galleryIda.get(position);
    }
}
