package com.android.example.finalprojectapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;



public class HomeActivity extends AppCompatActivity {
    private LinearLayout news, youtube, instagram, tiktok, facebook, snapchat;
    private TextView todoList, event, contactHub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        initializeViews();

        //ClickListener for Category and main
        setClickListener(todoList, ToDoListActivity.class);
        setClickListener(event, EventActivity.class);
        setClickListener(news, NewsFeedActivity.class);
        setClickListener(contactHub, ContactHubActivity.class);

        //ClickListener for Social media
        setSocialMediaClickListener(youtube, "com.google.android.youtube", "https://www.youtube.com/");
        setSocialMediaClickListener(facebook, "com.facebook.katana", "https://www.facebook.com/");
        setSocialMediaClickListener(instagram, "com.instagram.android", "https://www.instagram.com/");
        setSocialMediaClickListener(snapchat, "com.snapchat.android", "https://www.snapchat.com/");
        setSocialMediaClickListener(tiktok, "com.zhiliaoapp.musically", "https://www.tiktok.com/");

    }

    private void initializeViews() {
        todoList = findViewById(R.id.notes);
        event = findViewById(R.id.event);
        news = findViewById(R.id.news_view);
        contactHub = findViewById(R.id.allInOneChat);

        youtube = findViewById(R.id.youtube);
        facebook = findViewById(R.id.Facebook);
        instagram = findViewById(R.id.instagram);
        snapchat = findViewById(R.id.snapchat);
        tiktok = findViewById(R.id.tiktok);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, this.getClass()));
    }

    private void setClickListener(View view, Class<?> activityClass) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, activityClass);
            startActivity(intent);
        });
    }


    private void setSocialMediaClickListener(LinearLayout view, String packageName, String url) {
        view.setOnClickListener(v -> openAppOrUrl(packageName, url));
    }

    private void openAppOrUrl(String packageName, String url) {
        Intent intent;
        PackageManager packageManager = getPackageManager();
        try {
            intent = packageManager.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                throw new PackageManager.NameNotFoundException();
            }
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        } catch (PackageManager.NameNotFoundException e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        startActivity(intent);
    }
}
