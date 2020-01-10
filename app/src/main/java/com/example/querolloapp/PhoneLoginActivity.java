package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhoneLoginActivity extends AppCompatActivity {

    @BindView(R.id.btn_send_code)
    MaterialButton btnSendVerification;
    @BindView(R.id.btn_verify)
    MaterialButton btnVerify;
    @BindView(R.id.phone_number_input)
    TextInputEditText txtPhoneNumber;
    @BindView(R.id.verification_code_input)
    TextInputEditText txtVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        btnSendVerification.setOnClickListener(v -> {
            String phoneNumber = txtPhoneNumber.getText().toString();

            if (TextUtils.isEmpty(phoneNumber)) {
                Snackbar.make(btnSendVerification, "Ingresa un número de teléfono", Snackbar.LENGTH_LONG).show();
            } else {
                loadingBar.setTitle("Verificación de teléfono");
                loadingBar.setMessage("Por favor espere, estamos autenticando su número...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        PhoneLoginActivity.this, // Activity (for callback binding)
                        callbacks);
            }
        });

        btnVerify.setOnClickListener(v -> {
            btnSendVerification.setVisibility(View.INVISIBLE);
            findViewById(R.id.layout_phone_number).setVisibility(View.INVISIBLE);

            String verificationCode = txtVerificationCode.getText().toString();

            if (TextUtils.isEmpty(verificationCode)) {
                Snackbar.make(btnSendVerification, "Ingresa el código de verificación...", Snackbar.LENGTH_LONG).show();
            } else {
                loadingBar.setTitle("Código de verificación");
                loadingBar.setMessage("Por favor espere, estamos verificando su código...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Número de teléfono inválido, recuerda ingresar la lada de tu país.", Toast.LENGTH_SHORT).show();

                btnSendVerification.setVisibility(View.VISIBLE);
                findViewById(R.id.layout_phone_number).setVisibility(View.VISIBLE);

                btnVerify.setVisibility(View.INVISIBLE);
                findViewById(R.id.layout_verification_code).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Snackbar.make(btnSendVerification, "Código enviado...", Snackbar.LENGTH_LONG).show();

                btnSendVerification.setVisibility(View.INVISIBLE);
                findViewById(R.id.layout_phone_number).setVisibility(View.INVISIBLE);

                btnVerify.setVisibility(View.VISIBLE);
                findViewById(R.id.layout_verification_code).setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Felicidades, accediste correctamente", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                    } else {
                        Snackbar.make(btnVerify, "Error: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                    loadingBar.dismiss();
                });
    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
