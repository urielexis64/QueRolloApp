package com.example.querolloapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ProfileImagePreviewActivity extends AppCompatActivity {

    private ImageView image;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image_preview);

        getWindow().setSharedElementEnterTransition(new ChangeBounds().setDuration(150));

        image = findViewById(R.id.profile_group_image);

        if(getIntent().hasExtra("byteArray")) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
            image.setImageBitmap(bitmap);
        }

        ((TextView)findViewById(R.id.group_name)).setText(getIntent().getStringExtra("group_name"));
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    public void finishDialog(View view) {
        supportFinishAfterTransition();
    }
}
