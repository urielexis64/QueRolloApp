package com.example.querolloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PhoneLoginActivity extends AppCompatActivity {

    private MaterialButton btnSendVerification, btnVerify;
    private TextInputEditText txtPhoneNumber, txtVerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        btnSendVerification = findViewById(R.id.btn_send_code);
        btnVerify = findViewById(R.id.btn_verify);
        txtPhoneNumber = findViewById(R.id.phone_number_input);
        txtVerificationCode = findViewById(R.id.verification_code_input);

        btnSendVerification.setOnClickListener(v -> {
            btnSendVerification.setVisibility(View.INVISIBLE);
            findViewById(R.id.layout_phone_number).setVisibility(View.INVISIBLE);

            btnVerify.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_verification_code).setVisibility(View.VISIBLE);
        });

    }
}
