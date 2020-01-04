package com.example.querolloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    private Button btnCreateAccount;
    private EditText txtUserEmail, txtUserPassword;
    private TextView lblAlreadyHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();
        lblAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });
    }

    private void initializeFields() {
        btnCreateAccount = findViewById(R.id.register_button);
        txtUserEmail = findViewById(R.id.register_email);
        txtUserPassword = findViewById(R.id.register_password);
        lblAlreadyHaveAccount = findViewById(R.id.already_have_account_link);
    }

    public void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.zoomenter, R.anim.zoomexit);
    }
}
