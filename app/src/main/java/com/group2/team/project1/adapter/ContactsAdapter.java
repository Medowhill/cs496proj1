package com.group2.team.project1.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.group2.team.project1.Contact;
import com.group2.team.project1.R;


import java.util.List;

/**
 * Created by q on 2016-12-26.
 */

public class ContactsAdapter extends ArrayAdapter<Contact> {

    public ContactsAdapter(Context context, int resources, int textViewResourceId, List<Contact> data){//}, View.OnClickListener onClickListener){
        super(context, resources, textViewResourceId, data);
    }

    class ViewHolder{
        TextView mNameTv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View itemLayout = super.getView(position, convertView, parent);

        ViewHolder viewHolder = (ViewHolder)itemLayout.getTag();

        if(viewHolder == null){
            viewHolder = new ViewHolder();
            viewHolder.mNameTv = (TextView) itemLayout.findViewById(R.id.contacts_item_name_text);
            itemLayout.setTag(viewHolder);
        }


        viewHolder.mNameTv.setText(getItem(position).mName);
        ImageButton editButton = (ImageButton) itemLayout.findViewById(R.id.contacts_item_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent();
                Bundle newBundle = new Bundle();
                //newBundle.putString("name", getItem(position).mName);
                //newBundle.putString("phoneNumber", getItem(position).mPhoneNumber);
                newIntent.putExtra("data", newBundle);

            }
        });


        return itemLayout;
    }
}
