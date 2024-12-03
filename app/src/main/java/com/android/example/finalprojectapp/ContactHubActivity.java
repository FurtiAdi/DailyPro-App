package com.android.example.finalprojectapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ContactHubActivity extends AppCompatActivity {

    private ListView listContact;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_hub_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("ContactHub");
        }

        listContact = findViewById(R.id.listContact);

        if(ContextCompat.checkSelfPermission(ContactHubActivity.this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.READ_CONTACTS}, 0);
        } else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CALL_LOG}, 0);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
              int id =  item.getItemId();
               if(id==R.id.nav_contacts){
                   showContacts();
                   return true;
               }else if (id==R.id.nav_call_history){
                   showCallHistory();
                   return true;
               }
               return false;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_contacts);
    }

    private void showContacts() {
        TextView contactsText = findViewById(R.id.contacts);
        contactsText.setText("Contacts");
        getAllContacts();
    }

    private void getAllContacts() {
        ArrayList<String> contacts = new ArrayList<>();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = null;  // No filter criteria
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";  // Optional: sort by name

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                contacts.add(name + "\n" + number);
            }
            cursor.close();
        }

        listContact.setAdapter(new ArrayAdapter<>(ContactHubActivity.this, android.R.layout.simple_list_item_1, contacts));
        listContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedContact = contacts.get(position);
                String[] contactDetail = selectedContact.split("\n", 2);
                String contactName = contactDetail[0];
                String contactNumber = contactDetail[1];
                openContactDetails(contactName, contactNumber);
            }
        });
    }

    private void showCallHistory() {
        TextView contactsText = findViewById(R.id.contacts);
        contactsText.setText("Call History");
        loadCallHistory();
    }

    private void loadCallHistory() {
        ArrayList<String> callList = new ArrayList<>();
        ArrayList<String> phoneNumbers = new ArrayList<>();
        String[] projection = new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
        };

        try (Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, null, null, CallLog.Calls.DATE + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss", Locale.getDefault());

                do {
                    String number = cursor.getString(numberIndex);
                    long dateMillis = cursor.getLong(dateIndex);
                    String date = sdf.format(new Date(dateMillis));
                    String duration = cursor.getString(durationIndex);
                    int type = cursor.getInt(typeIndex);

                    String callType = getCallType(type);

                    String callInfo = "Phone: " + number + "\n " + date + "\n " + duration + " seconds\n " + callType;
                    phoneNumbers.add(number);
                    callList.add(callInfo);
                } while (cursor.moveToNext());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, callList);
        listContact.setAdapter(adapter);

        listContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneNumber = phoneNumbers.get(position);  // Get the phone number of the clicked item
                if (ContextCompat.checkSelfPermission(ContactHubActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ContactHubActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 2);
                }
                makeCall(phoneNumber);
            }
        });
    }

    private void makeCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private String getCallType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                return "Incoming call";
            case CallLog.Calls.OUTGOING_TYPE:
                return "Outgoing call";
            case CallLog.Calls.MISSED_TYPE:
                return "Missed voice call";
            default:
                return "Unknown";
        }
    }

    private void openContactDetails(String contactName, String contactNumber) {
        Intent intent = new Intent(ContactHubActivity.this, ContactDetailActivity.class);
        intent.putExtra("CONTACT_NAME", contactName);
        intent.putExtra("CONTACT_NUMBER", contactNumber);
        startActivity(intent);
    }
}

