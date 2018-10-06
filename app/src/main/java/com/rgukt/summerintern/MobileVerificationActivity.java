package com.rgukt.summerintern;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    private EditText editTextMobileNumber;
    String mobileNumber;
    ProgressBar progress;
    Window currentWindow;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        editTextMobileNumber = findViewById(R.id.editText_mobileNumber_VerificationActivity);
        progress = findViewById(R.id.progressBar_VerificationActivity);
        currentWindow = getWindow();
        context = getApplicationContext();
    }

    public void sendOTP(View view)
    {
        mobileNumber = editTextMobileNumber.getText().toString();
        if (mobileNumber.isEmpty())
        {
            editTextMobileNumber.setError("Mobile Number is required");
            editTextMobileNumber.requestFocus();
            return;
        }
        else if(mobileNumber.length() != 10)
        {
            editTextMobileNumber.setError("Invalid Mobile Number");
            editTextMobileNumber.requestFocus();
            return;
        }
        if(!GlobalFunctions.isNetworkAvailable(context))
        {
            Toast.makeText(MobileVerificationActivity.this,"Not internet connection",Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        //Disable user interaction
        currentWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mobileNumber = "+91" + mobileNumber;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobileNumber, 0, TimeUnit.SECONDS, this, mCallbacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
        {
            progress.setVisibility(View.GONE);
            currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable user interaction
            Toast.makeText(MobileVerificationActivity.this,"Verification completed",Toast.LENGTH_LONG).show();
            GlobalFunctions.startActivity(MobileVerificationActivity.this,HomeActivity.class,true);
        }

        @Override
        public void onVerificationFailed(FirebaseException e)
        {
            progress.setVisibility(View.GONE);
            currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable user interaction
            if(e instanceof FirebaseNetworkException)
            {
                Toast.makeText(MobileVerificationActivity.this,"No internet connection",Toast.LENGTH_LONG).show();
            }
            else if(e instanceof FirebaseAuthInvalidCredentialsException)
            {
                editTextMobileNumber.setError("Invalid mobile number");
                editTextMobileNumber.requestFocus();
            }
            else if(e instanceof FirebaseTooManyRequestsException)
            {
                Toast.makeText(MobileVerificationActivity.this,"Message quota exceeded",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(MobileVerificationActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCodeSent(String verificationID, PhoneAuthProvider.ForceResendingToken forceResendingToken)
        {
            progress.setVisibility(View.GONE);
            currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);//Enable user interaction
            super.onCodeSent(verificationID, forceResendingToken);
            Intent intent = new Intent(MobileVerificationActivity.this,OTPActivity.class);
            intent.putExtra("mobileNumber",mobileNumber);
            intent.putExtra("verificationID",verificationID);
            startActivity(intent);
            finish();
        }
    };
}
