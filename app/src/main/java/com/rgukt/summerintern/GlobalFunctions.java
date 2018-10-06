package com.rgukt.summerintern;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class GlobalFunctions
{
    public static void startActivity(Activity fromActivity,Class<?> cls,boolean isFinish)
    {
        Intent intent = new Intent(fromActivity,cls);
        fromActivity.startActivity(intent);
        if(isFinish)
            fromActivity.finish();
    }

    //Checks internet connection
    public static boolean isNetworkAvailable(Context con)
    {
        ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null)
        {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    //Saves image to app directory
    private static boolean saveImage(Bitmap profileBitmap, Context context)
    {
        boolean isSavedSuccessfully = false;
        ContextWrapper cw = new ContextWrapper(context);//Get context of application
        File directory = cw.getDir("images", Context.MODE_PRIVATE);//Returns app_directory/images directory to it
        File imagePath = new File(directory,"profile.jpg");//Create given path

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            isSavedSuccessfully = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try {
                if(fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isSavedSuccessfully;
    }

    public interface UploadCallback
    {
        void callback();
    }

    public static void uploadImage(final Activity activity, Bitmap bitmap, final Context context, final UploadCallback callback)
    {
        if(!GlobalFunctions.isNetworkAvailable(context))
        {
            Toast.makeText(activity,"No internet connection",Toast.LENGTH_SHORT).show();
            return;
        }


        bitmap = resizeImage(bitmap);

        //Save Image to internal storage
        boolean isImageSaved = GlobalFunctions.saveImage(bitmap,context);
        if(!isImageSaved)
        {
            //If image not saved properly
            Toast.makeText(activity,"Sorry, something went wrong",Toast.LENGTH_SHORT).show();
            return;
        }

        //Upload contact size image
        Bitmap contactBitmap = resizeToContactImage(bitmap);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageReference = storageReference.child("images/"+GlobalVariables.userID+"/contact.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        contactBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        imageReference.putBytes(data);


        //Upload profile size image
        storageReference = FirebaseStorage.getInstance().getReference();
        imageReference = storageReference.child("images/"+GlobalVariables.userID+"/profile.jpg");
        baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();

        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                Toast.makeText(activity,"Image upload failed",Toast.LENGTH_SHORT).show();
                callback.callback();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                SharedPreferences.Editor editor = context.getSharedPreferences(Constants.myPreferences,0).edit();
                editor.putBoolean(Constants.sharedPreferenceIsUserProfileSet,true);
                final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.fUsers).child(GlobalVariables.userID);
                databaseRef.child(Constants.fIsProfileSet).setValue(true);
                GlobalVariables.isProfileSet = true;
                editor.apply();
                callback.callback();
            }
        });
    }

    private static Bitmap resizeToContactImage(Bitmap bitmap)
    {
        int width, height;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        if (originalWidth >350)
        {
            width = (int) (originalWidth* 0.1);
            height = (int) (originalHeight * 0.1);
        }
        else if(originalWidth > 250)// 1/7
        {
            width = (int) (originalWidth* 0.143);
            height = (int) (originalHeight * 0.143);
        }
        else if(originalWidth > 150)// 1/5
        {
            width = (int) (originalWidth* 0.2);
            height = (int) (originalHeight * 0.2);
        }
        else if(originalWidth > 50)// 1/3
        {
            width = (int) (originalWidth* 0.3);
            height = (int) (originalHeight * 0.3);
        }
        else
        {
            width = originalWidth;
            height = originalHeight;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return bitmap;
    }

    private static Bitmap resizeImage(Bitmap bitmap)
    {
        int width, height;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        if(originalWidth > 2000)
        {
            width = (int) (originalWidth* 0.1);
            height = (int) (originalHeight * 0.1);
        }
        else if(originalWidth > 1000)
        {
            width = (int) (originalWidth* 0.2);
            height = (int) (originalHeight * 0.2);
        }
        else if(originalWidth > 500)
        {
            width = (int) (originalWidth* 0.4);
            height = (int) (originalHeight * 0.4);
        }
        else
        {
            width = originalWidth;
            height = originalHeight;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return bitmap;
    }

    public static String getlastUpdatedTime(long date)
    {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));

        int year = calendar.get(Calendar.YEAR) % 1000;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int currentYear = currentCalendar.get(Calendar.YEAR) % 1000;
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        if(currentDay == day && currentMonth == month && currentYear == year)
        {
            return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        }

        return day +"/"+ month +"/"+ year;
    }
}
