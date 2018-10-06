package com.rgukt.summerintern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OTPActivity extends AppCompatActivity {

    String mobileNumber, verificationID;
    TextView textViewMobileNumber;
    EditText editTextOTP;
    FirebaseAuth authentication;
    ProgressBar progress;
    Window currentWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        authentication = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        mobileNumber = intent.getStringExtra("mobileNumber");
        verificationID = intent.getStringExtra("verificationID");
        textViewMobileNumber = findViewById(R.id.textView_mobileNumber);
        editTextOTP = findViewById(R.id.editText_OTP_verificationActivity);
        textViewMobileNumber.setText(mobileNumber);
        progress = findViewById(R.id.progressBar_OTPActivity);
        currentWindow = getWindow();
    }

    public void verifyOTP(View view)
    {
        String OTP = editTextOTP.getText().toString();

        if (OTP.isEmpty() || OTP.length() < 6)
        {
            editTextOTP.setError("Enter valid OTP");
            editTextOTP.requestFocus();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        //Disable user interaction
        currentWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, OTP);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            createUser();
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                editTextOTP.setError("Invalid OTP");
                                editTextOTP.requestFocus();
                            }

                        }
                    }
                });
    }

    private void createUser()
    {
        //Save user data to shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.myPreferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.sharedPreferenceUserID,mobileNumber);
        GlobalVariables.userID = mobileNumber;
        editor.apply();

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        DatabaseReference userRef = database.child("Users").child(mobileNumber);
        Log.d("ssss",userRef.toString());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    Log.d("not exist","adfadf");
                    //create new user if user not exists
                    User user = new User();
                    database.child(Constants.fUsers +"/"+ mobileNumber +"/"+ Constants.fPublicData).setValue(user);

                    currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable user interaction
                    Intent intent = new Intent(OTPActivity.this,InitialProfileSetupActivity.class);
                    intent.putExtra("mobileNumber",mobileNumber);
                    startActivity(intent);
                    finish();
                }
                else//Go to user home page if he has account
                {
                    currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable user interaction
                    GlobalFunctions.startActivity(OTPActivity.this,HomeActivity.class,true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userRef.addListenerForSingleValueEvent(eventListener);
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        progress.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
