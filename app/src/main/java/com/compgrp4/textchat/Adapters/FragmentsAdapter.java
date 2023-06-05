package com.compgrp4.textchat.Adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.compgrp4.textchat.Fragments.ChatFragment;
import com.compgrp4.textchat.Fragments.StatusFragment;
import com.compgrp4.textchat.Fragments.UserFragment;

import org.jetbrains.annotations.NotNull;

public class FragmentsAdapter extends FragmentPagerAdapter {

    public FragmentsAdapter( @NotNull FragmentManager fm) {
        super(fm);
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {

        switch(position)
        {
            case 0: return new ChatFragment();
            case 1: return new UserFragment();
            case 2: return new StatusFragment();
            default: return new ChatFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        switch(position)
        {
            case 0:
                return "CHATS";
            case 1:
                return "Users";
            case 2:
                return "STATUS";
            default:
                return "CHATS";
        }
    }
}
