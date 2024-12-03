package com.android.example.finalprojectapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSION = 3;
    private static final int REQUEST_SMS_PERMISSION = 4;
    private static final String TAG = "MessageActivity";
    private EditText message;
    private ImageView camera, photo, send;
    private String phoneNumber;
    private Uri photoUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);


        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        // Initializing views
        TextView contactName = findViewById(R.id.receiver);
        message = findViewById(R.id.message_view);
        camera = findViewById(R.id.camera);
        photo = findViewById(R.id.photo);
        send = findViewById(R.id.send);

        // Getting contact details from the intent
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("CONTACT_NUMBER");
        String name = intent.getStringExtra("CONTACT_NAME");
        contactName.setText(name);

        // Setting click listeners for camera, gallery, and send
        camera.setOnClickListener(v -> openCamera());
        photo.setOnClickListener(v -> openGallery());
        send.setOnClickListener(v -> {
            if (photoUri != null) {
                sendMms(phoneNumber, message.getText().toString(), photoUri);
            } else {
                sendSmsMessage();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(MessageActivity.this, "com.android.example.finalprojectapp.fileprovider", photoFile);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Ensure the URI has read permission
                    startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                displayAttachedFile(photoUri);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null) {
                    photoUri = data.getData();
                    displayAttachedFile(photoUri);
                }
            }
        } else {
            if(!message.getText().toString().trim().isEmpty()) {
                showEmailSentDialog();
            }
        }
    }

    private void showEmailSentDialog() {
        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(this::finish, 2000);
    }

    private void displayAttachedFile(Uri uri) {
        String fileName = getFileName(uri);
        message.append("\nImage: " + fileName);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            insertImageInMessage(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void insertImageInMessage(Bitmap bitmap) {
        SpannableString ss = new SpannableString(" ");
        ImageSpan span = new ImageSpan(this, bitmap);
        ss.setSpan(span, 0, 1, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        message.append(ss);
    }

    private File createImageFile() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void sendSmsMessage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            sendMessage();
        }
    }

    private void sendMessage() {
        String smsMessage = message.getText().toString();
        if (!smsMessage.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, smsMessage, null, null);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
            message.setText("");
        } else {
            Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMms(String phoneNumber, String message, Uri imageUri) {
        Intent mmsIntent = new Intent(Intent.ACTION_SEND);
        mmsIntent.setType("image/jpeg");
        mmsIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        mmsIntent.putExtra("address", phoneNumber);
        mmsIntent.putExtra("sms_body", message);
        Intent chooser = Intent.createChooser(mmsIntent, "Send MMS");

        // Check if there's an app that can handle the intent
        if (mmsIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, 0);
        } else {
            Toast.makeText(this, "MMS failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMessage();
            } else {
                Toast.makeText(this, "SMS permission is required to send messages", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
