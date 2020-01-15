package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private Button btnLogin, btnPhoneLogin;
    private EditText txtUserEmail, txtUserPassword;
    private TextView lblNewAcount, lblForgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initializeFields();

        lblNewAcount.setOnClickListener(view -> sendUserToRegisterActivity());

        btnLogin.setOnClickListener(view -> allowUserToLogin());

        btnPhoneLogin.setOnClickListener(v ->{
            Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
            startActivity(phoneLoginIntent);
        });

    }

    private void allowUserToLogin() {
        String email = txtUserEmail.getText().toString();
        String password = txtUserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Signing in...");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(LoginActivity.this, "Logged in succesfull!", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(LoginActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            });
        }

    }

    private void initializeFields() {
        btnLogin = findViewById(R.id.login_button);
        btnPhoneLogin = findViewById(R.id.phone_login_button);

        txtUserEmail = findViewById(R.id.login_email);
        txtUserPassword = findViewById(R.id.login_password);
        lblNewAcount = findViewById(R.id.need_new_account_link);
        lblForgetPassword = findViewById(R.id.forget_password_link);

        loadingBar = new ProgressDialog(this);
    }

    public void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    public void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
