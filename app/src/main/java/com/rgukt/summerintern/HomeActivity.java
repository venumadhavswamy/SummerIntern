package com.rgukt.summerintern;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.input.InputManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rgukt.summerintern.contacts.ContactsActivity;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity
{
    Context context;
    TextView nameTextView, statusTextView, addressTextView, lastUpdatedTimeTextView;
    ImageView profileImageView;
    View locationView;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(1).setChecked(true);

        context = getApplicationContext();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationView = findViewById(R.id.locationView);
        nameTextView = findViewById(R.id.textView_name_HomeActivity);
        statusTextView = findViewById(R.id.textView_status_HomeActivity);
        profileImageView = findViewById(R.id.imageView_profile_HomeActivity);
        addressTextView = findViewById(R.id.textView_address_HomeActivity);
        lastUpdatedTimeTextView = findViewById(R.id.textView_lastUpdated_HomeActivity);

        setHome();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_contacts:
                    openContacts();
                    return true;
                case R.id.navigation_profile:
                    openProfile();
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home_help:
                return true;
            case R.id.menu_home_privacy:
                return true;
            case R.id.menu_home_sign_out:
                signOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut()
    {
        if (FirebaseAuth.getInstance() != null)
        {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this,MobileVerificationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void openContacts()
    {
        Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    private void openProfile()
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void setHome()
    {
        nameTextView.setText(GlobalVariables.userName);
        statusTextView.setText(GlobalVariables.userStatus);

        if(GlobalVariables.isProfileSet)
        {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir(Constants.imagesDirectory, Context.MODE_PRIVATE);
            String imagePath = new File(directory,"profile.jpg").getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            profileImageView.setImageBitmap(bitmap);
        }

        if(GlobalVariables.isLocationSet)
        {
            locationView.setVisibility(View.VISIBLE);
            addressTextView.setText(GlobalVariables.lastLocation);
            lastUpdatedTimeTextView.setText(GlobalVariables.lastUpdatedTime);
        }
    }

    public void editStatus(View view)
    {
        final EditText statusEditText = new EditText(this);
        statusEditText.setText(GlobalVariables.userStatus);
        statusEditText.selectAll();
        statusEditText.requestFocus();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your status");
        builder.setView(statusEditText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String status = statusEditText.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences(Constants.myPreferences,0).edit();
                if(status.isEmpty())
                {
                    GlobalVariables.userStatus = "Your status here";
                }
                else
                {
                    GlobalVariables.userStatus = status;
                }
                statusTextView.setText(GlobalVariables.userStatus);
                editor.putString(Constants.sharedPreferenceUserStatus,GlobalVariables.userStatus);
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.fUsers +"/"+ GlobalVariables.userID +"/"+ Constants.fPublicData);
                databaseRef.child(Constants.fStatus).setValue(GlobalVariables.userStatus);
                editor.apply();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    public void openLocation(View view)
    {
        String strUri = "http://maps.google.com/maps?q=loc:" + GlobalVariables.latitude + "," + GlobalVariables.longitude + " (" + "Label which you want" + ")";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        startActivity(intent);
    }

    public void nsendLocation(View view)
    {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null)
                            {
                                long date = new Date().getTime();
                                GlobalVariables.latitude = ""+location.getLatitude();
                                GlobalVariables.longitude = ""+location.getLongitude();
                                SLocation.uploadLocation(GlobalVariables.latitude,GlobalVariables.longitude,date);
                                String address = SLocation.getAddress(GlobalVariables.latitude,GlobalVariables.longitude,HomeActivity.this);

                                if(!address.equals("null")) {
                                    addressTextView.setText(address);
                                    locationView.setVisibility(View.VISIBLE);
                                    String time = GlobalFunctions.getlastUpdatedTime(date);
                                    lastUpdatedTimeTextView.setText(time);

                                    GlobalVariables.lastLocation = address;
                                    GlobalVariables.lastUpdatedTime = time;
                                    SLocation.saveLocation(GlobalVariables.latitude,GlobalVariables.longitude,date,HomeActivity.this);
                                    Toast.makeText(HomeActivity.this, "Location sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(HomeActivity.this,"location is null",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        catch (SecurityException e)
        {
            Toast.makeText(this,"unable to get location",Toast.LENGTH_SHORT).show();
        }
    }


    public void sendLocation(View view)
    {
        long date = new Date().getTime();
        String address;
        if(!isGPSAvailable())
            return;
        SLocation.getLocation(this);
        if(!GlobalVariables.isLocationSet)
            return;
        SLocation.uploadLocation(GlobalVariables.latitude,GlobalVariables.longitude,date);
        address = SLocation.getAddress(GlobalVariables.latitude,GlobalVariables.longitude,this);

        if(!address.equals("null"))
            addressTextView.setText(address);

    }

    //checks GPS availability, if not asks for turning on GPS
    public boolean isGPSAvailable()
    {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if(manager == null) {
            Toast.makeText(HomeActivity.this,"manager is null",Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Please Turn ON your GPS Connection")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return false;
        }

        return true;
    }

    public void alert(View view)
    {
        String message = GlobalVariables.userName + " is in danger! on " + GlobalVariables.lastUpdatedTime + " at "+GlobalVariables.lastLocation;
        sendSMS("+917386498882",message);
    }

    public void sendSMS(String phoneNumber,String message)
    {
        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber,null,message,null,null);
            Toast.makeText(context, "Alerted", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e)
        {
            Toast.makeText(HomeActivity.this,"Message sending failed to "+phoneNumber,Toast.LENGTH_SHORT).show();
        }
    }

    public void openVerifyVehicle(View view)
    {
        Intent intent = new Intent(getApplicationContext(),VerifyVehicleActivity.class);
        startActivity(intent);
    }
}
