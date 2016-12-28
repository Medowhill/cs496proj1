package com.group2.team.project1.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.group2.team.project1.Contact;
import com.group2.team.project1.R;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by q on 2016-12-26.
 */

public class ContactsAdapter extends ArrayAdapter<Contact> implements Filterable {
    //private List<Contact> original;
    private View.OnClickListener mOnClickListener;
    public ContactsAdapter(Context context, int resources, int textViewResourceId, List<Contact> data, View.OnClickListener onClickListener){
        super(context, resources, textViewResourceId, data);
        //original = new ArrayList<>();
        //original.addAll(data);
        mOnClickListener = onClickListener;
    }

    class ViewHolder{
        TextView mNameTv;
        ImageButton mDialButton;
        ImageButton mRemoveButton;
        ImageButton mSMSButton;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View itemLayout = super.getView(position, convertView, parent);

        ViewHolder viewHolder = (ViewHolder)itemLayout.getTag();

        if(viewHolder == null){
            viewHolder = new ViewHolder();
            viewHolder.mNameTv = (TextView) itemLayout.findViewById(R.id.contacts_item_name_text);
            viewHolder.mDialButton = (ImageButton) itemLayout.findViewById(R.id.contacts_item_dial_button);
            viewHolder.mDialButton.setFocusable(false);
            viewHolder.mRemoveButton = (ImageButton) itemLayout.findViewById(R.id.contacts_item_remove_button);
            viewHolder.mRemoveButton.setFocusable(false);
            viewHolder.mSMSButton = (ImageButton) itemLayout.findViewById(R.id.contacts_item_SMS_button);
            viewHolder.mSMSButton.setFocusable(false);
            itemLayout.setTag(viewHolder);
        }


        viewHolder.mNameTv.setText(getItem(position).mName);

        viewHolder.mDialButton.setOnClickListener(mOnClickListener);
        viewHolder.mDialButton.setTag(position);

        viewHolder.mRemoveButton.setOnClickListener(mOnClickListener);
        viewHolder.mRemoveButton.setTag(position);

        viewHolder.mSMSButton.setOnClickListener(mOnClickListener);
        viewHolder.mSMSButton.setTag(position);

        return itemLayout;
    }

    /*@Override
    public Filter getFilter(){
        return new Filter(){
            @Override
            protected FilterResults performFiltering(CharSequence cs){
                String filterString = cs.toString().toLowerCase();

                FilterResults results = new FilterResults();

                final ArrayList<Contact> list = new ArrayList<>();
                if(cs != null || cs.length() != 0) {
                    for (int i = 0; i <original.size(); i++){
                        Contact contact = original.get(i);
                        if(contact.mName.toLowerCase().contains(filterString)){
                            list.add(contact);
                        }
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    results.values = original;
                    results.count = original.size();
                }

                return results;
            }
            protected void publishResults(CharSequence cs, FilterResults rs){
                notifyDataSetChanged();
                clear();
                addAll((List<Contact>) rs.values);
                notifyDataSetInvalidated();

            }
        };
    }*/
}

