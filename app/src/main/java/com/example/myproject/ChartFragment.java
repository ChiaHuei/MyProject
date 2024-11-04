package com.example.myproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;

import android.widget.Toast;


public class ChartFragment extends Fragment {

    private ViewPager2 vp_statistics;
    private TabLayout tab_statistics;
    private FragmentViewPagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        vp_statistics = view.findViewById(R.id.vp_statistics);
        tab_statistics = view.findViewById(R.id.tab_statistics);

        adapter = new FragmentViewPagerAdapter(this);
        vp_statistics.setAdapter(adapter);
        vp_statistics.setCurrentItem(0);

        new TabLayoutMediator(tab_statistics, vp_statistics, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) tab.setText("支出");
                if (position == 1) tab.setText("收入");
            }
        }).attach();

        tab_statistics.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(getContext(), tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }
}