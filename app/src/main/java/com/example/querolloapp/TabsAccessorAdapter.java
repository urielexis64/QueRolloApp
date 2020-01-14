package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabsAccessorAdapter extends FragmentStatePagerAdapter {

    private String[] tabNames;


    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior, String... tabNames) {
        super(fm, behavior);
        this.tabNames = tabNames;
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new ChatsFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new ContactsFragment();
            case 3:
                return new RequestsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }



    @Nullable
    @Override
    public CharSequence getPageTitle(int i) {
        switch (i) {
            case 0:
                return tabNames[0];
            case 1:
                return tabNames[1];
            case 2:
                return tabNames[2];
            default:
                return null;
        }
    }
}
