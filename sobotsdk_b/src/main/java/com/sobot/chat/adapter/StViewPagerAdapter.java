package com.sobot.chat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.sobot.chat.fragment.SobotBaseFragment;

import java.util.List;

public class StViewPagerAdapter extends FragmentStatePagerAdapter {
    private String[] tabs;
    private List<SobotBaseFragment> pagers;
    private Context context;

    public StViewPagerAdapter(Context context, FragmentManager fm, String[] tabs,
                              List<SobotBaseFragment> pagers) {
        super(fm);
        this.tabs = tabs;
        this.pagers = pagers;
        this.context = context;
    }

    /**
     * 返回每一页需要的fragment
     */
    @Override
    public Fragment getItem(int position) {
        return pagers.get(position);
    }

    @Override
    public int getCount() {
        return pagers.size();
    }

    /**
     * 返回每一页对应的title
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (tabs != null && position < tabs.length) {
            return tabs[position];
        }
        return "";
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}