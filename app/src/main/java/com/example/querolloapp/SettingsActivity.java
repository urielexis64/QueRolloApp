package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.makeText;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.update_settings_button)
    Button btnUpdateAccountSettings;
    @BindView(R.id.set_user_name)
    EditText txtUsername;
    @BindView(R.id.set_profile_status)
    EditText txtStatus;
    @BindView(R.id.profile_image)
    ImageView imgProfile;
    @BindView(R.id.settings_toolbar)
    Toolbar settingsToolbar;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle(getString(R.string.settings));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnUpdateAccountSettings.setOnClickListener(v -> updateSettings());

        retrieveUserInfo();

        imgProfile.setOnClickListener(v -> sendUserToUserProfileImageActivity());
    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                    if (dataSnapshot.hasChild("image")) {
                        String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(retrieveProfileImage).into(imgProfile);
                    }
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    txtUsername.setText(retrieveUserName);
                    txtStatus.setText(retrieveStatus);
                } else {
                    makeText(SettingsActivity.this, "Por favor actualiza tu información personal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateSettings() {
        String username = txtUsername.getText().toString();
        String status = txtStatus.getText().toString();

        if (TextUtils.isEmpty(username)) {
            makeText(this, "Primero introduce tu nombre de usuario", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            makeText(this, "Por favor introduce la descripción de tu estado", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", username);
            profileMap.put("status", status);

            rootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    makeText(SettingsActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().getMessage();
                    makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToUserProfileImageActivity() {
        Intent previewIntent = new Intent(this, UserProfileImageActivity.class);

        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imgProfile, "settings_transition").toBundle();

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imgProfile.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        previewIntent.putExtra("image", imageInByte);
        startActivity(previewIntent, bundle);
    }
}
