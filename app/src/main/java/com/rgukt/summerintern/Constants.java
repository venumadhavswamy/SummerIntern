package com.rgukt.summerintern;

import android.graphics.Color;

public interface Constants
{

    String myPreferences = "myPreferences";
    String locationPreferences = "location";
    String sharedPreferenceLatitude = "latitude";
    String sharedPreferenceLongitude = "longitude";
    String sharedPreferencesLastUpdatedTime = "lastUpdatedTime";
    String sharedPreferenceLastLocation = "lastLocation";
    String sharedPreferencesIsLocationSet = "isLocationSet";
    String sharedPreferenceUserID = "userID";
    String sharedPreferenceUserName = "name";
    String sharedPreferenceIsUserProfileSet ="isProfileSet";
    String sharedPreferenceUserStatus = "status";


    //Colors
    int mainColor = Color.parseColor("#FF6600");
    int whiteColor = Color.parseColor("#ffffff");
    int lightGray = Color.parseColor("#AEAEAE");


    //Firebase
    String fUsers = "users";
    String fPublicData = "publicData";
    String fContacts = "contacts";
    String fName = "name";
    String fIsProfileSet = "isProfileSet";
    String fStatus = "status";
    String fLastLocation = "lastLocation";
    String fLastUpdatedTime = "lastUpdatedTime";
    String fIsLive = "isLive";
    String fLatitude = "latitude";
    String fLongitude = "longitude";

    String imagesDirectory = "images";
}
