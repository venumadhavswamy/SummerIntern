package com.rgukt.summerintern;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;

public class InitialProfileSetupActivity extends AppCompatActivity
{

    private static final int PICK_IMAGE = 1;
    private String mobileNumber;
    private Window currentWindow;
    ImageView profileImageView;
    ProgressBar progress;
    Context context;
    EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_profile_setup);

        context = getApplicationContext();

        profileImageView = findViewById(R.id.imageView_profile_InitialProfileActivity);
        progress = findViewById(R.id.progressBar_InitialProfileSetupActivity);
        editTextName = findViewById(R.id.editText_name_InitialProfileSetupActivity);

        mobileNumber = getIntent().getStringExtra("mobileNumber");
        currentWindow = getWindow();
    }

    public void pickImage(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null)
        {
            try {
                Uri imagePath = data.getData();
                Bitmap profileBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);

                progress.setVisibility(View.VISIBLE);
                currentWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                GlobalFunctions.uploadImage(InitialProfileSetupActivity.this, profileBitmap, context, new GlobalFunctions.UploadCallback() {
                    @Override
                    public void callback() {
                        if(GlobalVariables.isProfileSet)
                        {
                            ContextWrapper cw = new ContextWrapper(context);
                            File directory = cw.getDir(Constants.imagesDirectory, Context.MODE_PRIVATE);
                            String path = new File(directory,"profile.jpg").getAbsolutePath();
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            profileImageView.setImageBitmap(bitmap);
                        }
                        progress.setVisibility(View.GONE);
                        currentWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.d("Profile","failed");
        }
    }

    public void onProceed(View view)
    {
        String name = editTextName.getText().toString();
        if(name.isEmpty())
        {
            editTextName.setError("Name is required");
            editTextName.requestFocus();
            return;
        }
        else if(name.length() < 3)
        {
            editTextName.setError("Must contain at least 3 characters");
            return;
        }

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.fUsers +"/"+ mobileNumber +"/"+Constants.fPublicData);
        databaseRef.child(Constants.fName).setValue(name);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.sharedPreferenceUserName,name);
        editor.putString(Constants.sharedPreferenceUserID,mobileNumber);
        GlobalVariables.userName = name;
        GlobalVariables.userID = mobileNumber;
        editor.apply();
        GlobalFunctions.startActivity(InitialProfileSetupActivity.this,HomeActivity.class,true);
    }
}
