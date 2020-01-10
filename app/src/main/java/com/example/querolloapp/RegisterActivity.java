package com.example.querolloapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_button)
    Button btnCreateAccount;
    @BindView(R.id.register_email)
    EditText txtUserEmail;
    @BindView(R.id.register_password)
    EditText txtUserPassword;
    @BindView(R.id.already_have_account_link)
    TextView lblAlreadyHaveAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();

        lblAlreadyHaveAccount.setOnClickListener(view -> sendUserToLoginActivity());
        btnCreateAccount.setOnClickListener(view -> createNewAccount());
    }

    private void createNewAccount() {
        String email = txtUserEmail.getText().toString();
        String password = txtUserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Creating new account...");
            loadingBar.setMessage("Please wait while we are creating a new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String currentUserID = mAuth.getCurrentUser().getUid();
                            rootReference.child("Users").child(currentUserID).setValue("");

                            sendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Account created succesfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    });
        }
    }

    public void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    public void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
