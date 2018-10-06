package com.rgukt.summerintern;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.rgukt.summerintern.contacts.ContactsActivity;

import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    Context context;
    TextView nameTextView, mobileNumberTextView;
    ProgressBar progress;
    Window currentWindow;
    ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.getMenu().getItem(2).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        context = getApplicationContext();
        currentWindow = getWindow();

        nameTextView = findViewById(R.id.textView_name_profileActivity);
        mobileNumberTextView = findViewById(R.id.textView_mobileNumber_profileActivity);
        progress = findViewById(R.id.progressBar_profileActivity);
        profileImageView = findViewById(R.id.imageView_profile_ProfileActivity);

        setProfile();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    openHome();
                    return true;
                case R.id.navigation_contacts:
                    openContacts();
                    return true;
                case R.id.navigation_profile:
                    return true;
            }
            return false;
        }
    };

    private void openHome()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void openContacts()
    {
        Intent intent = new Intent(this,ContactsActivity.class);
        startActivity(intent);
        finish();
    }

    public void pickImage(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE);
    }

    private void setProfile()
    {
        nameTextView.setText(GlobalVariables.userName);
        mobileNumberTextView.setText(GlobalVariables.userID);

        if(GlobalVariables.isProfileSet)
        {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir(Constants.imagesDirectory, Context.MODE_PRIVATE);
            String imagePath = new File(directory,"profile.jpg").getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            profileImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null)
        {
            try {
                Uri imagePath = data.getData();
                Bitmap profileBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);

                progress.setVisibility(View.VISIBLE);
                currentWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                GlobalFunctions.uploadImage(ProfileActivity.this, profileBitmap, context, new GlobalFunctions.UploadCallback() {
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
    }

    public void deleteAccount(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Are you sure?");
        builder.setMessage("Deleting account will permanently delete all your data.");

        // Add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                deleteFromFirebase();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteFromFirebase()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null)
        {
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        //Delete user data from database
                        FirebaseDatabase.getInstance().getReference().child(GlobalVariables.userID).removeValue();
                        //Delete profile image from firebase storage
                        FirebaseStorage.getInstance().getReference().child("images/"+GlobalVariables.userID + ".jpg").delete();
                        clearSharedPreferences();
                        GlobalFunctions.startActivity(ProfileActivity.this,MobileVerificationActivity.class,true);
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this,"Sorry, something went wrong.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void clearSharedPreferences()
    {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.myPreferences,0).edit();
        editor.clear();
        editor.apply();
    }

    public void editName(View view)
    {
        final EditText nameEditText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your name");
        builder.setView(nameEditText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                String name = nameEditText.getText().toString();
                if(name.isEmpty() || name.length() <3)
                {
                    Toast.makeText(ProfileActivity.this,"Must contain at least 3 characters",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.myPreferences,0).edit();
                    GlobalVariables.userName = name;
                    nameTextView.setText(name);
                    editor.putString(Constants.sharedPreferenceUserName,name);
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.fUsers +"/"+ GlobalVariables.userID +"/"+ Constants.fPublicData);
                    databaseRef.child(Constants.fName).setValue(name);
                    editor.apply();
                }
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
}
