package com.android.example.finalprojectapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ContactDetailActivity extends AppCompatActivity {

    private ImageView call;
    private ImageView message;
    private ImageView email;
    private String phoneNumber;
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        TextView contactName = findViewById(R.id.contact_name);
        TextView contactNumber = findViewById(R.id.phone);

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("CONTACT_NUMBER");
        name = intent.getStringExtra("CONTACT_NAME");
        contactName.setText(name);
        contactNumber.setText(phoneNumber);

        call = findViewById(R.id.call);
        message = findViewById(R.id.message);
        email = findViewById(R.id.email);

        setCallOnClickListener();
        setEmailClickListener();
        setMessageClickListener();

    }

    private void setCallOnClickListener() {

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ContactDetailActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ContactDetailActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 2);
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });
    }

    private void setEmailClickListener() {
        email.setOnClickListener(v -> {
            Intent intent = new Intent(ContactDetailActivity.this, EmailActivity.class);
            startActivity(intent);
        });
    }

    private void setMessageClickListener() {
        message.setOnClickListener(v -> {
            Intent intent = new Intent(ContactDetailActivity.this, MessageActivity.class);
            intent.putExtra("CONTACT_NUMBER", phoneNumber);
            intent.putExtra("CONTACT_NAME", name);
            startActivity(intent);
        });
    }
}
