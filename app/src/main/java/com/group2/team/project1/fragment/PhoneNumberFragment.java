package com.group2.team.project1.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.group2.team.project1.AddContactActivity;
import com.group2.team.project1.Contact;
import com.group2.team.project1.MainActivity;
import com.group2.team.project1.R;
import com.group2.team.project1.adapter.ContactsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

// Fragment class for A tab (Phone book)
public class PhoneNumberFragment extends Fragment {

    private static final String CONTACTS = "./contacts";
    private JSONArray contacts_json = null;
    private ContactsAdapter mContactsAdapter = null;
    private FloatingActionButton addButton = null;
    private ArrayList<Contact> contactsList;
    private ListView mListView;

    public static PhoneNumberFragment newInstance() {
        return new PhoneNumberFragment();
    }

    public static PhoneNumberFragment newInstance(Bundle bundle){
        PhoneNumberFragment newPhoneNumberFragment = new PhoneNumberFragment();
        newPhoneNumberFragment.setArguments(bundle);
        return newPhoneNumberFragment;
    }

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        String c = load();

        if(c.isEmpty()){
            contacts_json = new JSONArray();
        } else {
            try {
                contacts_json = new JSONArray(c);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        contactsList = new ArrayList<Contact>();
        if(contacts_json != null){
            for(int i = 0; i< contacts_json.length(); i++){
                try {
                    Contact newContact = new Contact();
                    JSONObject j = contacts_json.getJSONObject(i);
                    newContact.mName = j.getString("name");
                    newContact.mPhoneNumber = j.getString("phoneNumber");
                    contactsList.add(newContact);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        mContactsAdapter = new ContactsAdapter(getActivity(), R.layout.contacts_list_item, R.id.contacts_item_name_text, contactsList);


    }
    public void addData(Bundle bundle){

            Contact newContact = new Contact();
            newContact.mName = bundle.getString("name");
            newContact.mPhoneNumber = bundle.getString("phoneNumber");
            mContactsAdapter.add(newContact);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phone, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.textView_phone);
        //textView.setText("Phone Number Fragment");


        mListView = (ListView) rootView.findViewById(R.id.listView_phone);
        mListView.setAdapter(mContactsAdapter);
        addButton = (FloatingActionButton) rootView.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), AddContactActivity.class);
                getActivity().startActivityForResult(intent, MainActivity.REQUEST_CONTACT_ADD);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Contact target = (Contact) parent.getItemAtPosition(position);
            }
        });
        return rootView;
    }

    public void onDestroy(){
        super.onDestroy();
        JSONArray array = new JSONArray();
        for(Contact i: contactsList){
            try {
                JSONObject newObject = new JSONObject();
                newObject.put("name", i.mName);
                newObject.put("phoneNumber", i.mPhoneNumber);
                array.put(newObject);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        save(array.toString());
    }
    private String load(){
        try{
            FileInputStream contactsIn = getActivity().openFileInput(CONTACTS);
            byte[] bytesData = new byte[contactsIn.available()];
            while(contactsIn.read(bytesData) != -1){}
            contactsIn.close();

            return new String(bytesData);
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private void save(String strData){
        if(strData == null || strData.isEmpty()){
            return;
        }
        FileOutputStream contactsOut;

        try{
            contactsOut = getActivity().openFileOutput(CONTACTS, Context.MODE_PRIVATE);
            contactsOut.write(strData.getBytes());
            contactsOut.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}