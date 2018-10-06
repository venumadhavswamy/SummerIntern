package com.rgukt.summerintern.contacts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rgukt.summerintern.Constants;
import com.rgukt.summerintern.GlobalFunctions;
import com.rgukt.summerintern.GlobalVariables;
import com.rgukt.summerintern.HomeActivity;
import com.rgukt.summerintern.ProfileActivity;
import com.rgukt.summerintern.R;
import com.rgukt.summerintern.SLocation;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private ArrayList<Contact> onlineContactsList;
    private ArrayList<OfflineContact> offlineContactsList;
    private ContactsAdapter contactsAdapter;
    private boolean isOffline = true;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(0).setChecked(true);

        progressBar = findViewById(R.id.progressBar_ContactsActivity);

        onlineContactsList = new ArrayList<>();
        offlineContactsList = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        setContactsListView();
        fetchAllContacts();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void fetchAllContacts()
    {
        if(!GlobalFunctions.isNetworkAvailable(this))
        {
            progressBar.setVisibility(View.GONE);
            fetchOfflineContacts();
            isOffline = true;
            return;
        }
        isOffline = false;

        String path = Constants.fUsers +"/"+ GlobalVariables.userID +"/"+ Constants.fContacts;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@Nullable DataSnapshot snapshot)
            {
                if(snapshot != null && snapshot.exists())
                {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        String mobileNumber = postSnapshot.getKey();
                        if(postSnapshot.getValue() != null)
                        {
                            String name = postSnapshot.getValue().toString();
                            fetchSingleContact(mobileNumber,name);
                        }
                    }
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchSingleContact(final String mobileNumber,final String name)
    {
        progressBar.setVisibility(View.GONE);
        String path = Constants.fUsers +"/"+ mobileNumber +"/"+ Constants.fPublicData;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@Nullable DataSnapshot snapshot)
            {
                Object obj1,obj2;
                Contact contact = new Contact(name, mobileNumber);
                if(snapshot != null && snapshot.exists())
                {
                    contact.setIsSaathian(true);

                    obj1 = snapshot.child(Constants.fStatus).getValue();
                    if(obj1 != null)
                    {
                        String status = obj1.toString();
                        contact.setStatus(status);
                    }

                    obj1 = snapshot.child(Constants.fLatitude).getValue();
                    obj2 = snapshot.child(Constants.fLongitude).getValue();

                    if(obj1 != null && obj2 != null)
                    {
                        contact.setLatitude(obj1.toString());
                        contact.setLongitude(obj2.toString());
                    }
                }

                onlineContactsList.add(contact);
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchOfflineContacts()
    {

    }

    public void addContact(MenuItem item)
    {
        Intent intent = new Intent(getApplicationContext(),AddContactActivity.class);
        startActivity(intent);
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
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_contacts_add_person:
                return true;
            case R.id.menu_contacts_location_privacy:
                return true;
            case R.id.menu_contacts_message_privacy:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openHome()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void openProfile()
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void setContactsListView()
    {
        ListView contactsListView = findViewById(R.id.listView_contacts);
        contactsAdapter = new ContactsAdapter(ContactsActivity.this,R.layout.contact_layout);
        contactsListView.setAdapter(contactsAdapter);
    }

    //avoids the findViewById() method in an adapter
    //Holds the references to the views in layout
    static class ViewHolder
    {
        public ImageView profileImageView;
        public ImageView locationImageView;
        public TextView name;
        public TextView status;
        public TextView lastUpdated;
    }

    class ContactsAdapter extends ArrayAdapter<Contact>
    {
        private Context context;

        ContactsAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            this.context = context;
        }

        @Override
        public int getCount()
        {
            if(isOffline)
                return offlineContactsList.size();
            else
                return onlineContactsList.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View reusableView, @NonNull ViewGroup parent)
        {
            View listItem = reusableView;

            if(isOffline)
            {
                OfflineContact contact = offlineContactsList.get(position);
            }

            Contact currentContact = onlineContactsList.get(position);
            ViewHolder viewHolder;
            if(listItem == null)
            {
                viewHolder = new ViewHolder();
                listItem = LayoutInflater.from(context).inflate(R.layout.contact_layout,parent,false);
                viewHolder.profileImageView = listItem.findViewById(R.id.imageView_profile_Contact);
                viewHolder.locationImageView = listItem.findViewById(R.id.imageView_location_Contact);
                viewHolder.name = listItem.findViewById(R.id.textView_name_Contact);
                viewHolder.status = listItem.findViewById(R.id.textView_status_Contact);
                viewHolder.lastUpdated = listItem.findViewById(R.id.textView_last_updated_contact);
                listItem.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) listItem.getTag();
            }

            viewHolder.name.setText(currentContact.getName());
            viewHolder.status.setText(currentContact.getStatus());
            viewHolder.locationImageView.setColorFilter(currentContact.getLocationColor());
            viewHolder.locationImageView.setTag(position);
            viewHolder.locationImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (int)view.getTag();
                    Contact contact = onlineContactsList.get(position);
                    String latitude = contact.getLatitude();
                    String longitude = contact.getLongitude();
                    if((latitude != null && !latitude.isEmpty()) && (longitude != null && !longitude.isEmpty())) {
                        showAddressAlert(latitude,longitude);
                        return;
                    }
                    Toast.makeText(ContactsActivity.this,"Location isn't available",Toast.LENGTH_SHORT).show();

                }
            });
            return listItem;
        }
    }

    public void showAddressAlert(final String latitude, final String longitude)
    {
        String address = SLocation.getAddress(latitude,longitude,this);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Address");
        builder.setMessage(address);

        Log.d("latitude",latitude);
        Log.d("longitude",longitude);

        builder.setPositiveButton("Open in maps", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openLocation(latitude,longitude);
            }
        });
        builder.show();
    }

    public void openLocation(String latitude,String longitude)
    {
        String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + "Label which you want" + ")";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        startActivity(intent);
    }
}
