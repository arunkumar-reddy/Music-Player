package com.example.thejoker.musplayer;

/**
 * Created by The Joker on 27/10/2015.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new Artist();
            case 1:
                return new Album();
            case 2:
                return new Songs();
        }
        return null;
    }
    @Override
    public int getCount()
    {
        return 3;
    }
}
