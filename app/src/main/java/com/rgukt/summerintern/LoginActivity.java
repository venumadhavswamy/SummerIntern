package com.rgukt.summerintern;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextMobileNumber, editTextOTP;
    FirebaseAuth authentication;
    String verificationID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextMobileNumber = findViewById(R.id.editText_mobileNumber);
        editTextOTP = findViewById(R.id.xeditText_OTP);

        authentication = FirebaseAuth.getInstance();
    }

    public void sendOTP(View view)
    {
        String mobileNumber = editTextMobileNumber.getText().toString();
        Log.d("d","sendOTP");
        if (mobileNumber.isEmpty())
        {
            editTextMobileNumber.setError("Mobile Number is required");
            editTextMobileNumber.requestFocus();
            return;
        }
        else if(mobileNumber.length() < 10)
        {
            editTextMobileNumber.setError("Invalid Mobile Number");
            editTextMobileNumber.requestFocus();
            return;
        }

        mobileNumber = "+91" + mobileNumber;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobileNumber, 120, TimeUnit.SECONDS, this, mCallbacks);
    }

    public void verifyOTP(View view)
    {
        String OTP = editTextOTP.getText().toString();

        if (OTP.isEmpty())
        {
            editTextMobileNumber.setError("Please enter OTP");
            editTextMobileNumber.requestFocus();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, OTP);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            //Open HomeActivity
                            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                editTextOTP.setError("Invalid OTP");
                            }

                        }
                    }
                });
    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
        {

        }

        @Override
        public void onVerificationFailed(FirebaseException e)
        {
            Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            verificationID = verificationId;
            Toast.makeText(LoginActivity.this,"Code Sent",Toast.LENGTH_LONG);
        }
    };
}
