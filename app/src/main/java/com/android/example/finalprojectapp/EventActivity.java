package com.android.example.finalprojectapp;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CALENDAR = 100;
    private ArrayAdapter<String> eventsAdapter;
    private ArrayList<String> eventsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("New Events");
        }

        ListView eventsListView = findViewById(R.id.events_list);
        eventsList = new ArrayList<>();
        eventsAdapter = new ArrayAdapter<>(this, R.layout.event_list_item, R.id.text1, eventsList);
        eventsListView.setAdapter(eventsAdapter);

        checkPermissionsAndFetchEvents();

    }

    private void checkPermissionsAndFetchEvents() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CALENDAR},
                    PERMISSIONS_REQUEST_READ_CALENDAR);
        } else {
            fetchTodayEvents();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_CALENDAR) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchTodayEvents();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchTodayEvents(){

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime= Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);
        endTime.set(Calendar.MILLISECOND, 999);

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

        Cursor cursor = this.getBaseContext().getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND},
                selection, null, null
        );

        if(cursor!=null){
            int titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE);
            int startIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART);
            int endIndex = cursor.getColumnIndex(CalendarContract.Events.DTEND);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss", Locale.getDefault());

            while(cursor.moveToNext()){
                String title = cursor.getString(titleIndex);
                long start = cursor.getLong(startIndex);
                long end = cursor.getLong(endIndex);

                String startTimeStr = formatter.format(new Date(start));
                String endTimeStr = formatter.format(new Date(end));

                eventsList.add("Event:  " + title + "\nStart:   " + startTimeStr + "\nEnd:     " + endTimeStr);
            }
            cursor.close();
        }

        eventsAdapter.notifyDataSetChanged();

    }
}
