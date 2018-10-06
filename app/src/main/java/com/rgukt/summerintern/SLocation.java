package com.rgukt.summerintern;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SLocation
{

    public static void getLocation(Context context)
    {
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        double latitude, longitude;
        if(manager == null)
        {
            Toast.makeText(context,"Sorry, something went wrong",Toast.LENGTH_SHORT).show();
            return;
        }
        Location location, location1, location2;
        try {
            location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //location1 = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //location2 = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        catch (SecurityException e)
        {
            Toast.makeText(context,"Security Exception",Toast.LENGTH_SHORT).show();
            return;
        }

        if (location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }
        /*else  if (location1 != null)
        {
            latitude = location1.getLatitude();
            longitude = location1.getLongitude();

        }
        else  if (location2 != null)
        {
            latitude = location2.getLatitude();
            longitude = location2.getLongitude();
        }*/
        else
        {
            Toast.makeText(context,"locations has null value",Toast.LENGTH_SHORT).show();
            return;
        }

        GlobalVariables.latitude = ""+latitude;
        GlobalVariables.longitude = ""+longitude;
        GlobalVariables.isProfileSet = true;
    }

    public static String getAddress(String latitude, String longitude,Context context)
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        String userAddress;

        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
            userAddress = addresses.get(0).getAddressLine(0);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context,"Sorry, can't get address", Toast.LENGTH_SHORT).show();
            return "null";
        }

        return userAddress;
    }

    public static void uploadLocation(String latitude,String longitude,long date)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String path = Constants.fUsers +"/"+ GlobalVariables.userID +"/"+ Constants.fPublicData;

        ref.child(path).child(Constants.fLatitude).setValue(latitude);
        ref.child(path).child(Constants.fLongitude).setValue(longitude);
        ref.child(path).child(Constants.fLastUpdatedTime).setValue(date);
    }

    public static void saveLocation(String latitude,String longitude,long date,Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.locationPreferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.sharedPreferenceLatitude,latitude);
        editor.putString(Constants.sharedPreferenceLongitude,longitude);
        editor.putBoolean(Constants.sharedPreferencesIsLocationSet,true);
        editor.putString(Constants.sharedPreferenceLastLocation,GlobalVariables.lastLocation);
        editor.putLong(Constants.sharedPreferencesLastUpdatedTime,date);
        editor.apply();
    }
}
