package com.group2.team.project1.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.group2.team.project1.FreeItem;
import com.group2.team.project1.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FreeAdapter extends RecyclerView.Adapter<FreeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<FreeItem> items;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FreeAdapter(Context context, ArrayList<FreeItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_free_cardview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FreeItem item = items.get(position);
        holder.textViewDate.setText(format.format(new Date(item.getDate())));
        holder.textViewContent.setText(item.getContent());
        if (item.isPhoto()) {
            try {
                FileInputStream fis = context.openFileInput(item.getDate() + "");
                byte[] arr = new byte[fis.available()];
                fis.read(arr);
                holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(arr, 0, arr.length));
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            holder.imageView.setImageBitmap(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewDate, textViewContent;

        ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.free_item_imageView);
            textViewDate = (TextView) view.findViewById(R.id.free_item_textView_date);
            textViewContent = (TextView) view.findViewById(R.id.free_item_textView_content);
        }
    }
}
