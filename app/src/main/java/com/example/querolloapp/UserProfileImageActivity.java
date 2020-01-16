package com.example.querolloapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class UserProfileImageActivity extends AppCompatActivity {
    private SubsamplingScaleImageView img;

    private ProgressDialog loadingBar;

    private String currentUserID;
    private static final int GALLERY_PICK = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_image);

        img = findViewById(R.id.user_profile_image);
        setSupportActionBar(findViewById(R.id.user_profile_image_toolbar));
        getSupportActionBar().setTitle(getString(R.string.settings));

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        currentUserID = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

        if (getIntent().hasExtra("image")) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("image"), 0, getIntent().getByteArrayExtra("image").length);
            img.setImage(ImageSource.bitmap(bitmap));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_image_menu_item:
                sendUserToGallery();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
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
                    .setMinCropResultSize(200, 200)
                    .setInitialCropWindowPaddingRatio(0)
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
                    Snackbar.make(img, "Imagen guardada correctamente", Snackbar.LENGTH_SHORT).show();
                    Uri downloadUri = task.getResult();
                    rootRef.child("Users").child(currentUserID).child("image").setValue(downloadUri.toString());
                    img.setImage(ImageSource.uri(resultUri));
                } else {
                    Snackbar.make(img, "Error: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }

                loadingBar.dismiss();
            });
        }
    }

}
