package com.example.querolloapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.querolloapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserProfileImageActivity extends AppCompatActivity {
    private SubsamplingScaleImageView img;
    private Bitmap mBitmap;

    private ProgressDialog loadingBar;

    private String currentUserID;
    private static final int GALLERY_PICK = 1;
    private boolean userProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_image);

        img = findViewById(R.id.user_profile_image);
        img.setMaxScale(30);
        setSupportActionBar(findViewById(R.id.user_profile_image_toolbar));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        currentUserID = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

        if (getIntent().hasExtra("image")) {
            mBitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("image"), 0, getIntent().getByteArrayExtra("image").length);
            img.setImage(ImageSource.bitmap(mBitmap));
        }

        if (getIntent().hasExtra("img")) {
            mBitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("img"), 0, getIntent().getByteArrayExtra("img").length);
            img.setImage(ImageSource.bitmap(mBitmap));
            getSupportActionBar().setTitle(getIntent().getStringExtra("username"));
            userProfile = true;
            return;
        }

        getSupportActionBar().setTitle(getString(R.string.settings));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (userProfile)
            getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        else
            getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_image_menu_item:
                sendUserToGallery();
                break;
            case R.id.share_profile_image:
                shareProfileImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareProfileImage() {
        Uri uri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "to-share.png");
            FileOutputStream stream = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = Uri.parse(file.getPath());
            } else {
                uri = Uri.fromFile(new File(file.getPath()));
            }
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent,"Share via"));
        } catch (IOException e) {
        }
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

            int h = 0, w = 0;
            try {
                Bitmap image = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, null);
                w = image.getWidth();
                h = image.getHeight();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "El archivo que elegiste no es una foto", Toast.LENGTH_SHORT).show();
                return;
            }

            if (h < 100 || w < 100) {
                Toast.makeText(this, "La imagen es demasiado pequeña. Mín. 100px", Toast.LENGTH_SHORT).show();
                return;
            }

            System.out.println("H :" + h + "  W: " + w);


            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropMenuCropButtonTitle("Recortar")
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setOutputCompressQuality(40)
                    .setMinCropResultSize(100, 100)
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
                    img.resetScaleAndCenter();
                    img.setImage(ImageSource.uri(resultUri));
                } else {
                    Snackbar.make(img, "Error: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }

                loadingBar.dismiss();
            });
        }
    }

}
