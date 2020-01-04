package com.example.querolloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button btnUpdateAccountSettings;
    private EditText txtUsername, txtStatus;
    private CircleImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializaFields();
    }

    private void initializaFields() {
        btnUpdateAccountSettings = findViewById(R.id.update_settings_button);
        txtUsername = findViewById(R.id.set_user_name);
        txtStatus = findViewById(R.id.set_profile_status);
        imgProfile = findViewById(R.id.profile_image);

    }
}
