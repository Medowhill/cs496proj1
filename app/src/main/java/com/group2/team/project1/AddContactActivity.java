package com.group2.team.project1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

/**
 * Created by q on 2016-12-27.
 */

public class AddContactActivity extends Activity {
    private EditText mNameEt;
    private EditText mPhonenumberEt;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcontact);

        mNameEt =  (EditText) findViewById(R.id.name_add_edit);
        mPhonenumberEt = (EditText) findViewById(R.id.phonenumber_add_edit);
        Intent gotIntent = getIntent();
        if(gotIntent != null) {
            Bundle gotBundle = gotIntent.getBundleExtra("data");
            if(gotBundle != null) {
                mNameEt.setText(gotBundle.getString("name"));
                mPhonenumberEt.setText(gotBundle.getString("phoneNumber"));
            }
        }

    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.addCommitButton:
            {
                //Contact newContact = new Contact();
                String mName = mNameEt.getText().toString();
                String mPhoneNumber = mPhonenumberEt.getText().toString();

                Bundle newBundle = new Bundle();

                newBundle.putString("name", mName);
                newBundle.putString("phoneNumber", mPhoneNumber);


                Intent newIntent = new Intent(this, MainActivity.class);
                newIntent.putExtra("data", newBundle);

                setResult(RESULT_OK, newIntent);
                finish();
            }
        }
    }
}
