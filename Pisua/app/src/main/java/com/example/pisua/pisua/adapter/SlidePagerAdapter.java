package com.example.pisua.pisua.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.pisua.pisua.fragment.IntroFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pisua on 2015/9/18.
 */
public class SlidePagerAdapter extends FragmentPagerAdapter {

    private List<String> destinationList = new ArrayList<>();

    public SlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return new IntroFragment(destinationList.get(i));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return destinationList.get(position);
    }

    @Override
    public int getCount() {
        return destinationList.size();
    }

    public void setDestinationList(List<String> destinationList) {
        this.destinationList = destinationList;
    }
}