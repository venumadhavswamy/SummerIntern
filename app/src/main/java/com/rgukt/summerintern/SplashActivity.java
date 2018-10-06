package com.rgukt.summerintern;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = authentication.getCurrentUser();
        retrieveSharedPreferences();
        if (currentUser != null)
        {
            Log.d("splash","current user");
            if(GlobalVariables.userID.equals("null"))
            {
                Log.d("splash","current user null");
                GlobalFunctions.startActivity(this, MobileVerificationActivity.class, true);
            }
            else
                GlobalFunctions.startActivity(this,HomeActivity.class,true);
        }
        else
        {
            Log.d("splash","no current user");
            GlobalFunctions.startActivity(this,MobileVerificationActivity.class,true);
        }
    }

    private void retrieveSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        GlobalVariables.userName = sharedPreferences.getString(Constants.sharedPreferenceUserName,"Your name");
        GlobalVariables.userID = sharedPreferences.getString(Constants.sharedPreferenceUserID,"null");
        GlobalVariables.userStatus = sharedPreferences.getString(Constants.sharedPreferenceUserStatus,"Your status here");
        GlobalVariables.isProfileSet = sharedPreferences.getBoolean(Constants.sharedPreferenceIsUserProfileSet,false);

        SharedPreferences locationPreferences = getSharedPreferences(Constants.locationPreferences,Context.MODE_PRIVATE);
        GlobalVariables.isLocationSet = locationPreferences.getBoolean(Constants.sharedPreferencesIsLocationSet,false);
        if(GlobalVariables.isLocationSet) {
            GlobalVariables.latitude = locationPreferences.getString(Constants.sharedPreferenceLatitude, "");
            GlobalVariables.longitude = locationPreferences.getString(Constants.sharedPreferenceLongitude, "");
            long tempTime = locationPreferences.getLong(Constants.sharedPreferencesLastUpdatedTime,0);
            if(tempTime != 0)
                GlobalVariables.lastUpdatedTime = GlobalFunctions.getlastUpdatedTime(tempTime);
            GlobalVariables.lastLocation = locationPreferences.getString(Constants.sharedPreferenceLastLocation,"No location");
        }
    }
}
