package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.makeText;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.update_settings_button)
    Button btnUpdateAccountSettings;
    @BindView(R.id.set_user_name)
    EditText txtUsername;
    @BindView(R.id.set_profile_status)
    EditText txtStatus;
    @BindView(R.id.profile_image)
    CircleImageView imgProfile;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private static final int GALLERY_PICK = 1;
    private StorageReference userProfileImagesRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        txtUsername.setVisibility(View.INVISIBLE);

        btnUpdateAccountSettings.setOnClickListener(v -> updateSettings());

        retrieveUserInfo();

        imgProfile.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_PICK);
        });

    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    txtUsername.setText(retrieveUserName);
                    txtStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(imgProfile);
                } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    txtUsername.setText(retrieveUserName);
                    txtStatus.setText(retrieveStatus);
                } else {
                    txtUsername.setVisibility(View.VISIBLE);
                    makeText(SettingsActivity.this, "Por favor actualiza tu información personal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropMenuCropButtonTitle("Recortar")
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setOutputCompressQuality(20)
                    .start(this);
            return;
        }

        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (resultCode == RESULT_OK) {
            loadingBar.setTitle("Subiendo foto de perfil");
            loadingBar.setMessage("Por favor, espere...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            Uri resultUri = result.getUri();
            StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");

            filePath.putFile(resultUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    makeText(SettingsActivity.this, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show();
                    Uri downloadUri = task.getResult();
                    rootRef.child("Users").child(currentUserID).child("image").setValue(downloadUri.toString());
                } else {
                    makeText(SettingsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            });
        }
    }

    private void updateSettings() {
        String username = txtUsername.getText().toString();
        String status = txtStatus.getText().toString();

        if (TextUtils.isEmpty(username)) {
            makeText(this, "Primero introduce tu nombre de usuario", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            makeText(this, "Por favor introduce la descripción de tu estado", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", username);
            profileMap.put("status", status);

            rootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(task -> {
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
}
