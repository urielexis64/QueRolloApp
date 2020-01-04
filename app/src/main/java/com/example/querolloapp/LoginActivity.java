package com.example.querolloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private Button btnLogin, btnPhoneLogin;
    private EditText txtUserEmail, txtUserPassword;
    private TextView lblNewAcount,lblForgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields();

        lblNewAcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });
    }

    private void initializeFields() {
        btnLogin = findViewById(R.id.login_button);
        btnPhoneLogin = findViewById(R.id.phone_login_button);

        txtUserEmail = findViewById(R.id.login_email);
        txtUserPassword = findViewById(R.id.login_password);
        lblNewAcount = findViewById(R.id.need_new_account_link);
        lblForgetPassword = findViewById(R.id.forget_password_link);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser!=null){
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(loginIntent);
    }

    public void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        overridePendingTransition(R.anim.zoomenter, R.anim.zoomexit);
    }
}
