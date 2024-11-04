package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentViewPagerAdapter extends FragmentStateAdapter {
    public FragmentViewPagerAdapter(@NonNull ChartFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0){
            return new ChartFragment_outcome();
        } else if (position == 1) {
            return new ChartFragment_income();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
