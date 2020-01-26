package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.querolloapp.activities.LoginActivity;
import com.example.querolloapp.adapters.TabsAccessorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private String currentUserId;
    private boolean onMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onMenuItem = false;
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager(), 0,
                getString(R.string.chats),
                getString(R.string.groups),
                getString(R.string.contacts));
        myViewPager.setAdapter(myTabAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        myTabLayout.getTabAt(3).setIcon(R.drawable.add_user);
        LinearLayout layout = (LinearLayout) ((LinearLayout) myTabLayout.getChildAt(0)).getChildAt(3);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.weight = .6f;
        layout.setLayoutParams(layoutParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            updateUserStatus("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !onMenuItem)
            updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            updateUserStatus("offline");
    }

    private void verifyUserExistance() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        final String currentUserID = user.getUid();

        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("name").exists())
                    sendUserToSettingsActivity();
                else {
                    rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        if (itemId == R.id.main_find_friends_option) {
            sendUserToFindFriendsActivity();
        } else if (itemId == R.id.main_settings_option) {
            sendUserToSettingsActivity();
        } else if (itemId == R.id.main_logout_option) {
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        } else if (itemId == R.id.main_create_group_option) {
            requestNewGroup();
        }
        onMenuItem = true;
        return super.onOptionsItemSelected(item);
    }

    private void requestNewGroup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Nombre del grupo: ");

        final EditText txtGroupName = new EditText(MainActivity.this);
        txtGroupName.setHint("p. ej. Mis amigos");
        builder.setView(txtGroupName);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String groupName = txtGroupName.getText().toString();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(getBaseContext(), "Por favor introduce un nombre de grupo...", Toast.LENGTH_SHORT).show();
            } else {
                createNewGroup(groupName);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "El grupo \"" + groupName + "\" fue creado correctamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MMM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineState);
    }
}
