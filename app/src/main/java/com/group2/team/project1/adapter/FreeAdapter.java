package com.group2.team.project1.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.group2.team.project1.FreeItem;
import com.group2.team.project1.R;
import com.group2.team.project1.fragment.FreeFragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FreeAdapter extends RecyclerView.Adapter<FreeAdapter.ViewHolder> {

    private Context context;
    private FreeFragment fragment;
    private ArrayList<FreeItem> items;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private HashMap<Long, Bitmap> tmpBitmaps;
    private HashMap<Long, ImageView> tmpImageViews;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long date = msg.getData().getLong("date");
            tmpImageViews.get(date).setImageBitmap(tmpBitmaps.get(date));
        }
    };

    public FreeAdapter(Context context, FreeFragment fragment, ArrayList<FreeItem> items) {
        this.context = context;
        this.fragment = fragment;
        this.items = items;
        tmpBitmaps = new HashMap<>();
        tmpImageViews = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_free_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final FreeItem item = items.get(position);
        holder.textViewDate.setText(format.format(new Date(item.getDate())));
        holder.textViewContent.setText(item.getContent());
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.handleDropdownClick(position);
            }
        });

        if (item.isPhoto()) {
            tmpImageViews.put(item.getDate(), holder.imageView);
            holder.imageView.setVisibility(View.VISIBLE);
            new Thread() {
                @Override
                public synchronized void run() {
                    try {
                        FileInputStream fis = context.openFileInput(item.getDate() + "");
                        byte[] arr = new byte[fis.available()];
                        fis.read(arr);
                        tmpBitmaps.put(item.getDate(), BitmapFactory.decodeByteArray(arr, 0, arr.length));
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putLong("date", item.getDate());
                        message.setData(bundle);
                        handler.sendMessage(message);
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            holder.imageView.setImageBitmap(null);
            holder.imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewDate, textViewContent;
        ImageButton imageButton;

        ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.free_item_imageView);
            textViewDate = (TextView) view.findViewById(R.id.free_item_textView_date);
            textViewContent = (TextView) view.findViewById(R.id.free_item_textView_content);
            imageButton = (ImageButton) view.findViewById(R.id.free_item_imageButton);
        }
    }
}
