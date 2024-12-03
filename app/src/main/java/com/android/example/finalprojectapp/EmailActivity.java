package com.android.example.finalprojectapp;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EmailActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private EditText editRecipient, editSubject, editMessage;
    private Uri fileUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);


        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        editRecipient = findViewById(R.id.receiver);
        editSubject = findViewById(R.id.subject_view);
        editMessage = findViewById(R.id.message_view);
        ImageView send = findViewById(R.id.send);
        ImageView attach = findViewById(R.id.attach);

        send.setOnClickListener(v -> sendEmail());

        attach.setOnClickListener(v -> openFilePicker());
    }

    private void sendEmail() {
        String recipient = editRecipient.getText().toString().trim();
        String subject = editSubject.getText().toString().trim();
        String message = editMessage.getText().toString().trim();

        // Check if any field is empty
        if (recipient.isEmpty() ) {
            Toast.makeText(this, "You must enter receiver address!", Toast.LENGTH_SHORT).show();
            return;
        }else if (message.isEmpty()){
            Toast.makeText(this, "Message is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (fileUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        Intent chooser = Intent.createChooser(intent, "Choose Email client:");

        try {
            startActivityForResult(chooser, PICK_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No email client found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                fileUri = data.getData();
                displayAttachedFile(fileUri);

            }else{

                if(!editMessage.getText().toString().trim().isEmpty()) {
                    showEmailSentDialog();
                }
            }
        }
    }

    private void showEmailSentDialog() {

       Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show();
       new Handler().postDelayed(this::finish,2000);
    }

    private void displayAttachedFile(Uri uri) {
        String fileName = getFileName(uri);
        editMessage.append("\nAttached file: " + fileName);
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
}
