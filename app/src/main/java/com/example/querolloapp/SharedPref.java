package com.example.querolloapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    SharedPreferences sharedPref;

    public SharedPref(Context context) {
        sharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    public void setNightModeState(boolean state) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("NightMode", state);
        editor.commit();
    }

    public boolean loadNightModeState() {
        boolean state = sharedPref.getBoolean("NightMode", false);
        return state;
    }
}
