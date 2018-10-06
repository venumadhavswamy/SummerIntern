package com.rgukt.summerintern.contacts;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rgukt.summerintern.Constants;
import com.rgukt.summerintern.GlobalFunctions;
import com.rgukt.summerintern.GlobalVariables;
import com.rgukt.summerintern.R;


public class AddContactActivity extends AppCompatActivity {

    EditText mobileNumberEditText, nameEditText;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        context = getApplicationContext();
        //Display back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mobileNumberEditText = findViewById(R.id.editText_mobileNumber_AddContactsActivity);
        nameEditText = findViewById(R.id.editText_name_AddContactsActivity);
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    public void addContact(View view)
    {
        if(!GlobalFunctions.isNetworkAvailable(context))
        {
            Toast.makeText(AddContactActivity.this,"No internet connection",Toast.LENGTH_SHORT).show();
            return;
        }

        String name, mobileNumber;
        name = nameEditText.getText().toString();
        if(name.isEmpty())
        {
            nameEditText.setError("Valid name required");
            nameEditText.requestFocus();
            return;
        }
        else if(name.length() <3)
        {
            nameEditText.setError("Must contain at least 3 characters");
            nameEditText.requestFocus();
            return;
        }

        mobileNumber = mobileNumberEditText.getText().toString();
        if(mobileNumber.isEmpty() || mobileNumber.length() != 10)
        {
            mobileNumberEditText.setError("Enter valid mobile number");
            mobileNumberEditText.requestFocus();
            return;
        }

        mobileNumber = "+91" + mobileNumber;
        String ref = Constants.fUsers +"/"+ GlobalVariables.userID +"/"+ Constants.fContacts;
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(ref);
        databaseRef.child(mobileNumber).setValue(name);
        onBackPressed();
    }
}