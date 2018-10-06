package com.rgukt.summerintern;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class VerifyVehicleActivity extends AppCompatActivity {

    EditText vehicleNumberEditText;
    View driverView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_vehicle);

        //Display back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        vehicleNumberEditText = findViewById(R.id.editText_vehicle_number_VerifyVehicle);
        driverView = findViewById(R.id.driver_view);
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    public void checkVehicle(View view)
    {
        String vehicleNumber = vehicleNumberEditText.getText().toString();
        if(vehicleNumber.isEmpty() || !vehicleNumber.equals("AP31VS1234"))
        {
            vehicleNumberEditText.setError("Valid vehicle number required");
            return;
        }
        if(!GlobalFunctions.isNetworkAvailable(this))
        {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        driverView.setVisibility(View.VISIBLE);
    }
}
